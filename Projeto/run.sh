#!/usr/bin/env bash
 
# Stored class files
BIN_DIR=./bin/TestApp
# File name without extension
FILE_NAME=TestApp
 
# Clean contents of directories for
# fresh compilation
rm -r "$BIN_DIR"
mkdir "$BIN_DIR"
 
# Start compilation

javac -d "$BIN_DIR" src/*.java

#run
java -cp "$BIN_DIR" "$FILE_NAME" "$@";