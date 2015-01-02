#!/bin/sh

rm -f READY/*
mkdir -p READY

#git checkout master
git checkout lr
./scripts/packageTool.sh --updateVersion
ant distclean
fbuild
mv bin/FBReaderJ.apk READY
#cp bin/proguard/mapping.txt mappings/mapping-`cat VERSION`.master.txt

#git checkout ice-cream-sandwich
#git checkout yota2
git checkout lr-ics
./scripts/packageTool.sh --updateVersion
ant clean
fbuild
mv bin/FBReaderJ.apk READY/FBReaderJ_ice-cream-sandwich.apk
#cp bin/proguard/mapping.txt mappings/mapping-`cat VERSION`.ice-cream-sandwich.txt

#git checkout nook
git checkout lr-nook
./scripts/packageTool.sh --updateVersion
ant clean
fbuild
mv bin/FBReaderJ.apk READY/FBReaderJ_nst.apk

#git checkout kindle
git checkout lr-kindle
./scripts/packageTool.sh --updateVersion
ant clean
fbuild
mv bin/FBReaderJ.apk READY/FBReaderJ_kindlehd.apk
