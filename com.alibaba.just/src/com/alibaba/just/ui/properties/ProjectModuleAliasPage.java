/**
 * 
 */
package com.alibaba.just.ui.properties;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.dialogs.PropertyPage;
import org.json.JSONObject;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;

import com.alibaba.just.api.bean.AliasInfo;
import com.alibaba.just.ui.dialogs.EditAliasConfigDialog;
import com.alibaba.just.ui.util.ImageManager;
import com.alibaba.just.ui.util.PluginResourceUtil;
import com.alibaba.just.ui.util.PreferenceUtil;
import com.alibaba.just.util.FileUtil;

/**
 * @author bruce.liz
 *
 */
public class ProjectModuleAliasPage extends PropertyPage{
	private static final int BUTTON_BAR_BTN_WIDTH= 140;

	private final String EDIT_TITLE = "Edit Module Alias";
	private final String ADD_TITLE = "Add Module Alias";
	private EditAliasConfigDialog aliasDialog = null;

	private TableViewer viewer;

	/**
	 * 
	 */
	public ProjectModuleAliasPage() {
		super();
	}

	private void createAliasInfoViewer(Composite parent){
		Composite composite = createDefaultComposite(parent);
		viewer = new TableViewer(composite,SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);

		//table.setColumnProperties(new String[]{"col1","col2"});

		viewer.addDoubleClickListener(new IDoubleClickListener(){

			public void doubleClick(DoubleClickEvent event) {
				handleEditAlias();
			}

		});

		Menu menu = new Menu(viewer.getControl());
		MenuItem delItem = new MenuItem(menu,SWT.PUSH);
		delItem.setText("&Delete");
		delItem.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				handleDeleteSelectAlias();
			}
		});

		viewer.getTable().setMenu(menu);

		TableViewerColumn col = new TableViewerColumn(viewer, SWT.NONE);
		col.getColumn().setWidth(200);
		col.getColumn().setText("Module &Name");
		col.setLabelProvider(new ColumnLabelProvider() {
			public String getText(Object element) {
				return ((AliasInfo)element).getName();
			}

			public Image getImage(Object element) {
				return ImageManager.getImage(ImageManager.IMG_MODULE_ICON);
			}

		});

		TableViewerColumn col1 = new TableViewerColumn(viewer, SWT.NONE);
		col1.getColumn().setWidth(200);
		col1.getColumn().setText("Module A&lias");
		col1.setLabelProvider(new ColumnLabelProvider() {
			public String getText(Object element) {
				return  ((AliasInfo)element).getAlias();
			}

			public Image getImage(Object element) {
				return ImageManager.getImage(ImageManager.IMG_ALIAS_MODULE_ICON);
			}
		});

		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLinesVisible(true); //显示表格线 

		GridData gd = new GridData(GridData.FILL_BOTH);	
		gd.heightHint=400;
		viewer.getTable().setLayoutData(gd);

		Composite btnbar = createDefaultComposite(composite,1,GridData.END);

		Button addBtn = new Button(btnbar,SWT.NULL);
		addBtn.setText("&Add");
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = BUTTON_BAR_BTN_WIDTH;
		addBtn.setLayoutData(gd);

		addBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleAddAlias();
			}
		});

		Button editBtn = new Button(btnbar,SWT.NULL);
		editBtn.setText("&Edit");
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = BUTTON_BAR_BTN_WIDTH;
		editBtn.setLayoutData(gd);

		editBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleEditAlias();
			}
		});

		Button importBtn = new Button(btnbar,SWT.NULL);
		importBtn.setText("&Import");
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = BUTTON_BAR_BTN_WIDTH;
		importBtn.setLayoutData(gd);

		importBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleImportAlias();
			}
		});

		Button exportBtn = new Button(btnbar,SWT.NULL);
		exportBtn.setText("Ex&port");
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = BUTTON_BAR_BTN_WIDTH;
		exportBtn.setLayoutData(gd);

		exportBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleExportAlias();
			}
		});

		Button deleteBtn = new Button(btnbar,SWT.NULL);
		deleteBtn.setText("&Delete");
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = BUTTON_BAR_BTN_WIDTH;
		deleteBtn.setLayoutData(gd);

		deleteBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleDeleteSelectAlias();
			}
		});


		loadAliasList();//load config data
	}

	private void loadAliasList(){
		viewer.add(PreferenceUtil.getProjectAliasList( (IProject) getElement()).toArray());
	}

	private Composite createDefaultComposite(Composite parent) {
		return createDefaultComposite(parent,2,GridData.FILL_HORIZONTAL);
	}

	private Composite createDefaultComposite(Composite parent,int columns,int style) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = columns;
		composite.setLayout(layout);

		GridData data = new GridData(style);
		composite.setLayoutData(data);

		return composite;
	}


	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL);
		composite.setLayoutData(data);

		createAliasInfoViewer(composite);

		return composite;
	}

	private EditAliasConfigDialog getAliasDlg(){
		if(aliasDialog==null){
			aliasDialog = new EditAliasConfigDialog(getShell()){

				protected void okPressed() {					
					if(this.getData()==null){
						//add
						String name=this.getModuleName();
						String alias=this.getModuleAlias();
						viewer.add(new AliasInfo(alias,name));
					}else{
						//edit
						AliasInfo info = (AliasInfo)this.getData();
						String name=this.getModuleName();
						String alias=this.getModuleAlias();
						info.setName(name);
						info.setAlias(alias);
						viewer.refresh(info, true);
					}					
					super.okPressed();
				}

			};
		}
		return aliasDialog;
	}

	private void handleAddAlias(){
		EditAliasConfigDialog dlg = getAliasDlg();
		dlg.setTitle(ADD_TITLE);
		dlg.setData(null);
		dlg.open();
	}

	private void handleEditAlias(){
		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();	
		if(selection==null || selection.isEmpty()){return;}
		handleEditAlias((AliasInfo)selection.getFirstElement());
	}

	private void handleEditAlias(AliasInfo info){
		EditAliasConfigDialog dlg = getAliasDlg();
		dlg.setTitle(EDIT_TITLE);
		dlg.setData(info);
		dlg.open();
		dlg.setModuleName(info.getName());
		dlg.setModuleAlias(info.getAlias());
	}

	private void handleDeleteSelectAlias(){
		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();	
		if(selection==null || selection.isEmpty()){return;}
		this.viewer.remove(selection.toArray());
	}

	/**
	 * Import alias list from file
	 */
	private void handleImportAlias(){
		FileDialog dialog = new FileDialog(
				getShell(),SWT.OPEN);
		IPath ipath = ResourcesPlugin.getWorkspace().getRoot().getLocation();
		if(ipath!=null){
			dialog.setFilterPath(ipath.toString());
		}
		dialog.setText("Select a alias list file");
		dialog.setFilterExtensions(new String[]{"*.js"});
		String path = dialog.open();
		if(path==null){
			return;
		}
		try {
			List<AliasInfo>  aliasList = loadAliasInfoFile(path);
			List<AliasInfo> newList = new ArrayList<AliasInfo>();
			int count = viewer.getTable().getItemCount();
			int aCount = aliasList.size();
			AliasInfo item = null;
			boolean exist = false;
			for(int i=0;i<aCount;i++){
				exist = false;
				item = aliasList.get(i);
				for(int j=0;j<count;j++){
					if(item.equals(viewer.getElementAt(j))){
						exist = true;
						break;
					}
				}
				if(!exist){
					newList.add(item);
				}
			}

			viewer.add(newList.toArray());

		} catch (Exception e) {			
			MessageDialog.openInformation(
					getShell(),
					"Error",
					"Import alias info error ! ["+e.toString()+"]");
		}
	}

	/**
	 * Export select alias info.If not select, export all
	 */
	private void handleExportAlias(){
		List<AliasInfo> list = this.getSelectAliasList();
		if(list.size()<=0){
			list = this.getAliasList();
		}
		if(list.size()<=0){
			MessageDialog.openInformation(
					getShell(),
					"Info",
					"Alias info list is empty!");
			return;
		}
		FileDialog dialog = new FileDialog(
				getShell(),SWT.SAVE);
		IPath ipath = ResourcesPlugin.getWorkspace().getRoot().getLocation();
		if(ipath!=null){
			dialog.setFilterPath(ipath.toString());
		}
		dialog.setFileName("alias-config.js");
		String path = dialog.open();
		if(path==null){
			return;
		}
		JSONObject jObj = new JSONObject();
		for(AliasInfo info:list){
			try {
				jObj.put(info.getAlias(), info.getName());
			} catch (Exception e) {}
		}
		try {
			String content = "(function(){\nreturn "+jObj.toString(1)+";\n})();";
			FileUtil.saveFileContent(path, content,null);
		} catch (Exception e) {
			MessageDialog.openInformation(
					getShell(),
					"Error",
					"Export alias info error ! ["+e.toString()+"]");
		}
	}

	/**
	 * Load alias info list from javascript config file
	 * (Support load from lofty config file)
	 * @param path
	 * @return
	 * @throws Exception
	 */
	private List<AliasInfo> loadAliasInfoFile(String path) throws Exception{
		List<AliasInfo> list = new ArrayList<AliasInfo>();
		File file = new File(path);
		if(file.exists() && file.isFile()){
			String script = FileUtil.getFileContent(file, PreferenceUtil.getFileCharset());
			Context cx = Context.enter();
			try {
				Scriptable scope = cx.initStandardObjects();

				script = "var lofty = {};lofty.config = function(cfg){lofty.alias = cfg.alias;}; \n"+script;
				Object result = cx.evaluateString(scope, script, path,0, null);

				Object rs = null;

				Object lofty = scope.get("lofty", scope);
				NativeObject no= null;
				if(NativeObject.class.isInstance(lofty)){
					no = (NativeObject)lofty;
					rs = no.get("alias", no);
				}

				if(!NativeObject.class.isInstance(rs)){
					if(NativeObject.class.isInstance(result)){
						rs = (NativeObject)result;
					}
				}

				if(NativeObject.class.isInstance(rs)){
					no = (NativeObject)rs;
					Set<Entry<Object, Object>>  set = no.entrySet();					 
					Iterator<Entry<Object, Object>> iter = set.iterator();
					Entry<Object, Object> item = null;
					while(iter.hasNext()){
						item =  iter.next();
						if(String.class.isInstance(item.getKey()) && String.class.isInstance(item.getValue())){
							list.add(new AliasInfo((String)item.getKey(),(String)item.getValue()));
						}
					}
				}
			}catch(Exception e){
				throw e;
			}finally {
				Context.exit();
			}
		}
		return list;
	}

	public void dispose() {
		this.viewer=null;
		super.dispose();
	}

	private List<AliasInfo> getAliasList(){
		List<AliasInfo> list = new ArrayList<AliasInfo>(5);
		int count = viewer.getTable().getItemCount();
		Object item = null;
		for(int j=0;j<count;j++){
			item= viewer.getElementAt(j);
			if(AliasInfo.class.isInstance(item))
				list.add((AliasInfo)item);
		}
		return list;
	}
	
	private List<AliasInfo> getSelectAliasList(){
		List<AliasInfo> list = new ArrayList<AliasInfo>(5);
		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();	
		if(selection==null || selection.isEmpty()){return list;}
		Object[] olist = selection.toArray();
		AliasInfo ai = null;
		for(Object obj:olist){
			if(AliasInfo.class.isInstance(obj)){;
			list.add((AliasInfo)obj);
			}
		}
		return list;
	}

	/**
	 * 
	 */
	public boolean performOk() {
		// store the value in the owner text field
		try {

			IProject project = (IProject) getElement();
			PreferenceUtil.setProjectAliasList(project,getAliasList());
			PluginResourceUtil.clearProjectCache(project);

		} catch (Exception e) {
			return false;
		}
		return true;
	}

}
