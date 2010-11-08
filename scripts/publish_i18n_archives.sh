#!/bin/sh

VERSION=`cat VERSION`

build_language_archive() {
	lang=$1
	dir=FBReaderJ-i18n-$lang-$VERSION
	mkdir $dir
	cp assets/resources/application/$lang.xml $dir/FBReader-$lang.xml
	cp assets/resources/zlibrary/$lang.xml $dir/zlibrary-$lang.xml
	cp assets/help/MiniHelp.$lang.fb2 $dir
	zip -r $dir.zip $dir
	rm -rf $dir
}

for file in assets/resources/application/*; do
	build_language_archive `basename $file .xml`;
done;

scp FBReaderJ-i18n-*-$VERSION.zip geometer@only.mawhrin.net:www/docs
rm FBReaderJ-i18n-*-$VERSION.zip
