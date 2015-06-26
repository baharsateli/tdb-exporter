# TDB Exporter
A command-line program for exporting a Jena TDB-based triple-store to various formats.

## Prerequisites

This program resolves its library dependencies using Ivy and thus does not need any external resources to run. However, please make sure you have the following tools installed on your system before installing/running this program:

* Java SE 7 or greater
* Apache ANT 8 or greater

## Installation

Browse the cloned directory on your file system and type `ant jar` (assuming that you already have ANT on your path). The build script will download the dependency libraries and generate an executable JAR file, named `tdb-exporter.jar` and store it within the same directory. That's it! :)

## Running

To run the program, simply type `java -jar tdb-exporter.jar [FORMAT] [TDB-PATH]` in your commandline.

* Replace the `[FORMAT]` option with one of format codes from the table below:

Format Code   | Serialization Format
------------- | -------------
TTL           | Turtle
XML           | RDF/XML
XMLA          | RDF/XML-ABBREV
N3            | Notation3
NT            | N-Triples
CSV           | Comma Separated Values


* The `[TDB-PATH]` option must be an absolute path to the TDB directory on your local filesystem. **Note** that the program does not support symbolic links to the TDB directory.


## Example

For example, if your TDB triple-store is in `/Users/bahar/tdb` and you would like to export it into Turtle format, you should use the command below:

`java -jar tdb-exporter.jar TTL /Users/bahar/tdb`
