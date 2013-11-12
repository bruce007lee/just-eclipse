package com.alibaba.just.ui.popup.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.FileEditorInput;

import com.alibaba.just.api.bean.Module;
import com.alibaba.just.api.parser.ModuleParser;
import com.alibaba.just.ui.dialogs.ListMultiSelectDialog;
import com.alibaba.just.ui.dialogs.ListSelectDialog;
import com.alibaba.just.ui.util.PluginResourceUtil;
import com.alibaba.just.ui.util.UIUtil;
import com.alibaba.just.ui.viewmodel.ViewItem;
import com.alibaba.just.util.FileUtil;

/**
 * 
 * @author bruce.liz
 *
 */
public class ImportFolderModulesViewAction extends ImportModulesViewAction{
	protected static final String MAIN_FILE_REGEX = "/\\*((?!\\*|\\*/).*)\\*/";

	/**
	 * Constructor for Action1.
	 */
	public ImportFolderModulesViewAction() {
		super();
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {

	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		if(targetEditor==null){
			return;
		}

		IEditorInput input = targetEditor.getEditorInput();

		if(FileEditorInput.class.isInstance(input)){
			Shell shell = UIUtil.getShell();
			FileEditorInput fileInput = (FileEditorInput)input;
			try {
				IFile ifile = fileInput.getFile();

				IContainer container = ifile.getParent();
				
				if(container==null){
					return;
				}

				List<IFile> fList = this.getMergeFileList(container);

				if(fList.size()==1){
					showSelectFolderFileDlg(shell,container, fList,fList.get(0));
				}else if(fList.size()>1){
					showSelectMergeFileDlg(shell,container,fList);
				}else{
					showSelectFolderFileDlg(shell,container, fList,null);
				}
			} catch (Exception e) {
				String error = e.getMessage();
				if(error==null || error.trim().length()==0){
					error = e.toString();
				}
				MessageDialog.openInformation(
						shell,
						"Error",
						error);  
			} 
		}
	}
	
	private List<String> getMainFiles(String group){
		List<String> list = new ArrayList<String>();
		Pattern p = Pattern.compile(MAIN_FILE_REGEX,Pattern.MULTILINE);
		Matcher m = p.matcher(group);
		String tmp = null;
		while (m.find()) {
			tmp = m.group(1);
			list.add(tmp.trim());
		}
		return list;
	}

	protected List<String> getFolderModuleMain(String content){
		List<String> list = new ArrayList<String>();
		Pattern p = Pattern.compile(SELECT_FOLDER_FILE_REGEX,Pattern.MULTILINE);
		Matcher m = p.matcher(content);
		while (m.find()) {
			list.addAll(getMainFiles(m.group(1)));
		}
		return list;
	}
	
	protected void showSelectFolderFileDlg(final Shell shell,final IContainer container,final List<IFile> mergeFileList,final IFile currentMergeFile){
		try{

			List<ViewItem> list = new ArrayList<ViewItem>();
			IResource[] resources = container.members();
			IFile f = null;
			ViewItem item = null;
			List<String> mainFiles = null;
			int l = -1;
			if(currentMergeFile!=null){
			  mainFiles = getFolderModuleMain(FileUtil.getFileContent(currentMergeFile.getContents(),currentMergeFile.getCharset()));
			  l = mainFiles.size();
			}
			for(IResource res:resources){
				if(IFile.class.isInstance(res)){
					f = (IFile)res;
					if(VALID_MODULE_EXT.equalsIgnoreCase(f.getFileExtension()) && !mergeFileList.contains(f)){
						item = new ViewItem(f);
						item.setLabel(f.getName());
						if(l<=0){
						   item.setChecked(true);
						}else if(mainFiles!=null && mainFiles.contains(f.getName())){
						   item.setChecked(true);
						}
						list.add(item);
					}
				}
			}

			ListMultiSelectDialog listDlg = new ListMultiSelectDialog(shell,"Select import folder files","",list){
				/*
				 * (non-Javadoc)
				 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
				 */
				protected void okPressed() {
					try{
						Object[] checkList = this.getCheckedElements();
						List<Module> allRequires = new ArrayList<Module>();
						IProject p = container.getProject();                    
						List<Module> moduleList = PluginResourceUtil.getAllModulesByProject(p);	
						List<String> mains = new ArrayList<String>();
						for(Object obj:checkList){
							IFile f = null;
							if(ViewItem.class.isInstance(obj)){
								obj = ((ViewItem)obj).getObj();
								if(IFile.class.isInstance(obj)){
									f = (IFile)obj;
									List<Module> fm = getRequire(moduleList, p, f);
									boolean exist = false;
									for(Module m:fm){
										exist = false;
										for(Module am:allRequires){
											if(am.getName()!=null && am.getName().equals(m.getName())){
												exist = true;
												break;
											}
										}
										if(!exist){
											allRequires.add(m);
										}
									}
									mains.add(f.getName());
								}
							}
							//List<IFile>  fList = ImportFolderModulesAction.this.getMergeFileList(container);
						}

						super.okPressed();
                        if(currentMergeFile==null){
                           showCreateMergeFileDlg(shell, container, allRequires,mains);
                        }else{
						   createMergeFile(currentMergeFile, allRequires,mains);
                        }
					}catch(Exception e){
						String error = e.getMessage();
						if(error==null || error.trim().length()==0){
							error = e.toString();
						}
						MessageDialog.openInformation(
								UIUtil.getShell(),
								"Error",
								error);  
					}
				}
			};

			listDlg.open();
		}catch(Exception e){
			String error = e.getMessage();
			if(error==null || error.trim().length()==0){
				error = e.toString();
			}
			MessageDialog.openInformation(
					UIUtil.getShell(),
					"Error",
					error);  
		}
	}

	private List<Module> getRequire(List<Module> moduleList,IProject project,IFile file) throws Exception{
		String path = file.getLocation().toFile().getAbsolutePath();
		ModuleParser parser = PluginResourceUtil.getModuleParser();
		parser.setThreadPool(UIUtil.getThreadPool());
		Module module = parser.getModule(path,ModuleParser.MODULE_TYPE_ALL);
		if(module==null){
			return new ArrayList<Module>(0);
		}
		List<Module> modules = parser.getAllRequiredModules(module, moduleList);
		modules.add(module);
		return modules;
	}


	protected void showSelectMergeFileDlg(final Shell shell,final IContainer container, final List<IFile> mergeFileList){

		List<String> fileNames = new ArrayList<String>();

		for(IFile f:mergeFileList){
			fileNames.add(f.getName());
		}
		ListSelectDialog selectDlg =  new ListSelectDialog(shell,"Select merge file","Please select a merge file:",fileNames){
			/*
			 * (non-Javadoc)
			 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
			 */
			protected void okPressed() {
				String name = this.getValue();
				if(name==null){
					name="";
				}
				if(name.length()>0){
					IFile file = null;
					for(IFile f:mergeFileList){
						if(f.getName().equals(name)){
							file = f;
							break;
						}
					}

					if(file!=null){
						super.okPressed();
						showSelectFolderFileDlg(shell, container, mergeFileList, file);
					}else{
						return;
					}
				}else{
					return;
				}
			}
		};

		selectDlg.open();
	}
}
