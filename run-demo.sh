#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")"
mkdir -p build/classes
javac -d build/classes $(find src/main/java -name '*.java')
java -cp build/classes com.example.legaltech.MatterReportExample
