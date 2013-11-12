/**
 * 
 */
package com.alibaba.just.api.bean;

/**
 * @author bruce.liz
 *
 */
public class AliasInfo {
	
	private String name=null;
	private String alias=null;
	private boolean isRegExp=false;

	/**
	 * 
	 */
	public AliasInfo() {}
	
	/**
	 * 
	 * @param alias The module alias
	 * @param name The original module name
	 */
	public AliasInfo(String alias,String name) {
		this.name=name;
		this.alias=alias;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public boolean isRegExp() {
		return isRegExp;
	}

	public void setRegExp(boolean isRegExp) {
		this.isRegExp = isRegExp;
	}

	public boolean equals(Object obj) {
		if(AliasInfo.class.isInstance(obj)){
			AliasInfo o = (AliasInfo)obj;
			if(((o.alias==null && this.alias==null) || (o.alias!=null && o.alias.equals(this.alias)))
					&& ((o.name==null && this.name==null) || (o.name!=null && o.name.equals(this.name)))
					&& o.isRegExp == this.isRegExp()){
				return true;
			}
		}
		return false;
	}

}
