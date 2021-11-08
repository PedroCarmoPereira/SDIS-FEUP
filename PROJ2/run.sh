#!/usr/bin/env bash
 
# Stored class files
BIN_DIR=./bin/TestApp
# File name without extension
#FILE_NAME=TestApp
 
# Clean contents of directories for
# fresh compilation
#rm -r "$BIN_DIR"
#mkdir "$BIN_DIR"
 
# Start compilation

#javac -d "$BIN_DIR" src/*.java

#run
java -cp "$BIN_DIR" TestApp 4545 1 backup testFiles/test_big2.pdf &
java -cp "$BIN_DIR" TestApp 4545 1 backup testFiles/testeSDIS