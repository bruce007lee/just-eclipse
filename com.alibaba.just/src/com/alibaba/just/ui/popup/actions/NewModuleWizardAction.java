package com.alibaba.just.ui.popup.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.wizards.IWizardDescriptor;

import com.alibaba.just.Activator;
import com.alibaba.just.ui.util.UIUtil;

/**
 * 
 * @author bruce.liz
 *
 */
public class NewModuleWizardAction implements IObjectActionDelegate {
	private static final String NEW_MODULE_WIZARD_ID = "com.alibaba.just.ui.wizards.ModuleNewWizard";
	private ISelection selection=null;

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {}

	public void run(IAction action) {
		try {
			if(selection instanceof IStructuredSelection){
				IWorkbench workbench = Activator.getDefault().getWorkbench();
				IWizardDescriptor iwd = workbench.getNewWizardRegistry().findWizard(NEW_MODULE_WIZARD_ID);
				IWorkbenchWizard  ww = iwd.createWizard();				
				WizardDialog wd = new WizardDialog(UIUtil.getShell(),ww);
				ww.init(workbench, (IStructuredSelection) selection);
				wd.open();
			}

		} catch (Exception e) {
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {	
		this.selection = selection;
	}

}
