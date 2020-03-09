#!/usr/bin/env bash
 
# Stored class files
BIN_DIR=./bin/client
# File name without extension
FILE_NAME=Client
 
# Clean contents of directories for
# fresh compilation
rm -r "$BIN_DIR"
mkdir "$BIN_DIR"
 
# Start compilation

javac -cp ".:./lib/pdfbox-2.0.19.jar:./lib/commons-logging-1.2.jar" -d "$BIN_DIR" *.java 

#run
java -cp ".:./lib/pdfbox-2.0.19.jar:./lib/commons-logging-1.2.jar:./bin/client" "$FILE_NAME" "$@";