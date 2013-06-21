package com.alibaba.just.ui.views;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.ViewPart;

import com.alibaba.just.Activator;
import com.alibaba.just.api.bean.Module;
import com.alibaba.just.api.parser.ModuleParser;
import com.alibaba.just.ui.preferences.PreferenceConstants;
import com.alibaba.just.ui.util.ImageManager;
import com.alibaba.just.ui.util.PluginResourceUtil;
import com.alibaba.just.ui.util.PreferenceUtil;

/**
 * Module view UI
 * @author bruce.liz
 *
 */
public class ModuleView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "com.alibaba.just.ui.views.ModuleView";

	private static final String TREE_LABEL_FILE_NOT_FOUND = " - [Not Found]";
	private static final String SHOW_TYPE_FLAT = "flat";
	private static final String SHOW_TYPE_HIER = "hier";

	private TreeViewer viewer;
	private ViewContentProvider vcp;
	//private DrillDownAdapter drillDownAdapter;
	//private Action action1;
	//private Action action2;
	//private Action doubleClickAction;

	private DefaultRunner  runner;


	private IFile currentFile = null;
	private String currentFilePath = null;
	private long currentFileTimeStamp = 0L;

	private boolean isDispose = false;

	private IPartListener partListener = null;

	private IAction action_flat;
	private IAction action_hier;
	private IAction action_refresh;
	private IAction action_used_list;


	/*
	 * tree node
	 */
	class TreeNode implements IAdaptable {
		private Object obj;
		private String iconName;

		private String desc;

		public String getIconName() {
			return this.iconName;
		}
		public void setIconName(String iconName) {
			this.iconName = iconName;
		}
		private TreeNode parent;
		private List<TreeNode> children;

		public TreeNode(Object obj) {
			this.obj = obj;
			children = new ArrayList<TreeNode>();
		}

		public String getName() {
			if(Module.class.isInstance(obj)){
				Module m = (Module)obj;
				if(m.isAnonymous()){
					return "<anonymous module>";
				}else{
					return ((Module)obj).getName();
				}
			}
			return obj.toString();
		}

		public Object getObject() {
			return obj;
		}
		public void setParent(TreeNode parent) {
			this.parent = parent;
		}
		public TreeNode getParent() {
			return parent;
		}

		public void addChild(TreeNode child) {
			this.children.add(child);
			child.setParent(this);
		}

		public void addChildren(List<TreeNode> children) {
			if(children!=null){
				for(TreeNode child:children){
					this.children.add(child);
					child.setParent(this);
				}
			}
		}

		public void removeChild(TreeNode child) {
			this.children.remove(child);
			child.setParent(null);
		}

		public TreeNode [] getChildren() {
			return (TreeNode [])this.children.toArray(new TreeNode[this.children.size()]);
		}
		public boolean hasChildren() {
			return this.children.size()>0;
		}

		public void removeChildren(){
			this.children.clear();
		}

		public String toString() {
			return getName();
		}
		public Object getAdapter(Class key) {
			return null;
		}

		public void dispose(boolean isRecursive){
			this.obj = null;
			if(isRecursive && this.children!=null && this.children.size()>0){
				for(TreeNode node:this.children){
					node.dispose(isRecursive);
				}
			}else if(this.children!=null){
				this.children.clear();
			}
			this.children = null;
		}
		public String getDesc() {
			return desc;
		}
		public void setDesc(String desc) {
			this.desc = desc;
		}

	}

	class ViewContentProvider implements IStructuredContentProvider, 
	ITreeContentProvider {
		private TreeNode invisibleRoot;

		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}
		public void dispose() {
			if(invisibleRoot!=null){
				invisibleRoot.dispose(true);
			}
			invisibleRoot=null;
		}
		public Object[] getElements(Object parent) {
			if (parent.equals(getViewSite())) {
				if (invisibleRoot==null) initialize();
				return getChildren(invisibleRoot);
			}
			return getChildren(parent);
		}
		public Object getParent(Object child) {
			if (child instanceof TreeNode) {
				return ((TreeNode)child).getParent();
			}
			return null;
		}
		public Object [] getChildren(Object parent) {
			if (parent instanceof TreeNode) {
				return ((TreeNode)parent).getChildren();
			}
			return new Object[0];
		}
		public boolean hasChildren(Object parent) {
			if (parent instanceof TreeNode)
				return ((TreeNode)parent).hasChildren();
			return false;
		}
		/*
		 * We will set up a dummy model to initialize tree heararchy.
		 * In a real code, you will connect to a real model and
		 * expose its hierarchy.
		 */
		private void initialize() {
			invisibleRoot = new TreeNode("");
		}

	}

	class TreeLabelProvider extends StyledCellLabelProvider {
		public void update(ViewerCell cell) {
			Object obj = cell.getElement();
			TreeNode node = null;
			Module m = null;
			if(TreeNode.class.isInstance(obj)){				
				StyledString text = new StyledString();				
				node = (TreeNode)obj;
				cell.setImage(ImageManager.getImage(node.getIconName()));

				text.append(node.getName());

				if(node.getDesc()!=null){
					text.append(node.getDesc(), StyledString.QUALIFIER_STYLER);
				}

				Object nodeData = node.getObject();
				if(Module.class.isInstance(nodeData)){
					m = (Module)nodeData;

					if(node.getChildren()!=null && node.getChildren().length>0){
						text.append(" - (" + node.getChildren().length +")", StyledString.QUALIFIER_STYLER);
					}

					if(m.getFilePath()==null){
						text.append(TREE_LABEL_FILE_NOT_FOUND, StyledString.QUALIFIER_STYLER);
					}
				}

				cell.setStyleRanges(text.getStyleRanges());
				cell.setText(text.toString());
				super.update(cell);
			}
		}

	}

	class NameSorter extends ViewerSorter {
	}

	/**
	 * 
	 * @author bruce.liz
	 *
	 */
	abstract class DefaultRunner extends Thread{
		private boolean isStop=false;
		public void stopRunner(){
			this.isStop = true;
		}

		public boolean isStop(){
			return this.isStop;
		}
	}

	/**
	 * The constructor.
	 */
	public ModuleView() {
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		//drillDownAdapter = new DrillDownAdapter(viewer);
		vcp = new ViewContentProvider();
		viewer.setContentProvider(vcp);
		viewer.setLabelProvider(new TreeLabelProvider());
		viewer.setSorter(new NameSorter());
		viewer.setInput(getViewSite());

		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "com.alibaba.just.viewer");
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
		initListener();
	}

	/**
	 * 
	 */
	private void removeListener(){
		Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getPartService().removePartListener(partListener);
		partListener = null;
	}

	/**
	 * 
	 * @param part
	 */
	private void updateView(IWorkbenchPart part){
		try{
			if(IEditorPart.class.isInstance(part)){
				IEditorInput input = ((IEditorPart)part).getEditorInput();
				if(FileEditorInput.class.isInstance(input)){
					currentFilePath = null;
					FileEditorInput fileInput = (FileEditorInput)input;
					IFile ff = fileInput.getFile();

					//没变化不刷新
					if(currentFile!=null && ff.equals(currentFile) && 
							currentFileTimeStamp != 0L && ff.getModificationStamp() == currentFileTimeStamp){
						return;
					}
					currentFile = ff;
					currentFileTimeStamp = ff.getModificationStamp();
					//异步刷新
					loadModuleRequires(currentFile,true);
				}else if(FileStoreEditorInput.class.isInstance(input)){
					FileStoreEditorInput fileInput = (FileStoreEditorInput)input;
					if(currentFile!=null){
						IProject project = currentFile.getProject();
						if(project!=null){
							File fi = new File(fileInput.getURI());
							String cf = fi.getAbsolutePath();

							//没变化不刷新
							if(currentFilePath!=null && currentFilePath.equals(cf) && 
									currentFileTimeStamp != 0L && fi.lastModified() == currentFileTimeStamp){
								return;
							}
							currentFilePath = cf;
							currentFileTimeStamp = fi.lastModified();
							//异步刷新
							loadModuleRequires(currentFilePath, project,true);
						}
					}
				}
			}
			/*else if(!ModuleView.class.isInstance(part)){
				clearView(true);
			}*/
		}catch(Exception e){
			//throw new InvocationTargetException(e);
			//e.printStackTrace();
		}


	}

	/**
	 * 
	 */
	private void initListener(){
		partListener = new IPartListener(){
			public void partActivated(IWorkbenchPart part) {
				updateView(part);
			}

			public void partBroughtToTop(IWorkbenchPart part) {
				updateView(part);
			}

			public void partClosed(IWorkbenchPart part) {

			}

			public void partDeactivated(IWorkbenchPart part) {

			}

			public void partOpened(IWorkbenchPart part) {
				// TODO Auto-generated method stub

			}


		};

		Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getPartService().addPartListener(partListener);
	}

	public void clearView(boolean isRefresh){
		vcp.invisibleRoot.removeChildren();//clear
		if(isRefresh){
			viewer.refresh();
		}
	}

	private void showLoading(boolean isAsyncExec){
		Runnable run = new Runnable(){
			public void run() {		
				vcp.invisibleRoot.removeChildren();//clear
				TreeNode loading = new TreeNode("Loading...");
				vcp.invisibleRoot.addChild(loading);
				viewer.refresh();
			}								
		};

		if(isAsyncExec){
			//同步ui显示
			Display.getDefault().syncExec(run);
		}else{
			run.run();
		}
	}


	public void loadModuleRequires(IFile ifile,boolean isAsyncExec) throws Exception{
		String path = ifile.getLocation().toFile().getAbsolutePath();
		IProject project = ifile.getProject();
		this.loadModuleRequires(path, project,isAsyncExec);
	}

	/**
	 * 得到使用指定模块的父模块列表树
	 * @param module
	 * @param allImported
	 * @return
	 */
 	private TreeNode getUsedTree(List<Module> moduleList){
		TreeNode tp = new TreeNode("Used Modules...");
		tp.setIconName(ImageManager.IMG_USED_LIST);
		List<String> tmp = new ArrayList<String>();
		if(moduleList!=null){
			TreeNode um = null;
			String key = null;
			for(Module m:moduleList){
				key = m.getName();
				if(m.isAnonymous() && m.getFilePath()!=null){
					key = m.getFilePath();
				}
				if(key!=null && !tmp.contains(key)){
					tmp.add(key);
					um = new TreeNode(m);
					if(m.isAnonymous() && m.getFilePath()!=null){
						um.setDesc(" - ["+m.getFilePath()+"]");
					}
					um.setIconName(ImageManager.IMG_MODULE_ICON);
					tp.addChild(um);
				}
			}
		}
		tp.setDesc(" - ("+tmp.size()+")");
		tmp.clear();
		return tp;
	}

	/**
	 * 得到树形结构的module树对象
	 * @param module
	 * @param allImported
	 * @return
	 */
	private TreeNode getRequiredModulesTree(Module module,List<Module> allImported){
		TreeNode tp = new TreeNode(module);
		tp.setIconName(ImageManager.IMG_MODULE_ICON);//set icon
		List<String> names = module.getRequiredModuleNames();
		for(String name:names){
			for(Module impModule:allImported){
				if(name.equals(impModule.getName())){
					tp.addChild(getRequiredModulesTree(impModule,allImported));
					break;
				}
			}
		}
		return tp;
	}

	/**
	 * 得到列表结构的module树对象
	 * @param module
	 * @param allImported
	 * @return
	 */
	private List<TreeNode> getRequiredModulesList(List<TreeNode> list,Module module,List<Module> allImported,boolean isStartModule){
		List<TreeNode> l = list==null ? new ArrayList<TreeNode>() : list;
		TreeNode tp = new TreeNode(module);
		tp.setIconName(ImageManager.IMG_MODULE_ICON);//set icon
		if(!isExist(tp,l)){
			l.add(tp);
			List<String> names = module.getRequiredModuleNames();
			List<TreeNode> tmp = l;
			if(isStartModule){
				tmp = new ArrayList<TreeNode>();
			}
			for(String name:names){
				for(Module impModule:allImported){
					if(name.equals(impModule.getName())){
						getRequiredModulesList(tmp,impModule,allImported,false);
						break;
					}
				}
			}
			if(isStartModule){
				tp.addChildren(tmp);
			}
		}		
		return l;
	}

	private boolean isExist(TreeNode checkNode,List<TreeNode> list){
		for(TreeNode node:list){
			if(checkNode!=null &&  
					node.getObject()!=null &&  
					checkNode.getObject()!=null &&  
					node.getObject().equals(checkNode.getObject())){
				return true;
			}
		}
		return false;
	}

	/**
	 * 读取模块内容
	 * @param ifile
	 * @throws Exception 
	 */
	public void loadModuleRequires(final String filepath,final IProject project,boolean isAsyncExec) throws Exception{
		clearView(true);//clear
		if(runner!=null){
			runner.stopRunner();
		}

		if(isDispose){
			runner = null;
			return;
		}

		runner = new DefaultRunner(){

			/*
			 * (non-Javadoc)
			 * @see java.lang.Thread#run()
			 */
			public void run(){
				try {
					ModuleParser parser = new ModuleParser(PreferenceUtil.getFileCharset());
					Module module = parser.getModule(filepath,ModuleParser.MODULE_TYPE_ALL);

					if(module==null || project==null){

						return;
					}

					showLoading(true);
					List<String> libs = PreferenceUtil.getProjectLibsList(project);	
					List<Module> moduleList = new ArrayList<Module>();
					IWorkspaceRoot  wRoot = ResourcesPlugin.getWorkspace().getRoot();
					for(String lib:libs){
						String lb = lib.trim();
						if(lb.length()>0){
							String folderPath = null;
							String type = PreferenceUtil.getProjectLibType(lb);
							if(PreferenceUtil.LIB_TYPE_WORKSPACE_FOLDER.equals(type) ||
									PreferenceUtil.LIB_TYPE_SELF.equals(type)){
								if(PreferenceUtil.LIB_TYPE_SELF.equals(type)){
									lb = project.getFullPath().toString();
								}else{
									lb = PreferenceUtil.getProjectLibPath(lb);
								}
								IPath rootPath = wRoot.getFullPath();
								IResource res = wRoot.findMember(rootPath.append(lb));
								if(res!=null && res.isAccessible()){
									PluginResourceUtil.getModulesByResource(res,moduleList,parser,ModuleParser.MODULE_TYPE_ALL);
								}
							}else{
								lb = PreferenceUtil.getProjectLibPath(lb);
								File f = new File(lb);
								if(f.exists() && f.isDirectory()){
									folderPath = f.getAbsolutePath();
									moduleList.addAll(parser.getAllModules(folderPath));
								}
							}

						}
					}

					List<Module> requires =  parser.getAllRequiredModules(module, moduleList);

					if(!this.isStop() && !isDispose){

						final List<TreeNode> roots = new ArrayList<TreeNode>(2);	

						clearView(false);
						TreeNode root = new TreeNode("Required Modules...");

						String showType = getShowType();
						if(SHOW_TYPE_FLAT.equalsIgnoreCase(showType)){
							root.addChildren(getRequiredModulesList(new ArrayList<TreeNode>(),module, requires,true));
							root.setIconName(ImageManager.IMG_FLAT_LAYOUT);
						}else{
							root.addChild(getRequiredModulesTree(module, requires));
							root.setIconName(ImageManager.IMG_HIERARCHICAL_LAYOUT);
						}

						roots.add(root);

						if(isShowUsedList()){
							List<Module> usedList = parser.getUsedModules(module, moduleList);
							final TreeNode usedRoot = getUsedTree(usedList);
							roots.add(usedRoot);
						}

						//同步ui显示
						Display.getDefault().syncExec(new Runnable(){
							public void run() {		
								ModuleView self = ModuleView.this;
								clearView(false);									
								self.vcp.invisibleRoot.addChildren(roots);
								self.viewer.refresh();
								self.viewer.expandAll();
							}								
						});
					}

				} catch (Exception e) {
					String error = e.getMessage();
					if(error==null || error.trim().length()==0){
						error = e.toString();
					}
					showException(e);					
				} 

			}


			private void showException(final Exception e){
				//同步ui显示
				if(!isDispose){
					Display.getDefault().syncExec(new Runnable(){
						public void run() {	
							ModuleView self = ModuleView.this;
							self.vcp.invisibleRoot.removeChildren();
							String error = e.getMessage();
							if(error==null || error.trim().length()==0){
								error = e.toString();
							}
							TreeNode root = new TreeNode("ERROR:"+error);
							self.vcp.invisibleRoot.addChild(root);
							self.viewer.refresh();
							self.viewer.expandAll();
						}
					});
				}
			}
		};

		if(isAsyncExec){
			runner.start();
		}else{
			runner.run();
		}

	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	public void dispose() {
		isDispose = true;
		currentFile = null;
		currentFilePath = null;
		viewer=null;
		vcp=null;
		//drillDownAdapter=null;
		removeListener();
		super.dispose();
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				ModuleView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		/*manager.add(action1);
		manager.add(new Separator());
		manager.add(action2);*/
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(action_refresh);
		manager.add(new Separator());		
		manager.add(action_used_list);
		manager.add(new Separator());
		manager.add(action_flat);
		manager.add(action_hier);
		manager.add(new Separator());
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(action_refresh);
		manager.add(new Separator());
		manager.add(action_used_list);
		manager.add(new Separator());
		manager.add(action_flat);
		manager.add(action_hier);
		manager.add(new Separator());
	}

	private void makeActions() {
		/*刷新按钮*/
		action_refresh = new Action("Refresh") {
			public void run() {
				changeShowType(getShowType());
			}
		};
		action_refresh.setToolTipText("Refresh View");
		action_refresh.setImageDescriptor(ImageDescriptor.createFromImage(ImageManager.getImage(ImageManager.IMG_REFRESH)));

		/*是否显示被引用模块列表*/
		action_used_list = new Action("Show Used Module List",Action.AS_CHECK_BOX) {
			public void run() {
				showUsedList(!isShowUsedList());
			}
		};
		action_used_list.setToolTipText("Show Used Module List");
		action_used_list.setImageDescriptor(ImageDescriptor.createFromImage(ImageManager.getImage(ImageManager.IMG_USED_LIST)));
		action_used_list.setChecked(isShowUsedList());

		/*单层展示按钮*/
		action_flat = new Action("Flat",Action.AS_RADIO_BUTTON) {
			public void run() {
				changeShowType(SHOW_TYPE_FLAT);
			}
		};
		action_flat.setToolTipText("Flat Show");
		action_flat.setImageDescriptor(ImageDescriptor.createFromImage(ImageManager.getImage(ImageManager.IMG_FLAT_LAYOUT)));

		/*多层展示按钮*/
		action_hier = new Action("Hierarchical",Action.AS_RADIO_BUTTON) {
			public void run() {
				changeShowType(SHOW_TYPE_HIER);
			}
		};
		action_hier.setToolTipText("Hierarchical Show");
		action_hier.setImageDescriptor(ImageDescriptor.createFromImage(ImageManager.getImage(ImageManager.IMG_HIERARCHICAL_LAYOUT)));

		if(SHOW_TYPE_FLAT.equalsIgnoreCase(getShowType())){
			action_flat.setChecked(true);
		}else{
			action_hier.setChecked(true);
		}
	}

	private String getShowType(){
		return PreferenceUtil.getPluginPreferenceStore().getString(PreferenceConstants.MODULE_VIEW_SHOW_TYPE);
	}

	private void changeShowType(String type){
		//异步更新
		if(currentFile!=null){
			try {
				PreferenceUtil.getPluginPreferenceStore().setValue(PreferenceConstants.MODULE_VIEW_SHOW_TYPE,type);
				if(currentFilePath!=null){
					loadModuleRequires(currentFilePath, currentFile.getProject(),true);
				}else{
					loadModuleRequires(currentFile,true);
				}
			} catch (Exception e) {
				//e.printStackTrace();
			}
		}
	}

	private Boolean isShowUsedList(){
		return PreferenceUtil.getPluginPreferenceStore().getBoolean(PreferenceConstants.MODULE_VIEW_SHOW_USED_LIST);
	}

	private void showUsedList(boolean isShow){
		try {
			PreferenceUtil.getPluginPreferenceStore().setValue(PreferenceConstants.MODULE_VIEW_SHOW_USED_LIST,isShow);
			changeShowType(getShowType());
		} catch (Exception e) {
			//e.printStackTrace();
		}
	}

	private void openSelectModule(){
		ISelection selection = viewer.getSelection();
		Object obj = ((IStructuredSelection)selection).getFirstElement();
		if(TreeNode.class.isInstance(obj)){
			TreeNode  node = (TreeNode)obj;
			Object o = node.getObject();
			if(Module.class.isInstance(o)){
				final String filePath = ((Module)o).getFilePath();

				if(filePath==null){
					IWorkbenchPage page =
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					MessageDialog.openWarning(page.getWorkbenchWindow().getShell(), "Warning", "File not found!");
					return;
				}

				Display.getDefault().asyncExec(new Runnable() {
					public void run() {

						IWorkbenchPage page =
							PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

						File fileToOpen = new File(filePath);

						if (fileToOpen.exists() && fileToOpen.isFile()) {
							IFileStore fileStore = EFS.getLocalFileSystem().getStore(fileToOpen.toURI());
							try {									
								IDE.openEditorOnFileStore(page, fileStore);
							} catch (Exception e ) {
								//e.printStackTrace();
							}
						} else {
							MessageDialog.openWarning(page.getWorkbenchWindow().getShell(), "Warning", "File not exist!");
						}

					}
				});
			}

		}
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				openSelectModule();
			}
		});
	}
	private void showMessage(String message) {
		MessageDialog.openInformation(
				viewer.getControl().getShell(),
				"Module View",
				message);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}

}