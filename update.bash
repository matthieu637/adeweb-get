#!/bin/bash

cmd='/usr/bin/java -jar /home/matthieu/run/edt/GetEDT.jar'

login='zimme0511'
pass='e`J5b]Y9'

id_annee='1'
recherche="S6 INFO"
id_EDT='18299'
base_path='/home/matthieu/public_html/fac/EDT/fixe'

#calcul date
iso_week=`date +%U`
ade_week=`expr $iso_week - 35` 

annee=`date +%Y`
mois=`date +%m`
jour=`date +%d`
pr_annee=`date --date='3 week' +%Y`
pr_mois=`date --date='3 week' +%m`
pr_jour=`date --date='3 week' +%d`

#get all edt
$cmd $login $pass edt $id_annee "$recherche" $id_EDT $base_path/cache/edt_ $ade_week 1530 600 >&/dev/null

nb=`ls $base_path/cache/ | grep 'edt_.*[.]gif' | wc -l`
if [ $nb -gt 0 ] ; then
	rm $base_path/edt_*.gif
	mv $base_path/cache/edt_*.gif $base_path/
	chmod g+rx $base_path/edt_*.gif
fi


#get ical
$cmd $login $pass ical $id_annee "$recherche" $id_EDT $base_path/ical $jour $mois $annee $pr_jour $pr_mois $pr_annee >&/dev/null
chmod g+rx $base_path/ical.ical



