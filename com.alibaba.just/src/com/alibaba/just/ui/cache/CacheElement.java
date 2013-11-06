package com.alibaba.just.ui.cache;

import java.io.Serializable;

public class CacheElement implements Serializable {	
	private static final long serialVersionUID = 2840011186944181805L;
	
	private Object stamp;
	private Object value;

	/**
	 * @param stamp
	 * @param value
	 */
	public CacheElement(Object stamp, Object value) {
		super();
		this.stamp = stamp;
		this.value = value;
	}
	
	/**
	 * @param stamp
	 * @param value
	 */
	public CacheElement(Object value) {
		super();
		this.value = value;
	}
	
	public Object getStamp() {
		return stamp;
	}
	public void setStamp(Object stamp) {
		this.stamp = stamp;
	}
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
}
