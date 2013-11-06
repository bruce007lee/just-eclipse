/**
 * 
 */
package com.alibaba.just.api.parser;

import java.io.File;
import java.util.List;

import com.alibaba.just.api.bean.Module;

/**
 * @author bruce.liz
 *
 */
public interface ParserEvent {
	public void onParseFileSuccess(ModuleParser parser,File file,List<Module> module);
	public void onParseFileEnd(ModuleParser parser,File file);
}
