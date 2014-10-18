package com.alibaba.just.ui.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.preference.IPreferenceStore;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.alibaba.just.Activator;
import com.alibaba.just.PluginConstants;
import com.alibaba.just.api.bean.AliasInfo;
import com.alibaba.just.api.parser.ParserFactory;
import com.alibaba.just.ui.preferences.PreferenceConstants;

public class PreferenceUtil {	

	private static final String PARSER_ENGINE_TYPE_SIMPLE = "0";
	private static final String SEP_REG = "[\n]";

	private static final String LIB_SEP= "|";

	public static final String LIB_TYPE_SELF = "s|";
	public static final String LIB_TYPE_EXTERNAL_FOLDER = "e|";
	public static final String LIB_TYPE_WORKSPACE_FOLDER = "w|";

	public static final String SELF_LIB_STR = LIB_TYPE_SELF + "<PROJECT>";

	public static final String CONFIG_FILE = ".justeclipse/project-cfg.xml";
	public static final String CONFIG_ROOT_NAME = "config";
	public static final String PROPERTY_NAME = "property";
	public static final String ATTR_NAME = "name";

	public static final String ALIAS_LIST_NAME = "aliasList";
	public static final String ALIAS_ITEM_NAME = "aliasItem";
	public static final String ALIAS_ATTR_NAME = "name";
	public static final String ALIAS_ATTR_ALIAS = "alias";

	public static final QualifiedName CONFIG_QUALIFIEDNAME = new QualifiedName(PluginConstants.QUALIFIED_NAME, CONFIG_ROOT_NAME);

	private PreferenceUtil(){}

	/**
	 * 
	 * @param project
	 * @param key
	 * @return
	 */
	public static String getProjectProperty(IProject project,String key){	
		if(project==null || project.getLocation()==null){
			return null;
		}
		String val = null;		
		try {
			Object obj = project.getSessionProperty(CONFIG_QUALIFIEDNAME);
			Document doc = null;
			if(obj instanceof Document){
				doc = (Document)obj;
			}else{
				//val = project.getPersistentProperty(new QualifiedName(PluginConstants.QUALIFIED_NAME, key));
				doc = loadConfig(getProjectConfigPath(project));
				if(doc==null){
					return null;
				}
				project.setSessionProperty(CONFIG_QUALIFIEDNAME,doc);
			}
			Node rootNode = doc.getDocumentElement();
			if(rootNode!=null){
				Element el = getPropertyValue(rootNode,key);
				if(el!=null){
					return el.getTextContent();
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}		
		return val;
	}

	private static String getProjectConfigPath(IProject project){
		if(project.getLocation()==null){
			return null;
		}
		return formatPath(project.getLocation().toFile().getAbsolutePath().replace('\\', '/'))+CONFIG_FILE;
	}

	private static String formatPath(String path){
		if(path!=null){
			path = path.replace('\\', '/');
			if(!path.endsWith("/")){
				path = path+"/";
			}
		}		
		return path;
	}

	/**
	 * 
	 * @param parent
	 * @param tagName
	 * @return
	 */
	private static List<Node> getChildNode(Node parent,String tagName){
		List<Node> list = new ArrayList<Node>();
		NodeList  nList =  parent.getChildNodes();
		if(nList!=null){
			for(int i=0,l=nList.getLength();i<l;i++){
				if(tagName.equals(nList.item(i).getNodeName())){
					list.add(nList.item(i));
				}
			}
		}
		return list;
	}

	/**
	 * 
	 * @param parent
	 * @param propertyName
	 * @return
	 */
	private static Element getPropertyValue(Node parent,String propertyName){
		List<Node> list = getChildNode(parent,PROPERTY_NAME);
		Element el = null;
		for(Node n:list){
			if(n instanceof Element){
				el = (Element)n;
				if(propertyName.equals(el.getAttribute(ATTR_NAME))){
					return el;
				}
			}
		}
		return null;
	}

	/**
	 * 保存配置
	 * @param path
	 * @throws Exception 
	 */
	private static void saveConfig(String path,Document doc) throws Exception{
		File file = new File(path);

		FileOutputStream fos =null;

		try {
			if(!file.exists()){
				if(!file.getParentFile().exists()){
					file.getParentFile().mkdirs();
				}
				file.createNewFile();
			}

			doc.setXmlStandalone(true);
			Transformer tf = TransformerFactory.newInstance().newTransformer();				
			tf.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			tf.setOutputProperty(OutputKeys.METHOD, "xml");
			tf.setOutputProperty(OutputKeys.INDENT, "yes");
			tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

			fos = new FileOutputStream(file);				
			tf.transform(new DOMSource(doc), new StreamResult(fos));   

		} catch (Exception e) {
			throw new Exception("Save config ["+path+"] error!",e);
		}finally{
			if(fos!=null){
				try {fos.close();} catch (Exception e) {}
			}
		}
	}

	/**
	 * 保存配置
	 * @param path
	 * @throws Exception 
	 */
	private static Document loadConfig(String path) throws Exception{    	
		File file = new File(path);
		if(!file.exists()){
			return null;
		}
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc =null;
			if(file.exists()){
				try{
					doc = builder.parse(new FileInputStream(file));	
				}catch(Exception e){
					doc = null;
				}
			}	

			return doc;
		} catch (Exception e) {
			throw new Exception("Load config ["+path+"] error!",e);
		} 
	}


	/**
	 * 
	 * @param project
	 * @param key
	 * @param value
	 * @return
	 */
	public static void setProjectProperty(IProject project,String key,String value){	
		if(project==null || project.getLocation()==null){
			return;
		}
		try {

			String path = getProjectConfigPath(project);
			Object obj = project.getSessionProperty(CONFIG_QUALIFIEDNAME);
			Document doc = null;
			if(obj instanceof Document){
				doc = (Document)obj;
			}else{
				//project.setPersistentProperty(new QualifiedName(PluginConstants.QUALIFIED_NAME, key),value);
				doc = loadConfig(path);
				if(doc==null){
					DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
					doc = builder.newDocument();
				}
				project.setSessionProperty(CONFIG_QUALIFIEDNAME,doc);
			}

			Element root = doc.getDocumentElement();
			if(root==null){
				root = doc.createElement(CONFIG_ROOT_NAME);
				doc.appendChild(root);
			}
			Element el = getPropertyValue(root,key);
			if(el==null){
				el = doc.createElement(PROPERTY_NAME);
				root.appendChild(el);
				el.setAttribute(ATTR_NAME, key);
			}			
			el.setTextContent(value);

			saveConfig(path,doc);//save

		} catch (Exception e) {
			e.printStackTrace();
		}
	}



	/**
	 * 获取指定项目的libs path列表
	 * @return
	 */
	public static List<String> getProjectLibsList(IProject project){
		List<String> list = new ArrayList<String>();
		String libsStr = PreferenceUtil.getProjectProperty(project,PluginConstants.LIBS_PROPERTY_KEY);
		if(libsStr!=null){
			String[] libs = libsStr.split(SEP_REG);
			for(String lib:libs){
				String lb = lib.trim();
				if(lb.length()>0){
					list.add(lb);
				}
			}
		}else{
			list.add(SELF_LIB_STR);	
		}
		return list;
	}

	/**
	 * 根据lib数据获取当前lib的类型
	 * @param libStr
	 * @return
	 */
	public static String getProjectLibType(String libStr){
		if(libStr==null){
			return null;
		}
		if(libStr.startsWith(LIB_TYPE_SELF)){
			return LIB_TYPE_SELF;
		}else if(libStr.startsWith(LIB_TYPE_EXTERNAL_FOLDER)){
			return LIB_TYPE_EXTERNAL_FOLDER;
		}else if(libStr.startsWith(LIB_TYPE_WORKSPACE_FOLDER)){
			return LIB_TYPE_WORKSPACE_FOLDER;
		}else{
			return null;
		}
	}

	/**
	 * 根据lib数据获取当前path的类型
	 * @param libStr
	 * @return
	 */
	public static String getProjectLibPath(String libStr){
		if(libStr==null){
			return null;
		}		
		libStr = libStr.trim();
		int idx = libStr.indexOf(LIB_SEP);
		if(idx>0){
			return libStr.substring(idx+LIB_SEP.length(),libStr.length());
		}else{
			return null;
		}
	}

	/**
	 * 获取指定项目的root path列表
	 * @return
	 */
	public static List<String> getProjectRootPathList(IProject project){
		List<String> list = new ArrayList<String>();
		String pathStr = getProjectProperty(project,PluginConstants.ROOT_PATH_PROPERTY_KEY);
		if(pathStr!=null){
			String[] libs = pathStr.split(SEP_REG);
			for(String lib:libs){
				String lb = lib.trim();
				if(lb.length()>0){
					list.add(lb);
				}
			}
		}
		return list;
	}

	/**
	 * 得到默认的container对应的rootpath文件夹,如没有设置返回<code>null</code>
	 * @param container
	 * @return
	 */
	public static IContainer getCurrentRoot(IContainer container){
		if(container==null){
			return null;
		}
		IContainer parent = container;
		IProject project = container.getProject();
		List<String> pathList = PreferenceUtil.getProjectRootPathList(project);
		while(parent!=null){
			String path = parent.getFullPath().toString();
			path = PreferenceUtil.getRootPathByIPath(project, path);			
			if(pathList.contains(path)){
				return parent;
			}
			parent = parent.getParent();
		}

		return null;
	}

	/**
	 * 
	 * @param containerPath
	 * @param rootPath
	 * @return
	 */
	public static String getRelativeRootPath(String containerPath ,String rootPath){
		if(containerPath.indexOf(rootPath)==0){
			return containerPath.substring(rootPath.length());
		}		
		return null;
	}

	/**
	 * 
	 * @param project
	 * @param ipath
	 */
	public static String getRootPathByIPath(IProject project,String ipath){
		String path = ipath;
		String projectPath = project.getFullPath().toString();
		if(path.indexOf(projectPath)==0){
			if(path.equals(projectPath)){
				path="/";
			}else{
				path = path.substring(projectPath.length());
			}
		}
		return path;
	}

	/**
	 * 
	 * @param project
	 * @param ipath
	 */
	public static String getRootPathByIPath(IProject project,IPath ipath){
		String path = ipath.toString();
		return PreferenceUtil.getRootPathByIPath(project, path);
	}

	/**
	 * 设置指定项目的root path列表
	 * @param project
	 * @param list
	 */
	public static void setProjectRootPathList(IProject project,List<String> list){
		if(list!=null){
			StringBuffer sb = new StringBuffer();	
			if(list!=null){
				for(int i=0,l=list.size();i<l;i++){
					if(i>0){
						sb.append("\n");
					}
					sb.append(list.get(i));
				}
			}
			setProjectProperty(project,PluginConstants.ROOT_PATH_PROPERTY_KEY, sb.toString());
		}		
	}

	/**
	 * 
	 * @param project
	 * @param list
	 */
	public static void setProjectAliasList(IProject project,List<AliasInfo> list){
		if(list!=null){	
			if(project==null || project.getLocation()==null){
				return;
			}
			try {

				String path = getProjectConfigPath(project);
				Object obj = project.getSessionProperty(CONFIG_QUALIFIEDNAME);
				Document doc = null;
				if(obj instanceof Document){
					doc = (Document)obj;
				}else{
					//project.setPersistentProperty(new QualifiedName(PluginConstants.QUALIFIED_NAME, key),value);
					doc = loadConfig(path);
					if(doc==null){
						DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
						doc = builder.newDocument();
					}
					project.setSessionProperty(CONFIG_QUALIFIEDNAME,doc);
				}

				Element root = doc.getDocumentElement();
				if(root==null){
					root = doc.createElement(CONFIG_ROOT_NAME);
					doc.appendChild(root);
				}
				NodeList alist = root.getElementsByTagName(ALIAS_LIST_NAME);

				//clear all
				if(alist!=null){
					for(int i=0,l=alist.getLength();i<l;i++){
						root.removeChild(alist.item(i));
					}
				}

				Element aliasListRoot = doc.createElement(ALIAS_LIST_NAME);
				root.appendChild(aliasListRoot);

				//add new list
				for(int i=0,l=list.size();i<l;i++){
					aliasListRoot.appendChild(createAliasInfoElement(doc,list.get(i)));
				}

				saveConfig(path,doc);//save

			} catch (Exception e) {
				e.printStackTrace();
			}
		}		
	}

	private static Element createAliasInfoElement(Document doc,AliasInfo info){
		Element item = doc.createElement(ALIAS_ITEM_NAME);
		item.setAttribute(ALIAS_ATTR_NAME, info.getName());
		item.setAttribute(ALIAS_ATTR_ALIAS, info.getAlias());
		return item;
	}

	/**
	 * 
	 * @return
	 */
	public static List<AliasInfo> getProjectAliasList(IProject project){
		List<AliasInfo> list = new ArrayList<AliasInfo>();

		if(project==null || project.getLocation()==null){
			return list;
		}
		try {
			Object obj = project.getSessionProperty(CONFIG_QUALIFIEDNAME);
			Document doc = null;
			if(obj instanceof Document){
				doc = (Document)obj;
			}else{
				//val = project.getPersistentProperty(new QualifiedName(PluginConstants.QUALIFIED_NAME, key));
				doc = loadConfig(getProjectConfigPath(project));
				if(doc==null){
					return list;
				}
				project.setSessionProperty(CONFIG_QUALIFIEDNAME,doc);
			}
			Element  root = doc.getDocumentElement();
			if(root!=null){
				NodeList alist = root.getElementsByTagName(ALIAS_LIST_NAME);

				//clear all
				if(alist!=null && alist.getLength()>0){
					NodeList items =((Element)alist.item(0)).getElementsByTagName(ALIAS_ITEM_NAME);
					Node item = null;
					AliasInfo info =null;
					for(int i=0,l=items.getLength();i<l;i++){
						item = items.item(i);
						if(item instanceof Element){
							list.add(convertToAliasInfo((Element)item));
						}
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}		

		return list;
	}

	private static AliasInfo convertToAliasInfo(Element el){
		return new AliasInfo(el.getAttribute(ALIAS_ATTR_ALIAS),el.getAttribute(ALIAS_ATTR_NAME));
	}

	/**
	 * 获取插件的PreferenceStore
	 * @return
	 */
	public static IPreferenceStore getPluginPreferenceStore(){
		return Activator.getDefault().getPreferenceStore();
	}

	/**
	 * 
	 * @return
	 */
	public static String getFileCharset(){
		String charset = PreferenceUtil.getPluginPreferenceStore().getString(PreferenceConstants.P_FILE_CHARSET);
		if(charset==null || charset.length()<=0){
			charset = PreferenceConstants.DEFAULT_FILE_CHARTSET;
		}
		return charset;
	}
	
	/**
	 * 
	 * @return
	 */
	public static String getDefineKeyWord(){
		String key = PreferenceUtil.getPluginPreferenceStore().getString(PreferenceConstants.P_DEFINE_KEY_WORD);
		if(key==null || key.length()<=0){
			key = PreferenceConstants.DEFAULT_DEFINE_KEY_WORD;
		}
		//key = key.trim();
		return key;
	}

	/**
	 * 
	 * @return
	 */
	public static int getParserEngineType(){
		String type = PreferenceUtil.getPluginPreferenceStore().getString(PreferenceConstants.P_PARSER_ENGINE);
		if(type==null || type.length()<=0){
			type = PreferenceConstants.DEFAULT_PARSER_ENGINE;
		}
		return PARSER_ENGINE_TYPE_SIMPLE.equalsIgnoreCase(type)?ParserFactory.TYPE_SIMPLE:ParserFactory.TYPE_RHINO;
	}
	
	/**
	 * 
	 * @return
	 */
	public static boolean isShowLibAnonymouseModule(){
		return PreferenceUtil.getPluginPreferenceStore().getBoolean(PreferenceConstants.P_SHOW_LIB_ANONYMOUSE);
	}
	
	/**
	 * 
	 * @return
	 */
	public static boolean isShowMatchStart(){
		return PreferenceUtil.getPluginPreferenceStore().getBoolean(PreferenceConstants.P_SHOW_MATCH_START);
	}
		
	/**
	 * 
	 * @return
	 */
	public static boolean isShowMatchPartial(){
		return PreferenceUtil.getPluginPreferenceStore().getBoolean(PreferenceConstants.P_SHOW_MATCH_PARTIAL);
	}
}
