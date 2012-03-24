#!/bin/sh

git checkout android-1.5
git merge master

git checkout ice-cream-sandwich
git merge master

git checkout native
git merge master

git checkout native-ics
git merge native
git merge ice-cream-sandwich

git checkout external-formats
git merge master

git checkout booksdb
git merge master

git checkout booksdb-ics
git merge booksdb
git merge ice-cream-sandwich

git checkout library-service-ics
git merge booksdb-ics

git checkout master
