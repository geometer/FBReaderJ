#!/bin/sh

name=FBReaderJ
size=`du -b ../../$name.jar | cut -f 1`
sed "s/%SIZE%/$size/" < $name.jad-pattern > ../../$name.jad
