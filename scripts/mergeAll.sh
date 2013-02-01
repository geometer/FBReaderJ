#!/bin/sh

git checkout android-1.5
git merge master

git checkout ice-cream-sandwich
git merge master

git checkout multidirs
git merge master

git checkout beta
git merge multidirs

git checkout beta-ics
git merge ice-cream-sandwich
git merge beta

git checkout library-service
git merge master

git checkout library-service-ics
git merge ice-cream-sandwich
git merge library-service

git checkout master
