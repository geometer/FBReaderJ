#!/bin/sh

doRepair() {
  ./repair.py $1 $2 | xmllint --format - | sed 's/\(toBeTranslated="true"\) \(value=".*"\)\(.*\)$/\2 \1\3/'
}

if [ "$1" == "" ]; then
	part=application
else
	part=$1
fi

sed "s/&#10;/#XXX;/g" ../../assets/resources/$part/en.xml > en.tra

for file in ../../assets/resources/$part/*.xml; do
  shortname=`basename $file .xml`
  if [ "$shortname" != en -a "$shortname" != "neutral" ]; then
    sed "s/&#10;/#XXX;/g" $file > $shortname.tra
    doRepair en.tra $shortname.tra | sed "s/#XXX;/\&#10;/g" > $file
  fi
done

rm *.tra
