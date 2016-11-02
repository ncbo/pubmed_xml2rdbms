# pubmed_xml2rdbms

[![Dependency Status](https://www.versioneye.com/user/projects/58055a924c74140037801d60/badge.svg?style=flat-square)](https://www.versioneye.com/user/projects/58055a924c74140037801d60)

This is an ETL project that transfers MEDLINE/PubMed citation record data from a set of XML files to a MySQL table.

The National Library of Medicine provides a [baseline set of MEDLINE/PubMed citation records](https://www.nlm.nih.gov/databases/download/pubmed_medline.html) in XML format for bulk download on an annual basis.  For each citation record, we extract the following subset of data:

* PubMed ID
* Article Title
* Abstract Text
* Keywords
* Medical Subject Headings (MeSH)

