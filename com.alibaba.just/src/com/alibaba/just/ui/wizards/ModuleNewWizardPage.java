package com.alibaba.just.ui.wizards;

import java.util.ArrayList;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

import com.alibaba.just.api.bean.Module;
import com.alibaba.just.ui.dialogs.ModuleSelectionDialog;
import com.alibaba.just.ui.util.PreferenceUtil;
import com.alibaba.just.ui.viewmodel.ModuleVO;

/**
 * The "New" wizard page allows setting the container for the new file as well
 * as the file name. The page will only accept file name without the extension
 * OR with the extension that matches the expected one (js).
 */

public class ModuleNewWizardPage extends WizardPage {

	private ModuleSelectionDialog moduleSelectionDialog = null;

	private Text containerText;

	private Text fileText;

	private Button isAnonymousCb;

	private Button isCreateModulePathCb;

	private List selectedModuleList;

	private ISelection selection;

	private IProject project;

	private String currentRootContainerPath="";

	private String currentContainerPath="";


	/**
	 * Constructor for SampleNewWizardPage.
	 * 
	 * @param pageName
	 */
	public ModuleNewWizardPage(ISelection selection) {
		super("Just Module Wizard");
		setTitle("Just Module File");
		setDescription("Create a new module that can be opened by a just module editor.");
		this.selection = selection;
	}

	public void dispose() {
		containerText=null;
		fileText=null;
		isAnonymousCb= null;
		isCreateModulePathCb=null;
		moduleSelectionDialog=null;	
		selectedModuleList=null;
		selection = null;
		project=null;
		super.dispose();
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 3;
		layout.verticalSpacing = 9;
		Label label = new Label(container, SWT.NULL);
		label.setText("&Root Path:");

		containerText = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		containerText.setLayoutData(gd);
		containerText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});

		Button button = new Button(container, SWT.PUSH);
		button.setText("Browse...");
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleBrowse();
			}
		});
		label = new Label(container, SWT.NULL);
		label.setText("&Module name:");

		fileText = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		fileText.setLayoutData(gd);
		fileText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});

		//是否按模块名创建路径
		Button cb = new Button(container, SWT.CHECK);
		isCreateModulePathCb = cb;
		isCreateModulePathCb.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleCreateModulePathChange();
				dialogChanged();
			}
		});
		cb.setSelection(true);
		cb.setText("Create &path by module name");

		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		cb.setLayoutData(gd);

		//是否是匿名模块
		cb = new Button(container, SWT.CHECK);
		isAnonymousCb = cb;
		cb.setText("&Anonymous");

		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		cb.setLayoutData(gd);


		//添加模块
		label = new Label(container, SWT.NULL);
		gd = new GridData(GridData.BEGINNING);
		gd.verticalAlignment=5;
		label.setLayoutData(gd);
		label.setText("Import modules:");

		selectedModuleList  = new List(container, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.heightHint=260;
		selectedModuleList.setLayoutData(gd);


		Composite btnbar = new Composite(container, SWT.NULL);
		gd = new GridData(GridData.END);
		btnbar.setLayoutData(gd);

		btnbar.setLayout(new GridLayout());

		//btnbar.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_GRAY));

		button = new Button(btnbar, SWT.NULL);
		button.setText("Add...");
		gd = new GridData(GridData.FILL_HORIZONTAL);
		button.setLayoutData(gd);
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleShowModuleSelectionDlg();
			}
		});

		button = new Button(btnbar, SWT.NULL);
		button.setText("Delete");
		gd = new GridData(GridData.FILL_HORIZONTAL);
		button.setLayoutData(gd);
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleModuleRemove();
			}
		});

		button = new Button(btnbar, SWT.NULL);
		button.setText("Up");
		gd = new GridData(GridData.FILL_HORIZONTAL);
		button.setLayoutData(gd);
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleModuleMove(-1);
			}
		});

		button = new Button(btnbar, SWT.NULL);
		button.setText("Down");
		gd = new GridData(GridData.FILL_HORIZONTAL);
		button.setLayoutData(gd);
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleModuleMove(1);
			}
		});


		initialize();
		dialogChanged();
		setControl(container);
	}

	private void handleCreateModulePathChange(){
		boolean isSelect = isCreateModulePathCb.getSelection();
		if(isSelect){
			this.containerText.setText(this.currentRootContainerPath);
		}else{
			this.containerText.setText(this.currentContainerPath);
		}
	}

	private void handleModuleRemove(){
		String[] selects = selectedModuleList.getSelection();
		if(selects!=null && selects.length>0){
			for(String item:selects){
				selectedModuleList.remove(item);
			}
		}

	}

	private void handleModuleMove(int move){
		String[] selects = selectedModuleList.getSelection();

		if(selects!=null && selects.length>0){
			String[] items = selectedModuleList.getItems();
			int currentIdx = -1;
			for(int i=0,l=items.length;i<l;i++){
				if(items[i].equalsIgnoreCase(selects[0])){
					currentIdx = i;
					break;
				}
			}

			if(currentIdx>-1){

				int to = currentIdx+move;
				int newIdx = -1;
				String str = selects[0];
				selectedModuleList.remove(selects[0]);
				selectedModuleList.deselectAll();
				if(to<0){
					newIdx = 0;
					selectedModuleList.add(str, newIdx);
				}else if(to>items.length-1){
					newIdx = selectedModuleList.getItems().length;
					selectedModuleList.add(str, newIdx);
				}else{
					newIdx = to;
					selectedModuleList.add(str,newIdx);
				}

				selectedModuleList.select(newIdx);
			}

		}
	}

	/**
	 * 
	 */
	private void handleShowModuleSelectionDlg(){
		moduleSelectionDialog = new ModuleSelectionDialog(this.getShell()){
			protected void okPressed() {
				this.computeResult();
				Object rs  = moduleSelectionDialog.getFirstResult();
				//System.out.println(rs);
				if(Module.class.isInstance(rs)){
					Module m = (Module)rs;
					String t = m.getName();
					if(!isExist(selectedModuleList,t)){
						boolean isAdd = false;
						if(ModuleVO.class.isInstance(rs)){
							if(((ModuleVO)rs).isUseAlias() && m.hasAlias()){
								isAdd = true;
								selectedModuleList.add(((ModuleVO)rs).getCurrentAlias());
							}
						}
						if(!isAdd){
							selectedModuleList.add(t);
						}
					}
				}

				super.okPressed();
			}

			/**
			 * close and clear
			 */
			public boolean close() {
				boolean rs = super.close();
				moduleSelectionDialog = null;//clear
				return rs;
			}

		};
		moduleSelectionDialog.setProject(project);

		moduleSelectionDialog.create();
		moduleSelectionDialog.getSelectedModBtn().addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				Object rs  = moduleSelectionDialog.getSelectedResult();
				//System.out.println(rs);
				if(Module.class.isInstance(rs)){
					Module m = (Module)rs;
					String t = m.getName();
					if(!isExist(selectedModuleList,t)){
						boolean isAdd = false;
						if(ModuleVO.class.isInstance(rs)){
							if(((ModuleVO)rs).isUseAlias() && m.hasAlias()){
								isAdd = true;
								selectedModuleList.add(((ModuleVO)rs).getCurrentAlias());
							}
						}
						if(!isAdd){
							selectedModuleList.add(t);
						}
					}
				}
			}
		});

		moduleSelectionDialog.getTextModBtn().addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				Control c = moduleSelectionDialog.getPatternControl();
				if(Text.class.isInstance(c)){
					Text  text = (Text) c;
					String t = text.getText().trim();
					if(!isExist(selectedModuleList,t)){
						selectedModuleList.add(t);
					}
				}
			}
		});
		moduleSelectionDialog.open();

	}

	private boolean isExist(List list,String item){		
		String[] items = list.getItems();
		for(String t:items){
			if(t.equals(item)){
				return true;
			}
		}
		return false;
	}

	/**
	 * Tests if the current workbench selection is a suitable container to use.
	 */

	private void initialize() {
		String parentCt = null;
		if (selection != null && selection.isEmpty() == false
				&& selection instanceof IStructuredSelection) {
			IStructuredSelection ssel = (IStructuredSelection) selection;
			if (ssel.size() > 1)
				return;
			Object obj = ssel.getFirstElement();
			if (obj instanceof IResource) {
				IContainer container;
				if (obj instanceof IContainer)
					container = (IContainer) obj;
				else
					container = ((IResource) obj).getParent();

				this.currentContainerPath = container.getFullPath().toString();

				IContainer root = PreferenceUtil.getCurrentRoot(container);
				if(root!=null && root.getFullPath()!=null){
					String rootPath = root.getFullPath().toString();
					containerText.setText(rootPath);
					parentCt = PreferenceUtil.getRelativeRootPath(container.getFullPath().toString(),rootPath);
				}else{
					containerText.setText(container.getFullPath().toString());	
				}
				this.currentRootContainerPath = containerText.getText();
				this.project=((IResource) obj).getProject();
			}
		}

		if(parentCt!=null){
			if(parentCt.length()>0 && parentCt.indexOf('/')==0){
				parentCt = parentCt.substring(1);
			}
			fileText.setText(parentCt+(parentCt.length()>0?"/":"")+"module");
		}else{
			fileText.setText("page/module");
		}

		handleCreateModulePathChange();
	}

	/**
	 * Uses the standard container selection dialog to choose the new value for
	 * the container field.
	 */

	private void handleBrowse() {
		ContainerSelectionDialog dialog = new ContainerSelectionDialog(
				getShell(), project, false,
		"Select new file container");
		if (dialog.open() == ContainerSelectionDialog.OK) {
			Object[] result = dialog.getResult();
			if (result.length == 1) {
				containerText.setText(((Path) result[0]).toString());
			}
		}
	}

	/**
	 * Ensures that both text fields are set.
	 */

	private void dialogChanged() {
		IResource container = ResourcesPlugin.getWorkspace().getRoot()
		.findMember(new Path(getContainerName()));
		//String fileName = getFileName();

		/*if (getContainerName().length() == 0) {
			updateStatus("File container must be specified");
			return;
		}
		if (container == null
				|| (container.getType() & (IResource.PROJECT | IResource.FOLDER)) == 0) {
			updateStatus("File container must exist");
			return;
		}*/
		if (!container.isAccessible()) {
			updateStatus("Project must be writable");
			return;
		}
		/*if (fileName.length() == 0) {
			updateStatus("File name must be specified");
			return;
		}
		if (fileName.replace('\\', '/').indexOf('/', 1) > 0) {
			updateStatus("File name must be valid");
			return;
		}*/
		/*int dotLoc = fileName.lastIndexOf('.');
		if (dotLoc != -1) {
			String ext = fileName.substring(dotLoc + 1);
			if (ext.equalsIgnoreCase("js") == false) {
				updateStatus("File extension must be \"js\"");
				return;
			}
		}*/
		updateStatus(null);
	}

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	public String getContainerName() {
		return containerText.getText();
	}

	public String getFileName() {
		if(isCreateModulePathCb.getSelection()){
			return fileText.getText().trim()+".js";
		}else{
			String fileName = fileText.getText();
			String[] seg  = fileName.split("[/]");
			return seg[seg.length-1]+".js";
		}
	}

	/*------------------------------------*/
	public String getModuleName() {
		return fileText.getText();
	}

	public java.util.List<Module> getRequiredModules() {
		java.util.List<Module> list = new ArrayList<Module>();
		String[] items = selectedModuleList.getItems();
		Module module = null;
		for(String item:items){
			module = new Module();
			module.setName(item);
			list.add(module);
		}
		return list;
	}


	public IProject getProject() {
		return project;
	}

	public void setProject(IProject project) {
		this.project = project;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isAnonymous(){
		return this.isAnonymousCb.getSelection();
	}

}