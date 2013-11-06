package com.alibaba.just.api.parser;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alibaba.just.api.bean.Module;
import com.alibaba.just.util.FileUtil;

/**
 * 
 * @author bruce.liz
 */
public class SimpleModuleParser extends AbstractModuleParser{

	/**
	 * 普通模块正则
	 */
	private static final String MODULE_REGEX_SEG ="\\Wdefine[\\s]*\\(" +
	"[\\s]*('[\\s]*[^\\(\\)]*'|\"[^\\(\\)]*\")[\\s]*" +
	"," +
	"[\\s]*(\\[[^\\[\\]]*\\])[\\s]*" +
	"," +
	"[\\s]*(function[\\s]*[\\s\\S]*)[\\s]*";
	
	/**
	 * 匿名模块正则单元
	 */
	private static final String ANONYMOUS_MODULE_REGEX_SEG="\\Wdefine[\\s]*\\(" +
	"[\\s]*(\\[[^\\[\\]]*\\])[\\s]*" +
	"," +
	"[\\s]*(function[\\s]*[\\s\\S]*)[\\s]*";
	
	/**
	 * 普通模块正则单元
	 */
	private static final String MODULE_REGEX ="\\Wdefine[\\s]*\\(" +
	"[\\s]*('[\\s]*[^\\(\\)]*'|\"[^\\(\\)]*\")[\\s]*" +
	"," +
	"[\\s]*(\\[[^\\[\\]]*\\])[\\s]*" +
	"," +
	"[\\s]*(function[\\s]*(?!("+MODULE_REGEX_SEG+")|("+ANONYMOUS_MODULE_REGEX_SEG+")))[\\s]*";

	/**
	 * 匿名模块正则
	 */
	private static final String ANONYMOUS_MODULE_REGEX ="\\Wdefine[\\s]*\\(" +
	"[\\s]*(\\[[^\\[\\]]*\\])[\\s]*" +
	"," +
	"[\\s]*(function[\\s]*(?!("+MODULE_REGEX_SEG+")|("+ANONYMOUS_MODULE_REGEX_SEG+")))[\\s]*";

	public SimpleModuleParser(String charset){
		this.charset = charset;		
	}

	public SimpleModuleParser(){}

	

	/**
	 * 由文件获取指定类型的模块
	 * @param file
	 * @param moduleType
	 * @param event
	 * @return
	 */
	public List<Module> getModules(File file,int moduleType,ParserEvent event){

		List<Module> moduleList = new ArrayList<Module>();

		String absPath = file.getAbsolutePath();
		if(file.exists() && (filter==null || (filter!=null && filter.accept(file)))){
			String content = null;
			try {
				content = FileUtil.getFileContent(file, charset);
			} catch (Exception e) {}

			if(content==null){return null ;}

			//去除注释
			content = JavaScriptCompressor.compress(content);		

			/*处理匿名模块*/
			if(moduleType == MODULE_TYPE_ALL || moduleType == MODULE_TYPE_ANONYMOUS){
				//只侦测标准形式define(["required1","required2"...],function(){...});

				Pattern p = Pattern.compile(ANONYMOUS_MODULE_REGEX,Pattern.MULTILINE);
				Matcher m = p.matcher(content);
				Module module = null;
				String tmp = null;
				while (m.find()) {
					module = new Module();

					module.setAnonymous(true);

					//模块依赖的子模块
					tmp = m.group(1);
					List<String> list = getRequiredModules(tmp);
					module.getRequiredModuleNames().addAll(list);

					module.setFilePath(absPath);
					moduleList.add(module);
				}
			}


			/*处理普通模块*/
			if(moduleType == MODULE_TYPE_ALL || moduleType == MODULE_TYPE_NORMAL){
				//只侦测标准形式define("module",["required1","required2"...],function(){...});

				Pattern p = Pattern.compile(MODULE_REGEX,Pattern.MULTILINE);
				Matcher m = p.matcher(content);
				Module module = null;
				String tmp = null;
				while (m.find()) {
					module = new Module();

					module.setAnonymous(false);

					//模块名
					tmp = m.group(1);
					tmp = tmp.trim().substring(1);
					tmp =  tmp.substring(0, tmp.length()-1);				
					module.setName(tmp);

					//模块依赖的子模块
					tmp = m.group(2);
					List<String> list = getRequiredModules(tmp);
					module.getRequiredModuleNames().addAll(list);

					//tmp = m.group(3);

					module.setFilePath(absPath);
					updateAlias(module,this.getAliasList());//update module alias
					moduleList.add(module);
				}
			}

			if(event!=null){
				event.onEnd(this,file,moduleList);
			}
		}	
		if(event!=null){
			event.onDispose(this);
		}
		return moduleList;
	}
	
	/**
	 * 
	 * @param module
	 * @return
	 */
	private List<String> getRequiredModules(String module){
		List<String> list = new ArrayList<String>();
		String str = module.trim();

		str = str.substring(1);
		str =  str.substring(0, str.length()-1);

		String[] modules = str.split("[,]");

		for(int i=0,l=modules.length;i<l;i++){
			modules[i] = modules[i].trim();
			if(modules[i].length()>2){
				modules[i] = modules[i].substring(1);
				modules[i] = modules[i].substring(0, modules[i].length()-1);
				list.add(modules[i]);
			}
		}

		return list;
	}
	
}
