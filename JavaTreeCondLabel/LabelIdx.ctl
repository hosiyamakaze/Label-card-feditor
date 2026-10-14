>cd G:\CMD\LabelforFeditor
>sqlite3 HŞƒ‰ƒxƒ‹.db3
drop table LabelIdxSTR;
.mode csv
.separator "\t"
.import ./LabelIdx.CSV LabelIdxSTR
.exit
