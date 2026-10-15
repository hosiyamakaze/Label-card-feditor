/**
**********************************************************************
*	CondDataFolder:	結合条件(AND/OR)を表す条件フォルダを定義します。
*	複数の条件を結合する場合は、それらを条件フォルダに含むことができます。
*	条件フォルダには、ANDフォルダとORフォルダがあります。
*	条件フォルダは、接続条件(plain/not)を反転させることができます。
*	条件フォルダを条件フォルダに含むことで結合の階層を構成することができます。
* @author	HosiYama
* @since	JDK1.2
* @see	TreeCond
**********************************************************************
*/

public class CondDataFolder	extends Object implements Cloneable
{
	/**	結合条件(1:AND/0:OR) */
	protected	boolean	connect;	/* 接続条件(true:plain/false:not) */
	protected	int		connFolder;	/* 1:AND/0:OR */
	/** 結合条件を表すSQL文(AND/OR)。フォルダのラベルテキストとしても使用する */
	protected	String	string;		/* AND/OR */

	/* -----	Constructers */
	/**
	 *	条件フォルダを生成するコンストラクタ
	 *	@param	newConnFolder	結合条件(1:ANDフォルダ/0:ORフォルダ)
	 *	@param	newConnect	接続条件(true:plain/false:not)
	 */
	public CondDataFolder(int newConnFolder,boolean newConnect){
		connFolder = newConnFolder;
		connect = newConnect;
		string	= (connect?"":"Not ")+(newConnFolder==1?" AND ":" OR ");
	}

	/* -----	Member */
	/** toString:	文字列(フォルダのラベル)を返す
	 *	@return	文字列(SQL文,フォルダのラベル)
	 */
	public String toString(){
		return string;
	}
	/** toSQLString:	文字列(SQL文)を返す
	 *	@return	文字列(SQL文)
	 */
	public String toSQLString(){
		return "\r\n" + (connFolder==1?" AND ":" OR ");
	}

	/** getConnect:	connect(結合条件(true:plain/false:not))を返す
	 *	@return	(true:plain/false:not)
	 */
	public boolean	getConnect(){
		return connect;
	}

	/** getConnFolder:	結合条件(1:AND/0:OR)を返す
	 *	@return	1:AND/0:OR
	 */
	public int	getConnFolder(){
		return connFolder;
	}

	/** changeConnect:	Plain/Not結合を反転させる */
	public void changeConnect(){
		connect = connect?false:true;
		string	= (connect?"":"Not ")+(connFolder==1?" AND ":" OR ");
	}
	/** changeConnFolder:	AND/OR結合を反転させる */
	public void changeConnFolder(){
		connFolder = connFolder==1?0:1;
		string	= (connect?"":"Not ")+(connFolder==1?" AND ":" OR ");
	}

	/** Clone:	複製を生成する(貼り付けなどで使用する)
	 *	@return	複製された条件フォルダ
	 */
	public Object clone(){
		CondDataFolder	folder = new CondDataFolder(this.connFolder, this.connect);
		folder.connect	= this.connect;
		folder.string	= new String(this.string);

		return (Object)folder;
	}
}
