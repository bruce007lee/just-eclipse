package com.alibaba.just.ui.popup.actions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.alibaba.just.PluginConstants;
import com.alibaba.just.api.bean.Module;
import com.alibaba.just.api.parser.ModuleParser;
import com.alibaba.just.ui.util.PreferenceUtil;
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
		if(!IStructuredSelection.class.isInstance(selection)){return;}		
		IStructuredSelection sel = (IStructuredSelection)selection;
		Object obj = sel.getFirstElement();		
		if(!IFile.class.isInstance(obj)){return;}

		Shell shell = UIUtil.getShell();
		try {
			IFile ifile = (IFile) obj;
			String path = ifile.getLocation().toFile().getAbsolutePath();
			if(!VALID_MODULE_EXT.equals(ifile.getFileExtension())){
				return;
			}

			ModuleParser parser = new ModuleParser(PreferenceUtil.getFileCharset());
			Module module = parser.getModule(path,ModuleParser.MODULE_TYPE_ALL);

			if(module==null){
				MessageDialog.openInformation(
						shell,
						"Warnning",
				"Invalid module file!");
				return;
			}

			List<Module> allModule = new ArrayList<Module>();


			String libsStr = ifile.getProject().getPersistentProperty(
					new QualifiedName(PluginConstants.QUALIFIED_NAME, PluginConstants.LIBS_PROPERTY_KEY));

			if(libsStr!=null){

				String[] libs = libsStr.split("[\n]");
				for(String lib:libs){
					String lb = lib.trim();
					if(lb.length()>0){

						String folderPath = null;

						if(lb.indexOf("@")==0){
							File f = null;								
							IPath ipath = ResourcesPlugin.getWorkspace().getRoot().getLocation();								
							if(ipath!=null){
								f = ipath.toFile();
							}

							f = new File(f.getAbsolutePath()+lb.substring(1));
							if(f.exists() && f.isDirectory()){
								folderPath = f.getAbsolutePath();
							}

						}else{

							File f = new File(lb);
							if(f.exists() && f.isDirectory()){
								folderPath = f.getAbsolutePath();
							}
						}

						if(folderPath==null){
							continue;
						}

						allModule.addAll(parser.getAllModules(folderPath));

					}
				}

				List<Module> requires =  parser.getAllRequiredModules(module, allModule);

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
			}

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
