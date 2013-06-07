/**
 * 
 */
package com.alibaba.just.ui.view;

import org.eclipse.core.runtime.IAdaptable;


/**
 * @author bruce.liz
 *
 */
public class ViewItem implements IAdaptable{

	private Object obj;
	private String iconName;
	private String label;
	
	/**
	 * @param obj
	 * @param iconName
	 * @param label
	 */
	public ViewItem(Object obj) {
		super();
		this.obj = obj;
	}

	/**
	 * @param obj
	 * @param iconName
	 * @param label
	 */
	public ViewItem(Object obj, String label) {
		super();
		this.obj = obj;
		this.label = label;
	}

	/**
	 * @param obj
	 * @param iconName
	 * @param label
	 */
	public ViewItem(Object obj, String label ,String iconName ) {
		super();
		this.obj = obj;
		this.iconName = iconName;
		this.label = label;
	}

	public Object getObj() {
		return obj;
	}

	public void setObj(Object obj) {
		this.obj = obj;
	}

	public String getIconName() {
		return iconName;
	}

	public void setIconName(String iconName) {
		this.iconName = iconName;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Object getAdapter(Class adapter) {
		return null;
	}


}
