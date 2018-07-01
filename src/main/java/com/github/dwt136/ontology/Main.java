package com.github.dwt136.ontology;

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Iterator;

public class Main {

    private static final String PATH_DATASETS = "datasets_sample";
    private static final String FILE_DBPEDIA = Paths.get(PATH_DATASETS, "dbpedia_2016-10.nt").toString();
    private static final String FILE_INSTANCE_TYPES_EN = Paths.get(PATH_DATASETS, "instance_types_en.ttl").toString();
    private static final String FILE_LITERALS_EN = Paths.get(PATH_DATASETS, "mappingbased_literals_en.ttl").toString();
    private static final String FILE_OBJECTS_EN = Paths.get(PATH_DATASETS, "mappingbased_objects_en.ttl").toString();
    private static final String FILE_INSTANCE_TYPES_JA = Paths.get(PATH_DATASETS, "instance_types_ja.ttl").toString();
    private static final String FILE_LITERALS_JA = Paths.get(PATH_DATASETS, "mappingbased_literals_ja.ttl").toString();
    private static final String FILE_OBJECTS_JA = Paths.get(PATH_DATASETS, "mappingbased_objects_ja.ttl").toString();
    private static final String FILE_LINKS_EN = Paths.get(PATH_DATASETS, "interlanguage_links_chapters_en.ttl").toString();
    private static final String FILE_LINKS_JA = Paths.get(PATH_DATASETS, "interlanguage_links_ja.ttl").toString();


    private static final String PATH_RESULTS = "results";
    private static final String FILE_MODEL = Paths.get(PATH_RESULTS, "model.ttl").toString();

    private static final int NUM_INSTANCES_EN = 100000;
    private static final int NUM_INSTANCES_JA = 100000;

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException {
        //OntModel model = createModel();
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        model.read(FILE_MODEL);
        sparql(model);
    }

    private static OntModel createModel() throws IOException {
        logger.info("Creating ontology model");
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        model.read(FILE_DBPEDIA);

        logger.info("Add instances");
        addInstances(model, FILE_INSTANCE_TYPES_EN, NUM_INSTANCES_EN);
        addInstances(model, FILE_INSTANCE_TYPES_JA, NUM_INSTANCES_JA);

        logger.info("Add properties");
        addProperties(model, FILE_LITERALS_EN);
        addProperties(model, FILE_LITERALS_JA);
        addProperties(model, FILE_OBJECTS_EN);
        addProperties(model, FILE_OBJECTS_JA);

        logger.info("Add inter language links");
        addLinks(model, FILE_LINKS_EN, FILE_LINKS_JA);

        logger.info("Writing model");
        try (FileOutputStream writer = new FileOutputStream(FILE_MODEL)) {
            model.write(writer, Lang.TURTLE.getName());
        }
        return model;
    }

    // add 100k instances to model from instance_types_xx.ttl
    private static void addInstances(OntModel model, String file, int n) {
        logger.info("Reading model from {}", file);
        OntModel instanceTypes = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        instanceTypes.read(file);

        logger.info("Adding instances from {}", file);
        int i = 0;
        StmtIterator iterator = instanceTypes.listStatements();
        while (iterator.hasNext() && i < n) {
            Statement statement = iterator.nextStatement();
            model.add(statement);
            i++;
        }
        instanceTypes.close();
    }

    // add properties of the individuals in the given model from mappingbased_xx_xx.ttl
    private static void addProperties(OntModel model, String file) {
        logger.info("Reading model from {}", file);
        OntModel properties = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        properties.read(file);

        logger.info("Adding properties from {}", file);
        Iterator<Individual> individualIterator = model.listIndividuals();
        while (individualIterator.hasNext()) {
            Individual individual = individualIterator.next();
            properties.listStatements(individual, null, (RDFNode) null).forEachRemaining(model::add);
            properties.listStatements(null, null, individual).forEachRemaining(model::add);
        }
        properties.close();
    }

    // add inter language links. create a new model with all files, and find individuals with sameAs+ relation, then add
    // the relations into the original model where head and tail are both in the model
    private static void addLinks(OntModel model, String... files) {
        OntModel links = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        for (String file : files) {
            logger.info("Reading model from {}", file);
            links.read(file);
        }

        logger.info("Adding sameAs links");
        StmtIterator linkStatements = links.listStatements();
        while (linkStatements.hasNext()) {
            Statement statement = linkStatements.nextStatement();
            if (model.containsResource(statement.getSubject()) && model.containsResource(statement.getObject())) {
                logger.info("{}, {}", statement.getSubject(), statement.getObject());
                model.add(statement);
            }
        }

        links.close();
    }

    private static void sparql(OntModel model) {
        countIndividuals(model);
        countDatatypes(model);
        countObjects(model);
        countSameAs(model);
        findMostPopularIndividuals(model);
    }

    private static void countIndividuals(OntModel model) {
        System.out.println("Count individuals of en");
        QueryExecution qe = QueryExecutionFactory.create(
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                        "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                        "SELECT ?class (COUNT(?individual) as ?count) WHERE {\n" +
                        "?individual a/rdfs:subClassOf* ?class. FILTER REGEX(STR(?individual), 'http://dbpedia.org/resource/.*')\n" +
                        "} GROUP BY ?class", model);
        ResultSet rs = qe.execSelect();
        while (rs.hasNext()) {
            QuerySolution qs = rs.nextSolution();
            System.out.println(String.format("%s\t%s", qs.get("class"), qs.get("count")));
        }

        System.out.println("Count individuals of ja");
        qe = QueryExecutionFactory.create(
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                        "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                        "SELECT ?class (COUNT(?individual) as ?count) WHERE {\n" +
                        "?individual a/rdfs:subClassOf* ?class. FILTER REGEX(STR(?individual), 'http://ja.dbpedia.org/resource/.*')\n" +
                        "} GROUP BY ?class", model);
        rs = qe.execSelect();
        while (rs.hasNext()) {
            QuerySolution qs = rs.nextSolution();
            System.out.println(String.format("%s\t%s", qs.get("class"), qs.get("count")));
        }
    }

    private static void countDatatypes(OntModel model) {
        System.out.println("Count datatypes of en");
        QueryExecution qe = QueryExecutionFactory.create(
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                        "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                        "SELECT ?class (COUNT(?datatype) as ?count) WHERE {\n" +
                        "?individual a/rdfs:subClassOf* ?class. FILTER REGEX(STR(?individual), 'http://dbpedia.org/resource/.*')\n" +
                        "?datatype a owl:DatatypeProperty. ?individual ?datatype ?value.\n" +
                        "} GROUP BY ?class", model);
        ResultSet rs = qe.execSelect();
        while (rs.hasNext()) {
            QuerySolution qs = rs.nextSolution();
            System.out.println(String.format("%s\t%s", qs.get("class"), qs.get("count")));
        }

        System.out.println("Count datatypes of ja");
        qe = QueryExecutionFactory.create(
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                        "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                        "SELECT ?class (COUNT(?datatype) as ?count) WHERE {\n" +
                        "?individual a/rdfs:subClassOf* ?class. FILTER REGEX(STR(?individual), 'http://ja.dbpedia.org/resource/.*')\n" +
                        "?datatype a owl:DatatypeProperty. ?individual ?datatype ?value.\n" +
                        "} GROUP BY ?class", model);
        rs = qe.execSelect();
        while (rs.hasNext()) {
            QuerySolution qs = rs.nextSolution();
            System.out.println(String.format("%s\t%s", qs.get("class"), qs.get("count")));
        }
    }

    private static void countObjects(OntModel model) {
        System.out.println("Count objects of en");
        QueryExecution qe = QueryExecutionFactory.create(
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                        "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                        "SELECT ?class (COUNT(?object) as ?count) WHERE {\n" +
                        "?individual a/rdfs:subClassOf* ?class. FILTER REGEX(STR(?individual), 'http://dbpedia.org/resource/.*')\n" +
                        "?object a owl:ObjectProperty. ?individual ?object ?value.\n" +
                        "} GROUP BY ?class", model);
        ResultSet rs = qe.execSelect();
        while (rs.hasNext()) {
            QuerySolution qs = rs.nextSolution();
            System.out.println(String.format("%s\t%s", qs.get("class"), qs.get("count")));
        }

        System.out.println("Count objects of ja");
        qe = QueryExecutionFactory.create(
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                        "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                        "SELECT ?class (COUNT(?object) as ?count) WHERE {\n" +
                        "?individual a/rdfs:subClassOf* ?class. FILTER REGEX(STR(?individual), 'http://ja.dbpedia.org/resource/.*')\n" +
                        "?object a owl:ObjectProperty. ?individual ?object ?value.\n" +
                        "} GROUP BY ?class", model);
        rs = qe.execSelect();
        while (rs.hasNext()) {
            QuerySolution qs = rs.nextSolution();
            System.out.println(String.format("%s\t%s", qs.get("class"), qs.get("count")));
        }
    }

    private static void countSameAs(OntModel model) {
        System.out.println("Count sameAs");
        QueryExecution qe = QueryExecutionFactory.create(
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                        "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                        "SELECT (COUNT(?s) as ?count) WHERE {\n" +
                        "?s owl:sameAs ?o." +
                        "}", model);
        ResultSet rs = qe.execSelect();
        while (rs.hasNext()) {
            QuerySolution qs = rs.nextSolution();
            System.out.println(qs.get("count"));
        }
    }

    private static void findMostPopularIndividuals(OntModel model) {
        System.out.println("Find most popular individuals");
        QueryExecution qe = QueryExecutionFactory.create(
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                        "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                        "SELECT ?s (COUNT(?p) as ?count) WHERE {\n" +
                        "?s ?p ?o." +
                        "} GROUP BY ?s ORDER BY DESC(?count) LIMIT 10", model);
        ResultSet rs = qe.execSelect();
        while (rs.hasNext()) {
            QuerySolution qs = rs.nextSolution();
            System.out.println(String.format("%s\t%s", qs.get("s"), qs.get("count")));
        }
    }

//        System.out.println("");
//        qe = QueryExecutionFactory.create(
//                "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
//                        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
//                        "SELECT ?s ?o WHERE {" +
//                        "?s owl:sameAs/owl:sameAs+ ?o. FILTER NOT EXISTS { ?s owl:sameAs ?o} FILTER ( ?s != ?o )" +
//                        "}", model);
//        rs = qe.execSelect();
//        while (rs.hasNext()) {
//            QuerySolution qs = rs.nextSolution();
//            Resource subject = qs.getResource("s");
//            Resource object = qs.getResource("o");
//            if (model.containsResource(subject) && model.containsResource(object)) {
//                System.out.println(String.format("%s, %s", subject.toString(), object.toString()));
//            }
//        }

}
