#!/bin/sh

git checkout master
git tag `cat VERSION`
#git checkout ice-cream-sandwich
git checkout yota2
git tag `cat VERSION`-ics
git checkout nook
git tag `cat VERSION`-nst
git checkout kindle
git tag `cat VERSION`-kindlefire
