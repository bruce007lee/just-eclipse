package com.alibaba.just.api.converter.impl;

import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import com.alibaba.just.api.converter.NameConverter;
import com.alibaba.just.api.util.FormatUtil;

public class NodeJsJavascriptNameConverter implements NameConverter{
	private String script = null;
	/**
	 * 
	 */
	public NodeJsJavascriptNameConverter(String script) {
		super();
		this.script = script;
	}

	private String executeScript(String path){
		Context cx = Context.enter();
		Object name = null;
		try {
			Scriptable scope = cx.initStandardObjects();
			cx.evaluateString(scope, script, null,0, null);
			Object fun = scope.get("convert", scope);
			if(fun instanceof Callable){
				name = ((Callable)fun).call(cx, scope, scope, new Object[]{path});
			}

		}catch(Exception e){
		}finally {
			Context.exit();
		}
		if(name==null){
			return FormatUtil.getName(path);
		}
		return name.toString();
	}


	/*
	 * (non-Javadoc)
	 * @see com.alibaba.just.api.converter.NameConverter#convert(java.lang.Object)
	 */
	public String convert(Object obj) {
		String path = (String)obj;
		return executeScript(FormatUtil.formatPath(path));
	}

	public static void main(String[]  args){
		String script = "function convert(a){return '<'+a+'>';}";
		NameConverter  cn = new NodeJsJavascriptNameConverter(script);
		cn.convert("test");
	}

}
