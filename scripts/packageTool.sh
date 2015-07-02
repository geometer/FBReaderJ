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
		ice-cream-sandwich|yota|yota2|lr-kindle|kindle|betayota|yotabeta|*-ics)
			variant=2
			;;
		*)
			variant=1
			;;
	esac

	fixed=`echo $version | sed "s/ *beta */./"`

	major=`echo $fixed | cut -d . -f 1`
	minor=`echo $fixed | cut -d . -f 2`
	micro=`echo $fixed | cut -d . -f 3`
	local=`echo $fixed | cut -d . -f 4`
	if [ "$branch" == "nook" -o "$branch" == "lr-nook" ]; then
		readable_version=$version-nst
	elif [ "$branch" == "kindle" -o "$branch" == "lr-kindle" ]; then
		readable_version=$version-kindlehd
	else
		readable_version=$version
	fi
	
	if [ "$micro" == "" ]; then
    micro=0
  fi
	if [ "$version" != "$fixed" ]; then
		minor=$(($minor - 1))
		micro=$(($micro + 50))
	fi
	if [ "$local" == "" ]; then
    local=0
  fi
	intversion=$((1000000*$major+10000*$minor+100*$micro+10*$variant+$local))
	sed "s/@INTVERSION@/$intversion/" AndroidManifest.xml.pattern | sed "s/@VERSION@/$readable_version/" > AndroidManifest.xml
}

buildSourceArchive() {
	updateVersion
  ant distclean
  rm -rf $dir $archive
  mkdir $dir
  cp -r assets src jni build.xml AndroidManifest.xml* res *.properties HowToBuild $0 VERSION ChangeLog $dir
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
