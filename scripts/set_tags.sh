#!/bin/sh

version=`cat VERSION | sed 's/ //g'`
git checkout lr
git tag $version
#git checkout ice-cream-sandwich
git checkout lr-ics
git tag $version-ics
git checkout lr-nook
git tag $version-nst
git checkout lr-kindle
git tag $version-kindlefire
git checkout master
