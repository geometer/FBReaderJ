#!/bin/sh

git checkout android-1.5
git merge --no-edit master

git checkout ice-cream-sandwich
git merge --no-edit master

git checkout beta
git merge --no-edit master

git checkout beta-ics
git merge --no-edit beta
git merge --no-edit ice-cream-sandwich

git checkout master
