#!/bin/sh

aliasArg="-alias"
fileArg="-file"
listArg="-list"
keystoreArg="-keystore"

storepass="fbreader"


printUsage() {
	echo "usage:\n  $0 $aliasArg <alias> $fileArg <certificate-file> $keystoreArg <keystore-file> \n  $0 $listArg [verbose|rfc]";
	exit;
}

listCerts() {
	if [ ".$1." = ".verbose." ]; then
		mode="-v"
	elif [ ".$1." = ".rfc." ]; then
		mode="-rfc"
	fi
	keytool -list $mode -keystore "$keystore" -storepass "$storepass"
	exit;
}

importCert() {
	keytool -importcert -trustcacerts -alias "$alias" -file "$file" -keystore "$keystore" -storepass "$storepass"
	exit;
}

if [ $# -ne 6 -a $# -ne 1 -a $# -ne 2 ]; then
	printUsage;
fi

case $1 in
	$listArg)
		listCerts $2;
		;;
	$aliasArg)
		alias=$2;
		;;
	$fileArg)
		file=$2;
		;;
	$keystoreArg)
		keystore=$2
		;;
	*)
		printUsage;
		;;
esac

case $3 in
	$aliasArg)
		alias=$4;
		;;
	$fileArg)
		file=$4;
		;;
	$keystoreArg)
		keystore=$4
		;;
	*)
		printUsage;
		;;
esac

case $5 in
	$aliasArg)
		alias=$6;
		;;
	$fileArg)
		file=$6;
		;;
	$keystoreArg)
		keystore=$6
		;;
	*)
		printUsage;
		;;
esac

if [ ".$alias." = ".." -o ".$file." = ".." -o ".$keystore." = ".." ]; then
	printUsage;
fi

importCert;

