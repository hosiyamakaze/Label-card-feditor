# Osirase

共有フォルダのメッセージファイルを監視し、メール送信する .NET 8 Worker Service です。

## 監視ルール

- 書き込み中のファイルは名前の先頭を `.` または `_` にする
- 書き込み完了後、その先頭文字を外して同じフォルダ内でリネームする
- 先頭が `.` `_` のファイルは送信しない
- メッセージファイルは本文のみ。To / Cc / Subject は appsettings の `Mail`（各1件）

## サービス登録

```
dotnet publish -c Release -o C:\Apps\Osirase
sc create Osirase binPath= "C:\Apps\Osirase\Osirase.exe" start= auto
sc description Osirase "サクラエディタ・お知らせ送信するマクロにより配置されたファイルをメール送信する"
sc start Osirase
```

##appsettings.json
`RetentionDays` は送信済みフォルダの保持日数です。開始時と日付が変わったときに、最終更新日時で削除します。
Cc を使わない場合は空文字にしてください。
