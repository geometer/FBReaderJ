#!/usr/bin/env python2
import os, sys

# Retreived from Android Project Import Summary, README.md
unmoved = [
    "HowToBuild",
    "TODO.1.5",
    "TODO.2pages",
    "TODO.bookInfo",
    "TODO.catalogs",
    "TODO.dictionary",
    "TODO.favorites",
    "TODO.highlighting",
    "TODO.honeycomb",
    "TODO.library",
    "TODO.libraryService",
    "TODO.litres",
    "TODO.network",
    "TODO.selection",
    "TODO.tips",
    "help/"
]

ignored = [
    "ChangeLog",
    "VERSION",
    "AndroidManifest.xml.pattern",
    "scripts/"
]

conversionMap = {
    #In AmbilWarna:
    #third-party/drag-sort-listview/library/
    "third-party/AmbilWarna/AndroidManifest.xml" : "ambilWarna/src/main/AndroidManifest.xml",
    "third-party/AmbilWarna/res/" : "ambilWarna/src/main/res/",
    "third-party/AmbilWarna/src/" : "ambilWarna/src/main/java/",

    #In code:
    #third-party/android-filechooser/code/
    "third-party/android-filechooser/code/AndroidManifest.xml" : "code/src/main/AndroidManifest.xml",
    "third-party/android-filechooser/code/res/" : "code/src/main/res/",
    "third-party/android-filechooser/code/src/" : "code/src/main/java/",

    #In library:
    #third-party/drag-sort-listview/library/
    "third-party/drag-sort-listview/library/AndroidManifest.xml" : "library/src/main/AndroidManifest.xml",
    "third-party/drag-sort-listview/library/res/" : "library/src/main/res/",
    "third-party/drag-sort-listview/library/src/" : "library/src/main/java/",

    #In FBReaderJ:
    "AndroidManifest.xml" : "fBReaderJ/src/main/AndroidManifest.xml",
    "assets/" : "fBReaderJ/src/main/assets/",
    "jni/" : "fBReaderJ/src/main/jni/",
    "libs/httpmime-4.2.5.jar" : "fBReaderJ/libs/httpmime-4.2.5.jar",
    "libs/json-simple-1.1.1.jar" : "fBReaderJ/libs/json-simple-1.1.1.jar",
    "libs/LingvoIntegration_2.5.2.12.jar" : "fBReaderJ/libs/LingvoIntegration_2.5.2.12.jar",
    "libs/nanohttpd-2.0.5.jar" : "fBReaderJ/libs/nanohttpd-2.0.5.jar",
    "libs/open-dictionary-api-1.2.1.jar" : "fBReaderJ/libs/open-dictionary-api-1.2.1.jar",
    "libs/pdfparse.jar" : "fBReaderJ/libs/pdfparse.jar",
    "res/" : "fBReaderJ/src/main/res/",
    "src/" : "fBReaderJ/src/main/java/",
    "src/org/geometerplus/android/fbreader/api/ApiInterface.aidl" : "fBReaderJ/src/main/aidl/org/geometerplus/android/fbreader/api/ApiInterface.aidl",
    "src/org/geometerplus/android/fbreader/api/ApiObject.aidl" : "fBReaderJ/src/main/aidl/org/geometerplus/android/fbreader/api/ApiObject.aidl",
    "src/org/geometerplus/android/fbreader/api/TextPosition.aidl" : "fBReaderJ/src/main/aidl/org/geometerplus/android/fbreader/api/TextPosition.aidl",
    "src/org/geometerplus/android/fbreader/config/ConfigInterface.aidl" : "fBReaderJ/src/main/aidl/org/geometerplus/android/fbreader/config/ConfigInterface.aidl",
    "src/org/geometerplus/android/fbreader/formatPlugin/CoverReader.aidl" : "fBReaderJ/src/main/aidl/org/geometerplus/android/fbreader/formatPlugin/CoverReader.aidl",
    "src/org/geometerplus/android/fbreader/httpd/DataInterface.aidl" : "fBReaderJ/src/main/aidl/org/geometerplus/android/fbreader/httpd/DataInterface.aidl",
    "src/org/geometerplus/android/fbreader/libraryService/LibraryInterface.aidl" : "fBReaderJ/src/main/aidl/org/geometerplus/android/fbreader/libraryService/LibraryInterface.aidl",
    "src/org/geometerplus/android/fbreader/libraryService/PositionWithTimestamp.aidl" : "fBReaderJ/src/main/aidl/org/geometerplus/android/fbreader/libraryService/PositionWithTimestamp.aidl",
    "src/org/geometerplus/android/fbreader/network/BookDownloaderInterface.aidl" : "fBReaderJ/src/main/aidl/org/geometerplus/android/fbreader/network/BookDownloaderInterface.aidl",
}


def getABFileNames(line):
    split = line.split()
    fileA = ""
    fileB = ""
    for word in split:
        if word.startswith("a/"):
            fileA = word[2:]
        elif word.startswith("b/"):
            fileB = word[2:]
    return fileA, fileB

deleteLine = False
deleteLineMarker = "@@[[{{!!TO_BE_DELETED!!}}]]@@"

def convertToAndroidStudioPaths(line):
    global deleteLine
    print line
    a, b = getABFileNames(line)
    fullPath = a if b == "" else b
    splitPaths = fullPath.split('/')
    firstPath = splitPaths[0] + '/' if len(splitPaths) > 1 else splitPaths[0]

    deleteLine = False
    converted = ""
    if firstPath in conversionMap:
        if fullPath in conversionMap:
            # Then this file is an .aidl file
            print "OK: AIDL file \'" + firstPath + "\' is in map as " + conversionMap[firstPath]
            converted = conversionMap[fullPath]
        else:
            print "OK: \'" + firstPath + "\' is in map as " + conversionMap[firstPath]
            splitPaths[0] = conversionMap[firstPath][:-1] if len(splitPaths) > 1 else conversionMap[firstPath]
            converted = '/'.join(splitPaths)
    else:
        if firstPath in unmoved:
            converted = fullPath
            print "UNMOVED: \'" + firstPath + "\' is unmoved"
        elif firstPath in ignored:
            deleteLine = True
            converted = deleteLineMarker
            print "IGNORED: \'" + firstPath + "\' is ignored"
        else:
            print "FAIL: \'" + firstPath + "\' is not in map"
            sys.exit()
    print converted + '\n'

    convertedLine = ""
    if line.startswith("diff --git a/"):
        convertedLine = "diff --git a/" + converted + " b/" + converted
    elif line.startswith("--- a/"):
        convertedLine = "--- a/" + converted
    elif line.startswith("+++ b/"):
        convertedLine = "+++ b/" + converted
    return deleteLineMarker if deleteLine else convertedLine

def isADiffHeader(line):
    return line.startswith("diff --git a/") or line.startswith("--- a/") or line.startswith("+++ b/")

def keepLine(line):
    return deleteLineMarker if deleteLine else line


inputFileName = "patch" 
outputFileName = ""
# Optional arguments to specify input/output files
try:
    inputFileName = sys.argv[1]
    outputFileName = sys.argv[2]
except:
    {}

# Do the conversion
try:
    with open(inputFileName, 'r') as originalPatch:
        # Read all lines into memory
        lines = [line.rstrip() for line in originalPatch.readlines()]
        # Convert paths to android studio paths if it's a diff header, and mark 
        # the line for deletion if it is a diff for an ignored file
        altered_lines = [convertToAndroidStudioPaths(line) if isADiffHeader(line) else keepLine(line) for line in lines]
        # Remove the lines marked by deleteLineMarker from our new diff
        altered_lines = [line for line in altered_lines if line != deleteLineMarker]

    outputFileName = inputFileName + "NEW" if outputFileName == "" else outputFileName
    # Open as binary to avoid EOL errors
    with open(outputFileName, 'wb') as convertedPatch:
        # Rejoin the lines and write to the new file
        convertedPatch.write('\n'.join(altered_lines) + '\n')
    
    print "SUCCESS: Converted diff saved as \'" + outputFileName + "\'"
except Exception as e:
    print str(e)
    """
    Usage: converter.py [inputFile] [outputFile]

    This script converts FBReaderJ diffs from the Eclipse project
    structure to the Android Studio project structure

	git checkout upstream_master
	git pull
	git diff (SHA of last full sync) > ../patch
	git checkout android_studio
	converter.py ../patch ../patchNEW
	git apply ../patchNEW

    Unrecognized files will immediately stop conversion

    Default [inputFile] is file name "patch" if no arguments are supplied
    Appends "NEW" onto output file name if [outputFile] is not supplied
    """
