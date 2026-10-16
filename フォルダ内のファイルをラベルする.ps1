#SJIS $Workfile: フォルダ内のファイルをラベルする.ps1 $$Revision: 2 $$Date: 26/10/16 12:27 $
#$NoKeywords: $

$root = "G:\CMD\LabelforFeditor"
cd $root
$importRoot = ".\レシピ"
$defs = Import-Csv -Path "LabelId.csv" -Delimiter "`t" -Encoding UTF8
$index = "labelidx.csv"

#FIP2ファイル名をコンソールより入力する
Write-Host -ForegroundColor green -NoNewline "FIP2ファイル名?"
$tarfl = read-host
#入力ファイルが存在することを確認する
if(-not (Test-Path -Path $tarfl)){
    Write-Host -ForegroundColor red -NoNewline "FIP2ファイルが存在しません。処理を中断します。"
    exit
}

#出力のfipファイル名を決定する
$fipfl = (get-item $tarfl).Directory.FullName+"\"+(get-item $tarfl).basename +"_Labeled.fip2"
Write-Host -ForegroundColor Yellow -NoNewline "Labeled.FIP2ファイル名:"
Write-Host $fipfl

#出力ファイルが存在しないことを確認する
if(Test-Path -Path $fipfl){
    Write-Host -ForegroundColor red -NoNewline "同名のFIP2ファイルが存在します。更新しますか(y/n)?"
    $ans = Read-Host 
    if ($ans.ToUpper() -eq "Y"){
        Remove-Item -Path  $fipfl -Force
    }else{
        Write-Host -ForegroundColor red "処理を中断します。"
        exit
    }
}

#ラベルタイプをコンソールより入力する
Write-Host -ForegroundColor green -NoNewline "ラベルタイプ(1:ラベル/2:正規表現/3(else):正規表現 as ラベル)?"
$LabelType = read-host

#Labelを検索する
$total = $defs.Count
Write-Host -ForegroundColor Yellow  "Label索引を検索中..."
$Labels = foreach ($d in $defs) {
Write-Host  -ForegroundColor white  $d.id.Trim() "/" $defs.Count $d.Label.Trim() 
    Get-ChildItem -Path $importRoot -Filter *.txt -Recurse -File |
        Where-Object {
            Select-String -Path $_.FullName -Pattern $d.expr -Quiet 
        } |
        ForEach-Object {
            [pscustomobject]@{
                Title = [System.IO.Path]::GetFileNameWithoutExtension($_.Name)
                id = $d.id.Trim()
                label = $d.Label.Trim()
                full_path = $_.FullName.Trim()
            }
        }
}
Write-Host -ForegroundColor Yellow "済み."

#Label索引ファイルに保存する
Write-Host -ForegroundColor Yellow -NoNewline "Label索引を保存中..."
$Labels | Export-Csv -Path $index -NoTypeInformation -Encoding UTF8 -Delimiter "`t"


#TitleのLable Idをリスト化する
$IdsBbyTitle = @{}
Import-Csv $index -Delimiter "`t" -Encoding UTF8 |
    Group-Object Title |
    ForEach-Object {
        $IdsBbyTitle[$_.Name] = ($_.Group.id | Sort-Object -Unique) -join ","
    }
Write-Host -ForegroundColor Yellow "済み."


#FIP2ファイルを読み込む
Write-Host -ForegroundColor Cyan "Labeled.FIP2ファイル出力開始"
$CardDataRAW = Get-Content -Path $tarfl -Encoding UTF8

# １行ずつ処理する
$rowno = 0

#先頭～[[LABELS type=0]]まではそのまま出力
$literal = [regex]::Escape("[[LABELS type=0]]")
for(; $rowno -lt $CardDataRAW.length; $rowno++) {
    $CardDataRAW[$rowno] |Out-File -FilePath $fipfl -Append -Encoding utf8
    if($CardDataRAW[$rowno] -match $literal){ break;}
}

Write-Host -ForegroundColor Yellow -NoNewline "card labelsセクションを更新中..."
#現在のラベル(card labels)を削除する
$literal = [regex]::Escape("[[END:LABELS]]")
for(; $rowno -lt $CardDataRAW.length; $rowno++) {
    #棄てる
    if($CardDataRAW[$rowno] -match "[[END:LABELS]]"){ break;}
}

#Labelを出力する

ForEach( $label in $defs){
    $labelStr =switch ($LabelType){
        "1" { $label.Label.Trim(); break; }
        "2" { $label.expr.Trim(); break; } 
        default {$label.expr.Trim() + " as " + $label.Label.Trim();}
    }
    ("[[LABEL]]",`
    (("id=" + $label.id.Trim()) -replace "[\r\n]", ""),`
    (("name=" + $labelStr) -replace "[\r\n]", ""),`
    (("color=" + (0x00FF00 + $label.id.Trim() % 0x100).ToString()) -replace "[\r\n]", ""),`
    "enabled=1",`
    "show=1",`
    "hide=0",`
    "fold=0",`
    "size=100",`
    "[[END:LABEL]]") -join "`r`n" | Out-File -FilePath $fipfl -Append -Encoding utf8
}
$CardDataRAW[$rowno] | Out-File -FilePath $fipfl -Append -Encoding utf8
Write-Host -ForegroundColor Yellow "済み."

#～[[END:LINKS]]まではそのまま出力
$literal = [regex]::Escape("[[END:LINKS]]")
for(; $rowno -lt $CardDataRAW.length; $rowno++) {
    $CardDataRAW[$rowno] |Out-File -FilePath $fipfl -Append -Encoding utf8
    if($CardDataRAW[$rowno] -match "[[END:LINKS]]"){ break;}
}
#カードのLabelListを更新する
Write-Host -ForegroundColor Yellow "cardセクション、labelListを更新中..."
$curTitle = ""
for(; $rowno -lt $CardDataRAW.length; $rowno++) {
    if($CardDataRAW[$rowno] -match "^title="){ $curTitle = (($CardDataRAW[$rowno] -replace "^Title=", "")-replace "[\r\n]", "")}
    if($CardDataRAW[$rowno] -match "^labelList="){
        "labelList=" + $IdsBbyTitle[$curTitle]|Out-File -FilePath $fipfl -Append -Encoding utf8
Write-Host -ForegroundColor white  $rowno "/"$CardDataRAW.length  $curTitle
     }else{
        $CardDataRAW[$rowno] |Out-File -FilePath $fipfl -Append -Encoding utf8
    }
}
Write-Host -ForegroundColor Yellow "済み."
Write-Host -ForegroundColor Cyan $fipfl "出力しました。"

