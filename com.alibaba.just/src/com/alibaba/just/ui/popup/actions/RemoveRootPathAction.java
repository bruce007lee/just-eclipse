/**
 * 
 */
package com.alibaba.just.ui.popup.actions;

import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.alibaba.just.ui.decorators.RootPathDecorator;
import com.alibaba.just.ui.util.PreferenceUtil;
import com.alibaba.just.ui.util.UIUtil;

/**
 * @author bruce.liz
 *
 */
public class RemoveRootPathAction implements IObjectActionDelegate {

	private ISelection selection = null;

	public void run(IAction action) {
		
		if(selection==null){return;}		
		if(!(selection instanceof IStructuredSelection)){return;}		
		IStructuredSelection sel = (IStructuredSelection)selection;
		Object obj = sel.getFirstElement();		
		if(!(obj instanceof IFolder)){return;}

		try {
			IFolder ifolder = (IFolder) obj;
			String path = ifolder.getFullPath().toString();
			IProject project = ifolder.getProject();
			path = PreferenceUtil.getRootPathByIPath(project, path);
			List<String> list=PreferenceUtil.getProjectRootPathList(project);
			if(list.contains(path)){
				list.remove(path);
				PreferenceUtil.setProjectRootPathList(project, list);	
				RootPathDecorator.refresh();
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

	public void selectionChanged(IAction action, ISelection selection) {	
		this.selection  = selection;
	}


	public void setActivePart(IAction action, IWorkbenchPart targetPart) {}

}
