/**
 * 
 */
package com.alibaba.just.ui.properties;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.alibaba.just.Activator;

/**
 * @author bruce.liz
 *
 */
public class ProjectModuleAliasPage extends PreferencePage implements
		IWorkbenchPreferencePage {

	/**
	 * 
	 */
	public ProjectModuleAliasPage() {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}


	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		// TODO Auto-generated method stub
		return null;
	}

}
