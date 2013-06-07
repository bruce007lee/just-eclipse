package com.alibaba.just.ui.dialogs;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
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
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.FilteredItemsSelectionDialog;

import com.alibaba.just.api.bean.Module;
import com.alibaba.just.api.parser.ModuleParser;
import com.alibaba.just.ui.util.ImageManager;
import com.alibaba.just.ui.util.PluginResourceUtil;
import com.alibaba.just.ui.util.PreferenceUtil;

public class ModuleSelectionDialog extends FilteredItemsSelectionDialog {

	private static final int DAILOG_ID_ADD_SELECTED_MOD = 10;
	private static final int DAILOG_ID_ADD_TEXT_MOD = 11;
	private static final String DIALOG_HEIGHT = "DIALOG_HEIGHT"; //$NON-NLS-1$
	private static final String DIALOG_WIDTH = "DIALOG_WIDTH"; //$NON-NLS-1$
	private static final String DIALOG_SETTINGS = "FilteredResourcesSelectionDialogExampleSettings";
	private static final String SEARCH_LABEL = "Searching"; //$NON-NLS-1$
	
	private static final String SEP = " - ";

	private Button addSelectedModBtn = null;
	private Button addTextModBtn = null;


	private IProject project = null;


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

	protected Control createDialogArea(Composite parent){
		this.getShell().setText("Select Module");
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
					return matches(((Module)item).getName());
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
				if(Module.class.isInstance(arg0) && Module.class.isInstance(arg1)){
					return ((Module)arg0).getName().compareTo(((Module)arg1).getName());
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

		if(project!=null){
			// Populate libs
			try {
				List<String> libs = PreferenceUtil.getProjectLibsList(project);					
				List<Module> moduleList = new ArrayList<Module>();
				ModuleParser parser = new ModuleParser(PreferenceUtil.getFileCharset());
				IWorkspaceRoot  wRoot = ResourcesPlugin.getWorkspace().getRoot();
				progressMonitor.beginTask(SEARCH_LABEL, libs.size()); //$NON-NLS-1$
				for(String lib:libs){
					progressMonitor.worked(1);
					String lb = lib.trim();
					if(lb.length()>0){
						String folderPath = null;
						String type = PreferenceUtil.getProjectLibType(lb);
						if(PreferenceUtil.LIB_TYPE_WORKSPACE_FOLDER.equals(type) ||
								PreferenceUtil.LIB_TYPE_SELF.equals(type)){
							/*如果是workspace里的目录*/
							if(PreferenceUtil.LIB_TYPE_SELF.equals(type)){
							   lb = project.getFullPath().toString();
							}else{
							   lb = PreferenceUtil.getProjectLibPath(lb);
							}
							progressMonitor.setTaskName(SEARCH_LABEL+"["+lb+"]");
							IPath rootPath = wRoot.getFullPath();
							IResource res = wRoot.findMember(rootPath.append(lb));
							if(res!=null && res.isAccessible()){
								PluginResourceUtil.getModulesByResource(res,moduleList,parser);
							}
						}else if(PreferenceUtil.LIB_TYPE_EXTERNAL_FOLDER.equals(type)){
							/*如果是workspace外的目录*/
							lb = PreferenceUtil.getProjectLibPath(lb);
							progressMonitor.setTaskName(SEARCH_LABEL+"["+lb+"]");
							File f = new File(lb);
							if(f.exists() && f.isDirectory()){
								folderPath = f.getAbsolutePath();
								moduleList.addAll(parser.getAllModules(folderPath));
							}								
						}
					}
				}
				for (Iterator<Module> iter = moduleList.iterator(); iter.hasNext();) {
					contentProvider.add(iter.next(), itemsFilter);
				}
				progressMonitor.done();

			} catch (Exception e) {
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
		}
		return rs;
	}
	
	private class StyledLabelProvider implements IStyledLabelProvider,ILabelProvider{
		
		public Image getImage(Object element) {
			return ImageManager.getImage(ImageManager.IMG_MODULE_ICON);
		}

		public String getText(Object element) {
			if(Module.class.isInstance(element)){
				return((Module)element).getName();
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
			}				
			return new StyledString();
		}

	}

}
