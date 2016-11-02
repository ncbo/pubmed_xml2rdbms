# pubmed_xml2rdbms

[![Dependency Status](https://www.versioneye.com/user/projects/58055a924c74140037801d60/badge.svg?style=flat-square)](https://www.versioneye.com/user/projects/58055a924c74140037801d60)

This is an ETL project that transfers MEDLINE/PubMed citation record data from a set of XML files to a MySQL table.

The National Library of Medicine (NLM) provides a [baseline set of MEDLINE/PubMed citation records](https://www.nlm.nih.gov/databases/download/pubmed_medline.html) in XML format for bulk download on an annual basis.  For each citation record, we extract the following subset of data:

* PubMed ID
* Article Title
* Abstract Text
* Keywords
* Medical Subject Headings (MeSH)

After all citation records are processed, the resulting fully-populated MySQL table is utilized by the [NCBO Resource Index](https://github.com/ncbo/resource_index) project.

### Configuration

The configuration file in `src/main/resources` allows for specification of a path to the baseline set of XML files, as well as database information, e.g., table name, credentials, etc.

Use the logback.xml file in `src/main/resources` to customize log output.

### Build

This is a Maven project.  Use the typical Maven command to compile and package a runnable JAR file:

`mvn package`

Make sure to use the JAR file with dependencies included, e.g.:

`pubmed-xml2rdbms-1.0-SNAPSHOT-jar-with-dependencies.jar`

### Run

Successful execution of the JAR file assumes that:

* You have access to the MySQL database specified in the configuration file
* You downloaded the baseline set of XML files from NLM, and specified the path in the configuration file

`java -jar pubmed-xml2rdbms-1.0-SNAPSHOT-jar-with-dependencies.jar`
