#!/bin/sh
mvn package
java -jar target/PomatoTimer-1.0-SNAPSHOT.jar
echo "If this ^^^ fails try cd target/classes; java org.example.Main;  echo FTW;"
