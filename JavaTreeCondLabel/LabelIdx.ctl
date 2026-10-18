>cd G:\CMD\LabelforFeditor
>sqlite3 食材ラベル.db3
drop table LabelIdxSTR;
.mode csv
.separator "\t"
.import ./LabelIdx.CSV LabelIdxSTR
.exit
