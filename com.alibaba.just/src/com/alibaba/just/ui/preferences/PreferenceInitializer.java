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
		store.setDefault(PreferenceConstants.P_FILE_CHARSET,PreferenceConstants.DEFAULT_FILE_CHARTSET);
		store.setDefault(PreferenceConstants.P_MD_TYPE,PreferenceConstants.DEFAULT_MD_TYPE);
		store.setDefault(PreferenceConstants.P_SHOW_LIB_ANONYMOUSE,PreferenceConstants.DEFAULT_SHOW_LIB_ANONYMOUSE);
		store.setDefault(PreferenceConstants.P_SHOW_MATCH_START,PreferenceConstants.DEFAULT_SHOW_MATCH_START);
		store.setDefault(PreferenceConstants.P_SHOW_MATCH_PARTIAL,PreferenceConstants.DEFAULT_SHOW_MATCH_PARTIAL);
		store.setDefault(PreferenceConstants.P_IS_NODEJS,PreferenceConstants.DEFAULT_IS_NODEJS);
		store.setDefault(PreferenceConstants.P_DEFINE_KEY_WORD, PreferenceConstants.DEFAULT_DEFINE_KEY_WORD);
		store.setDefault(PreferenceConstants.P_REQUIRE_KEY_WORD, PreferenceConstants.DEFAULT_REQUIRE_KEY_WORD);
	}

}
