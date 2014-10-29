package com.alibaba.just.api.parser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ast.ArrayLiteral;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.NodeVisitor;
import org.mozilla.javascript.ast.PropertyGet;
import org.mozilla.javascript.ast.StringLiteral;

import com.alibaba.just.api.bean.Module;
import com.alibaba.just.api.converter.impl.NodeJsNameConverter;
import com.alibaba.just.util.FileUtil;

public class RhinoModuleParser extends AbstractModuleParser {

	private static final String DEFINE_KEY_REG = "^(\\w+\\.)*(define)$";
	private static final String REQUIRE_KEY_REG = "^require$";

	protected String defineKeyWord=DEFINE_KEY_REG;
	protected String requireKeyWord=REQUIRE_KEY_REG;

	private static final NodeJsNameConverter DEFAULT_CMD_CONVERT = new NodeJsNameConverter();

	public RhinoModuleParser(ParserOptions options){
		if(options!=null){
			if(options.getCharset()!=null){
				charset = options.getCharset();	
			}
			String defineKeyWord = DEFINE_KEY_REG;
			if(options.getDefineKeyWord()!=null){
				defineKeyWord = options.getDefineKeyWord();
			}
			this.setDefineKeyWord(defineKeyWord);
			String requireKeyWord = REQUIRE_KEY_REG;
			if(options.getRequireKeyWord()!=null){
				requireKeyWord = options.getRequireKeyWord();
			}
			this.setRequireKeyWord(requireKeyWord);
			this.mdType = options.getMdType();
			this.setIsNodeJs(options.isNodeJs());
		}
	}

	/**
	 * 
	 */
	public RhinoModuleParser() {
		this(null);
	}

	/*
	 * (non-Javadoc)
	 * @see com.alibaba.just.api.parser.ModuleParser#setDefineKeyWord(java.lang.String)
	 */
	public void setDefineKeyWord(String str){
		if(str!=null && str.length()>0){
			this.defineKeyWord = str;
		}
	}

	public void setRequireKeyWord(String str){
		if(str!=null && str.length()>0){
			this.requireKeyWord = str;
		}
	}

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

				ModuleNodeVisitor visitor = new ModuleNodeVisitor(absPath,moduleList,moduleType);
				astRoot.visit(visitor);
				if(isNodeJs && visitor.getCmdModule()==null){
					//nodejs中每个文件都为模块
					moduleList.add(createCMDModule(absPath));
				}
				visitor.dispose();
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
		if(target instanceof PropertyGet){
			AstNode left = ((PropertyGet)target).getLeft();
			if(left instanceof FunctionCall){
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
			if(strNode instanceof StringLiteral){
				list.add(((StringLiteral)strNode).getValue(false));
			}
		}
		return list;
	}

	private Module createCMDModule(String absPath){
		Module cmdModule = new Module();
		cmdModule.setFilePath(absPath);
		if(isNodeJs){
			//nodejs 中每个js文件都为模块
			if(nameConverter!=null){
				cmdModule.setName(nameConverter.convert(absPath));
			}else{
				cmdModule.setName(DEFAULT_CMD_CONVERT.convert(absPath));
			}
			cmdModule.setAnonymous(false);
			updateAlias(cmdModule,RhinoModuleParser.this.getAliasList());//update module alias
		}else{
			cmdModule.setAnonymous(true);
		}
		return cmdModule;
	}


	/**
	 * inner class ,deal with js ast node visitor,
	 * search the AMD module.
	 */
	class ModuleNodeVisitor implements NodeVisitor{

		private int moduleType;
		private List<Module>  moduleList;
		private String absPath = null;

		private Module cmdModule = null;//cmd模式一个文件就是一个模块

		ModuleNodeVisitor(String absPath,List<Module>  moduleList,int moduleType){
			this.moduleType = moduleType;
			this.moduleList = moduleList;
			this.absPath = absPath;
		}

		public void dispose(){
			moduleList = null;
			absPath=null;
			cmdModule=null;
		}

		public boolean visit(AstNode node) {
			if(node instanceof  FunctionCall){	

				/*AMD逻辑*/
				FunctionCall fc = (FunctionCall)node;
				fc = RhinoModuleParser.this.getFirstFunctionCall(fc);
				AstNode nameNode = fc.getTarget();
				//System.out.println("NAME:"+n.getIdentifier());	
				if((mdType== MD_TYPE_AMD || mdType== MD_TYPE_UMD) && Pattern.matches(defineKeyWord,nameNode.toSource().trim())){
					Module module = null;
					List<AstNode> args = fc.getArguments();
					//校验3参数普通模块
					//侦测define("module",["required1","required2"...],function(){...});
					if(args.size()==3 && (moduleType == RhinoModuleParser.MODULE_TYPE_ALL || moduleType == RhinoModuleParser.MODULE_TYPE_NORMAL)){
						AstNode stringNode = args.get(0);
						AstNode arrayNode = args.get(1);
						AstNode funNode = args.get(2);

						if(stringNode instanceof StringLiteral &&
								arrayNode instanceof ArrayLiteral &&
								funNode instanceof FunctionNode){
							module = new Module();
							module.setAnonymous(false);
							module.setName(((StringLiteral)stringNode).getValue(false));
							module.getRequiredModuleNames().addAll(getRequiredModules((ArrayLiteral)arrayNode));
							module.setFilePath(absPath);
							updateAlias(module,RhinoModuleParser.this.getAliasList());//update module alias
							moduleList.add(module);
						}
					}else if(args.size()==2){
						AstNode node1 = args.get(0);
						AstNode node2 = args.get(1);

						//校验2参数的普通模块
						//侦测define("module",function(){...});
						if((moduleType == MODULE_TYPE_ALL || moduleType == MODULE_TYPE_NORMAL) && node1 instanceof StringLiteral &&
								node2 instanceof FunctionNode){
							module = new Module();
							module.setAnonymous(false);
							module.setName(((StringLiteral)node1).getValue(false));
							module.setFilePath(absPath);
							updateAlias(module,RhinoModuleParser.this.getAliasList());//update module alias
							moduleList.add(module);
						}

						//校验2参数匿名模块
						//侦测define(["required1","required2"...],function(){...});
						if((moduleType == MODULE_TYPE_ALL || moduleType == MODULE_TYPE_ANONYMOUS) && node1 instanceof ArrayLiteral &&
								node2 instanceof FunctionNode){
							module = new Module();
							module.setAnonymous(true);
							module.getRequiredModuleNames().addAll(getRequiredModules((ArrayLiteral)node1));
							module.setFilePath(absPath);
							moduleList.add(module);
						}
					}else if(args.size()==1 && (moduleType == MODULE_TYPE_ALL || moduleType == MODULE_TYPE_ANONYMOUS)){
						//校验1参数的匿名模块
						//侦测define(function(){...});

						AstNode node1 = args.get(0);
						if(node1 instanceof FunctionNode){
							module = new Module();
							module.setAnonymous(true);
							module.setFilePath(absPath);
							moduleList.add(module);
						}
					}

					if(module!=null){
						cmdModule = module;
					}

				}else if((mdType== MD_TYPE_CMD || mdType== MD_TYPE_UMD) && Pattern.matches(requireKeyWord,nameNode.toSource().trim())){
					/*CMD逻辑*/
					List<AstNode> args = fc.getArguments();
					//校验cmd构造形式
					//侦测require("module");
					if(args.size()==1){
						AstNode stringNode = args.get(0);

						if(stringNode instanceof StringLiteral){
							if(cmdModule==null){
								cmdModule = createCMDModule(absPath);
								moduleList.add(cmdModule);
							}
							cmdModule.getRequiredModuleNames().add(((StringLiteral)stringNode).getValue(false));
						}
					}

				}
			}
			return true;
		}

		public Module getCmdModule() {
			return cmdModule;
		}

	}


}
