#!/bin/sh

rm -f READY/*
mkdir -p READY

git checkout master
./scripts/packageTool.sh --updateVersion
ant distclean
fbuild
mv bin/FBReaderJ.apk READY
#cp bin/proguard/mapping.txt mappings/mapping-`cat VERSION`.master.txt

#git checkout ice-cream-sandwich
git checkout yota2
./scripts/packageTool.sh --updateVersion
ant clean
fbuild
mv bin/FBReaderJ.apk READY/FBReaderJ_ice-cream-sandwich.apk
#cp bin/proguard/mapping.txt mappings/mapping-`cat VERSION`.ice-cream-sandwich.txt

git checkout nook
./scripts/packageTool.sh --updateVersion
ant clean
fbuild
mv bin/FBReaderJ.apk READY/FBReaderJ_nst.apk

git checkout kindle
./scripts/packageTool.sh --updateVersion
ant clean
fbuild
mv bin/FBReaderJ.apk READY/FBReaderJ_kindlehd.apk
