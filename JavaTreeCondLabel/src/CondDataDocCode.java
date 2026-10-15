/*
**********************************************************************
*	CondDataDocCode:	code検索条件
**********************************************************************
*/

import	java.awt.*;
import	javax.swing.*;
import	java.io.*;
import	java.net.*;
import java.nio.charset.StandardCharsets;
import	java.sql.Connection;
import	java.sql.DriverManager;
import	java.sql.ResultSet;
import java.sql.SQLException;
import	java.sql.Statement;
import	java.util.ArrayList;
import java.util.HashMap;
import	java.util.List;
import	java.util.Map;
import java.util.Properties;

/**
 * 検索条件を表すドキュメントのうち、code条件を定義します。
 * @author	HosiYama
 * @since	JDK1.2
 * @see	TreeCond
 */
public class CondDataDocCode	extends CondDataDoc implements Cloneable
{
	/** code条件の一覧 */
	protected	Map<String, List<String>> condCodes = new HashMap<>();	/* code条件の値コードをリスト */

	/* -----	static initalizer */

	/* -----	Constructers */
	/** 条件名(TreeCond.codeString[n]),接続条件(connect)からオブジェクトを生成する
	 * @param	newCond	条件名称(TreeCond.codeString[n])
	 * @param	newConnect	接続条件(connect)
	 */
	public CondDataDocCode(String newCond,boolean newConnect){
		super(newCond,newConnect);

		Properties props = new Properties();
		try (InputStream in = new FileInputStream("config.properties")) {
			props.load(new InputStreamReader(in, StandardCharsets.UTF_8));
		} catch (Exception exc) {
	    	System.err.println("Error loading L&F: " + exc);
		}
		String url = props.getProperty("db.url");

		//Codesセットアップ(初回にDISTINCTで得てコードをcondCodesにセットする)
		if(condCodes.get(cond) == null){
			condCodes.put(cond, new ArrayList<>());
			String sql = "SELECT DISTINCT TRIM(Label) FROM LabelId WHERE Label IS NOT NULL ORDER BY Label";
			Connection con = null;
			Statement st = null;
			ResultSet rs = null;
			try {
				 Class.forName("org.sqlite.JDBC");
				 con = DriverManager.getConnection(url);
				 st = con.createStatement();
				 rs = st.executeQuery(sql);
				while (rs.next()) {
					condCodes.get(cond).add(rs.getString(1));
				}
			} catch (SQLException e) {
				e.printStackTrace();
				if (e.getCause() != null) {
					JOptionPane.showMessageDialog(null,e.getCause().toString());
				}
			} catch (ClassNotFoundException e) {
				JOptionPane.showMessageDialog(null, e.getMessage());
			}
		}
	}

	/* -----	Member */

	/** getValue:	現在の値のコード('現在の値')を返す */
	public String getValue(){
		return new String("'" + String.valueOf(currentValue) + "'");
	}
	
	/** getSQLString:	SQLのwhere条件文を返す */
	public String getSQLString(){
		String	SQLString = "";

		SQLString = new String(cond + " = " + getValue());

		return(new String((getConnect()?"":"NOT ") + "(" +SQLString + ")"));
	}

	/** setCond:	条件の値を設定するダイアログを表示し値を得る
	 * @param	container	contentPane(ダイアログのためのFrameオブジェクトを得る)
	 */
	public void setCond(Container container){
		Frame	frame = null;
		String	title = this.toString();	
		String	messageSelect = "選択してください";
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
		selectedValue = currentValue;
		selectedValue = JOptionPane.showInputDialog(frame,messageSelect,title,
							JOptionPane.QUESTION_MESSAGE,
							icon,condCodes.get(cond).toArray(),currentValue);

		if(selectedValue != null){
			currentValue = selectedValue;
			if(((String)currentValue).length()==0) currentValue = noneValue;
		}

	}

	/** Clone:	コピー/貼り付けなどの際に複製を生成する */
	public Object clone(){
		CondDataDocCode	doc = new CondDataDocCode(this.cond,this.connect);
		doc.currentValue	= this.currentValue;

		return (Object)doc;
	}
}
