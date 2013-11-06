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
	public void onEnd(ModuleParser parser,File file,List<Module> module);
	public void onDispose(ModuleParser parser);
}
