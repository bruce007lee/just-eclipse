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
import com.alibaba.just.ui.preferences.PreferenceConstants;

public class PreferenceUtil {	

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
			if(Document.class.isInstance(obj)){
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
		for(int i=0,l=nList.getLength();i<l;i++){
			if(nList.item(i).getNodeName().equals(tagName)){
				list.add(nList.item(i));
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
			if(Element.class.isInstance(n)){
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
			if(Document.class.isInstance(obj)){
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
}
