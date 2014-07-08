package com.alibaba.just;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.alibaba.just.ui.cache.ResourceCacheManager;
import com.alibaba.just.ui.listener.CacheRemoveListener;
import com.alibaba.just.ui.util.ImageManager;
import com.alibaba.just.ui.util.PluginResourceUtil;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.alibaba.just"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	private static InstanceScope is;
	
	private CacheRemoveListener cacheRemoveListener = null;
		
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		ImageManager.init(this);	
		cacheRemoveListener = new CacheRemoveListener();		
		PluginResourceUtil.getWorkspace().addResourceChangeListener(cacheRemoveListener);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		PluginResourceUtil.getWorkspace().removeResourceChangeListener(cacheRemoveListener);
		cacheRemoveListener = null;
		ResourceCacheManager.shutdown();
		plugin = null;
		ImageManager.dispose();
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}
	
	public static InstanceScope getInstanceScope(){
		if(is == null){
			is = new InstanceScope();
		}
		return is;
	}
	
}