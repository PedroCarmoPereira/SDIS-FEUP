#!/usr/bin/env bash
 
# Stored class files
BIN_DIR=./bin/Server
 
# Clean contents of directories for
# fresh compilation
rm -r "$BIN_DIR"
mkdir "$BIN_DIR"
 
# Start compilation

javac -d "$BIN_DIR" src/*.java 

# Stored class files
BIN_DIR=./bin/TestApp
 
# Clean contents of directories for
# fresh compilation
rm -r "$BIN_DIR"
mkdir "$BIN_DIR"
 
# Start compilation

javac -d "$BIN_DIR" src/*.java