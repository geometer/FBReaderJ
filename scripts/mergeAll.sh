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

git checkout library-service
git merge master

git checkout library-service-ics
git merge library-service
git merge ice-cream-sandwich

git checkout master
