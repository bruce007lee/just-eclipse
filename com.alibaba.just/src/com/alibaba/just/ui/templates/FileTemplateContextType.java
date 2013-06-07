package com.alibaba.just.ui.templates;

import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.TemplateVariableResolver;

public class FileTemplateContextType extends TemplateContextType {
	
	public static final String CONTEXT_TYPE 
	  = "com.alibaba.just.ui.templates.FileTemplateContextType";
	
	
	private TemplateVariableResolver createResolver(String type,String desc){
		TemplateVariableResolver  rs = new TemplateVariableResolver();
		rs.setType(type);
		rs.setDescription(desc);
		return rs;
	}

	public FileTemplateContextType() {
		 addResolver(createResolver("moduleName",""));
		 addResolver(createResolver("requiredModules",""));
		 addResolver(createResolver("requiredModuleArrayStr",""));
		 addResolver(createResolver("requiredModuleParamsStr",""));
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.text.templates.TemplateContextType#validate(java.lang.String)
	 */
	public void validate(String pattern) throws TemplateException {
	}

	
}
