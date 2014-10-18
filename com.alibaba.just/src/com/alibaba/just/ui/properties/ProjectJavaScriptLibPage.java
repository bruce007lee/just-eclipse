package com.alibaba.just.ui.properties;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionValidator;
import org.eclipse.ui.dialogs.PropertyPage;

import com.alibaba.just.PluginConstants;
import com.alibaba.just.ui.util.ImageManager;
import com.alibaba.just.ui.util.PluginResourceUtil;
import com.alibaba.just.ui.util.PreferenceUtil;
import com.alibaba.just.ui.viewmodel.ViewContentProvider;
import com.alibaba.just.ui.viewmodel.ViewItem;
import com.alibaba.just.ui.viewmodel.ViewLabelProvider;

/**
 * TODO 对jsdt增强的代码库相关
 * @author bruce.liz
 *
 */
public class ProjectJavaScriptLibPage extends PropertyPage {

	//private static final String QUALIFIED_NAME = PluginConstants.QUALIFIED_NAME;
	private static final String LIBS_PROPERTY_KEY = PluginConstants.LIBS_PROPERTY_KEY;
	private static final String ROOT_PATH_PROPERTY_KEY = PluginConstants.ROOT_PATH_PROPERTY_KEY;

	private static final int BUTTON_BAR_BTN_WIDTH= 140;

	private StructuredViewer  libPathList;
	private StructuredViewer  rootPathList;

	/**
	 * Constructor
	 */
	public ProjectJavaScriptLibPage() {
		super();
	}

	/*------------------------------------------------------------*/

	/**
	 * 初始化StructuredViewer 
	 */
	private void initListView(StructuredViewer  viewer){
		viewer.setContentProvider(new ViewContentProvider<ViewItem>());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setInput(this);
	}

	/**
	 * 
	 * @param viewer
	 * @return
	 */
	private ViewContentProvider<ViewItem> getViewContentProvider(StructuredViewer  viewer){
		return (ViewContentProvider<ViewItem>) viewer.getContentProvider();
	}

	/**
	 * 创建libray设定区域
	 * @param parent
	 */
	private void createLibraySection(Composite parent){
		Composite composite = createDefaultComposite(parent);

		Label listLabel = new Label(composite, SWT.NULL);
		listLabel.setText("Selected module libraries:");
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		listLabel.setLayoutData(gd);

		libPathList = new TableViewer (composite, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		this.initListView(libPathList);
		gd = new GridData(GridData.FILL_BOTH);	
		gd.heightHint=200;
		libPathList.getControl().setLayoutData(gd);

		Composite btnbar = createDefaultComposite(composite,1,GridData.END);

		Button addSelfBtn = new Button(btnbar,SWT.NULL);
		addSelfBtn.setText("Add &Self");
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = BUTTON_BAR_BTN_WIDTH;
		addSelfBtn.setLayoutData(gd);

		addSelfBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleAddSelfResource();
			}
		});

		Button addLibBtn = new Button(btnbar,SWT.NULL);
		addLibBtn.setText("Add &External");
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = BUTTON_BAR_BTN_WIDTH;
		addLibBtn.setLayoutData(gd);

		addLibBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleAddExternalResource();
			}
		});

		Button addWsLibBtn = new Button(btnbar,SWT.NULL);
		addWsLibBtn.setText("Add &Workspace");
		gd = new GridData(GridData.FILL_HORIZONTAL);
		addWsLibBtn.setLayoutData(gd);

		addWsLibBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleAddWorkspaceResource();
			}
		});

		Button removeBtn = new Button(btnbar,SWT.NULL);
		removeBtn.setText("&Remove");
		gd = new GridData(GridData.FILL_HORIZONTAL);
		removeBtn.setLayoutData(gd);

		removeBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleRemoveItems(libPathList);
			}
		});

		Button upBtn = new Button(btnbar,SWT.NULL);
		upBtn.setText("&Up");
		gd = new GridData(GridData.FILL_HORIZONTAL);
		upBtn.setLayoutData(gd);

		upBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleMove(libPathList,-1);
			}
		});

		Button downBtn = new Button(btnbar,SWT.NULL);
		downBtn.setText("&Down");
		gd = new GridData(GridData.FILL_HORIZONTAL);
		downBtn.setLayoutData(gd);

		downBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleMove(libPathList,1);
			}
		});
	}

	/**
	 * 移动选中的对象
	 * @param viewer
	 * @param move
	 */
	private void handleMove(StructuredViewer viewer,int move){		
		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();	
		if(selection==null || selection.isEmpty()){return;}
		List items = selection.toList();

		ViewContentProvider<ViewItem> provider = this.getViewContentProvider(viewer);
		int index = -1;
		for(Object item:items){
			if(item instanceof ViewItem){
				index = provider.indexOf((ViewItem)item);
				break;
			}
		}

		if(index>-1){	
			ViewItem item = provider.remove(index);
			if(item!=null){
				int m = index+move;
				if(m<0){
					m=0;
				}else if(m > provider.getItemList().size()-1){
					m = provider.getItemList().size();
				}
				provider.add(m,item);
				viewer.setSelection(new StructuredSelection(item));	
			}
		}

		viewer.refresh();
	}

	/**
	 * 创建root path设定区域
	 * @param parent
	 */
	private void createRootPathSection(Composite parent){
		Composite composite = createDefaultComposite(parent);

		Label listLabel = new Label(composite, SWT.NULL);
		listLabel.setText("Added module root path:");
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);	
		gd.horizontalSpan = 2;
		listLabel.setLayoutData(gd);

		rootPathList = new TableViewer (composite, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		this.initListView(rootPathList);
		gd = new GridData(GridData.FILL_BOTH);	
		gd.heightHint=200;
		rootPathList.getControl().setLayoutData(gd);

		Composite btnbar = createDefaultComposite(composite,1,GridData.END);

		Button addRootBtn = new Button(btnbar,SWT.NULL);
		addRootBtn.setText("Add &Root");
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = BUTTON_BAR_BTN_WIDTH;
		addRootBtn.setLayoutData(gd);

		addRootBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleAddRootPath();
			}
		});

		Button removeBtn = new Button(btnbar,SWT.NULL);
		removeBtn.setText("&Remove");
		gd = new GridData(GridData.FILL_HORIZONTAL);
		removeBtn.setLayoutData(gd);

		removeBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleRemoveItems(rootPathList);
			}
		});

		Button upBtn = new Button(btnbar,SWT.NULL);
		upBtn.setText("&Up");
		gd = new GridData(GridData.FILL_HORIZONTAL);
		upBtn.setLayoutData(gd);

		upBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleMove(rootPathList,-1);
			}
		});

		Button downBtn = new Button(btnbar,SWT.NULL);
		downBtn.setText("&Down");
		gd = new GridData(GridData.FILL_HORIZONTAL);
		downBtn.setLayoutData(gd);

		downBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleMove(rootPathList,1);
			}
		});

		// Populate root paths
		try{
			List<String> libs = PreferenceUtil.getProjectRootPathList(((IProject) getElement()).getProject());		  
			for(String lib:libs){
				String lb = lib.trim();
				if(lb.length()>0){
					this.getViewContentProvider(rootPathList).add(new ViewItem(lb));
				}
			}	
			rootPathList.refresh();
		}catch(Exception e){}
	}

	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL);
		composite.setLayoutData(data);

		createLibraySection(composite);
		createRootPathSection(composite);

		return composite;
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

		//test
		//composite.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_GRAY));

		return composite;
	}

	protected void performDefaults() {
		super.performDefaults();
		this.getViewContentProvider(libPathList).clear();
		this.getViewContentProvider(libPathList).add(getItemByLib(PreferenceUtil.SELF_LIB_STR));
		libPathList.refresh();
	}

	/**
	 * 根据lib拼接传创建list view item
	 * @param libstr
	 * @return
	 */
	private ViewItem getItemByLib(String libStr){
		String imgId = null;
		String type = PreferenceUtil.getProjectLibType(libStr);
		if(PreferenceUtil.LIB_TYPE_SELF.equals(type)){
			imgId = ImageManager.IMG_LIB_PROJECT;
		}else if(PreferenceUtil.LIB_TYPE_WORKSPACE_FOLDER.equals(type)){
			imgId = ImageManager.IMG_LIB_FOLDER;
		}else{
			imgId = ImageManager.IMG_LIB;
		}
		ViewItem item = new ViewItem(libStr,PreferenceUtil.getProjectLibPath(libStr),imgId);
		return item;
	}

	/**
	 * Uses the standard container selection dialog to choose the new value for
	 * the container field.
	 */

	private void handleAddWorkspaceResource() {
		ContainerSelectionDialog dialog = new ContainerSelectionDialog(
				getShell(), (IProject)getElement(), false,
		"Select workspace folder as a libray path.");
		if (dialog.open() == ContainerSelectionDialog.OK) {
			Object[] result = dialog.getResult();
			if (result.length == 1) {
				String path = ((Path) result[0]).toString();
				if(path!=null && path.length()>0){
					if(!isExist(libPathList,PreferenceUtil.LIB_TYPE_WORKSPACE_FOLDER+path)){
						this.getViewContentProvider(libPathList).add(getItemByLib(PreferenceUtil.LIB_TYPE_WORKSPACE_FOLDER+path));
						libPathList.refresh();
					}
				}
			}
		}
	}

	/**
	 * 选择作为root path的project路径
	 */
	private void handleAddRootPath() {
		final IProject project = (IProject)getElement();
		ContainerSelectionDialog dialog = new ContainerSelectionDialog(
				getShell(), project, false,
		"Select a folder as root path.");

		dialog.setValidator(new ISelectionValidator(){
			public String isValid(Object selection) {
				if(selection instanceof IPath){
					IPath path = (IPath)selection;
					if(project.getName().equals(path.segment(0))){
						return null;
					}
				}
				return "Please select a valid path of project \""+project.getName()+"\"";
			}			
		});

		if (dialog.open() == ContainerSelectionDialog.OK) {
			Object[] result = dialog.getResult();
			if (result.length == 1) {
				String projectPath = project.getFullPath().toString();
				String path = ((Path) result[0]).toString();
				if(path!=null && path.length()>0){
					path = PreferenceUtil.getRootPathByIPath(project, path);
					if(!isExist(rootPathList,path)){
						//rootPathList.add(path);
						this.getViewContentProvider(rootPathList).add(new ViewItem(path));
						rootPathList.refresh();
					}
				}
			}
		}
	}

	/**
	 * 
	 */
	private void handleRemoveItems(StructuredViewer viewer) {
		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();			
		if(selection==null || selection.isEmpty()){return;}
		List items = selection.toList();			
		for(Object item:items){
			if(item instanceof ViewItem){
				this.getViewContentProvider(viewer).remove((ViewItem)item);
			}
		}
		viewer.refresh();
	}

	private void handleAddSelfResource(){
		if(!isExist(libPathList,PreferenceUtil.SELF_LIB_STR)){
			//libPathList.add(new ViewItem(PreferenceUtil.SELF_LIB_STR,PreferenceUtil.getProjectLibPath(PreferenceUtil.SELF_LIB_STR)));
			this.getViewContentProvider(libPathList).add(getItemByLib(PreferenceUtil.SELF_LIB_STR));
			libPathList.refresh();
		}
	}

	private void handleAddExternalResource() {
		FileDialog dialog = new FileDialog(
				getShell(),SWT.OPEN);
		IPath ipath = ResourcesPlugin.getWorkspace().getRoot().getLocation();
		if(ipath!=null){
			dialog.setFilterPath(ipath.toString());
		}
		//dialog.setMessage("Select a lib file");
		String path = dialog.open();
		if(path==null){
			return;
		}
		if(!isExist(libPathList,PreferenceUtil.LIB_TYPE_EXTERNAL_FOLDER+path)){
			this.getViewContentProvider(libPathList).add(getItemByLib(PreferenceUtil.LIB_TYPE_EXTERNAL_FOLDER+path));
			libPathList.refresh();
		}
	}

	private boolean isExist(StructuredViewer  list,String item){
		List items = this.getViewContentProvider(list).getItemList();	
		for(Object t:items){
			if(t instanceof ViewItem){
				if(((ViewItem)t).getObj().equals(item)){
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 
	 * @param viewer
	 * @return
	 */
	private String getToSaveStr(StructuredViewer  viewer){
		StringBuffer sb = new StringBuffer();	
		List<ViewItem> items = this.getViewContentProvider(viewer).getItemList();
		if(items!=null){
			int i = 0;
			for(ViewItem item:items){
				if(i>0){
					sb.append("\n");
				}
				sb.append(item.getObj().toString());
				i++;
			}
		}
		return sb.toString();
	}

	/**
	 * 
	 * @param before
	 * @param after
	 * @return
	 */
	private void clearLibCache(IProject project,List<String> before,List<String> after){
		List<String> list = new ArrayList<String>(5);
		if(before!=null && after!=null){
			String tmp = null;
			boolean isRemove = true;
			for(int i=0,l=before.size();i<l;i++){
				tmp = before.get(i);
				isRemove = true;
				for(int j=0,ll=after.size();j<ll;j++){
					if(tmp.equals(after.get(j))){
						isRemove = false;
						break;
					}
				}
				if(isRemove){
					list.add(tmp);
				}
			}
		}
		PluginResourceUtil.removeProjectLibCache(project,list);
	}

	public boolean performOk() {
		// store the value in the owner text field
		try {

			IProject project = (IProject) getElement();
			PreferenceUtil.setProjectProperty(project,PluginConstants.JAVASCRIPT_LIBS_PROPERTY_KEY, this.getToSaveStr(rootPathList));

		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public void dispose() {
		this.libPathList=null;
		this.rootPathList=null;
		super.dispose();
	}

}