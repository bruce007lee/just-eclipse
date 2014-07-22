/**
 * 
 */
package com.alibaba.just.ui.viewmodel;

import com.alibaba.just.api.bean.Module;

/**
 * @author bruce.liz
 *
 */
public class ModuleVO extends Module {
    private boolean isUseAlias = false;
    private String currentAlias = null;

	public boolean isUseAlias() {
		return isUseAlias;
	}

	public void setUseAlias(boolean isUseAlias) {
		this.isUseAlias = isUseAlias;
	}

	public String getCurrentAlias() {
		return currentAlias;
	}

	public void setCurrentAlias(String currentAlias) {
		this.currentAlias = currentAlias;
	}
      
}
