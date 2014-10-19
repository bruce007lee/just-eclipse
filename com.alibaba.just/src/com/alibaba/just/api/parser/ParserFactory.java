/**
 * 
 */
package com.alibaba.just.api.parser;

/**
 * @author bruce.liz
 *
 */
public class ParserFactory {

	public static final int MD_TYPE_AMD = ModuleParser.MD_TYPE_AMD;
	public static final int MD_TYPE_CMD = ModuleParser.MD_TYPE_CMD;
	public static final int MD_TYPE_UMD = ModuleParser.MD_TYPE_UMD;
	public static final int DEFAULT_MD_TYPE =MD_TYPE_UMD;

	private ParserFactory(){};

	public static ModuleParser getModuleParser(ParserOptions options){
		return new RhinoModuleParser(options);
	}

	public static ModuleParser getModuleParser(){
		return getModuleParser(null);
	}

}
