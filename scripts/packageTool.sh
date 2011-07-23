#!/bin/sh

updateVersionArg="--updateVersion"
buildSourceArchiveArg="--buildSourceArchive"

printUsage() {
	echo "usages:\n  $0 $updateVersionArg\n  $0 $buildSourceArchiveArg";
	exit;
}

if [ $# -ne 1 -a $# -ne 2 ]; then
	printUsage;
fi

updateVersion() {
	major=`echo $version | cut -d . -f 1`
	minor=`echo $version | cut -d . -f 2`
	micro=`echo $version | cut -d . -f 3`
	if [ "$micro" == "" ]; then
     micro=0
  fi
	intversion=$((100000*$major+1000*$minor+10*$micro+$variant))
	sed "s/@INTVERSION@/$intversion/" AndroidManifest.xml.pattern | sed "s/@VERSION@/$version/" > AndroidManifest.xml
}

buildSourceArchive() {
	updateVersion
  ant distclean
  rm -rf $dir $archive
  mkdir $dir
  cp -r assets icons src jni build.xml AndroidManifest.xml* res *.properties HowToBuild $0 VERSION ChangeLog $dir
  rm -rf `find $dir -name .svn`
  zip -rq $archive $dir/*
  rm -rf $dir
}

version=`cat VERSION`
if [ $# -eq 2 ]; then
	variant=$2;
else
	variant=0
fi

dir=FBReaderJ-sources-$version
archive=FBReaderJ-sources-$version.zip

case $1 in
	$updateVersionArg)
		variant=$2
		updateVersion;
		;;
	$buildSourceArchiveArg)
		buildSourceArchive;
		;;
	*)
		printUsage;
		;;
esac
