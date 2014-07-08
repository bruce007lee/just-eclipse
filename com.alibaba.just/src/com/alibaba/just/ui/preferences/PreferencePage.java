package com.alibaba.just.ui.preferences;

import java.util.regex.Pattern;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.alibaba.just.Activator;
import com.alibaba.just.ui.cache.ResourceCacheManager;
import com.alibaba.just.ui.util.PreferenceUtil;

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

public class PreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {

	public PreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Just Eclipse Preference");
	}

	/**
	 * 
	 */
	public void createFieldEditors() {
		/*addField(new DirectoryFieldEditor(PreferenceConstants.P_PATH, 
				"&Directory preference:", getFieldEditorParent()));
		addField(
			new BooleanFieldEditor(
				PreferenceConstants.P_BOOLEAN,
				"&An example of a boolean preference",
				getFieldEditorParent()));

		addField(new RadioGroupFieldEditor(
				PreferenceConstants.P_CHOICE,
			"An example of a multiple-choice preference",
			1,
			new String[][] { { "&Choice 1", "choice1" }, {
				"C&hoice 2", "choice2" }
		}, getFieldEditorParent()));*/
		addField(new StringFieldEditor(PreferenceConstants.P_FILE_CHARSET, "File &Charset:", getFieldEditorParent()));
		addField(new RadioGroupFieldEditor(PreferenceConstants.P_PARSER_ENGINE,"Choose Module Parser Engine",1,
				new String[][]{{"RegExp Engine (more fast)","0"},{"Rhino Engine (more strict)","1"}},getFieldEditorParent(),true));
		addField(new BooleanFieldEditor(PreferenceConstants.P_SHOW_LIB_ANONYMOUSE, "Show library's anonymouse module in Module View", getFieldEditorParent()));
		addField(new BooleanFieldEditor(PreferenceConstants.P_SHOW_MATCH_START, "Show module assist with start string matches.(JSDT require)", getFieldEditorParent()));
		addField(new BooleanFieldEditor(PreferenceConstants.P_SHOW_MATCH_PARTIAL, "Show module assist with partial matches.(JSDT require)", getFieldEditorParent()));
		//define key word
		StringFieldEditor edit = new StringFieldEditor(PreferenceConstants.P_DEFINE_KEY_WORD, "AMD &define key word:",getFieldEditorParent()){
			 protected boolean doCheckState() {
				String val = this.getStringValue();
				if(val==null || val.trim().length()<=0){
					this.setErrorMessage("Please input the AMD define key word!");
					return false;
				}
				try{
					Pattern.compile(val);
				}catch(Exception e){
					this.setErrorMessage("Please input a valid regular expression!");
					return false;
				}
				return true;
			}
		};
		edit.setValidateStrategy(StringFieldEditor.VALIDATE_ON_FOCUS_LOST);
		addField(edit);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

	public boolean performOk() {
		String charset = PreferenceUtil.getFileCharset();
		int engineType = PreferenceUtil.getParserEngineType();
		boolean showAnoyModule = PreferenceUtil.isShowLibAnonymouseModule();
		String key = PreferenceUtil.getDefineKeyWord();
		boolean rs =  super.performOk();
		if(!rs){
			return false;
		}
		//if module parser config change,clear all cache
		if(engineType!=PreferenceUtil.getParserEngineType()                                 //engine type change
				|| !charset.equalsIgnoreCase((PreferenceUtil.getFileCharset()))           //charset change
				|| showAnoyModule != PreferenceUtil.isShowLibAnonymouseModule()    //show lib mode change
				|| key != PreferenceUtil.getDefineKeyWord() ){                                 //AMD define key word change
			ResourceCacheManager.removeAll();
		}
		
		return rs;
	}
	
}