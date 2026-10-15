/*
**********************************************************************
*	CondDataDocKkn:	期間検索条件
**********************************************************************
*/

import	java.awt.*;
import	javax.swing.*;

/**
 * 検索条件を表すドキュメントのうち、期間(BETWEEN/From/To)条件を定義します。
 * @author	HosiYama
 * @since	JDK1.2
 * @see	TreeCond
 */
public class CondDataDocKkn	extends CondDataDoc implements Cloneable 
{
	/** 初期の値 */
	static final	String	noneValue = "(未設定)";
	/** From(YYYYMMDD,YYYYMM,YYYY,number) */
	protected	String	currentValueStart = noneValue;	
	/** To(YYYYMMDD,YYYYMM,YYYY,number) */
	protected	String	currentValueEnd = noneValue;		
	/** beepやclipboardを使用 */
	protected Toolkit toolkit = Toolkit.getDefaultToolkit();	

	/* -----	Constructers */
	/** 条件名(TreeCond.kknString[n]),接続条件(connect)からオブジェクトを生成する
	 * @param	newCond	条件名(TreeCond.kknString[n])
	 * @param	newConnect	接続条件(connect)
	 */
	public CondDataDocKkn(String newCond,boolean newConnect){
		super(newCond,newConnect);
	}

	/** getValue:	現在の値(From～To)を返す */
	public String getValue(){
		return new String(currentValueStart + "～" + currentValueEnd); 
	}

	/** getSQLString:	SQLのwhere条件文を返す */
	public String getSQLString(){
		String	SQLString;
		
		/* SQL文を編集する */
		SQLString = mkSQLString();
		
		return(new String((getConnect()?"":"NOT ") + SQLString));
	}

	/** mkSQLStringKkn:	期間条件をSQLに編集する
	 */
	private	String mkSQLString(){
		String	kknSQL = "";
		String formedStart = "";
		String formedEnd = "";

		/* YYYYMMの期間開始、終了を編集する */
		kknSQL = "";

		if(cond.equals("日付")){
			if(!currentValueStart.equals(noneValue)){
				if(currentValueStart.length() == 8){
					formedStart = currentValueStart.substring(0, 4) + "-"
									+ currentValueStart.substring(4, 6) + "-"
									+ currentValueStart.substring(6, 8);
				}else{
					currentValueStart = noneValue;
					toolkit.beep();
				}
			}
			if(!currentValueEnd.equals(noneValue)){
				if(currentValueEnd.length() == 8){
					formedEnd = currentValueEnd.substring(0, 4) + "-"
									+ currentValueEnd.substring(4, 6) + "-"
									+ currentValueEnd.substring(6, 8);
				}else{
					currentValueEnd = noneValue;
					toolkit.beep();
				}
			}
		}

		if(!currentValueStart.equals(noneValue) && currentValueEnd.equals(noneValue)){
			kknSQL = new String("idx." + cond + " >= '" + (cond.equals("日付")?formedStart:currentValueStart) + "'");
		}else if(currentValueStart.equals(noneValue) && !currentValueEnd.equals(noneValue)){
			kknSQL = new String("idx." + cond + " <= '" + (cond.equals("日付")?formedEnd:currentValueEnd) + "'");
		}else if(!currentValueStart.equals(noneValue) && !currentValueEnd.equals(noneValue)){
			kknSQL = new String("idx." + cond + " BETWEEN '" + (cond.equals("日付")?formedStart:currentValueStart) 
								+ "' AND '" + (cond.equals("日付")?formedEnd:currentValueEnd) + "'");
		}else{
			kknSQL = new String("idx." + cond + " BETWEEN '" + currentValueStart + "' AND '" + currentValueEnd + "'");
		}

		if(!cond.equals("日付")){
				kknSQL = kknSQL.replaceAll("'", "");	
		}

		return "(" + kknSQL + ")";
	}

	/** setCond:	条件の値を設定するダイアログを表示し値を得る
	 * @param	container	contentPane(ダイアログのためのFrameオブジェクトを得る)
	 */
	public void setCond(Container container){
		Frame	frame = null;
		String	title = this.toString();	
		String	messageStart = "";
		String	messageEnd = "";
		Icon	icon = null;
		Object	selectedValue;

		/* Frameを得る */
		for(Component c=container.getParent(); c != null; c=c.getParent()){
			if(c instanceof Frame){
				frame = (Frame)c;
				break;
			}
		}

		switch(cond){
			case "日付":
				messageStart = "FromをYYYYMMDD形式で入力してください";
				messageEnd = "ToをYYYYMMDD形式で入力してください";
				break;
			case "数量":
			case "金額":
				messageStart = "Fromを数値で入力してください";
				messageEnd = "Toを数値で入力してください";
				break;
			case "YY":
				messageStart = "FromをYYYY形式で入力してください";
				messageEnd = "ToをYYYY形式で入力してください";
				break;
			case "YM":
				messageStart = "FromをYYYYMM形式で入力してください";
				messageEnd = "ToをYYYYMM形式で入力してください";
				break;
		}
		/* ダイアログを表示する */
		/* 開始年月日 */
		selectedValue = JOptionPane.showInputDialog(frame,messageStart,title,
										JOptionPane.QUESTION_MESSAGE,
										icon,null,
										currentValueStart==noneValue?"":currentValueStart);
		if(selectedValue != null){
			currentValueStart = ((String)selectedValue).toString();
		}
		if(currentValueStart==null || currentValueStart.length() == 0) currentValueStart = noneValue;
		/* 終了年月日 */
		selectedValue = JOptionPane.showInputDialog(frame,messageEnd,title,
										JOptionPane.QUESTION_MESSAGE,
										icon,null,
										currentValueEnd==noneValue?"":currentValueEnd);
		if(selectedValue != null){
			currentValueEnd = ((String)selectedValue).toString();
		}
		if(currentValueEnd==null || currentValueEnd.length() == 0) currentValueEnd = noneValue;
//System.out.println(getValue()+"/"+getValueCode()+"/"+getSQLString()); 
	}

	/** Clone:	貼り付けなどの際に複製を生成する */
	public Object clone(){
		CondDataDocKkn	docKkn = new CondDataDocKkn(this.cond,this.connect);
		docKkn.currentValueStart = this.currentValueStart;	/* From */
		docKkn.currentValueEnd = this.currentValueEnd;		/* To */

		return (Object)docKkn;
	}
}
