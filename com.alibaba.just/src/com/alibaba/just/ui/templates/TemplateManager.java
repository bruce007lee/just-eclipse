package com.alibaba.just.ui.templates;

import java.io.IOException;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.ui.editors.text.templates.ContributionContextTypeRegistry;
import org.eclipse.ui.editors.text.templates.ContributionTemplateStore;
import org.osgi.service.prefs.BackingStoreException;

import com.alibaba.just.Activator;

public class TemplateManager {
 private static final String CUSTOM_TEMPLATES_KEY 
 = Activator.getDefault().PLUGIN_ID + ".filetemplates";
 private static TemplateManager instance;
 private TemplateStore fStore;
 private ContributionContextTypeRegistry fRegistry;
 private TemplateManager(){}

public static TemplateManager getInstance(){
 if(instance==null){
 instance = new TemplateManager();
 }
 return instance;
}

public TemplateStore getTemplateStore(){

 if (fStore == null){
  fStore = new ContributionTemplateStore(getContextTypeRegistry(), 
  Activator.getDefault().getPreferenceStore(), CUSTOM_TEMPLATES_KEY);
  try {
  fStore.load();
  } catch (IOException e){
  e.printStackTrace();

 }
}
return fStore;
}



public ContextTypeRegistry getContextTypeRegistry(){
 if (fRegistry == null){
  fRegistry = new ContributionContextTypeRegistry();
 }
 fRegistry.addContextType(FileTemplateContextType.CONTEXT_TYPE);
 return fRegistry;
}

public IPreferenceStore getPreferenceStore(){
 return Activator.getDefault().getPreferenceStore();
}

public void savePluginPreferences(){
 //Activator.getDefault().savePluginPreferences();
 try {
		Activator.getInstanceScope().getNode(Activator.PLUGIN_ID).flush();
	} catch (BackingStoreException e) {
		e.printStackTrace();
	}
}

public static String getTemplate(String templateId) throws Exception{
	try{
	     return TemplateManager.getInstance().getTemplateStore().getTemplateData(templateId).getTemplate().getPattern();
	}catch(Exception e){
		throw new Exception("Load template[id:"+templateId+"] error.",e);
	}
}

}