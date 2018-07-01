Homework of Knowledge Engineering course

# Build
Use maven to build and package project.
```sh
mvn pakcage
```
This will make a directory named "target", and a file named "cross-lingual-ontology-1.0-SNAPSHOT.jar" in it.

# Download datasets
Download and extract datasets from dbpedia
```sh
./download_datasets.sh
```
This will make a directory named "datasets", with the following file:
- dbpedia_2016-10.nt
- instance_types_en.ttl
- instance_types_ja.ttl
- interlanguage_links_chapters_en.ttl
- interlanguage_links_ja.ttl
- mappingbased_literals_en.ttl
- mappingbased_literals_ja.ttl
- mappingbased_objects_en.ttl
- mappingbased_objects_ja.ttl

# Run
Run the jar built by maven
```sh
./run.sh
```
This will make a directory named "results", with a file named "model.ttl"
