package com.alibaba.just.ui.popup.actions;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.alibaba.just.api.bean.Module;
import com.alibaba.just.api.parser.ModuleParser;
import com.alibaba.just.ui.util.PluginResourceUtil;
import com.alibaba.just.ui.util.UIUtil;

/**
 * 
 * @author bruce.liz
 *
 */
public class ImportModulesAction extends ImportModulesViewAction{
	private ISelection selection=null;

	/**
	 * Constructor for Action1.
	 */
	public ImportModulesAction() {
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
		if(selection==null){return;}		
		if(!(selection instanceof IStructuredSelection)){return;}		
		IStructuredSelection sel = (IStructuredSelection)selection;
		Object obj = sel.getFirstElement();		
		if(!(obj instanceof IFile)){return;}

		Shell shell = UIUtil.getShell();
		try {
			IFile ifile = (IFile) obj;
			IPath ipath =  ifile.getLocation();
			if(ipath==null){
				return;
			}
			String path = ipath.toFile().getAbsolutePath();
			if(!VALID_MODULE_EXT.equals(ifile.getFileExtension())){
				return;
			}

			ModuleParser parser = PluginResourceUtil.getModuleParser(ifile.getProject());
			parser.setThreadPool(UIUtil.getThreadPool());
			Module module = parser.getModule(path,ModuleParser.MODULE_TYPE_ALL);

			if(module==null){
				MessageDialog.openInformation(
						shell,
						"Warnning",
				"Invalid module file!");
				return;
			}

			List<Module> moduleList = PluginResourceUtil.getAllModulesByProject(ifile.getProject());			
			List<Module> requires =  parser.getAllRequiredModules(module, moduleList);

			//add self
			requires.add(module);

			//IFile file = ifile.getParent().getFile(new Path("merge.js"));

			List<IFile>  fList = this.getMergeFileList(ifile.getParent());

			if(fList.size()==0){
				showCreateMergeFileDlg(shell,ifile.getParent(), requires);
			}else if(fList.size()==1){
				createMergeFile(fList.get(0), requires);
			}else{
				showSelectMergeFileDlg(shell,fList,requires);
			}

			//this.createMergeFile(file, requires);

		} catch (Exception e) {
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
	
	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}

}
