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
 * @deprecated
 * @author bruce.liz
 */
public class SimpleModuleParser extends AbstractModuleParser{

	//private static final String DEFINE_KEY_REG = "\\W(define)";

	/**
	 * 3参普通模块正则单元
	 */
	private static final String NORMAL_SEG ="[\\s]*\\(" +
	"[\\s]*('[\\s]*[^\\(\\)]*'|\"[^\\(\\)]*\")[\\s]*" +
	"," +
	"[\\s]*(\\[[^\\[\\]]*\\])[\\s]*" +
	"," +
	"[\\s]*(function[\\s]*[\\s\\S]*)[\\s]*";

	/**
	 * 2参匿名模块正则单元
	 */
	private static final String ANONYMOUS_SEG="[\\s]*\\(" +
	"[\\s]*(\\[[^\\[\\]]*\\])[\\s]*" +
	"," +
	"[\\s]*(function[\\s]*[\\s\\S]*)[\\s]*";

	/**
	 * 2参普通模块正则单元
	 */
	private static final String NORMAL_SEG_SIMPLE="[\\s]*\\(" +
	"[\\s]*('[\\s]*[^\\(\\)]*'|\"[^\\(\\)]*\")[\\s]*" +
	"," +
	"[\\s]*(function[\\s]*[\\s\\S]*)[\\s]*";

	/**
	 * 1参匿名模块正则单元
	 */
	private static final String ANONYMOUS_SEG_SIMPLE="[\\s]*\\(" +
	"[\\s]*(function[\\s]*[\\s\\S]*)[\\s]*";

	//private static final String REG_POSTFIX = "[\\s]*(function[\\s]*(?!("+NORMAL_SEG+")|("+ANONYMOUS_SEG+")|("+NORMAL_SEG_SIMPLE+")|("+ANONYMOUS_SEG_SIMPLE+")))[\\s]*";

	/*----------------------------------------------------------------------*/

	/**
	 * 普通模块正则单元
	 */
	private static final String MODULE_REGEX ="[\\s]*\\(" +
	"[\\s]*('[\\s]*[^\\(\\)]*'|\"[^\\(\\)]*\")[\\s]*" +
	"," +
	"[\\s]*(\\[[^\\[\\]]*\\])[\\s]*" +
	"," ;

	/**
	 * 简单普通模块正则单元
	 */
	private static final String MODULE_REGEX_SIMPLE ="[\\s]*\\(" +
	"[\\s]*('[\\s]*[^\\(\\)]*'|\"[^\\(\\)]*\")[\\s]*" +
	"," ;

	/**
	 * 匿名模块正则
	 */
	private static final String ANONYMOUS_MODULE_REGEX ="[\\s]*\\(" +
	"[\\s]*(\\[[^\\[\\]]*\\])[\\s]*" +
	"," ;

	/**
	 * 简单匿名模块正则
	 */
	private static final String ANONYMOUS_MODULE_REGEX_SIMPLE ="[\\s]*\\(";

	/*------reg.start----*/

	private String normal_seg = null;	
	private String anonymous_seg = null;	
	private String normal_seg_simple = null;	
	private String anonymous_seg_simple = null;
	private String  reg_postfix	 = null;

	private String  module_regex = null;	
	private String  module_regex_simple = null;
	private String  anonymous_module_regex = null;
	private String  anonymous_module_regex_simple = null;

	/*------reg.end-----*/

	private String defineKeyWord=null;

	public SimpleModuleParser(ParserOptions options){
		if(options!=null){
			if(options.getCharset()!=null){
				charset = options.getCharset();	
			}
			String defineKeyWord = DEFINE_KEY_REG;
			if(options.getDefineKeyWord()!=null){
				defineKeyWord = options.getDefineKeyWord();
			}
			this.mdType = options.getMdType();
			this.setDefineKeyWord(defineKeyWord);
		}
	}

	public SimpleModuleParser(){
		this(null);
	}

	/*
	 * (non-Javadoc)
	 * @see com.alibaba.just.api.parser.ModuleParser#setDefineKeyWord(java.lang.String)
	 */
	public void setDefineKeyWord(String str){
		if(str!=null && str.length()>0){
			defineKeyWord = "\\W("+str+")";

			normal_seg = this.defineKeyWord + NORMAL_SEG;
			anonymous_seg = this.defineKeyWord + ANONYMOUS_SEG;
			normal_seg_simple = this.defineKeyWord + NORMAL_SEG_SIMPLE;
			anonymous_seg_simple = this.defineKeyWord + ANONYMOUS_SEG_SIMPLE;
			reg_postfix = "[\\s]*(function[\\s]*(?!("+normal_seg+")|("+anonymous_seg+")|("+normal_seg_simple+")|("+anonymous_seg_simple+")))[\\s]*";

			module_regex = this.defineKeyWord + MODULE_REGEX + reg_postfix;
			module_regex_simple = this.defineKeyWord + MODULE_REGEX_SIMPLE + reg_postfix;
			anonymous_module_regex = this.defineKeyWord + ANONYMOUS_MODULE_REGEX + reg_postfix;
			anonymous_module_regex_simple = this.defineKeyWord + ANONYMOUS_MODULE_REGEX_SIMPLE + reg_postfix;
		}
	}

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
		if(!isDispose && file.exists() && (filter==null || (filter!=null && filter.accept(file)))){
			String content = null;
			try {
				content = FileUtil.getFileContent(file, charset);
			} catch (Exception e) {}

			if(content==null){return moduleList ;}

			//去除注释
			content = JavaScriptCompressor.compress(content);		

			/*处理匿名模块*/
			if(moduleType == MODULE_TYPE_ALL || moduleType == MODULE_TYPE_ANONYMOUS){
				//侦测标准形式define(["required1","required2"...],function(){...});
				Pattern p = Pattern.compile(anonymous_module_regex,Pattern.MULTILINE);
				Matcher m = p.matcher(content);
				Module module = null;
				String tmp = null;
				while (m.find()) {
					module = new Module();

					module.setAnonymous(true);

					//模块依赖的子模块
					tmp = m.group(2);
					List<String> list = getRequiredModules(tmp);
					module.getRequiredModuleNames().addAll(list);

					module.setFilePath(absPath);
					moduleList.add(module);
				}

				//侦测简易形式define(function(){...});
				p = Pattern.compile(anonymous_module_regex_simple,Pattern.MULTILINE);
				m = p.matcher(content);
				module = null;
				tmp = null;
				while (m.find()) {
					module = new Module();

					module.setAnonymous(true);
					module.setFilePath(absPath);
					moduleList.add(module);
				}
			}


			/*处理普通模块*/
			if(moduleType == MODULE_TYPE_ALL || moduleType == MODULE_TYPE_NORMAL){
				//侦测标准形式define("module",["required1","required2"...],function(){...});
				Pattern p = Pattern.compile(module_regex,Pattern.MULTILINE);
				Matcher m = p.matcher(content);
				Module module = null;
				String tmp = null;
				while (m.find()) {
					module = new Module();

					module.setAnonymous(false);

					//模块名
					tmp = m.group(2);
					tmp = tmp.trim().substring(1);
					tmp =  tmp.substring(0, tmp.length()-1);				
					module.setName(tmp);

					//模块依赖的子模块
					tmp = m.group(3);
					List<String> list = getRequiredModules(tmp);
					module.getRequiredModuleNames().addAll(list);

					module.setFilePath(absPath);
					updateAlias(module,this.getAliasList());//update module alias
					moduleList.add(module);
				}

				//侦测简易形式define("module",function(){...});
				p = Pattern.compile(module_regex_simple,Pattern.MULTILINE);
				m = p.matcher(content);
				module = null;
				tmp = null;
				while (m.find()) {
					module = new Module();

					module.setAnonymous(false);

					//模块名
					tmp = m.group(2);
					tmp = tmp.trim().substring(1);
					tmp =  tmp.substring(0, tmp.length()-1);				
					module.setName(tmp);

					module.setFilePath(absPath);
					updateAlias(module,this.getAliasList());//update module alias
					moduleList.add(module);
				}
			}

			if(event!=null){
				event.onParseFileSuccess(this,file,moduleList);
			}
		}	
		if(event!=null){
			event.onParseFileEnd(this,file);
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
