
/*
 * <Applet code=TreeCond width=700 height=300>
 * </Applet>
 */

/*
**********************************************************************
*	TreeCond:	高度な条件設定
*
*	jarファイルの実行)
*	>jar -xvf JavaTreeCondLabel.jar icons config.properties
*	>java -jar JavaTreeCondLabel.jar
**********************************************************************
*/

import javax.swing.*; // Import the Swing classes
import javax.swing.tree.*; // Import the Swing tree classes
import javax.swing.event.*; // Import the Swing event classes
import java.awt.*; // Import awt classes
import java.awt.event.*; // for the WindowAdapter and WindowEvent classes
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.awt.datatransfer.*;	/* for Clipboard */
import java.util.*;
import java.util.List;

/**
 * TreeCond: 高度検索条件を編集するためのクラス
 * フォルダとドキュメントを階層構造をもつツリー状に配置することで、AND/OR結合の階層構造を表現する。
 * フォルダには、AND/ORの2種類があり、ドキュメントはCODE,Exists,LIKE,BETWEEN(期間,範囲)項目の種類がある。
 * 左側の表示領域でツリーを編集し、右側の表示領域に対応するSQL文を表示する。
 * Exists条件は、LabelIdxテーブルのLabel列に対して、副参照で確認するための条件を生成する。
 * Likeは、Labelについては、LabelIdxテーブルのLabel列に対して、副参照で確認するための条件を生成する。
 * Codeは、in(...)にまとめる条件を生成する。
 * @author	Hosiyama
 * @since	JDK1.2
 * @see	CondCellRenderer
 * @see	CondDataFolder
 * @see	CondDataDoc
 * @see	CondDataDocKkn
 */
public class TreeCond extends JApplet {
	/** Exists条件を表すテキスト */
	static final String[]	existsString = new String[]{
			"Label"};
	/** コード値条件を表すテキスト */
	static final String[]	codeString = new String[]{
			""};
	/** 名称条件を表すテキスト */
	static final String[]	likeString = new String[]{
			"Label","Title"};
	/** BETWEEN条件の種類を表すテキスト */
	static final String[]	kknString = new String[]{
			"ID"};
	/** アプレットかアプリかの判定 */
	static boolean	isApplet = true;		
	/** contentPane(いわばアプレットの表示領域) */
	protected Container container;			
	/** ツリーを表示するスクロール領域 */
	protected JScrollPane treescrollpane;	
	/** SQLを表示する領域 */
	protected JEditorPane editorPane;		
	/** SQLを表示する領域を表示するスクロール領域 */
	protected JScrollPane editorscrollpane;	
	/** ふたつのスクロール領域を表示する領域 */
	protected JSplitPane  splitpane;		
	/** ツリーコントロール */
	protected JTree	tree;					
	/** treeNodeを使うツリーデータモデル */
	protected DefaultTreeModel	treeModel;	
	/** beepやclipboardを使用 */
	protected Toolkit toolkit = Toolkit.getDefaultToolkit();	
	/** ツリー上のポップアップメニュー */
	protected JPopupMenu	popupMenu;		
	/** 最後に選択され、削除、コピー、切取りが実行されたノード */
	protected DefaultMutableTreeNode	latestNode=null;	
	/** データベース接続URL */
	static String url;

	/**	init:	init(アプレットはここからスタートする)
	 *	ツリー表示用と、SQL文表示用のパネルを作成し、左右に分割して表示する
	 *	ツリー表示領域には、当初ANDフォルダを1つ表示する
	 */
    public void init() {
		// L&F
		try {
	    	UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
	    	// If you want the System L&F instead, comment out the above line and
	    	// uncomment the following:
	    	// UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception exc) {
	    	System.err.println("Error loading L&F: " + exc);
		}

		container = getContentPane();

		/* ツリー表示用のpanelを作成する */
		JMenuBar         menuBar = constructMenuBar();	// メニューバーを生成
		JPanel           panel = new JPanel(true);		// ツリー表示用パネル
		popupMenu = constructPopupMenu();				// ポップアップメニューを作成

		/* rootノード(ANDフォルダ)を作成しTreeModel→Tree作成する */
		DefaultMutableTreeNode root = createNewNode(1/* AND */,true/* plain */);
		treeModel = new DefaultTreeModel(root);
		tree = new JTree(treeModel);

		/* TreeCellRendererの設定などTreeに対する設定 */
		tree.setCellRenderer(new CondCellRenderer());
		tree.addMouseListener(new TreeCondListener()); 
		tree.setRowHeight(-1);	// row毎に高さを問い合せる
		tree.putClientProperty("JTree.lineStyle","Angled");
		// ToolTipManager.sharedInstance().registerComponent(tree);	// ツールチップを表示

		/* Tree表示用のスクロールペインを作る */
        treescrollpane = new JScrollPane();
		treescrollpane.setPreferredSize(new Dimension(600, 600));
		treescrollpane.getViewport().add(tree);

		/* パネルにレイアウトする */
		panel.setLayout(new BorderLayout());
		panel.add("North", menuBar);
		panel.add("Center", treescrollpane);

		/* SQL文表示用のpanelを作成する */
		JMenuBar         menuBarSQL = constructMenuBarSQL();	// SQLメニューバーを生成
		JPanel           panelSQL = new JPanel(true);		// SQL表示用パネル
		editorPane = new JEditorPane();
		editorscrollpane = new JScrollPane(editorPane);
		editorscrollpane.setPreferredSize(new Dimension(450, 600));

		/* パネルにレイアウトする */
		panelSQL.setLayout(new BorderLayout());
		panelSQL.add("North", menuBarSQL);
		panelSQL.add("Center", editorscrollpane);

		/* ツリーとSQL文のpanelを表示する */
		splitpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,true,
								   panel,panelSQL);
		//splitpane.setDividerLocation(0.60);
		splitpane.setDividerSize(3);
		container.add(splitpane);
    }

	/**	main:	フレームを作成してアプレットのinitモジュールを実行する(アプリはここからスタートする)
	 * @param	args	現在は使用しない
	 */
    public static void main(String[] args) {
        // Create a Swing frame.
        JFrame f = new JFrame("TreeCond");

        // Create an instance of the THello applet.
        TreeCond TreeCondApplet = new TreeCond();

        // Initialize the applet instance.
		isApplet = false;
        TreeCondApplet.init();
        
        // Add the applet to the JFrame (to it's content pane).
        f.getContentPane().add(TreeCondApplet);

        // Add the listener to close the frame.
        f.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e) {
	        	System.exit(0);
	    	}
		});

        // Assign the frame size and display it.
        f.pack();
        f.setVisible(true); // Display the frame

		Properties props = new Properties();
		try (InputStream in = new FileInputStream("config.properties")) {
		// try (InputStream in = TreeCond.class.getResourceAsStream("/config.properties")) {
		// 	if (in == null) {
		// 		throw new IllegalStateException("config.properties が JAR にありません");
 		//    }	
		   props.load(new InputStreamReader(in, StandardCharsets.UTF_8));
		} catch (Exception exc) {
	    	System.err.println("Error loading L&F: " + exc);
		}
		url = props.getProperty("db.url");
    }
	
	/**	constructMenuBar:	ツリー用のメニューバーを作成する
	 * ファイル(アプリケーションの場合のみ)、各条件メニュー
	 */
	private JMenuBar constructMenuBar(){
		JMenu		menu;
		JMenuBar	menuBar	= new JMenuBar();
		JMenuItem	menuItem;

		/* ファイルメニューを作成する */
		if(!isApplet){
			menu = new JMenu("ファイル");
			menuBar.add(menu);
	
			menuItem = menu.add(new JMenuItem("終了"));
			menuItem.addActionListener(new ActionListener() {
		    	public void actionPerformed(ActionEvent e) {
					System.exit(0);
		    	}
			});
		}
	
		/* 条件メニューを作成する */
		menu = new JMenu("条件フォルダ");
		menuBar.add(menu);

		menuItem = menu.add(new JMenuItem("ANDフォルダ"));
		menuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				addFolder(1/* AND */,true/* plain */);
			}
		});

		menuItem = menu.add(new JMenuItem("ORフォルダ"));
		menuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				addFolder(0/* OR */,true/* plain */);
			}
		});

		menuItem = menu.add(new JMenuItem("Not ANDフォルダ"));
		menuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				addFolder(1/* AND */,false/* not */);
			}
		});

		menuItem = menu.add(new JMenuItem("Not ORフォルダ"));
		menuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				addFolder(0/* OR */,false/* not */);
			}
		});


		/* Exists条件メニューを作成する */
		menu = new JMenu("Exists条件");
		menuBar.add(menu);
	
		for( int i=0;i<existsString.length;i++){
			menuItem = menu.add(new JMenuItem(existsString[i]));
			menuItem.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					//System.out.println("selected menu " + e.getActionCommand());
					addDoc(e,4/* Exists */);
				}
			});
		}

		// /* CODE条件メニューを作成する */
		// menu = new JMenu("CODE条件");
		// menuBar.add(menu);
	
		// for( int i=0;i<codeString.length;i++){
		// 	menuItem = menu.add(new JMenuItem(codeString[i]));
		// 	menuItem.addActionListener(new ActionListener(){
		// 		public void actionPerformed(ActionEvent e){
		// 			//System.out.println("selected menu " + e.getActionCommand());
		// 			addDoc(e,1/* CODE */);
		// 		}
		// 	});
		// }

		/* LIKE条件メニューを作成する */
		menu = new JMenu("LIKE条件");
		menuBar.add(menu);
	
		for( int i=0;i<likeString.length;i++){
			menuItem = menu.add(new JMenuItem(likeString[i]));
			menuItem.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					//System.out.println("selected menu " + e.getActionCommand());
					addDoc(e,2/* LIKE */);
				}
			});
		}


		// /* BETWEEN条件メニューを作成する */
		// menu = new JMenu("BETWEEN条件");
		// menuBar.add(menu);
	
		// for( int i=0;i<kknString.length;i++){
		// 	menuItem = menu.add(new JMenuItem(kknString[i]));
		// 	menuItem.addActionListener(new ActionListener(){
		// 		public void actionPerformed(ActionEvent e){
		// 			//System.out.println("selected menu " + e.getActionCommand());
		// 			addDoc(e,3/* kkn */);
		// 		}
		// 	});
		// }
		return menuBar;
	}

	/**	createNewNode:	フォルダノードを作成する
	 * @param	connect	条件フォルダの条件
	 * @see CondDataFolder
	 */
	protected DefaultMutableTreeNode createNewNode(int connFolder, boolean connect) {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(new CondDataFolder(connFolder, connect));
		node.setAllowsChildren(true);

		return node;
    }
	
	/**	getSelectedNode:	現在の選択位置を返す	*/
	protected DefaultMutableTreeNode getSelectedNode() {
		TreePath   selPath = tree.getSelectionPath();
	
		if(selPath != null)
		    return (DefaultMutableTreeNode)selPath.getLastPathComponent();
		return null;
	}
	
	/**	addFolder:	条件フォルダを追加する(条件→AND/ORフォルダが選択された場合など)
	 * @param	connFolder	条件フォルダの条件(1:AND/0:OR)
	 * @param	connect		条件フォルダの接続条件(Ture:plain/False:Not)
	 * @see CondDataFolder
	 */
	protected void addFolder(int connFolder,boolean connect) {
		addNode(new DefaultMutableTreeNode(new CondDataFolder(connFolder, connect)));
	}
	
	/**	addNode:	node(条件フォルダまたは条件ドキュメント)を追加する
	 * @param	child	新しいノード
	 */
	protected void addNode(DefaultMutableTreeNode child) {
		int               newIndex;
		DefaultMutableTreeNode	lastItem = getSelectedNode();
		DefaultMutableTreeNode	parent;
		Object					obj;
	
		/*
		 *	フォルダが選択されている場合は、そのフォルダの最後
		 *	ドキュメントが選択されている場合は、そのドキュメントの次
		 */
	    /* 新しいノードの挿入フォルダを決定する */
	    if(lastItem != null) {
			obj = lastItem.getUserObject();
			if(obj instanceof CondDataFolder){
				/* 選択位置がフォルダならこれをフォルダとする */
				parent = lastItem;
			}else{
				/* 選択位置はドキュメントなので親フォルダを得る */
				parent = (DefaultMutableTreeNode)lastItem.getParent();
			}
			if(parent == null) {
				/* (あり得ない)親が見つからない場合はrootに追加する */
		    	parent = (DefaultMutableTreeNode)treeModel.getRoot();
			}
	    }else{
			/* 位置が選択されていない場合は、beepする */
			toolkit.beep();
//exit_procedure:
			return;
		}

		/* 挿入フォルダ内の位置を決定する */
		if(obj instanceof CondDataFolder){
			newIndex = treeModel.getChildCount(parent);	// フォルダの最後
	    }else{
			newIndex = parent.getIndex(lastItem) + 1;	// 現在の次
		}
	
	    /* フォルダを追加する */
	    treeModel.insertNodeInto(child, parent, newIndex);
		tree.scrollPathToVisible(new TreePath(child.getPath()));	// 結果を見せる
	}

	/** addNodeNest:	条件フォルダの子を再帰的に複製する
	 * @param	tarNode	複製先のノード
	 * @param	srcNode	複製元のノード
	 */
	protected void addNodeNest(DefaultMutableTreeNode tarNode,DefaultMutableTreeNode srcNode){
		DefaultMutableTreeNode newNode;	/* tarNodeの新しい子Node */

		for(int i=0;i<srcNode.getChildCount();i++){
			DefaultMutableTreeNode currNode = (DefaultMutableTreeNode)srcNode.getChildAt(i);
			Object	obj = currNode.getUserObject();
			if(obj instanceof CondDataDocKkn){
				newNode = new DefaultMutableTreeNode(((CondDataDocKkn)obj).clone());
				tarNode.add(newNode);
				//tree.scrollPathToVisible(new TreePath(newNode.getPath()));	// 結果を見せる
			}else if(obj instanceof CondDataDocCode){
				newNode = new DefaultMutableTreeNode(((CondDataDocCode)obj).clone());
				tarNode.add(newNode);
				//tree.scrollPathToVisible(new TreePath(newNode.getPath()));	// 結果を見せる
			}else if(obj instanceof CondDataDoc){
				newNode = new DefaultMutableTreeNode(((CondDataDoc)obj).clone());
				tarNode.add(newNode);
				//tree.scrollPathToVisible(new TreePath(newNode.getPath()));	// 結果を見せる
			}else if(obj instanceof CondDataFolder){
				newNode = new DefaultMutableTreeNode(((CondDataFolder)obj).clone());
				tarNode.add(newNode);
				if(currNode.getChildCount()>0){
					addNodeNest(newNode,currNode);
				}
			}
		}
	}
	
	/**	changeNode:	接続条件を切替える(AND-OR/)
	 * フォルダならAND-OR/not切替え、ドキュメントならpalin(含む)/NOT(含まない)を反転させます。
	 * @param	mode	接続条件識別する(1:フォルダ条件(AND-OR)/2:接続条件(not切替え)の切替え)
	 */
	protected void changeNode(int mode) {
	    DefaultMutableTreeNode	lastItem = getSelectedNode();
		Object					obj;

	    if(lastItem != null) {
			obj = lastItem.getUserObject();
			if(mode==1 && obj instanceof CondDataFolder){
	    		/* 現在のノードがフォルダの場合AND-ORを切替える */
				((CondDataFolder)obj).changeConnFolder();
//exit_procedure:
				return;
			}else if(mode==2 && obj instanceof CondDataFolder){
	    		/* 現在のノードがフォルダの場合notを切替える */
				((CondDataFolder)obj).changeConnect();
//exit_procedure:
				return;
			}else if(mode==2 && obj instanceof CondDataDoc){
	    		/* 現在のノードがドキュメントの場合notを切替える */
				((CondDataDoc)obj).changeConnect();
//exit_procedure:
				return;
			}
		}
		toolkit.beep();
	}
	
	/**	delFolder:	現在選択されている条件フォルダを削除 */
	protected void delFolder() {
	    DefaultMutableTreeNode	lastItem = getSelectedNode();

	    /* 現在のノードがルート以外のフォルダを削除する */
	    if(lastItem != null && !lastItem.isRoot() &&
		   lastItem.getUserObject() instanceof CondDataFolder) {
			/* latestNodeを現在の選択ノードにする */
			latestNode = lastItem;
			treeModel.removeNodeFromParent(lastItem);
//exit_procedure:
			return;
		}
		toolkit.beep();
	}
	
		
	/**	addDocCode:	code,LIKE,BETWEEN条件ドキュメントを追加する
	 * @param	e	イベント(コマンド名称を得る)
	 * @param	condMode	条件ドキュメントのクラスを識別する(1:Code条件等/2:LIKE条件/3:BETWEEN条件)
	 */
	protected void addDoc(ActionEvent e,int condMode) {
		addNode(new DefaultMutableTreeNode(
				condMode==1?new CondDataDocCode(e.getActionCommand(),true):
				(condMode==2?new CondDataDoc(e.getActionCommand(),true):
				(condMode==3?new CondDataDocKkn(e.getActionCommand(),true):
				(condMode==4?new CondDataDocExists(e.getActionCommand(),true):null))))); 
	}
	
	/**	delDoc:	現在選択されている条件ドキュメントを削除 */
	protected void delDoc() {
	    DefaultMutableTreeNode	lastItem = getSelectedNode();

	    /* 現在のノードのドキュメントを削除する */
	    if(lastItem != null && !lastItem.isRoot()) {		
			if(lastItem.getUserObject() instanceof CondDataDocCode ||
			   lastItem.getUserObject() instanceof CondDataDoc ||
			   lastItem.getUserObject() instanceof CondDataDocKkn ||
			   lastItem.getUserObject() instanceof CondDataFolder) {
				/* latestNodeを現在の選択ノードにする */
				latestNode = lastItem;
				treeModel.removeNodeFromParent(lastItem);
//exit_procedure:
				return;
			}
		}
		toolkit.beep();
	}

	/***********************************************************************
	*	TreeCondListener:	ツリー上のマウスの操作を実現する
	**********************************************************************/
	/**
	 * マウスによる操作を定義します。
	 * ダブルクリック:ドキュメントの条件内容を編集する
	 * 右クリック:ポップアップメニューを表示する
	 */
	class TreeCondListener	extends MouseAdapter
	{
		/* マウスクリックに対する反応(条件編集) */
		public void mouseClicked(MouseEvent e){
			DefaultMutableTreeNode	lastItem = getSelectedNode();
	
			/* ダブルクリックで現在のノードのドキュメントに対応する条件を編集する */
	    	if(e.getClickCount() == 2/* ダブルクリック */ && lastItem != null){
				Object	obj = lastItem.getUserObject();
				if(obj instanceof CondDataDoc){
					((CondDataDoc)obj).setCond(container);
					// 描画領域を更新するために必要
					lastItem.setUserObject(obj);
					treeModel.nodeChanged(lastItem);
				}
			}
		}

		/* マウスプレスに対する反応(ポップアップメニュー) */
		public void mousePressed(MouseEvent me) {
            showPopup(me);
        }

        public void mouseReleased(MouseEvent me) {
            showPopup(me);
        }

        private void showPopup(MouseEvent me) {
            if (me.isPopupTrigger()) {
				int row = tree.getRowForLocation(me.getX(), me.getY());
				if (row >= 0) {
					tree.setSelectionRow(row);//右ボタンでクリックした位置のノードを選択する
				}
                popupMenu.show(me.getComponent(),
                               me.getX(), me.getY());
            }
        }
	}

	/**	getSQLString:	SQLのwhere条件を返す。
	 * フォルダが選択されている場合はそのフォルダ以下について、
	 * ドキュメントが選択されている場合はそのドキュメントについてのみ、SQL文を表示します。
	 */
	protected void getSQLString(){
		DefaultMutableTreeNode	lastItem = getSelectedNode();
		String	SQLString = "条件が選択されていません";

		if(lastItem != null){
			Object	obj = lastItem.getUserObject();
			if(obj instanceof CondDataFolder && lastItem.getChildCount()>0){
				/* フォルダが空でないとき内包のSQLを作成する */
				SQLString = getSQLStringFolder(lastItem);
			}else{
				SQLString = ((CondDataDoc)obj).getSQLString();
				lastItem.setUserObject(obj);
				treeModel.nodeChanged(lastItem);	// 条件編集後の表示を更新する(未設定に変わる場合あり)
			}
		}

		if(SQLString.length() == 0){
			SQLString = "条件が選択されていません";
		}
		/* SQLStringをテキスト表示する */
		//editorPane.setContentType("text/html");
		editorPane.setText(SQLString);
	}

	/** getSQLStringFolder:	フォルダ内の条件のSQLを再帰的に作成して返す
	 * in ('',...)にまとめられるものはまとめます。
	 * まとめられないものは、ドキュメントのSQLを直接連結します。
	 * ツリーの階層順序とSQLの順序は必ずしも対応しません。
	 */
	private String getSQLStringFolder(DefaultMutableTreeNode folderNode){
		CondDataFolder folder = (CondDataFolder)folderNode.getUserObject();
		TreePath pathFolder = new TreePath(folderNode.getPath());
		String		SQLString = null;	/* 現在フォルダのSQL */
		int			childCount;
		int			idx;				/* 条件を特定する(index for attrStting) */
		Map<String, List<String>> condcodePlain = new HashMap<>();	/* code条件のplainの値/コードを連結 */
		Map<String, List<String>> condcodeNot = new HashMap<>(); 	/* code条件のnotの値/コードを連結 */
		String		condKkn = null;		/* 期間条件のSQLを連結 */
		String		condFolder = null;	/* 子フォルダのSQLを連結 */
		DefaultMutableTreeNode	child;	/* 子node */
		Object		obj;
		CondDataDoc	doc;				/* 条件ドキュメント */
		CondDataDocCode	docCode;		/* CODE条件ドキュメント */
		String		cond;				/* 条件の名称 */
		String	strtmp;

		/* フォルダ内の条件のSQLを作成する(閉じているフォルダは除外する) */
		childCount = tree.isExpanded(pathFolder) ? folderNode.getChildCount() : 0;
		for(int i=0; i < childCount; i ++){
			child = (DefaultMutableTreeNode)folderNode.getChildAt(i);
			obj = child.getUserObject();	/* フォルダ、または、条件 */
			if(obj instanceof CondDataDocKkn){
				/* 期間条件(DocKkn)の条件を直接連結する */
				strtmp = ((CondDataDocKkn)obj).getSQLString();
				child.setUserObject(obj);
				treeModel.nodeChanged(child);	// 条件編集後の表示を更新する(未設定に変わる場合あり)
				if(!strtmp.equals("")){
					condKkn = new String((condKkn!=null?condKkn+folder.toSQLString():"") + strtmp);
				}
// System.out.println(folder.toString()+((CondDataDocKkn)obj).getSQLString());
// return(folder.toString()+((CondDataDocKkn)obj).getSQLString());
			}else if(obj instanceof CondDataDocCode){
				/* 同一属性条件(DocCode)の条件をin用に連結する(idxに条件を特定する) */
				docCode = (CondDataDocCode)obj;
				cond = docCode.getCond();	/* 条件名を取出す */

				if(!condcodePlain.containsKey(cond)){
					condcodePlain.put(cond, new ArrayList<>());
				}
				if(!condcodeNot.containsKey(cond)){
					condcodeNot.put(cond, new ArrayList<>());
				}
				/* コード系は","で連結する(in ('','',...)) */
				if(docCode.getConnect()){
					boolean isListed = false;
					for (String row : condcodePlain.get(cond)) {
						if (row.contains(docCode.getValue())) {
							isListed = true;
							break;
						}
					}
					if(!isListed){
						condcodePlain.get(cond).add(docCode.getValue());
					}
				}else{
					boolean isListed = false;
					for (String row : condcodeNot.get(cond)) {
						if (row.contains(docCode.getValue())) {
							isListed = true;
							break;
						}
					}
					if(!isListed){
						condcodeNot.get(cond).add(docCode.getValue());
					}
				}
			}else if(obj instanceof CondDataDoc){
				doc = (CondDataDoc)obj;
				cond = doc.getCond();	/* 条件名を取出す */
				/* まとめないものは直接連結する */
				SQLString = new String((SQLString!=null?SQLString+folder.toSQLString():"") + doc.getSQLString());
			}else if(obj instanceof CondDataFolder &&  child.getChildCount()>0){
				/* 子folderの条件を連結する(閉じているフォルダは除く) */
				pathFolder = new TreePath(child.getPath());
				if(tree.isExpanded(pathFolder)){
					condFolder = new String((condFolder!=null?condFolder+folder.toSQLString():"")+
											getSQLStringFolder(child));
				}
//System.out.println(folder.toString()+condFolder);
			}
		}

		/* in (''...)用に収集した条件のSQL文を連結する */
		for(idx = 0; idx < codeString.length; idx ++){
			/* column名を得て、SQLを編集する */
			if(condcodePlain.containsKey(codeString[idx])){	
				if(condcodePlain.get(codeString[idx]).size()>0){
					SQLString = new String((SQLString!=null?SQLString+folder.toSQLString():"") + 
											codeString[idx] + " IN (" + String.join(",", condcodePlain.get(codeString[idx])) + ")");
				}
			}
			if(condcodeNot.containsKey(codeString[idx])){
				if(condcodeNot.get(codeString[idx]).size()>0){	
					SQLString = new String((SQLString!=null?SQLString+folder.toSQLString():"") + 
											codeString[idx] + " NOT IN (" + String.join(",", condcodeNot.get(codeString[idx])) + ")");
				}
			}
		}	

		if(condFolder != null){
			SQLString = new String((SQLString!=null?SQLString+folder.toSQLString():"") + condFolder);
		}

		if(condKkn != null){
			SQLString = new String((SQLString!=null?SQLString+folder.toSQLString():"") + condKkn);
		}

		return	new String(SQLString!=null? (folder.getConnect()?"":" Not ") +"(" + SQLString + ")" : "");
	}

	/**	constructMenuBarSQL:	SQL用のメニューバーを作成する	*/
	private JMenuBar constructMenuBarSQL(){
		JMenu		menu;
		JMenuBar	menuBar	= new JMenuBar();
		JMenuItem	menuItem;

			
		/* 表示メニューを作成する */
		menu = new JMenu("SQL");
		menuBar.add(menu);
	
		menuItem = menu.add(new JMenuItem("表示"));
		menuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				getSQLString();
			}
		});

		menuItem = menu.add(new JMenuItem("実行"));
		menuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				runSQLString();
			}
		});

		/* 編集メニューを作成する */
		menu = new JMenu("編集");
		menuBar.add(menu);
	
		menuItem = menu.add(new JMenuItem("コピー"));
		menuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				String	s = editorPane.getText();
				if(s.length()==0){
					toolkit.beep();
				}
				StringSelection ss = new StringSelection(s);
				toolkit.getSystemClipboard().setContents(ss,ss);
			}
		});

		return menuBar;
	}

	/** constructPopupMenu:	ツリー上のポップアップメニューを作成する */
	private JPopupMenu constructPopupMenu(){
		JPopupMenu pm = new JPopupMenu();
		JMenuItem	menuItem;

        menuItem = new JMenuItem("切り取り");
        pm.add(menuItem);
		menuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
	    		DefaultMutableTreeNode	lastItem = getSelectedNode();
				if(lastItem != null && !lastItem.isRoot()/* rootの切り取りは禁止する */){
					if(lastItem.getParent() != null){
						/* latestNodeを現在の選択ノードにする */
						latestNode = lastItem;
						/* 現在の選択ノードを削除する */
						treeModel.removeNodeFromParent(lastItem);
//exit_procedure:
						return;
					}
				}
				toolkit.beep();
			}
		});

        menuItem = new JMenuItem("コピー");
        pm.add(menuItem);
		menuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				/* latestNodeを現在の選択ノードにする */
	    		DefaultMutableTreeNode	lastItem = getSelectedNode();
				if(lastItem != null){
					latestNode = lastItem;
					/**
					 * 選択されたノードがフォルダの場合はツリーを複製する
					 * 選択ノードの下位構成に貼り付けされた場合に、無限ループになるのを防ぐため
					 */
					Object	obj = latestNode.getUserObject();
					if(obj instanceof CondDataFolder){
						latestNode = new DefaultMutableTreeNode(((CondDataFolder)obj).clone());
						if(lastItem.getChildCount()>0){
							addNodeNest(latestNode,lastItem);
						}
					}
				}else{
					toolkit.beep();
				}
			}
		});

        menuItem = new JMenuItem("貼り付け");
        pm.add(menuItem);
		menuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				/* latestNodeのuserObjectを複製して現在の選択ノードに追加する */
				if(latestNode != null){
					Object	obj = latestNode.getUserObject();
					if(obj instanceof CondDataFolder){
						DefaultMutableTreeNode	newNode;
						newNode = new DefaultMutableTreeNode(((CondDataFolder)obj).clone());
						addNode(newNode);
						if(latestNode.getChildCount()>0){
							addNodeNest(newNode,latestNode);
						}
					}else if(obj instanceof CondDataDocKkn){
						addNode(new DefaultMutableTreeNode(((CondDataDocKkn)obj).clone()));
					}else if(obj instanceof CondDataDocCode){
						addNode(new DefaultMutableTreeNode(((CondDataDocCode)obj).clone()));
					}else if(obj instanceof CondDataDoc){
						addNode(new DefaultMutableTreeNode(((CondDataDoc)obj).clone()));
					}
				}else{
					toolkit.beep();
				}
			}
		});

 		pm.addSeparator();

		menuItem = new JMenuItem("AND-OR切替え");
		pm.add(	menuItem);
		menuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				changeNode(1);
	    		DefaultMutableTreeNode	lastItem = getSelectedNode();
				if(lastItem != null){
					// 描画領域を更新するために必要
					treeModel.nodeChanged(lastItem);
				}
			}
		});
	
		menuItem = new JMenuItem("NOT切替え");
        pm.add(menuItem);
		menuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				changeNode(2);
	    		DefaultMutableTreeNode	lastItem = getSelectedNode();
				if(lastItem != null){
					// 描画領域を更新するために必要
					treeModel.nodeChanged(lastItem);
				}
			}
		});

 		pm.addSeparator();

		menuItem = new JMenuItem("削除");
        pm.add(menuItem);
		menuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				delDoc();
			}
		});

		return pm;
	}

	/**	runSQLString:	SQLをwhere条件として実行する。
	 * フォルダが選択されている場合はそのフォルダ以下について、
	 * ドキュメントが選択されている場合はそのドキュメントについてのみ、where条件を適用する。
	 */
	protected void runSQLString(){
		DefaultMutableTreeNode	lastItem = getSelectedNode();
		String	SQLString = "";
		String	sql;

		if(lastItem != null){
			Object	obj = lastItem.getUserObject();
			if(obj instanceof CondDataFolder){
				if(lastItem.getChildCount()>0){
					/* フォルダが空でないとき内包のSQLを作成する */
					SQLString = getSQLStringFolder(lastItem);
				}
			}else{
					SQLString = ((CondDataDoc)obj).getSQLString();
					lastItem.setUserObject(obj);
					treeModel.nodeChanged(lastItem);

			}
		}
		/* 最新のSQLStringをテキスト表示する */
		editorPane.setText(SQLString);

		/* SQLStringを実行する */
		if(SQLString.length() == 0){
			SQLString = "条件が選択されていません";
			sql = "select '" + SQLString + "' as message;";
			toolkit.beep();
		}else if(SQLString.indexOf("(未設定)")>-1){
			SQLString = "(未設定)が含まれています";
			sql = "select '" + SQLString + "' as message;";
			toolkit.beep();
		}else{
			sql = "SELECT distinct Title FROM LabelIdx as idx WHERE " + SQLString + " ;";
		}
	
		// JOptionPane.showMessageDialog(null,sql);
		try (Connection con = DriverManager.getConnection(url);
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(sql)) {

			java.sql.ResultSetMetaData meta = rs.getMetaData();
			int cols = meta.getColumnCount();
			String[] names = new String[cols];
			for (int i = 0; i < cols; i++) {
				names[i] = meta.getColumnLabel(i + 1);
			}

			javax.swing.table.DefaultTableModel model =
					new javax.swing.table.DefaultTableModel(names, 0);
			while (rs.next()) {
				Object[] row = new Object[cols];
				for (int i = 0; i < cols; i++) {
					row[i] = rs.getString(i + 1);
				}
				model.addRow(row);
			}

			javax.swing.JTable table = new javax.swing.JTable(model);
			table.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
			javax.swing.JScrollPane scroll = new javax.swing.JScrollPane(table);
			scroll.setPreferredSize(new java.awt.Dimension(1000, 500));

			javax.swing.JOptionPane.showMessageDialog(
					null, scroll, "検索結果 " + model.getRowCount() + "件",
					javax.swing.JOptionPane.PLAIN_MESSAGE);
		} catch (SQLException e) {
			e.printStackTrace();
			if (e.getCause() != null) {
				JOptionPane.showMessageDialog(null,e.getCause().toString());
			}
		}
	}
}
