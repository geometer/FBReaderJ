#!/bin/sh

git checkout ice-cream-sandwich
git merge master

git checkout beta
git merge master

git checkout beta-ics
git merge beta
git merge ice-cream-sandwich

git checkout plugins
git merge master

git checkout plugins-ics
git merge plugins
git merge ice-cream-sandwich

git checkout yota
git merge ice-cream-sandwich

git checkout yota2
git merge ice-cream-sandwich

git checkout master
