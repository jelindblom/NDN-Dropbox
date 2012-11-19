#!/bin/sh

# Make bin directory
mkdir -p ./NDNDropbox/bin

# Compile Application Source
javac -d ./NDNDropbox/bin -classpath ./NDNDropbox/libs/ccn.jar:./NDNDropbox/libs/bcprov-jdk16-143.jar:./NDNDropbox/libs/commons-codec-1.7.jar:./NDNDropbox/libs/commons-io-2.4.jar:./NDNDropbox/libs/jnotify-0.94.jar ./NDNDropbox/src/*.java

# Compiled
echo "Compiled."
