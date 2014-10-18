package com.alibaba.just.ui.popup.actions;

import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;

import com.alibaba.just.ui.util.UIUtil;

/**
 * 
 * @author bruce.liz
 *
 */
public class ImportFolderModulesAction extends ImportFolderModulesViewAction{

	private ISelection selection=null;

	/**
	 * Constructor for Action1.
	 */
	public ImportFolderModulesAction() {
		super();
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {		
		if(selection==null){return;}		
		if(!(selection instanceof IStructuredSelection)){return;}		
		IStructuredSelection sel = (IStructuredSelection)selection;
		Object obj = sel.getFirstElement();
		IContainer container = null;

		if(obj instanceof IContainer){
			container = (IFolder)obj;
		}

		if(obj instanceof IFile){
			container = ((IFile)obj).getParent();
		}

		if(container==null){return;}

		Shell shell = UIUtil.getShell();

		try{

			List<IFile> fList = this.getMergeFileList(container);

			if(fList.size()==1){
				showSelectFolderFileDlg(shell,container, fList,fList.get(0));
			}else if(fList.size()>1){
				showSelectMergeFileDlg(shell,container,fList);
			}else{
				showSelectFolderFileDlg(shell,container, fList,null);
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
	

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}

}
