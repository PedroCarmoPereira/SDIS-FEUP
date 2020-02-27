#!/usr/bin/env bash
 
# Stored class files
BIN_DIR=./bin/server
# File name without extension
FILE_NAME=Server
 
# Clean contents of directories for
# fresh compilation
rm -r "$BIN_DIR"
mkdir "$BIN_DIR"
 
# Start compilation

javac *.java -d "$BIN_DIR"

#run
java -cp bin/server "$FILE_NAME" "$@";