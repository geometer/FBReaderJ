#!/bin/sh
for file in ../../assets/resources/application/*.xml; do
  lfile=`echo $file | sed "s/application/zlibrary/"`
  lang=`basename $file .xml;`
  all_count=`egrep 'name=.+value="' $file | wc | awk '{ print $1 }'`
  all_lcount=`egrep 'name=.+value="' $lfile | wc | awk '{ print $1 }'`
  neg_count=`fgrep toBeTranslated $file | wc | awk '{ print $1 }'`
  neg_lcount=`fgrep toBeTranslated $lfile | wc | awk '{ print $1 }'`
	full_count=$(($all_count+$all_lcount))
	pos_full_count=$(($all_count+$all_lcount-$neg_count-$neg_lcount))
	percent=$(($pos_full_count*100/$full_count))
	if [ "$1" == "-html" ]; then
		echo "<tr><td>$lang</td><td>$pos_full_count ($percent %)</td>"
	else
		echo $lang $(($all_count+$all_lcount-$neg_count-$neg_lcount)) of $(($all_count+$all_lcount)) $percent
	fi
done
