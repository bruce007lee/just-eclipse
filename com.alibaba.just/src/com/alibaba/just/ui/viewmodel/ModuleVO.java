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

	public boolean isUseAlias() {
		return isUseAlias;
	}

	public void setUseAlias(boolean isUseAlias) {
		this.isUseAlias = isUseAlias;
	}
      
}
