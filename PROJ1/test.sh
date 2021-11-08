#!/usr/bin/env bash
 
# Stored class files
BIN_DIR=./bin/TestApp
# File name without extension
FILE_NAME=TestApp

#run
java -cp "$BIN_DIR" "$FILE_NAME" "$@";