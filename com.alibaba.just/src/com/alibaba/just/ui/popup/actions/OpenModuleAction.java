/**
 * 
 */
package com.alibaba.just.ui.popup.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.part.FileEditorInput;

import com.alibaba.just.ui.dialogs.OpenModuleDialog;
import com.alibaba.just.ui.util.UIUtil;

/**
 * @author bruce.liz
 *
 */
public class OpenModuleAction implements IWorkbenchWindowActionDelegate {

	private ISelection selection;

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		IEditorPart part = UIUtil.getCurrentActiveEditor();		    
		if(part==null){return;}

		String selectText = null;
		if(this.selection instanceof TextSelection){
			selectText = ((TextSelection)this.selection).getText();
		}

		IEditorInput input = part.getEditorInput();
		if(input instanceof FileEditorInput){
			FileEditorInput fileInput = (FileEditorInput)input;
			try {
				IFile ifile = fileInput.getFile();
				IProject project = ifile.getProject();
				OpenModuleDialog dlg = new OpenModuleDialog(UIUtil.getShell());
				dlg.setProject(project);
				if(selectText!=null){
					dlg.setInitialPattern(selectText);
				}
				dlg.open();

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

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
		this.selection = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
		// TODO Auto-generated method stub

	}

}
