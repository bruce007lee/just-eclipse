package com.alibaba.just.api.parser;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alibaba.just.api.bean.Module;
import com.alibaba.just.api.exception.ModuleParseException;
import com.alibaba.just.util.FileUtil;


/**
 * 
 * @author bruce.liz
 */
public class ModuleParser {

	public static final String DEFAULT_CHARSET = "GBK";	
	private String charset = DEFAULT_CHARSET;

	/**
	 * 普通模块正则
	 */
	private static final String MODULE_REGEX ="define[\\s]*\\(" +
	"[\\s]*('[\\s]*[^\\(\\)]*'|\"[^\\(\\)]*\")[\\s]*" +
	"," +
	"[\\s]*(\\[[^\\[\\]]*\\])[\\s]*" +
	"," +
	"[\\s]*(function[\\s]*[\\s\\S]*)[\\s]*";

	/**
	 * 匿名模块正则
	 */
	private static final String ANONYMOUS_MODULE_REGEX ="define[\\s]*\\(" +
	"[\\s]*(\\[[^\\[\\]]*\\])[\\s]*" +
	"," +
	"[\\s]*(function[\\s]*[\\s\\S]*)[\\s]*";


	public static final int MODULE_TYPE_NORMAL = 0;
	public static final int MODULE_TYPE_ANONYMOUS = 1;
	public static final int MODULE_TYPE_ALL = 2;

	public ModuleParser(String charset){
		this.charset = charset;		
	}
	
	public ModuleParser(){}

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


	/**
	 * 是否是要加载的文件
	 * (去掉min文件)
	 * @param absolutePath
	 * @return
	 */
	private boolean isProcessFile(String absolutePath) {
		if(absolutePath.endsWith(".js") && !absolutePath.endsWith("-min.js")){
			return true;
		}
		return false;
	}

	private List<Module> processFolder(File file,List<Module> list,int moduleType){

		if(!file.exists() || !file.isDirectory()){return list;}
		File[]  flist  = file.listFiles();

		for(File f:flist){
			if(f.isFile()){
				list.addAll(getModules(f,moduleType));
			}else if(f.isDirectory()){
				processFolder(f,list,moduleType);
			}
		}

		return list;
	}

	
	/**
	 * 模块是否在列表中重复
	 * @param module
	 * @param list
	 * @return
	 */
	private boolean isDuplicate(Module module,List<Module> list){	
		if(module.getName()==null){
			return false;
		}
		for(Module mod : list){
			if(mod.getName()!=null && mod.getName().equals(module.getName())){
				return true;
			}
		}		
		return false;
	}


	/**
	 * 递归得到指定模块的所有依赖子模块
	 * (如果发生循环依赖的话抛出异常)
	 * @param module
	 * @param moduleTree
	 * @param newList
	 * @param list
	 * @return
	 * @throws ModuleParseException 
	 */
	private List<Module> getAllRequiredModules(Module module,List<Module> moduleTree,List<Module> newList,List<Module> list) throws ModuleParseException{
		List<String> subModNames = module.getRequiredModuleNames();

		Module subMod = null;
		for(String subname : subModNames){
			subMod = getModuleByName(subname, list);

			if(subMod==null && subname!=null){
				subMod = new Module();
				subMod.setName(subname);
			}

			if(subMod!=null){
				if(!isDuplicate(subMod, newList)){
				  newList.add(subMod);
				}
				
				//检测是否有循环引用的情况
				if(isDuplicate(module, moduleTree)){
					throw new ModuleParseException("Import loop with module:[name:"+module.getName()+" file:"+module.getFilePath()+"]");
				}
				
				List<Module> tree =  new ArrayList<Module>();
				tree.addAll(moduleTree);
				tree.add(module);
				
				getAllRequiredModules(subMod, tree , newList, list);
				tree.clear();
			}
		}

		return newList;
	}

	/**
	 * 递归得到指定模块的所有依赖子模块
	 * @param module
	 * @param list
	 * @return
	 * @throws ModuleParseException 
	 */
	public List<Module> getAllRequiredModules(Module module,List<Module> list) throws ModuleParseException{
		return getAllRequiredModules(module,new ArrayList<Module>(), new ArrayList<Module>() ,list);
	}


	/**
	 * 
	 * @param moduleName
	 * @param list
	 * @return
	 */
	private Module getModuleByName(String moduleName,List<Module> list){

		if(moduleName==null){
			return null;
		}
		for(Module submod : list){
			if(moduleName.equals(submod.getName())){
				return submod;
			}
		}

		return null;
	}

	public List<Module> getAllModules(String path){
		return getAllModules(path,MODULE_TYPE_NORMAL);
	}

	/**
	 * 获取指定路径下的所有指定类型的模块
	 * @param path
	 * @param moduleType
	 * @return
	 */
	public List<Module> getAllModules(String path,int moduleType){
		List<Module> moduleList = new ArrayList<Module>();

		File  file = new File(path);

		if(file.exists()){
			if(file.isDirectory()){
				processFolder(file, moduleList,moduleType);
			}else if(file.isFile()){
				moduleList.addAll(getModules(file,moduleType));
			}
		}

		return moduleList;
	}


	/**
	 * 得到当前模块文件的所有模块
	 * @param filePath
	 * @param moduleType
	 * @return
	 */
	public List<Module> getModules(String filePath,int moduleType){
		File file = new File(filePath);
		return getModules(file,moduleType);
	}

	/**
	 * 得到当前模块文件的所有非匿名模块
	 * @param filePath
	 * @return
	 */
	public List<Module> getModules(String filePath){
		return getModules(filePath,MODULE_TYPE_NORMAL);
	}

	/**
	 * 得到当前模块文件的默认模块(默认返回文件中的第一个模块)
	 * @param filePath
	 * @return
	 */
	public Module getModule(String filePath,int moduleType){
		List<Module> modules =  getModules(filePath,moduleType);
		if(modules!=null && modules.size()>0){
			return modules.get(0);
		}
		return null;
	}

	/**
	 * 得到当前模块文件的默认非匿名模块(默认返回文件中的第一个模块)
	 * @param filePath
	 * @return
	 */
	public Module getModule(String filePath){
		return getModule(filePath,MODULE_TYPE_NORMAL);
	}

	/**
	 * 默认取非匿名模块
	 * @param file
	 * @return
	 */
	public List<Module> getModules(File file){
		return getModules(file,MODULE_TYPE_NORMAL);
	}

	/**
	 * 由文件获取指定类型的模块
	 * @param file
	 * @param moduleType
	 * @return
	 */
	public List<Module> getModules(File file,int moduleType){

		List<Module> moduleList = new ArrayList<Module>();

		String absPath = file.getAbsolutePath();
		if(file.exists() && isProcessFile(absPath)){
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
					moduleList.add(module);
				}
			}

		}	
		return moduleList;
	}

	
	/**
	 * only for test
	 * @param args
	 */
	public static void main(String[] args){

		//String spath = "E:/MyEclipse/workspace/trade_new/WebRoot/stage_make/app/trade/js/widget/ui/stage-min.js";

		//Module mm = ModuleUtil.getModules(spath).get(0);

		//System.out.println(mm);

		//if(args!=null && args.length>1){

		//String path = args[0].trim();

		String path="E:/MyEclipse/workspace/trade_new/WebRoot/rate_rb/app/trade/js/judge/page/rateeditframe/rate-edit-frame.js";

		path="E:/test_brach/rate_rb_20130122/app/trade/js/just/core/compatible-may.js";

		//String folderPath = args[1].trim();

		String folderPath="E:/MyEclipse/workspace/trade_new/WebRoot/rate_rb/app";

		folderPath = "E:/test_brach/rate_rb_20130122/app/trade/js";

		System.out.println("---------------------------------");
		System.out.println("File:"+path);
		System.out.println("---------------------------------");
		System.out.println("Root Folder:"+folderPath);

		File  file = new File(path);
		
		ModuleParser parser =new ModuleParser();

		List<Module> list= parser.getAllModules(folderPath);

		Module m = parser.getModules(file).get(0);

		System.out.println("---------------------------------");
		System.out.println(m);

		System.out.println("---------------------------------");

		try {
			list = parser.getAllRequiredModules(m, list);
		} catch (ModuleParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for(Module module:list){
			System.out.println(module.getName());
		}		
		System.out.println("\nAll required mod:"+list.size());

	}
	//}

}
