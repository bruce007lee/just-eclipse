package com.alibaba.just.api.parser;

import java.util.List;

import com.alibaba.just.api.bean.Module;

public class ParserUtil {

	/**
	 * 指定的名称是否为指定模块的别名
	 * @param moduleAlias
	 * @param submod
	 * @return
	 */
	public static boolean isMatchAlias(String moduleAlias,Module submod){
		if(moduleAlias!=null && submod.getAlias()!=null){
			List<String> list = submod.getAlias();
			for(String alias:list){
				if(moduleAlias.equals(alias)){
					return true;
				}
			}
		}
		return false;
	}

}
