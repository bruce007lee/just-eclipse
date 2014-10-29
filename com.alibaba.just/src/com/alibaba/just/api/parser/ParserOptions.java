package com.alibaba.just.api.parser;

import java.util.List;

import com.alibaba.just.api.bean.AliasInfo;

public class ParserOptions {

	private int mdType = ParserFactory.DEFAULT_MD_TYPE;
	private String charset;
	private String defineKeyWord;
	private String requireKeyWord;
	private boolean isNodeJs=false;
	private List<AliasInfo> aliasList = null;		

	public int getMdType() {
		return mdType;
	}
	public void setMdType(int mdType) {
		this.mdType = mdType;
	}
	public String getCharset() {
		return charset;
	}
	public void setCharset(String charset) {
		this.charset = charset;
	}
	public String getDefineKeyWord() {
		return defineKeyWord;
	}
	public void setDefineKeyWord(String defineKeyWord) {
		this.defineKeyWord = defineKeyWord;
	}
	public String getRequireKeyWord() {
		return requireKeyWord;
	}
	public void setRequireKeyWord(String requireKeyWord) {
		this.requireKeyWord = requireKeyWord;
	}
	public boolean isNodeJs() {
		return isNodeJs;
	}
	public void setIsNodeJs(boolean isNodeJs) {
		this.isNodeJs = isNodeJs;
	}
	public List<AliasInfo> getAliasList() {
		return aliasList;
	}
	public void setAliasList(List<AliasInfo> aliasList) {
		this.aliasList = aliasList;
	}

}
