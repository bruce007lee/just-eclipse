package com.alibaba.just.ui.dialogs;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.FilteredItemsSelectionDialog;

import com.alibaba.just.api.bean.Module;
import com.alibaba.just.api.parser.ModuleParser;
import com.alibaba.just.api.parser.ParserUtil;
import com.alibaba.just.ui.util.ImageManager;
import com.alibaba.just.ui.util.PluginResourceUtil;
import com.alibaba.just.ui.viewmodel.ModuleVO;
import com.alibaba.just.ui.viewmodel.ViewItem;

public class ModuleSelectionDialog extends FilteredItemsSelectionDialog {

	public static final int TYPE_PACKAGE = 2;
	public static final int TYPE_ALIAS = 1;
	public static final int TYPE_ORI = 0;

	private static final int DAILOG_ID_ADD_SELECTED_MOD = 10;
	private static final int DAILOG_ID_ADD_TEXT_MOD = 11;
	private static final String DIALOG_HEIGHT = "DIALOG_HEIGHT"; //$NON-NLS-1$
	private static final String DIALOG_WIDTH = "DIALOG_WIDTH"; //$NON-NLS-1$
	private static final String DIALOG_SETTINGS = "FilteredResourcesSelectionDialogExampleSettings";
	private static final String DLG_TITLE = "Select Module";

	private static final String SEP = " - ";

	protected Button addSelectedModBtn = null;
	protected Button addTextModBtn = null;


	private IProject project = null;
	private String packagePath = null;


	public ModuleSelectionDialog(Shell shell) {
		super(shell);
	}

	public ModuleSelectionDialog(Shell shell,IProject project) {
		super(shell);
		this.project = project;
	}

	protected IDialogSettings getDialogBoundsSettings() {
		IDialogSettings  section = super.getDialogBoundsSettings();
		if(section!=null){
			section.put(DIALOG_HEIGHT, 500);
			section.put(DIALOG_WIDTH, 700);
		}
		return section;
	}

	protected void updateButtonsEnableState(IStatus status) {
		super.updateButtonsEnableState(status);
		Button btn = getSelectedModBtn();
		if (btn != null && !btn.isDisposed()) {
			btn.setEnabled(!status.matches(IStatus.ERROR));
		}
	}

	public Object getSelectedResult(){
		this.computeResult();
		return this.getFirstResult();
	}

	/**
	 * 
	 * @return
	 */
	public String getFirstResultString(){
		Object obj = this.getFirstResult();
		if(ModuleVO.class.isInstance(obj)){
			ModuleVO vo = (ModuleVO)obj;
			if(vo.isUseAlias()){
				return vo.getCurrentAlias();
			}else{
				return vo.getName();
			}
		}else if(Module.class.isInstance(obj)){
			return ((Module)obj).getName();
		}else if(obj!=null){
			return obj.toString();
		}	    
		return null;
	}

	public Object getFirstResult() {
		Object obj = super.getFirstResult();
		//convert to module
		if(ViewItem.class.isInstance(obj)){
			ViewItem vi = (ViewItem)obj;
			obj = vi.getObj();
			if(Module.class.isInstance(obj)){
				ModuleVO  vo = convertModule((Module)obj);
				if(ParserUtil.isMatchAlias(vi.getLabel(), vo)){
					vo.setUseAlias(true);
					vo.setCurrentAlias(vi.getLabel());
				}
				obj = vo;
			}
		}else if(Module.class.isInstance(obj)){
			obj = convertModule((Module)obj);
		}
		return obj;
	}

	private ModuleVO convertModule(Module m){
		ModuleVO vo = new ModuleVO();
		vo.setName(m.getName());
		vo.setAlias(m.getAlias());
		vo.setFilePath(m.getFilePath());
		vo.setAnonymous(m.isAnonymous());
		vo.setRequiredModuleNames(m.getRequiredModuleNames());
		return  vo;
	}

	protected Control createDialogArea(Composite parent){
		this.getShell().setText(DLG_TITLE);
		Control control = super.createDialogArea(parent);

		setListLabelProvider(new StyledLabelProvider());

		setDetailsLabelProvider(new ILabelProvider(){
			public void addListener(ILabelProviderListener listener) {
			}

			public void dispose() {
			}

			public boolean isLabelProperty(Object element, String property) {
				return false;
			}

			public void removeListener(ILabelProviderListener listener) {
			}

			public Image getImage(Object element) {
				return null;
			}

			public String getText(Object element) {
				if(Module.class.isInstance(element)){
					return((Module)element).getFilePath();
				}else if(ViewItem.class.isInstance(element)){
					ViewItem vi = (ViewItem)element;
					if(Module.class.isInstance(vi.getObj())){
						return ((Module)vi.getObj()).getFilePath();
					}
				}
				return null;
			}

		});

		return control;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#createExtendedContentArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createExtendedContentArea(Composite parent) {
		// TODO Auto-generated method stub
		return null;
	}



	protected void createButtonsForButtonBar(Composite parent) {
		addSelectedModBtn = this.createButton(parent,DAILOG_ID_ADD_SELECTED_MOD, "Add Selected", false);
		addTextModBtn = this.createButton(parent, DAILOG_ID_ADD_TEXT_MOD, "Add Text", false);
		super.createButtonsForButtonBar(parent);
		this.getButton(IDialogConstants.CANCEL_ID).setText(IDialogConstants.CLOSE_LABEL);
	}

	public Button getSelectedModBtn(){
		return this.addSelectedModBtn;
	}

	public Button getTextModBtn(){
		return this.addTextModBtn;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#getDialogSettings()
	 */
	protected IDialogSettings getDialogSettings() {
		DialogSettings settings = new DialogSettings(DIALOG_SETTINGS);
		return settings;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#validateItem(java.lang.Object)
	 */
	protected IStatus validateItem(Object item) {
		// TODO Auto-generated method stub
		return Status.OK_STATUS;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#createFilter()
	 */
	protected ItemsFilter createFilter() {
		return new ItemsFilter() {
			public boolean matchItem(Object item) {
				if(Module.class.isInstance(item)){
					return ((Module)item).getName()!=null && matches(((Module)item).getName());
				}else if(ViewItem.class.isInstance(item)){
					return((ViewItem)item).getLabel()!=null && matches(((ViewItem)item).getLabel());
				}	
				return false;
			}
			public boolean isConsistentItem(Object item) {
				return true;
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#getItemsComparator()
	 */
	protected Comparator getItemsComparator() {
		return new Comparator() {
			public int compare(Object arg0, Object arg1) {
				if(ViewItem.class.isInstance(arg0)){
					if(!ViewItem.class.isInstance(arg1)){
						return -1;
					}else if(arg1!=null && ((ViewItem)arg0).getType() > ((ViewItem)arg1).getType()){
						return -1;
					}
				}

				if(ViewItem.class.isInstance(arg1)){
					if(!ViewItem.class.isInstance(arg0)){
						return 1;
					}else if(arg1!=null && ((ViewItem)arg1).getType() > ((ViewItem)arg0).getType()){
						return 1;
					}
				}

				if(ViewItem.class.isInstance(arg0)){
					arg0 = ((ViewItem)arg0).getLabel();

				}else if(Module.class.isInstance(arg0)){
					arg0 =  ((Module)arg0).getName();
				}
				if(ViewItem.class.isInstance(arg1)){
					arg1 = ((ViewItem)arg1).getLabel();

				}else if(Module.class.isInstance(arg1)){
					arg1 =  ((Module)arg1).getName();
				}
				if(String.class.isInstance(arg0) && String.class.isInstance(arg1)){
					return ((String)arg0).compareTo((String)arg1);
				}else{
					return 0;
				}
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#fillContentProvider(org.eclipse.ui.dialogs.FilteredItemsSelectionDialog.AbstractContentProvider, org.eclipse.ui.dialogs.FilteredItemsSelectionDialog.ItemsFilter, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void fillContentProvider(AbstractContentProvider contentProvider,
			ItemsFilter itemsFilter, IProgressMonitor progressMonitor)
	throws CoreException { 
		if(this.project==null){
			IWorkspaceRoot  wRoot = ResourcesPlugin.getWorkspace().getRoot();
			IProject[] projects = wRoot.getProjects();
			for(IProject p:projects){
				fillProjectContentProvider(p,contentProvider,itemsFilter,  progressMonitor);
			}
		}else{
			fillProjectContentProvider( this.project,contentProvider,itemsFilter,  progressMonitor);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#fillContentProvider(org.eclipse.ui.dialogs.FilteredItemsSelectionDialog.AbstractContentProvider, org.eclipse.ui.dialogs.FilteredItemsSelectionDialog.ItemsFilter, org.eclipse.core.runtime.IProgressMonitor)
	 */
	private void fillProjectContentProvider(IProject project,AbstractContentProvider contentProvider,
			ItemsFilter itemsFilter, IProgressMonitor progressMonitor)
	throws CoreException { 

		if(project!=null){
			ViewItem vi = null;

			//current package path
			if(packagePath!=null){
				vi = new ViewItem(packagePath,packagePath);
				vi.setIconName(ImageManager.IMG_PACKAGE_OBJ);
				vi.setType(TYPE_PACKAGE);
				contentProvider.add(vi, itemsFilter);
			}

			// Populate libs
			try {
				List<Module> moduleList = PluginResourceUtil.getAllModulesByProject(project,ModuleParser.MODULE_TYPE_NORMAL,progressMonitor);

				Module tmp = null;
				List<String> aliasList = null;
				for (Iterator<Module> iter = moduleList.iterator(); iter.hasNext();) {
					tmp = iter.next();
					if(tmp.hasAlias()){
						aliasList = tmp.getAlias();
						for(String alias:aliasList){
							vi = new ViewItem(tmp,alias);
							vi.setIconName(ImageManager.IMG_ALIAS_MODULE_ICON);
							vi.setType(TYPE_ALIAS);
							contentProvider.add(vi, itemsFilter);
						}
					}
					contentProvider.add(tmp, itemsFilter);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#getElementName(java.lang.Object)
	 */
	public String getElementName(Object item) {
		if(Module.class.isInstance(item)){
			return ((Module)item).getName();
		}else if(ViewItem.class.isInstance(item)){
			return((ViewItem)item).getLabel();
		}
		return null;
	}

	public IProject getProject() {
		return project;
	}

	public void setProject(IProject project) {
		this.project = project;
	}

	public boolean close() {
		boolean rs = super.close();
		if(rs){//clear all
			addSelectedModBtn = null;
			addTextModBtn=null;
			project=null;
			packagePath = null;
		}
		return rs;
	}

	private class StyledLabelProvider implements IStyledLabelProvider,ILabelProvider{

		public Image getImage(Object element) {
			if(ViewItem.class.isInstance(element)&& ((ViewItem)element).getIconName()!=null){
				return ImageManager.getImage(((ViewItem)element).getIconName());
			}else{
				return ImageManager.getImage(ImageManager.IMG_MODULE_ICON);
			}
		}

		public String getText(Object element) {
			if(Module.class.isInstance(element)){
				return((Module)element).getName();
			}else if(ViewItem.class.isInstance(element)){
				return((ViewItem)element).getLabel();
			}
			return null;
		}

		public void addListener(ILabelProviderListener listener) {
			// TODO Auto-generated method stub

		}

		public void dispose() {
			// TODO Auto-generated method stub

		}

		public boolean isLabelProperty(Object element, String property) {
			// TODO Auto-generated method stub
			return false;
		}

		public void removeListener(ILabelProviderListener listener) {
			// TODO Auto-generated method stub

		}

		public StyledString getStyledText(Object element) {
			if(Module.class.isInstance(element)){
				String name = ((Module)element).getName();
				String path = ((Module)element).getFilePath();
				StyledString ss = new StyledString(name);
				if(path!=null){
					ss.append(new StyledString(SEP + path,StyledString.QUALIFIER_STYLER));
				}
				return ss;
			}if(ViewItem.class.isInstance(element)){				
				ViewItem vi = (ViewItem)element;
				if(Module.class.isInstance(vi.getObj())){
					Module m = (Module)vi.getObj();
					String name = vi.getLabel();
					String path = m.getFilePath();
					StyledString ss = new StyledString(name);
					if(path!=null){
						ss.append(new StyledString(SEP + path,StyledString.QUALIFIER_STYLER));
					}
					return ss;					
				}else{
					return new StyledString(vi.getLabel());
				}

			}					
			return new StyledString();
		}

	}

	public String getPackagePath() {
		return packagePath;
	}

	public void setPackagePath(String packagePath) {
		this.packagePath = packagePath;
	}

}
