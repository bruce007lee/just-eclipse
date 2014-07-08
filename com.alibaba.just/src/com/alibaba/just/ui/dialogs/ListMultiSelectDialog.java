package com.alibaba.just.ui.dialogs;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;

import com.alibaba.just.ui.viewmodel.ViewContentProvider;
import com.alibaba.just.ui.viewmodel.ViewItem;


public class ListMultiSelectDialog extends Dialog {
	private CheckboxTableViewer tableViewer;

	private String value;
	private Label listLabel;
	private List listItems = null;
	private String listLabelStr = null;
	private String title = null;

	public ListMultiSelectDialog(Shell parentShell) {
		super(parentShell);
	}

	public boolean close() {
		boolean rs = super.close();
		if(rs){
			tableViewer = null;
			listLabel = null;
			listItems= null;
			value=null;
			title=null;
			listLabelStr=null;
		}
		return rs;
	}

	public ListMultiSelectDialog(IShellProvider parentShell) {
		super(parentShell);
	}

	public ListMultiSelectDialog(Shell parentShell,String dialogTitle,String listLabelStr,List listItems) {
		super(parentShell);
		this.title = dialogTitle;
		this.listLabelStr = listLabelStr;
		this.listItems = listItems;
	}

	public void setListItem(List listItems){
		if(this.tableViewer==null){
			this.listItems = listItems;
			return;
		}
		//tableViewer.removeAll();
		this.setItems(listItems);
	}

	private void setItems(List listItems){
		if(listItems!=null && listItems.size()>0){
			//tableViewer.setItems(listItems.toArray(new String[0]));
			//tableViewer.select(0);
			for(Object obj: listItems){
				tableViewer.add(obj);
			}
		}
	}

	/**
	 * 取得checkbox选中的项目
	 * @return
	 */
	public Object[] getCheckedElements(){
		if(tableViewer!=null){
			return tableViewer.getCheckedElements();
		}		
		return new Object[0];
	}

	public CheckboxTableViewer getCheckboxTableViewer(){
		return this.tableViewer;
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
		gd.widthHint=230;
		listLabel = new Label(composite,SWT.WRAP);
		listLabel.setLayoutData(gd);
		if(listLabelStr!=null){
			listLabel.setText(listLabelStr);
		}
		listLabelStr= null;

		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 1;
		gd.widthHint=230;
		gd.heightHint=200;
		tableViewer = CheckboxTableViewer.newCheckList(composite, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		tableViewer.getControl().setLayoutData(gd);

		this.initListView(tableViewer);

		this.setItems(listItems);
		this.listItems = null;

		return composite;
	}
	
	/**
	 * 
	 * @return
	 */
	protected void createColumns(CheckboxTableViewer  viewer){		
		TableViewerColumn column = this.createColumn(viewer, "", 30, 0, false, false);
		column.setLabelProvider(new ColumnLabelProvider() {
              public String getText(Object element) {
				return null;
			}
	      });
	    column = this.createColumn(viewer, "", 200, 0, true, false);
		column.setLabelProvider(new ColumnLabelProvider() {
			public String getText(Object element) {				
				if(ViewItem.class.isInstance(element)){
					return ((ViewItem)element).getLabel();
				}				
				return super.getText(element);
			}
	        
	      });
	};

	/**
	 * create view column
	 * @param viewer
	 * @param title
	 * @param bound
	 * @param colNumber
	 * @return
	 */
	protected TableViewerColumn createColumn(CheckboxTableViewer  viewer,String title, int bound, final int colNumber ,boolean resizable ,boolean moveable) {
	    TableViewerColumn viewerColumn = new TableViewerColumn(viewer,SWT.NONE);
	    TableColumn column = viewerColumn.getColumn();
	    column.setText(title);
	    column.setWidth(bound);
	    column.setResizable(resizable);
	    column.setMoveable(moveable);	    
	    return viewerColumn;
	  }

	/**
	 * 初始化StructuredViewer 
	 */
	protected void initListView(CheckboxTableViewer  viewer){
		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLinesVisible(true);

		this.createColumns(viewer);		
		viewer.setContentProvider(new ViewContentProvider<ViewItem>());
		viewer.setCheckStateProvider(new ICheckStateProvider(){
			public boolean isChecked(Object element) {
				if(ViewItem.class.isInstance(element)){
					return ((ViewItem)element).isChecked();
				}			
				return false;
			}

			public boolean isGrayed(Object element) {
				if(ViewItem.class.isInstance(element)){
					return ((ViewItem)element).isGray();
				}			
				return false;
			}			
		});
		viewer.setInput(this);
	}
	
	protected boolean isResizable() {
		return true;
	}

	
}
