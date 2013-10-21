#!/bin/sh

if [ "$1" == "" ]; then
	part=application
else
	part=$1
fi

for file in ../../assets/resources/$part/*.xml; do
	xsltproc clean.xslt $file > `basename $file`;
	egrep "[^%]%[^%s01]" $file
done
for file in *.xml; do
	diff $file en.xml > `basename $file .xml`.diff;
done
rm *.xml
