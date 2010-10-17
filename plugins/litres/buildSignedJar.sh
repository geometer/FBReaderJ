#!/bin/sh

tools_dir=~/android-sdk-mac_86/tools
signed_jar=bin/FBReaderJ-plugin-litres.apk
unsigned_jar=bin/FBReaderJ-plugin-litres-unsigned.apk

if ant package; then
	rm -f $signed_jar
	jarsigner -storepass tinstaafl -keystore ~/.android/geometerplus.keystore $unsigned_jar geometerplus
	$tools_dir/zipalign -v 4 $unsigned_jar $signed_jar

	if [ $# -eq 1 -a "$1" == "install" ]; then
		$tools_dir/adb install -r $signed_jar
	fi
fi
