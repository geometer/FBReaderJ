#!/bin/sh

git checkout ice-cream-sandwich
git merge master

git checkout yota2
git merge ice-cream-sandwich

git checkout kindle
git merge yota2

git checkout nook
git merge master

git checkout microtypography
git merge yota2

git checkout 2.6ics
git merge ice-cream-sandwich

git checkout 2.6
git merge 2.6ics
git merge yota2

git checkout 2.6-kindle
git merge 2.6
git merge kindle

git checkout master
