/*
**********************************************************************
*	CondDataDoc:	like検索条件
**********************************************************************
*/

import	java.awt.*;
import	javax.swing.*;
import	java.io.*;
import	java.net.*;
import	java.util.*;

/**
 * 検索条件を表すドキュメントのうち、code条件、like条件を定義します。
 * @author	HosiYama
 * @since	JDK1.2
 * @see	TreeCond
 */
public class CondDataDoc	extends Object implements Cloneable
{
	/** 当初の値 */
	static final	String	noneValue = "(未設定)";

	/** 条件名(=カラム名)(TreeCond.likeString[n]) */
	protected	String		cond;	
	/** 接続条件(true:plain(含む)/false:not(含まない)) */
	protected	boolean	connect;	
	/** 現在の値 */
	protected	Object	currentValue = noneValue;

	/* -----	static initalizer */
	static{
		try{
		}catch(Exception e){
			System.out.println(e);
		}
	}

	/* -----	Constructers */
	/** 条件名(TreeCond.likeString[n]),接続条件(connect)からオブジェクトを生成する
	 * @param	newCond	like条件名称(TreeCond.likeString[n])
	 * @param	newConnect	接続条件(connect)
	 */
	public CondDataDoc(String newCond,boolean newConnect){
		cond	= newCond;
		connect = newConnect;
	}

	/* -----	Member */
	/** getCond:	条件名(Cond)を返す */
	public String	getCond(){
		return cond;
	}

	/** getConnect:	接続条件(connect)を返す */
	public boolean	getConnect(){
		return connect;
	}

	/** getValue:	現在の値(currentValue)を返す */
	public String getValue(){
		return "%" + String.valueOf(currentValue) + "%";
	}
	
	/** toString:	liek条件名、現在の値、接続条件を含むstringを返す */
	public String toString(){
		return new String(cond + "が" + getValue() + (connect?"を含む":"を含まない"));
	}

	/** getSQLString:	SQLのwhere条件文を返す */
	public String getSQLString(){
		String	SQLString = "";

		if(cond.equals("Label")){
			SQLString = new String(" exists (select idx.id from LabelIdx as idx2 where idx.Title = idx2.title and idx2.Label like '" + getValue() + "')");
		}else{
			SQLString = new String(" idx."+cond+" like '" + getValue() + "'");
		}

		return(new String((getConnect()?"":"NOT ") + "(" +SQLString + ")"));
	}
	/** changeConnect:	接続条件(plain/not)を反転させる */
	public void changeConnect(){
		connect = connect?false:true;
	}

	/** setCond:	条件の値を設定するダイアログを表示し値を得る
	 * @param	container	contentPane(ダイアログのためのFrameオブジェクトを得る)
	 */
	public void setCond(Container container){
		Frame	frame = null;
		String	title = this.toString();	
		String	messageInput = "入力してください";
		Icon	icon = null;
		Object	selectedValue;

		/* Frameを得る */
		for(Component c=container.getParent(); c != null; c=c.getParent()){
			if(c instanceof Frame){
				frame = (Frame)c;
				break;
			}
		}

		/* ダイアログを表示する */
		selectedValue = null;
		selectedValue = JOptionPane.showInputDialog(frame,messageInput,title,
												JOptionPane.QUESTION_MESSAGE,
												icon,null,
												currentValue==noneValue?"":currentValue);

		if(selectedValue != null){
			currentValue = selectedValue;
			if(((String)currentValue).length()==0) currentValue = noneValue;
		}

	}

	/** Clone:	コピー/貼り付けなどの際に複製を生成する */
	public Object clone(){
		CondDataDoc	doc = new CondDataDoc(this.cond,this.connect);
		doc.currentValue	= this.currentValue;

		return (Object)doc;
	}
}
