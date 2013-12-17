package com.alibaba.just.api.parser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.mozilla.javascript.Node;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ast.ArrayLiteral;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.ExpressionStatement;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.PropertyGet;
import org.mozilla.javascript.ast.StringLiteral;

import com.alibaba.just.api.bean.Module;
import com.alibaba.just.util.FileUtil;

public class RhinoModuleParser extends AbstractModuleParser {
	/**
	 * 
	 */
	public RhinoModuleParser() {
		super();
	}

	/**
	 * @param charset
	 */
	public RhinoModuleParser(String charset) {
		super(charset);
	}

	private static final String DEFINE_KEY_REG = "^(\\w+\\.)*define$";


	/* (non-Javadoc)
	 * @see com.alibaba.just.api.parser.ModuleParser#getModules(java.io.File, int)
	 */
	public List<Module> getModules(File file,int moduleType,ParserEvent event){
		List<Module> moduleList = new ArrayList<Module>();		
		String absPath = file.getAbsolutePath();
		try{
			if(!isDispose && file.exists() && (filter==null || (filter!=null && filter.accept(file)))){
				String content = null;
				try {
					content = FileUtil.getFileContent(file, charset);
				} catch (Exception e) {}

				if(content==null){return null ;}


				/*CompilerEnvirons ce = new CompilerEnvirons();
			ce.setRecordingLocalJsDocComments(true);
			ce.setRecordingComments(true);
			Parser  parser = new  Parser(ce);*/	

				Parser  parser = new Parser();
				AstRoot astRoot = parser.parse(content, null, 0);
				Node node = astRoot.getFirstChild();
				while(node!=null){
					//System.out.println(node.getClass().getName());
					if(ExpressionStatement.class.isInstance(node)){
						ExpressionStatement es = (ExpressionStatement)node;
						AstNode astNode = es.getExpression();
						if(FunctionCall.class.isInstance(astNode)){
							FunctionCall fc = (FunctionCall)astNode;
							fc = this.getFirstFunctionCall(fc);
							AstNode nameNode = fc.getTarget();
							//System.out.println("NAME:"+n.getIdentifier());	
							if(Pattern.matches(DEFINE_KEY_REG,nameNode.toSource().trim())){

								List<AstNode> args = fc.getArguments();
								//校验3参数普通模块
								//侦测define("module",["required1","required2"...],function(){...});
								if(args.size()==3 && (moduleType == MODULE_TYPE_ALL || moduleType == MODULE_TYPE_NORMAL)){
									AstNode stringNode = args.get(0);
									AstNode arrayNode = args.get(1);
									AstNode funNode = args.get(2);

									if(StringLiteral.class.isInstance(stringNode) &&
											ArrayLiteral.class.isInstance(arrayNode) &&
											FunctionNode.class.isInstance(funNode)){
										Module module = new Module();
										module.setAnonymous(false);
										module.setName(((StringLiteral)stringNode).getValue(false));
										module.getRequiredModuleNames().addAll(getRequiredModules((ArrayLiteral)arrayNode));
										module.setFilePath(absPath);
										updateAlias(module,this.getAliasList());//update module alias
										moduleList.add(module);
									}
								}

								if(args.size()==2){
									AstNode node1 = args.get(0);
									AstNode node2 = args.get(1);
									
									//校验2参数的普通模块
									//侦测define("module",function(){...});
									if((moduleType == MODULE_TYPE_ALL || moduleType == MODULE_TYPE_NORMAL) && StringLiteral.class.isInstance(node1) &&
											FunctionNode.class.isInstance(node2)){
										Module module = new Module();
										module.setAnonymous(false);
										module.setName(((StringLiteral)node1).getValue(false));
										module.setFilePath(absPath);
										updateAlias(module,this.getAliasList());//update module alias
										moduleList.add(module);
									}
									
									//校验2参数匿名模块
									//侦测define(["required1","required2"...],function(){...});
									if((moduleType == MODULE_TYPE_ALL || moduleType == MODULE_TYPE_ANONYMOUS) && ArrayLiteral.class.isInstance(node1) &&
											FunctionNode.class.isInstance(node2)){
										Module module = new Module();
										module.setAnonymous(true);
										module.getRequiredModuleNames().addAll(getRequiredModules((ArrayLiteral)node1));
										module.setFilePath(absPath);
										moduleList.add(module);
									}
								}
								
								//校验1参数的匿名模块
								//侦测define(function(){...});
								if(args.size()==1 && (moduleType == MODULE_TYPE_ALL || moduleType == MODULE_TYPE_ANONYMOUS)){
									AstNode node1 = args.get(0);
									if(FunctionNode.class.isInstance(node1)){
										Module module = new Module();
										module.setAnonymous(true);
										module.setFilePath(absPath);
										moduleList.add(module);
									}
								}

							}

						}
					}
					node = node.getNext();
				}
				if(event!=null){
					event.onParseFileSuccess(this,file,moduleList);
				}
			}	

		}catch(Exception e){
			//文件解析异常暂时不处理
			//throw new ModuleParseException("Failed to parse file ["+absPath+"]:"+(e.getMessage()==null?e.toString():e.getMessage()),e);
		}
		
		if(event!=null){
			event.onParseFileEnd(this,file);
		}

		return moduleList;
	}

	/**
	 * Fix 如：define(['jquery'],function($){}).register();之类的用法
	 * TODO：以后去掉对非AMD标准匿名模块的支持
	 * @param funcall
	 * @return
	 */
	private FunctionCall getFirstFunctionCall(FunctionCall funcall){
		AstNode target = funcall.getTarget();
		if(PropertyGet.class.isInstance(target)){
			AstNode left = ((PropertyGet)target).getLeft();
			if(FunctionCall.class.isInstance(left)){
				return getFirstFunctionCall((FunctionCall)left);
			}else{
				return funcall;
			}
		}		
		return funcall;
	}

	/**
	 * 
	 * @param module
	 * @return
	 */
	private List<String> getRequiredModules(ArrayLiteral array){
		List<String> list = new ArrayList<String>();
		List<AstNode> strList = array.getElements();		
		for(AstNode strNode:strList){
			if(StringLiteral.class.isInstance(strNode)){
				list.add(((StringLiteral)strNode).getValue(false));
			}
		}
		return list;
	}

}
