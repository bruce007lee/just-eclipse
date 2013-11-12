package com.alibaba.just.template;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import com.alibaba.just.api.bean.Module;
import com.alibaba.just.ui.util.TemplateUtil;

/**
 * 
 * @author bruce.liz
 *
 */
public class ModuleTemplate {
	
	public static final String DEFAULT_TPL = "default.tpl";
	
	public static final String DEFAULT_DATETIME_FORMART = "yyyy-MM-dd";

	private String moduleName = null;

	private List<Module> requiredModules = null;

	private String tpl = null;

	public String getTpl() {
		return tpl;
	}

	public void setTpl(String tpl) {
		this.tpl = tpl;
	}


	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public List<Module> getRequiredModules() {
		return requiredModules;
	}

	public void setRequiredModules(List<Module> requiredModules) {
		this.requiredModules = requiredModules;
	}


	public String getParseContent() throws Exception{
		return getParseContent(null);
	}
	
	private String getRequiredModuleParamsStr(List<Module> requiredModules){
		StringBuffer sb = new StringBuffer();
		if(requiredModules!=null){
			for(int i=0,l=requiredModules.size();i<l;i++){
				if(i>0){
					sb.append(",");
				}
				sb.append(getParamName(requiredModules.get(i).getName(),i));
			}
		}
		return sb.toString();
	}
	
	private String getParamName(String moduleName,int idx){
		if(moduleName ==null){
			return "";
		}
		//对jquery做特别处理
		if(moduleName.equalsIgnoreCase("jquery")){
			return "$";
		}
		
		String[] segs = moduleName.split("/");		
		String name = segs[segs.length-1];
		
		if(name.matches("[\\d\\.]*")){
			return "Module"+idx;
		}
		
		String[] name_segs = name.split("[\\.\\-_]");
		
		StringBuffer sb = new StringBuffer();
		
		for(String nseg:name_segs){
			if(nseg.length()>0){
		      sb.append(nseg.substring(0, 1).toUpperCase());
		      sb.append(nseg.substring(1, nseg.length()));
			}
		}
		
		return sb.toString();
	}

	private String getRequiredModuleArrayStr(List<Module> requiredModules){
		StringBuffer sb = new StringBuffer();
		if(requiredModules!=null){
			for(int i=0,l=requiredModules.size();i<l;i++){
				if(i>0){
					sb.append(",\n\t\t");
				}
				sb.append("'")
				.append(requiredModules.get(i).getName())
				.append("'");
			}
		}
		return sb.toString();
	}

	public String getParseContent(Map props) throws Exception{

		String content = "";
		ByteArrayOutputStream sb = null;

		OutputStreamWriter writer = null;
		try
		{

			if(tpl!=null){
				content = tpl;
			}
			
			//Velocity.init("velocity.properties");

			Velocity.init();

			/*
			 *  Make a context object and populate with the data.  This
			 *  is where the Velocity engine gets the data to resolve the
			 *  references (ex. $list) in the template
			 */

			VelocityContext context = new VelocityContext();

			context.put("dateTime", new SimpleDateFormat(DEFAULT_DATETIME_FORMART).format(new Date()));
			context.put("moduleName", moduleName);
			context.put("requiredModules", requiredModules);
			context.put("requiredModuleArrayStr", getRequiredModuleArrayStr(requiredModules));
			context.put("requiredModuleParamsStr", getRequiredModuleParamsStr(requiredModules));
			context.put("templateUtil", TemplateUtil.getTemplateUtil());

			if(props!=null){
				Iterator iter = props.keySet().iterator();

				String key = null;
				while(iter.hasNext()){
					key = (String) iter.next();
					context.put(key, props.get(key));
				}
			}

			sb = new ByteArrayOutputStream();

			writer = new OutputStreamWriter(sb);

			Velocity.evaluate(context, writer, DEFAULT_TPL , content);
			writer.flush();
			content =  sb.toString();

			writer.close();

		}catch(Exception e){
			try{
				if(sb!=null){sb.close();};
				if(writer!=null){writer.close();};
			}catch(Exception e1){
				
			}
			throw e;
		}
		return content;
	}

	public void destroy(){
		moduleName = null;
		if(requiredModules!=null){
			requiredModules.clear();
		}
		requiredModules = null;
		tpl=null;
	}
}
