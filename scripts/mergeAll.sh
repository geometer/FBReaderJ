#!/bin/sh

git checkout android-1.5
git merge master

git checkout ice-cream-sandwich
git merge master

git checkout beta
git merge master

git checkout beta-ics
git merge ice-cream-sandwich
git merge beta

git checkout booksdb
git merge master

git checkout booksdb-ics
git merge ice-cream-sandwich
git merge booksdb

git checkout library-service-ics
git merge booksdb-ics

git checkout epub-native
git merge master

git checkout epub-native-ics
git merge ice-cream-sandwich
git merge epub-native

git checkout multidirs
git merge master

git checkout master
