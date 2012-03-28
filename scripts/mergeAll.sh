#!/bin/sh

git checkout android-1.5
git merge master

git checkout ice-cream-sandwich
git merge master

git checkout beta
git merge master

git checkout beta-ics
git merge beta
git merge ice-cream-sandwich

git checkout booksdb
git merge master

git checkout booksdb-ics
git merge booksdb
git merge ice-cream-sandwich

git checkout library-service-ics
git merge booksdb-ics

git checkout master
