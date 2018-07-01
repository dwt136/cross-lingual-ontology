#!/bin/sh

mkdir -p datasets

echo 'Donwload dbpedia_2016-10.nt'
wget -P datasets http://downloads.dbpedia.org/2016-10/dbpedia_2016-10.nt

echo 'Download instance_types_en.ttl.bz2'
wget -P datasets http://downloads.dbpedia.org/2016-10/core/instance_types_en.ttl.bz2
echo 'Extract instance_types_en.ttl.bz2'
bzip2 -d datasets/instance_types_en.ttl.bz2

echo 'Download interlanguage_links_chapters_en.ttl.bz2'
wget -P datasets http://downloads.dbpedia.org/2016-10/core/interlanguage_links_chapters_en.ttl.bz2
echo 'Extract interlanguage_links_chapters_en.ttl.bz2'
bzip2 -d datasets/interlanguage_links_chapters_en.ttl.bz2

echo 'Download mappingbased_literals_en.ttl.bz2'
wget -P datasets http://downloads.dbpedia.org/2016-10/core/mappingbased_literals_en.ttl.bz2
echo 'Extract mappingbased_literals_en.ttl.bz2'
bzip2 -d datasets/mappingbased_literals_en.ttl.bz2

echo 'Download mappingbased_objects_en.ttl.bz2'
wget -P datasets http://downloads.dbpedia.org/2016-10/core/mappingbased_objects_en.ttl.bz2
echo 'Extract mappingbased_objects_en.ttl.bz2'
bzip2 -d datasets/mappingbased_objects_en.ttl.bz2

echo 'Download instance_types_ja.ttl.bz2'
wget -P datasets http://downloads.dbpedia.org/2016-10/core-i18n/ja/instance_types_ja.ttl.bz2
echo 'Extract instance_types_ja.ttl.bz2'
bzip2 -d datasets/instance_types_ja.ttl.bz2

echo 'Download interlanguage_links_ja.ttl.bz2'
wget -P datasets http://downloads.dbpedia.org/2016-10/core-i18n/ja/interlanguage_links_ja.ttl.bz2
echo 'Extract interlanguage_links_ja.ttl.bz2'
bzip2 -d datasets/interlanguage_links_ja.ttl.bz2

echo 'Download mappingbased_literals_ja.ttl.bz2'
wget -P datasets http://downloads.dbpedia.org/2016-10/core-i18n/ja/mappingbased_literals_ja.ttl.bz2
echo 'Extract mappingbased_literals_ja.ttl.bz2'
bzip2 -d datasets/mappingbased_literals_ja.ttl.bz2

echo 'Download mappingbased_objects_ja.ttl.bz2'
wget -P datasets http://downloads.dbpedia.org/2016-10/core-i18n/ja/mappingbased_objects_ja.ttl.bz2
echo 'Extract mappingbased_objects_ja.ttl.bz2'
bzip2 -d datasets/mappingbased_objects_ja.ttl.bz2
