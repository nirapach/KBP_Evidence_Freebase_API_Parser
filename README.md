# OpenFDA
Created a project from scratch to enable Json Parsing of Freebase and wkipedia extraction data sets using Spring and HttpClient


To Execute:
1) Clone the repo
2) Created the jar file
    a) mvn clean generate-resources
    b) mvn clean install
    c) mvn exec:java
3) Execute the jar file with three folder paths
    a) Source folder for reading the freebase triples
     b) destination folder for freebase data(this is same for wikipedia source folder)
     c) destination folder for wikipedia data
