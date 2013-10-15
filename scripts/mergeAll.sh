#!/bin/sh

git checkout ice-cream-sandwich
git merge master

git checkout beta
git merge master

git checkout beta-ics
git merge beta
git merge ice-cream-sandwich

git checkout master
