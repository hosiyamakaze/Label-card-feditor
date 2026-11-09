Label cards for importing folder texts into feditor.<br>
For example, after importing recipe texts from a folder into feditor, assign ingredient labels and use them to control what is shown in the view.<br>


# フォルダ内テキストをラベルする(レシピを例とする) (日本語説明)#

## feditorでフォルダ内テキストをimportした.fip2ファイルにカードラベルを登録する ##
0. feditorをインストールする
    - Frieve Editor複雑な思考や斬新なアイデアの発想を支援するアイデアプロセッサ
    [feditor](https://www.frieve.com/software/frieve-editor) (V3.00)
1. feditorで.fip2ファイルを作成する
    - feditorを起動する
    - ファイル->インポート->ファイル内テキスト...->レシピフォルダ選択
    - 初期表示の「No title」を削除する
    - ファイル->名前を付けて保存する...
2. フォルダ内のファイルをラベルする
    - LabelId.csvラベルファイルを準備する
        |Id	|Label	|Expr|
        |---------|---------|---------|
        |1	|けんますクッキング	|けんますクッキング|
        |2	|キャベツ	|春?キャベツ|
        |3	|きゅうり	|(きゅうり|キュウリ)|
        |4	|ごま油	|(ごま|胡麻)油|
        |5	|すりごま	|(すり|スリ|擂り)(ごま|ゴマ|胡麻)|
        ...<br>
        ** 食材テーブルデータシート.xlsm を利用することも出来る **
    - .fip2ファイルのフルパスを取得する
3. PoweShellで「フォルダ内のファイルをラベルする.ps1」を実行する
    ```PoweShell
    cd yourLocalDirctory
    ./フォルダ内のファイルをラベルする.ps1
    ```
    - .fip2ファイルのフルパスを応答する
    - ラベルタイプ(ラベルの表示形式)を応答する
    - 中間成果物としてlabelidx.csvインデックスファイルが作成される
    - 成果物として_Labeled.fip2ファイルが作成される
4. _Labeled.fip2ファイルをfeditorで開く
    - 「view」ボタンでViewウインドを表示し、カードの表示を制御する<br>
        ** ラベルの有効を一括で切り替える(All/Clear)にはソースの追加が必要 **

## 中間成果物のLabelIdxをSQLiteデータベースに登録し、Tree-Based WHERE Builderで検索する(オプション) ##
0. ツールをインストールする(SQliteデータベース,Java実行環境)
    - [SQLite管理ツールおよびDLL](https://www.sqlite.org/index.html)
    - [DB Browser for SQLite](https://sqlitebrowser.org/dl/)
    - [Eclipse Temurin](https://adoptium.net/)
1. 食材ラベル.db3データベースを作成する
    - 食材テーブルレイアウト.xlsm のマクロから生成したLabelIdx.ctlを参照してcommandを実行する
    - 食材ラベル.db3データベースを開き、「プロジェクトを保存」する
    - 食材テーブルデータシート.xlsm にLabelIdを準備する
    - LabelId登録用のINSERT文をマクロにより生成し、プロジェクトから実行する
    - LabelIdx定義用のDDL,loadしたLabelIdxSTRテーブルをLabelIdxテーブルに移行するSQLを実行する
2. jarファイルを展開し、実行する
    - jarファイルからicons,configを抽出する
    ```command
    cd yourLocalDirctory
    jar -xvf JavaTreeCondLabel.jar icons config.properties
    ```
    - config.propertiesファイル内のdb.urlのパスを調整する
    - jarファイルを実行する
    ```command
    java -jar JavaTreeCondLabel.jar
    ```
    - フォルダ、条件を登録し、SQLを実行する
3. javaを改修するにはさらにツールが必要である。
    - [Visual Studio Code](https://code.visualstudio.com/)
    - [Oracle GraalVM](https://www.graalvm.org/downloads/)
    - 拡張機能 Extension Pack for Java（Microsoft 発行）
    - Reference Library<br>
        [sqlite-jdbc](https://github.com/xerial/sqlite-jdbc/releases)<br>
            sqlite-jdbc-3.53.4.0-natives-windows.jar<br>
            sqlite-jdbc-3.53.4.0-without-natives.jar<br>
        [json](https://github.com/stleary/JSON-java)<br>
            json-20260814.jar<br>
 
## 選択したカードの元ファイルを外部リンクで開き、携帯に送信する(オプション) ##
0. ツールをインストールする
    - マクロ等により機能拡張可能な外部エディタ<br>
        [サクラエディタはMS Windows上で動作する日本語テキストエディタです](https://sakura-editor.github.io/)
    - 携帯等にメール送信するOsiraseサービス<br>
        [Visual Studio 2026 (Osiraseサービスを実装する場合)](https://visualstudio.microsoft.com/ja/vs/community/)
    - カードにフルパスを登録し外部リンクを実装する場合は下記を参照しfeditorをビルドする<br>
        [feditorインポート「フォルダ内テキスト」で外部リンクする](https://github.com/hosiyamakaze/grep2FIP2/blob/main/grep2FIP2.pdf)
1. お知らせ送信する
    - 「お知らせ送信する.vbs」ファイルはサクラエディタフォルダに格納する
    - Osiraseサービスを実装し、サービスを実行する(README.md参照)
    - feditorのカードから外部リンク(Ctrl+clickなど)で元ファイルをサクラエディタで開き、お知らせ送信する

