#!/bin/bash
set -e

# Clean previous artifacts
rm -f *.class eduardo_lima_dos_santos.tar.gz

# Compile
javac HtmlAnalyzer.java

# Package (NO directories inside tar)
tar -czf eduardo_lima_dos_santos.tar.gz HtmlAnalyzer.java README.md

echo "Build and package completed: eduardo_lima_dos_santos.tar.gz"
