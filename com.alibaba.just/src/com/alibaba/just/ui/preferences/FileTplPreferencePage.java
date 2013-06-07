package com.alibaba.just.ui.preferences;

import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.texteditor.templates.TemplatePreferencePage;
import org.osgi.service.prefs.BackingStoreException;

import com.alibaba.just.Activator;
import com.alibaba.just.ui.templates.TemplateManager;

/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By 
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to 
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 */

public class FileTplPreferencePage
	extends TemplatePreferencePage
	implements IWorkbenchPreferencePage {

	public FileTplPreferencePage() {
		try {
			 setPreferenceStore(Activator.getDefault().getPreferenceStore());
			 setTemplateStore(TemplateManager.getInstance().getTemplateStore());
			 setContextTypeRegistry(TemplateManager.getInstance().getContextTypeRegistry());
			} catch(Exception ex){
			 ex.printStackTrace();
	    }
	}
	
	 protected boolean isShowFormatterSetting() {
		 return false;
		 }

		public boolean performOk() {
		 boolean ok = super.performOk();
		 //Activator.getDefault().savePluginPreferences();
		 
		 try {
			Activator.getInstanceScope().getNode(Activator.PLUGIN_ID).flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
		 
		 return ok;
	}

}