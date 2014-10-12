#!/bin/sh

version=`cat VERSION | sed 's/ //g'`
git checkout master
git tag $version
#git checkout ice-cream-sandwich
git checkout yota2
git tag $version-ics
git checkout nook
git tag $version-nst
git checkout kindle
git tag $version-kindlefire
git checkout master
