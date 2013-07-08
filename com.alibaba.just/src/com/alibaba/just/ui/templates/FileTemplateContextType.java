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
		 addResolver(createResolver("dateTime","Current Date & Time. e.g:2012-02-10"));
		 addResolver(createResolver("moduleName","Current module name."));
		 addResolver(createResolver("requiredModules","Required modules in current module."));
		 addResolver(createResolver("requiredModuleArrayStr",""));
		 addResolver(createResolver("requiredModuleParamsStr",""));
		 addResolver(createResolver("templateUtil.getModulePath($rootPath,$module)","Get module path in merge file."));
		 addResolver(createResolver("templateUtil.toCamelPath($path)","Convert to camel case path."));
		 addResolver(createResolver("templateUtil.toCamel($str)","Convert to camel case string."));
		 addResolver(createResolver("templateUtil.toHorLine($path)",""));
		 addResolver(createResolver("templateUtil.toHorLinePath($str)",""));
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.text.templates.TemplateContextType#validate(java.lang.String)
	 */
	public void validate(String pattern) throws TemplateException {
	}

	
}
