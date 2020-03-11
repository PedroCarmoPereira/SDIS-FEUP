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

javac -d "$BIN_DIR" *.java 

#run
java -cp "./bin/client" "$FILE_NAME" "$@";