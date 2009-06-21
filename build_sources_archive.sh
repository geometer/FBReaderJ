#!/bin/sh

version=`cat VERSION`
intversion=`echo $version | sed "s/\\./0/g" | sed -E "s/^0+//"`
dir=FBReaderJ-sources-$version
archive=FBReaderJ-sources-$version.zip

echo "<!ENTITY FBReaderVersion   \"$version\">" > data/formats/fb2/FBReaderVersion.ent
sed "s/@INTVERSION@/$intversion/" platform/android/AndroidManifest.xml.pattern | sed "s/@VERSION@/$version/" > platform/android/AndroidManifest.xml

rm -rf $dir $archive
mkdir $dir
cp -r data icons src platform manifest.mf build.xml build_sources_archive.sh VERSION $dir
rm -rf `find $dir -name .svn`
zip -rq $archive $dir/*
rm -rf $dir
