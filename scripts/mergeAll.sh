#!/bin/sh

git checkout new-book-events
git merge master

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

git checkout master
