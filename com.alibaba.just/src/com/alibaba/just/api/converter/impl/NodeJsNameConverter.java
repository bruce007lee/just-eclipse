package com.alibaba.just.api.converter.impl;

import java.util.List;

import com.alibaba.just.api.converter.NameConverter;
import com.alibaba.just.api.util.FormatUtil;

public class NodeJsNameConverter implements NameConverter{
	public static final int FORMAT_TYPE_ORI = 0;
	public static final int FORMAT_TYPE_CAMEL = 1;
	public static final int FORMAT_TYPE_HORLINE = 2;
	private List<String> patterns = null;
	private int formatType = FORMAT_TYPE_CAMEL;

	/**
	 * 
	 */
	public NodeJsNameConverter(List<String> patterns,int formatType) {
		super();
		this.setPatterns(patterns);
		this.formatType = formatType;
	}
	
	public NodeJsNameConverter(List<String> patterns){
		super();
		this.setPatterns(patterns);
	}

	public NodeJsNameConverter(){
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see com.alibaba.just.api.converter.NameConverter#convert(java.lang.Object)
	 */
	public String convert(Object obj) {
		String path = (String)obj;
		path = FormatUtil.getPathName(path);
		boolean done = false;
		if(patterns!=null && path!=null){
			for(String p:patterns){
				if(path.indexOf(p)==0){
					path = path.substring(p.length(),path.length());
					done = true;
					break;
				}
			}
		}
		if(!done && path!=null){
			path = FormatUtil.getName(path);
			if(formatType==FORMAT_TYPE_CAMEL){
				path = FormatUtil.toCamelPath(path);
			}else if(formatType==FORMAT_TYPE_HORLINE){
				path = FormatUtil.toHorLinePath(path);
			}
		}
		return path;
	}

	public List<String> getPatterns() {
		return patterns;
	}

	public void setPatterns(List<String> patterns) {
		if(patterns!=null){
			for(int i=0,l=patterns.size();i<l;i++){
				patterns.set(i, FormatUtil.formatPath(patterns.get(i)));
			}
		}
		this.patterns = patterns;
	}

}
