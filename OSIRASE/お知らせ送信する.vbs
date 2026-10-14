'SJIS $Workfile: お知らせ送信する.vbs $$Revision: 2 $$Date: 26/10/03 21:33 $
'$NoKeywords: $

'-----    下書きファイル(^_*)をメールする(Osirase\inに置く)マクロ

' FileSystemObjectのインスタンスを作成
Set fso = CreateObject("Scripting.FileSystemObject")

OriginalFile = Editor.ExpandParameter("$F")
MailDir ="G:\CMD\OSIRASE\IN\"
fileName = fso.GetFileName(OriginalFile)

	'下書きファイル名を決定する
	if Left(fileName, 1) = "_"  or Left(fileName, 1) = "."  THEN
		underFile = MailDir & fileName
		tarFile = Mid(fileName,2)
	else
		underFile = MailDir & "_" & fileName
		tarFile = Mid(fileName,1)
	end if

	Editor.TraceOut "-----	お知らせ送信します"
	Editor.TraceOut "original:" & OriginalFile
	Editor.TraceOut "spoolin:" & underFile
	Editor.TraceOut "Target:" & tarFile
	Editor.TraceOut "code(0:SJIS/4:UTF8):" & Editor.GetCharCode()

	sourceEncoding = "Default"
	if Editor.GetCharCode()<>0  then
'		Editor.ErrorMsg("SJISファイルを送信できます")
		sourceEncoding = "utf8"
	end if


	'下書きファイル(^_*)が存在することを確認する
	if Right(fileName, 4) <> ".txt"  THEN
		Editor.ErrorMsg(".txtファイルを送信できます")
	elseif (NOT fso.FileExists(OriginalFile)) then
		Editor.ErrorMsg("ファイルは保存されていません")
	else
		msgstr = fileName & "をお知らせ送信しますか?"
		ans = Editor.OkCancelBox(msgstr)
		if ans = 1 then
			cmdstr = "powershell.exe -Command get-content -encoding "& sourceEncoding &" -path " &_
			"'" & OriginalFile  & "'" & " |Out-File -FilePath " & _
			"'" & underFile & "'" & " -Encoding utf8"
			outstr= Editor.ExecCommand(cmdstr,1) 
			Editor.TraceOut outstr
			cmdstr = "rename """  & underFile & """ """  & tarFile & """"
			outstr= Editor.ExecCommand(cmdstr,1)
			Editor.TraceOut outstr
		end if
	end if
