#!/bin/sh

git checkout ice-cream-sandwich
git merge master

git checkout beta
git merge master

git checkout beta-ics
git merge beta
git merge ice-cream-sandwich

git checkout catalogs-ics
git merge ice-cream-sandwich

git checkout catalogs-ics-reorder
git merge catalogs-ics

git checkout postion-2
git merge master

git checkout master
