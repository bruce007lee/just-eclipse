package com.alibaba.just.ui.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


public class EditAliasConfigDialog extends Dialog {

	private Object data = null;

	private Text nameText=null;
	private Text aliasText=null;
	private Label tip = null;
	private String title=null;

	private final String ERROR_REQUIRE = "Please input module name and alias!";
	private final String ERROR_SHOULD_DIFFERENT = "Module name and alias should be different!";
	private final String Empty = "";
	
	private Color redColor = null;

	public EditAliasConfigDialog(Shell parentShell) {
		super(parentShell);
		this.setBlockOnOpen(false);
	}


	public EditAliasConfigDialog(IShellProvider parentShell) {
		super(parentShell);
		this.setBlockOnOpen(false);
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
		title = null;
	}

	protected boolean isResizable() {
		return true;
	}

	public void setTitle(String title){
		if(getShell()!=null){
			getShell().setText(title);
		}else{
			this.title = title;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		redColor = new Color(getShell().getDisplay(),255,0,0);
		
		// create composite
		Composite composite = (Composite) super.createDialogArea(parent);		
		Composite container = new Composite(composite, SWT.NULL);
		GridData gd = new GridData(GridData.FILL_BOTH);
		container.setLayoutData(gd);

		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 2;

		/*module name field*/
		gd = new GridData();
		Label label = new Label(container, SWT.NULL);
		label.setLayoutData(gd);
		label.setText("Module Name:");

		FocusAdapter fl = new FocusAdapter(){
			public void focusGained(FocusEvent e) {
				if(tip!=null){
					tip.setText(Empty);
				}
			}

			public void focusLost(FocusEvent e) {
				if(Text.class.isInstance(e.getSource())){
					Text t = (Text)e.getSource();
					t.setText(t.getText().trim());
				}
			}
		};

		nameText = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint=200;
		nameText.setLayoutData(gd);
		nameText.addFocusListener(fl);

		/*module alias field*/
		gd = new GridData();
		label = new Label(container, SWT.NULL);
		label.setLayoutData(gd);
		label.setText("Module Alias:");

		aliasText = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint=200;
		aliasText.setLayoutData(gd);
		aliasText.addFocusListener(fl);

		/*tip*/
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan=2;
		tip = new Label(container, SWT.NULL);
		tip.setForeground(redColor);
		tip.setLayoutData(gd);

		return composite;
	}

	private boolean validFields(){
		String name=this.getModuleName();
		String alias=this.getModuleAlias();
		if(name==null || alias==null || name.length()<=0 && alias.length()<=0){
			tip.setText(ERROR_REQUIRE);
			return false;
		}else if(name.equals(alias)){
			tip.setText(ERROR_SHOULD_DIFFERENT);
			return false;
		}
		return true;
	}

	protected void buttonPressed(int buttonId) {
		if (IDialogConstants.OK_ID == buttonId && !validFields()) {
			return;
		}		
		super.buttonPressed(buttonId);
	}


	public Object getData() {
		return data;
	}


	public void setData(Object data) {
		this.data = data;
	}

	public String getModuleName(){
		return nameText.getText().trim();
	}

	public String getModuleAlias(){
		return aliasText.getText().trim();
	}

	public void setModuleName(String name){
		nameText.setText(name);
	}

	public void setModuleAlias(String alias){
		aliasText.setText(alias);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#close()
	 */
	public boolean close() {
		this.data=null;
		this.title=null;
		redColor.dispose();
		redColor= null;
		return super.close();
	}

}
