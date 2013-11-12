/**
 * 
 */
package com.alibaba.just.ui.properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * @author bruce.liz
 *
 */
public class ProjectCategoryPage extends PropertyPage {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		 this.noDefaultAndApplyButton();
		  return new Composite(parent, SWT.NULL);
	}


}
