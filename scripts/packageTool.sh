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
	branch=`git branch | grep "*" | cut -d " " -f 2`
	case $branch in
		android-1.5)
			variant=0
			;;
		ice-cream-sandwich)
			variant=2
			;;
		*-ics)
			variant=2
			;;
		*)
			variant=1
			;;
	esac
		
	if [ "$branch" == "beta-ics" -o "$branch" == "beta" ]; then
		version=`cat VERSION-BETA`
		major=1
		minor=9
		micro=`echo $version | cut -d " " -f 3`
	else
		major=`echo $version | cut -d . -f 1`
		minor=`echo $version | cut -d . -f 2`
		micro=`echo $version | cut -d . -f 3`
	fi
	
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

if [ $# -eq 2 ]; then
	version=$2
	echo $version > VERSION
else
	version=`cat VERSION`
fi

dir=FBReaderJ-sources-$version
archive=FBReaderJ-sources-$version.zip

case $1 in
	$updateVersionArg)
		updateVersion;
		;;
	$buildSourceArchiveArg)
		buildSourceArchive;
		;;
	*)
		printUsage;
		;;
esac
