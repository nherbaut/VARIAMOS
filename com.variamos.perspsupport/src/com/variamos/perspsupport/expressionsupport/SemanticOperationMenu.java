package com.variamos.perspsupport.expressionsupport;

import com.variamos.perspsupport.semanticsupport.AbstractSemanticElement;
import com.variamos.perspsupport.semanticsupport.SemanticRelationType;

/**
 * A class to support the menus for the operations. Part of PhD work at
 * University of Paris 1
 * 
 * @author Juan C. Mu�oz Fern�ndez <jcmunoz@gmail.com>
 * 
 * @version 1.1
 * @since 2015-12-20
 */
public class SemanticOperationMenu extends AbstractSemanticElement {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6532989148655801713L;

	private int position;
	private int type;

	public static final String VAR_MENUTYPE_IDEN = "menuType",
			VAR_MENUTYPE_NAME = "Menu Type",
			VAR_MENUTYPE_CLASS = SemanticRelationType.class.getCanonicalName(),
			VAR_NAME_IDEN = "name", VAR_NAME_NAME = "Menu Name",
			VAR_SHORTCUT_IDEN = "shorcut", VAR_SHORTCUT_NAME = "Menu Shortcut";

	public SemanticOperationMenu(int position, String identifier, int type) {
		super(identifier);
		this.position = position;
		this.type = type;
	}

	public SemanticOperationMenu() {
		super(null);
		// TODO Auto-generated constructor stub
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

}