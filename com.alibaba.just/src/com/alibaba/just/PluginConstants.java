package com.alibaba.just;

import org.eclipse.core.runtime.QualifiedName;

public class PluginConstants {
	public static final String QUALIFIED_NAME = "com.alibaba.just";
	public static final String LIBS_PROPERTY_KEY = "just_libs_property_key";
	public static final String ROOT_PATH_PROPERTY_KEY = "just_root_path_property_key";
	public static final String JAVASCRIPT_LIBS_PROPERTY_KEY = "just_javascript_libs_property_key";
	
	public static final String MODULE_MARKER_ID = "com.alibaba.just.ui.markers";
	public static final String MODULE_MARKER_ATTR_TIMESTAMP = "timestamp";
	public static final String MODULE_MARKER_ATTR_INFO = "moduleinfo";
	public static final String MODULE_MARKER_ATTR_LIB_PATH = "libpath";
	
	public static final String JAVASCRIPT_EXT = "js";
	
	public static final QualifiedName CACHE_QUALIFIEDNAME = new QualifiedName(QUALIFIED_NAME,"stamp");
}
