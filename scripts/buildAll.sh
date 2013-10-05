#!/bin/sh

rm -f READY/*

git checkout master
./scripts/packageTool.sh --updateVersion
./scripts/packageTool.sh --buildSourceArchive
mv FBReader*sources*.zip READY
ant distclean
./buildSignedJar.sh
mv bin/FBReaderJ.apk READY
cp bin/proguard/mapping.txt mappings/mapping-`cat VERSION`.master.txt

git checkout ice-cream-sandwich
./scripts/packageTool.sh --updateVersion
ant clean
./buildSignedJar.sh
mv bin/FBReaderJ.apk READY/FBReaderJ_ice-cream-sandwich.apk
cp bin/proguard/mapping.txt mappings/mapping-`cat VERSION`.ice-cream-sandwich.txt

git checkout beta
ant distclean
./buildSignedJar.sh
mv bin/FBReaderJ.apk READY/FBReaderJ-`cat VERSION-BETA | sed "s/ //g"`.apk

git checkout beta-ics
ant clean
./buildSignedJar.sh
mv bin/FBReaderJ.apk READY/FBReaderJ_ice-cream-sandwich-`cat VERSION-BETA | sed "s/ //g"`.apk
