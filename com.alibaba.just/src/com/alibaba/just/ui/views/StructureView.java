package com.alibaba.just.ui.views;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.ViewPart;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NodeVisitor;
import org.mozilla.javascript.ast.PropertyGet;

import com.alibaba.just.Activator;
import com.alibaba.just.PluginConstants;
import com.alibaba.just.api.bean.Module;
import com.alibaba.just.ui.util.ImageManager;
import com.alibaba.just.ui.util.PluginResourceUtil;
import com.alibaba.just.util.FileUtil;

public class StructureView extends ViewPart {

	private TreeViewer viewer;
	private ViewContentProvider vcp;
	private IPartListener partListener = null;
	private IResourceChangeListener resourceChangeListener = null;
	private IFile currentFile = null;
	private String currentFilePath = null;
	private long currentFileTimeStamp = 0L;
	private Thread  runner;
	private boolean isDispose = false;

	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		//drillDownAdapter = new DrillDownAdapter(viewer);
		vcp = new ViewContentProvider();
		viewer.setContentProvider(vcp);
		viewer.setLabelProvider(new TreeLabelProvider());
		viewer.setInput(getViewSite());

		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "com.alibaba.just.viewer");

		initListener();
	}

	/**
	 * 
	 * @param part
	 */
	private void updateView(IWorkbenchPart part){
		try{
			if(part instanceof IEditorPart){
				IEditorInput input = ((IEditorPart)part).getEditorInput();
				if(input instanceof FileEditorInput){
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
					createStructureTree(ff);
				}else if(input instanceof FileStoreEditorInput){
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
						}
					}
				}
			}
			/*else if(! part istanceof ModuleView){
				clearView(true);
			}*/
		}catch(Exception e){
			//throw new InvocationTargetException(e);
			//e.printStackTrace();
		}

	}

	private void createStructureTree(final IFile file){
		if(isDispose){
			runner = null;
			return;
		}

		runner = new Thread(){

			/*
			 * (non-Javadoc)
			 * @see java.lang.Thread#run()
			 */
			public void run(){
				try {
					Parser  parser = new Parser();
					AstRoot astRoot;
					astRoot = parser.parse(FileUtil.getFileContent(file.getContents(), file.getCharset()), null, 0);
					final TreeNode tNode = new TreeNode(astRoot);
					clearView(false);
					astRoot.visit(new JSNodeVisitor(tNode));
					//同步ui显示
					Display.getDefault().syncExec(new Runnable(){
						public void run() {
							vcp.invisibleRoot.addChildren(tNode.getChildren());
							tNode.dispose(false);
							viewer.refresh();
							viewer.getTree().setRedraw(false);
							try {
								viewer.expandToLevel(2);
							} finally {
								viewer.getTree().setRedraw(true); 
							}
						}								
					});

				}catch (Exception e) {
					e.printStackTrace();
					//showException(e);					
				} 
			}
		};

		runner.start();
	}

	public void clearView(boolean isRefresh){
		vcp.invisibleRoot.removeChildren();//clear
		if(isRefresh){
			viewer.refresh();
		}
	}

	class JSNodeVisitor implements  NodeVisitor{
		private TreeNode treeNode;
		private TreeNode parentTreeNode;

		JSNodeVisitor(TreeNode parentTreeNode){
			this.parentTreeNode = parentTreeNode;
		}

		public boolean visit(AstNode node) {
			AstNode p = (AstNode)parentTreeNode.getObject();
			if(p.depth()+1 == node.depth()){
				//System.out.println(node.depth()+" "+node.shortName());
				treeNode = new TreeNode(node);
				node.visit(new JSNodeVisitor(treeNode));
				parentTreeNode.addChild(treeNode);
				return false;
			}else if(p.depth()==node.depth()){
				return true;
			}
			return false;
		}

	}


	/**
	 * register workbenchwindow events
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

		final IResourceDeltaVisitor vistor = new IResourceDeltaVisitor(){
			public boolean visit(IResourceDelta delta)
			throws CoreException {
				//System.out.println(delta +" "+ delta.getKind());
				if( (delta.getKind()==IResourceDelta.CHANGED) 
						&& delta.getResource()!=null
						&& IResource.FILE==delta.getResource().getType()
						&& PluginConstants.JAVASCRIPT_EXT.equalsIgnoreCase(delta.getResource().getFileExtension()) ){
					//System.out.println("remove cache file:"+ PluginResourceUtil.getResourceCacheKey( delta.getResource()));
					IWorkbenchWindow win = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					if(win!=null){
						IWorkbenchPage page  = win.getActivePage();
						if(page!=null){
							updateView(page.getActivePart());
						}
					}					
				}
				return true;
			}			
		};

		resourceChangeListener = new IResourceChangeListener(){
			public void resourceChanged(IResourceChangeEvent event) {
				try {
					event.getDelta().accept(vistor);
				} catch (CoreException e) {
					e.printStackTrace();
				}
				//System.out.println("type:"+event.getType()+" resource:"+event.getResource()+" source:"+event.getSource());
			}

		};

		Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getPartService().addPartListener(partListener);
		PluginResourceUtil.getWorkspace().addResourceChangeListener(resourceChangeListener,IResourceChangeEvent.POST_CHANGE);
	}


	public void setFocus() {


	}

	/**
	 * Fix 如：define(['jquery'],function($){}).register();之类的用法
	 * TODO：以后去掉对非AMD标准匿名模块的支持
	 * @param funcall
	 * @return
	 */
	private FunctionCall getFirstFunctionCall(FunctionCall funcall){
		AstNode target = funcall.getTarget();
		if(target instanceof PropertyGet){
			AstNode left = ((PropertyGet)target).getLeft();
			if(left instanceof FunctionCall){
				return getFirstFunctionCall((FunctionCall)left);
			}else{
				return funcall;
			}
		}		
		return funcall;
	}

	/*
	 * tree node
	 */
	class TreeNode implements IAdaptable {
		private Object obj;
		private String iconName;
		private int type = 1;
		private String desc;
		private String name=null;

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

		public void setName(String name){
			this.name = name;
		}

		public String getName() {
			if(obj instanceof Name){
				Name na = (Name)obj;
				return na.toSource();
			}
			if(obj instanceof AstNode){
				return "<"+((AstNode)obj).shortName()+">";
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

		public void addChildren(TreeNode[] children) {
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
		public int getType() {
			return type;
		}
		public void setType(int type) {
			this.type = type;
		}

	}

	class TreeLabelProvider extends StyledCellLabelProvider {
		public void update(ViewerCell cell) {
			Object obj = cell.getElement();
			TreeNode node = null;
			Module m = null;
			if(obj instanceof TreeNode){				
				StyledString text = new StyledString();				
				node = (TreeNode)obj;
				if(node.getObject() instanceof Module){
					m = (Module)node.getObject();
				}

				cell.setImage(ImageManager.getImage(node.getIconName()));

				text.append(node.getName());

				cell.setStyleRanges(text.getStyleRanges());
				cell.setText(text.toString());
				super.update(cell);
			}
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

}
