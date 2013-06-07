package com.alibaba.just.ui.dialogs;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;


public class ListSelectDialog extends Dialog {

	private org.eclipse.swt.widgets.List viewList;

	private String value;
	private Label listLabel;
	private List<String> listItems = null;
	private String listLabelStr = null;
	private String title = null;

	public ListSelectDialog(Shell parentShell) {
		super(parentShell);
	}

	public boolean close() {
		boolean rs = super.close();
		if(rs){
			viewList = null;
			listLabel = null;
			listItems= null;
			value=null;
			title=null;
			listLabelStr=null;
		}
		return rs;
	}

	public ListSelectDialog(IShellProvider parentShell) {
		super(parentShell);
	}

	public ListSelectDialog(Shell parentShell,String dialogTitle,String listLabelStr,List<String> listItems) {
		super(parentShell);
		this.title = dialogTitle;
		this.listLabelStr = listLabelStr;
		this.listItems = listItems;
	}

	public void setListItem(List<String> listItems){
		if(this.viewList==null){
			this.listItems = listItems;
			return;
		}
		viewList.removeAll();
		if(listItems!=null && listItems.size()>0){
			viewList.setItems(listItems.toArray(new String[0]));
			viewList.select(0);
		}
	}

	public String getValue(){
		return this.value;
	}

	public org.eclipse.swt.widgets.List getViewList(){
		return this.viewList;
	}

	public void setListTitle(String title){
		if(listLabel==null){
			listLabelStr = title;
		}else{
			listLabel.setText(title);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		if (title != null) {
			shell.setText(title);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		// create composite
		Composite composite = (Composite) super.createDialogArea(parent);		
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		gd.widthHint=200;
		listLabel = new Label(composite,SWT.WRAP);
		listLabel.setLayoutData(gd);
		if(listLabelStr!=null){
			listLabel.setText(listLabelStr);
		}
		listLabelStr= null;

		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		gd.widthHint=200;
		gd.heightHint=200;
		viewList = new org.eclipse.swt.widgets.List(composite, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
		viewList.setLayoutData(gd);

		if(this.listItems!=null && listItems.size()>0){
			viewList.setItems(listItems.toArray(new String[0]));
			viewList.select(0);
		}
		this.listItems = null;
		return composite;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
	 */
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			String[] vals = viewList.getSelection();
			if(vals==null ||  vals.length==0){
				return;
			}else{
				value = vals[0];
			}
		} else {
			value = null;
		}
		super.buttonPressed(buttonId);
	}
}
