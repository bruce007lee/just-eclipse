package com.alibaba.just.ui.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.alibaba.just.Activator;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(PreferenceConstants.P_FILE_CHARSET,
				PreferenceConstants.DEFAULT_FILE_CHARTSET);
		store.setDefault(PreferenceConstants.P_PARSER_ENGINE,
				PreferenceConstants.DEFAULT_PARSER_ENGINE);
	}

}
