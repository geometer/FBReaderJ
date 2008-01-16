#!/bin/sh

SP0=../../src
SP1=src

javac -source 1.3 -target 1.3 -sourcepath $SP0:$SP1 -d ../../bin -Xbootclasspath:/home/geometer/WTK2.5.2/lib/cldcapi11.jar:/home/geometer/WTK2.5.2/lib/midpapi20.jar `find $SP0 -name "*.java" | grep -v sax | grep -v xmlconfig | grep -v swing | grep -v android` `find $SP1 -name "*.java"`
