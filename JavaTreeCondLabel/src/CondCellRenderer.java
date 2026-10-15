/*
**********************************************************************
*	CondCellRenderer:	CondDataNodeを描画する
**********************************************************************
*/

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import java.awt.Component;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import java.net.URL;

/**
 * ツリー状に定義された高度検索条件を表示するオブジェクトに関する要求を定義します。
 * アイコンを持つJLabelを拡張した表示形式を使用します。
 * @author	H.Yano
 * @since	JDK1.2
 * @see	TreeCond
 */
public class CondCellRenderer extends JLabel implements TreeCellRenderer
{
	/** デフォルフォント */
    static protected Font             defaultFont;
	/** 閉じているAND条件フォルダを表すアイコン */
    static protected ImageIcon        clsfldandIcon;
    static protected ImageIcon        clsfldNotandIcon;
	/** 閉じているOR条件フォルダを表すアイコン */
    static protected ImageIcon        clsfldorIcon;
    static protected ImageIcon        clsfldNotorIcon;
	/** 開いているAND条件フォルダを表すアイコン */
    static protected ImageIcon        opnfldandIcon;
    static protected ImageIcon        opnfldNotandIcon;
	/** 開いているOR条件フォルダを表すアイコン */
    static protected ImageIcon        opnfldorIcon;
    static protected ImageIcon        opnfldNotorIcon;
	/** plain接続の条件ドキュメントを表すアイコン */
    static protected ImageIcon        docandIcon;
	/** NOT接続の条件ドキュメントを表すアイコン */
    static protected ImageIcon        docnotIcon;
	/** 選択されたノードの背景色 */
    static protected final Color SelectedBackgroundColor = Color.yellow;//new Color(0, 0, 128);

	/* アイコンをロードするstatic initializer */
    static
    {
		try {
	    	defaultFont = new Font("SansSerif", 0, 12);
		} catch (Exception e) {}
		try {
	    	clsfldandIcon = new ImageIcon("icons/clsfldand.gif");
	    	clsfldNotandIcon = new ImageIcon("icons/clsfldNotand.gif");
	    	clsfldorIcon = new ImageIcon("icons/clsfldor.gif");
	    	clsfldNotorIcon = new ImageIcon("icons/clsfldNotor.gif");
	    	opnfldandIcon = new ImageIcon("icons/opnfldand.gif");
	    	opnfldNotandIcon = new ImageIcon("icons/opnfldNotand.gif");
	    	opnfldorIcon = new ImageIcon("icons/opnfldor.gif");
	    	opnfldNotorIcon = new ImageIcon("icons/opnfldNotor.gif");
	    	docandIcon = new ImageIcon("icons/docand.gif");
	    	docnotIcon = new ImageIcon("icons/docnot.gif");
			// clsfldandIcon = new ImageIcon(TreeCond.class.getResource("/icons/clsfldand.GIF"));
			// clsfldNotandIcon = new ImageIcon(TreeCond.class.getResource("/icons/clsflNotdand.GIF"));
			// clsfldorIcon = new ImageIcon(TreeCond.class.getResource("/icons/clsfldor.GIF"));
			// clsfldNotorIcon = new ImageIcon(TreeCond.class.getResource("/icons/clsflNotdor.GIF"));
			// opnfldandIcon = new ImageIcon(TreeCond.class.getResource("/icons/opnfldand.GIF"));
			// opnfldNotandIcon = new ImageIcon(TreeCond.class.getResource("/icons/opnfldNotand.GIF"));
			// opnfldorIcon = new ImageIcon(TreeCond.class.getResource("/icons/opnfldor.GIF"));
			// opnfldNotorIcon = new ImageIcon(TreeCond.class.getResource("/icons/opnfldNotor.GIF"));
			// docandIcon = new ImageIcon(TreeCond.class.getResource("/icons/docandIcon.GIF"));
			// docnotIcon = new ImageIcon(TreeCond.class.getResource("/icons/docnotIcon.GIF"));
		} catch (Exception e) {
	    	System.out.println("Couldn't load images: " + e);
		}
    }

    /** Whether or not the item that was last configured is selected. */
    protected boolean            selected;

    /**
	  * (DefaultMutableTreeNode)valueのuserObjectを得て、該当するノードのアイコンおよびテキストを設定した、
	  * JLaelオブジェクトを返します。
      */
    public Component getTreeCellRendererComponent(JTree tree, Object value,
					  boolean selected, boolean expanded,
					  boolean leaf, int row,
					  boolean hasFocus) {
		Font            font;
		String          stringValue = tree.convertValueToText(value, selected,
					   expanded, leaf, row, hasFocus);
		CondDataFolder  folder;

		/* tree nodeのデータuserObjectを得る */
		Object    userObject = ((DefaultMutableTreeNode)value).getUserObject();

		/* textを決定する */
		setText(stringValue);
		/* Tooltips used by the tree. */
		setToolTipText(stringValue);

		/* Icon imageを決定する */
		if(userObject instanceof CondDataFolder){
			folder = (CondDataFolder)userObject;
			if(expanded){
				if(folder.connect){
					setIcon(opnfldorIcon);
					if(folder.getConnFolder() == 1) setIcon(opnfldandIcon);
				}else{
					setIcon(opnfldNotorIcon);
					if(folder.getConnFolder() == 1) setIcon(opnfldNotandIcon);
				}
			}else{
				if(folder.connect){
					setIcon(clsfldorIcon);
					if(folder.getConnFolder() == 1) setIcon(clsfldandIcon);
				}else{
					setIcon(clsfldNotorIcon);
					if(folder.getConnFolder() == 1) setIcon(clsfldNotandIcon);
				}
			}
		}else if(userObject instanceof CondDataDocCode){
			if(((CondDataDocCode)userObject).getConnect()){
	    		setIcon(docandIcon);
			}else{
	    		setIcon(docnotIcon);
			}
		}else{
			if(((CondDataDoc)userObject).getConnect()){
	    		setIcon(docandIcon);
			}else{
	    		setIcon(docnotIcon);
			}
		}

		//if(hasFocus) setForeground(Color.cyan);
	    setFont(defaultFont);
	
		/* Update the selected flag for the next paint. */
		this.selected = selected;

		return this;
    }

    /**
	  * ツリーの場合は、バックグランドカラーは白のみとは限らないので、valueの選択状態などから
	  * 異なるバックグランドカラーを得て、あらかじめ消去を実行するようpaint()をオーバライドする。
      */
    public void paint(Graphics g) {
		Color            bColor;
		Icon             currentI = getIcon();

		/* バックグランドカラーを決定して領域を消去する */
		if(selected)
	    	bColor = SelectedBackgroundColor;
		else if(getParent() != null)
	    	/* Pick background color up from parent (which will come from
	       	the JTree we're contained in). */
	    	bColor = getParent().getBackground();
		else
	    	bColor = getBackground();
		g.setColor(bColor);

		if(currentI != null && getText() != null) {
	    	int          offset = (currentI.getIconWidth() + getIconTextGap());

	    	g.fillRect(offset, 0, getWidth() - 1 - offset, getHeight() - 1);
		}else{
	    	g.fillRect(0, 0, getWidth()-1, getHeight()-1);
		}
		
		/* 現在のイメージを表示する */
		super.paint(g);
    }
}

