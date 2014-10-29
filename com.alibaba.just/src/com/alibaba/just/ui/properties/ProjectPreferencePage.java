package com.alibaba.just.ui.properties;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPropertyPage;

import com.alibaba.just.ui.preferences.PreferenceConstants;
import com.alibaba.just.ui.util.PluginResourceUtil;
import com.alibaba.just.ui.util.PreferenceUtil;

/**
 * 
 * @author bruce.liz
 *
 */
public class ProjectPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPropertyPage{

	/**
	 * The element.
	 */
	private IAdaptable element;
	private List<FieldEditor> editors = null;
	private List<Composite> comps = null;


	public ProjectPreferencePage() {
		super();
		this.setPreferenceStore(new PreferenceStore(){
			public void save() throws IOException {
				//noting
			}

			public void save(OutputStream out, String header)
			throws IOException {
				//nothing
			}
		});
	}

	/**
	 * 
	 */
	public void createFieldEditors() {

		IProject project = (IProject) getElement();
		boolean isEnable =  PreferenceUtil.getEnableProjectSetting(project);
		Composite parent = getFieldEditorParent();
		FieldEditor item = new BooleanFieldEditor(PreferenceConstants.P_ENABLE_PROJECT_SETTING, "Enable project specific setting", parent){
			protected void valueChanged(boolean oldValue, boolean newValue) {
				super.valueChanged(oldValue, newValue);
				setAllEnable(Boolean.TRUE.equals(newValue));
			}
		};
		addField(item);
		
		// Create a horizontal separator
	    Label separator = new Label(parent, SWT.HORIZONTAL | SWT.SEPARATOR);
	    separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		parent = getFieldEditorParent();
		item = new StringFieldEditor(PreferenceConstants.P_FILE_CHARSET, "File &Charset:", parent){
			protected void valueChanged() {
				String str = this.getStringValue();
				if(!trim(str).equals(str)){
					this.setStringValue(trim(str));
				}
				super.valueChanged();
			}
		};
		addField(item);
		add(item,parent);
		parent = getFieldEditorParent();
		item =  new RadioGroupFieldEditor(PreferenceConstants.P_MD_TYPE,"Choose Parser Module Definition Type",1,
				new String[][]{{"AMD (RequireJS)","1"},{"CMD (CommonJS)","2"},{"UMD (AMD && CMD)","3"}},parent,true);
		addField(item);
		add(item,parent);

		//define key word
		parent = getFieldEditorParent();
		StringFieldEditor edit = new StringFieldEditor(PreferenceConstants.P_DEFINE_KEY_WORD, "AMD &define key word:",parent){
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

			protected void valueChanged() {
				String str = this.getStringValue();
				if(!trim(str).equals(str)){
					this.setStringValue(trim(str));
				}
				super.valueChanged();
			}
		};
		edit.setValidateStrategy(StringFieldEditor.VALIDATE_ON_FOCUS_LOST);
		addField(edit);
		add(edit,parent);

		//require key word
		parent = getFieldEditorParent();
		edit = new StringFieldEditor(PreferenceConstants.P_REQUIRE_KEY_WORD, "CMD &require key word:",parent){
			protected boolean doCheckState() {
				String val = this.getStringValue();
				if(val==null || val.trim().length()<=0){
					this.setErrorMessage("Please input the CMD require key word!");
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

			protected void valueChanged() {
				String str = this.getStringValue();
				if(!trim(str).equals(str)){
					this.setStringValue(trim(str));
				}
				super.valueChanged();
			}
		};
		edit.setValidateStrategy(StringFieldEditor.VALIDATE_ON_FOCUS_LOST);
		addField(edit);
		add(edit,parent);

		parent = getFieldEditorParent();
		item =new BooleanFieldEditor(PreferenceConstants.P_IS_NODEJS, "Compatible For NodeJs", parent);
		addField(item);
		add(item,parent);

		this.setAllEnable(isEnable);
	}

	private void setAllEnable(boolean isEnable){
		if(editors!=null){
			for(int i=0,l=editors.size();i<l;i++){
				editors.get(i).setEnabled(isEnable, comps.get(i));
			}
		}
	}

	private void add(FieldEditor editor,Composite parent) {
		if(editors==null){
			editors = new ArrayList<FieldEditor>(5);
			comps = new ArrayList<Composite>(5);
		}
		editors.add(editor);
		comps.add(parent);
	}

	private String trim(String str){
		if(str!=null){
			return str.trim();
		}
		return str;
	}

	public boolean performOk() {
		IProject project = (IProject) getElement();

		boolean rs = super.performOk();
		IPreferenceStore ps = this.getPreferenceStore();

		String charset = PreferenceUtil.getFileCharset(project);
		int mdType = PreferenceUtil.getMDType(project);
		String key1 = PreferenceUtil.getDefineKeyWord(project);
		String key2 = PreferenceUtil.getRequireKeyWord(project);
		boolean isNodeJs = PreferenceUtil.getIsNodeJs(project);
		boolean isEnableProjectSetting = PreferenceUtil.getEnableProjectSetting(project);

		PreferenceUtil.setProjectProperty(project,PreferenceConstants.P_ENABLE_PROJECT_SETTING,ps.getString(PreferenceConstants.P_ENABLE_PROJECT_SETTING));
		PreferenceUtil.setProjectProperty(project,PreferenceConstants.P_FILE_CHARSET,ps.getString(PreferenceConstants.P_FILE_CHARSET));
		PreferenceUtil.setProjectProperty(project,PreferenceConstants.P_DEFINE_KEY_WORD,ps.getString(PreferenceConstants.P_DEFINE_KEY_WORD));
		PreferenceUtil.setProjectProperty(project,PreferenceConstants.P_REQUIRE_KEY_WORD,ps.getString(PreferenceConstants.P_REQUIRE_KEY_WORD));
		PreferenceUtil.setProjectProperty(project,PreferenceConstants.P_MD_TYPE,ps.getString(PreferenceConstants.P_MD_TYPE));
		PreferenceUtil.setProjectProperty(project,PreferenceConstants.P_IS_NODEJS,ps.getString(PreferenceConstants.P_IS_NODEJS));

		//if module parser config change,clear all cache
		if(isEnableProjectSetting != PreferenceUtil.getEnableProjectSetting(project)//show lib mode change
				|| mdType!=PreferenceUtil.getMDType(project)//MD type change
				|| !charset.equalsIgnoreCase((PreferenceUtil.getFileCharset(project)))//charset change
				|| isNodeJs != PreferenceUtil.getIsNodeJs(project)//nodejs                      
				|| key1 != PreferenceUtil.getDefineKeyWord(project)//AMD define key word change
				|| key2 != PreferenceUtil.getRequireKeyWord(project)){//CMD require key word change
			PluginResourceUtil.clearProjectCache(project);
		}

		return rs;
	}


	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPropertyPage#getElement()
	 */
	public IAdaptable getElement() {
		return element;
	}

	/**
	 * Sets the element that owns properties shown on this page.
	 * 
	 * @param element
	 *            the element
	 */
	public void setElement(IAdaptable element) {
		this.element = element;
		if(element instanceof IProject){
			IProject project = (IProject)element;
			IPreferenceStore ps = this.getPreferenceStore();

			ps.setDefault(PreferenceConstants.P_FILE_CHARSET, PreferenceConstants.DEFAULT_FILE_CHARTSET);
			ps.setDefault(PreferenceConstants.P_DEFINE_KEY_WORD, PreferenceConstants.DEFAULT_DEFINE_KEY_WORD);
			ps.setDefault(PreferenceConstants.P_REQUIRE_KEY_WORD, PreferenceConstants.DEFAULT_REQUIRE_KEY_WORD);
			ps.setDefault(PreferenceConstants.P_MD_TYPE, PreferenceConstants.DEFAULT_MD_TYPE);
			ps.setDefault(PreferenceConstants.P_IS_NODEJS, PreferenceConstants.DEFAULT_IS_NODEJS);
			ps.setDefault(PreferenceConstants.P_ENABLE_PROJECT_SETTING,  PreferenceConstants.DEFAULT_ENABLE_PROJECT_SETTING);

			ps.setValue(PreferenceConstants.P_FILE_CHARSET, PreferenceUtil.getFileCharset(project));
			ps.setValue(PreferenceConstants.P_DEFINE_KEY_WORD, PreferenceUtil.getDefineKeyWord(project));
			ps.setValue(PreferenceConstants.P_REQUIRE_KEY_WORD, PreferenceUtil.getRequireKeyWord(project));
			ps.setValue(PreferenceConstants.P_MD_TYPE, PreferenceUtil.getMDType(project)+"");
			ps.setValue(PreferenceConstants.P_IS_NODEJS, PreferenceUtil.getIsNodeJs(project));
			ps.setValue(PreferenceConstants.P_ENABLE_PROJECT_SETTING,  PreferenceUtil.getEnableProjectSetting(project));
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#dispose()
	 */
	public void dispose() {
		element = null;
		comps.clear();
		comps = null;
		editors.clear();
		editors = null;
		super.dispose();
	}

	protected void performDefaults() {
		super.performDefaults();
		IPreferenceStore ps = this.getPreferenceStore();
		setAllEnable(!ps.getBoolean(PreferenceConstants.P_ENABLE_PROJECT_SETTING));
	}


}