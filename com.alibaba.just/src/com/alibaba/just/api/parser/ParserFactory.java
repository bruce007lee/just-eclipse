/**
 * 
 */
package com.alibaba.just.api.parser;

/**
 * @author bruce.liz
 *
 */
public class ParserFactory {
  public static final int TYPE_SIMPLE = 1;	
  public static final int TYPE_RHINO = 0;	
  public static int TYPE_DEFAULT = TYPE_RHINO;
	
   private ParserFactory(){};
   
   public static ModuleParser getModuleParser(String charset){
	   return getModuleParser(TYPE_DEFAULT,charset);
   }
   
   public static ModuleParser getModuleParser(int type,String charset){
	   if(type==TYPE_SIMPLE){
		   return new SimpleModuleParser(charset);
	   }else if(type==TYPE_RHINO){
		   return new RhinoModuleParser(charset);
	   }else{
		   return new RhinoModuleParser(charset);
	   }
   }
   
   public static ModuleParser getModuleParser(){
	   return getModuleParser(TYPE_DEFAULT);
   }
   
   public static ModuleParser getModuleParser(int type){
	   if(type==TYPE_SIMPLE){
		   return new SimpleModuleParser();
	   }else if(type==TYPE_RHINO){
		   return new RhinoModuleParser();
	   }else{
		   return new RhinoModuleParser();
	   }
   }
   
}
