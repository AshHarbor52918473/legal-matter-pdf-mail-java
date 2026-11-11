#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")"
mkdir -p build/test-classes
javac -d build/test-classes $(find src/main/java src/test/java -name '*.java')
java -cp build/test-classes com.example.legaltech.MatterDecisionTest
