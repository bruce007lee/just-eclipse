/**
 * 
 */
package com.alibaba.just.ui.listener;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;

import com.alibaba.just.PluginConstants;
import com.alibaba.just.ui.cache.ResourceCacheManager;
import com.alibaba.just.ui.util.PluginResourceUtil;

/**
 * deal with module cache remove
 * @author bruce.liz
 *
 */
public class CacheRemoveListener implements IResourceChangeListener {

	private static IResourceDeltaVisitor vistor = new IResourceDeltaVisitor(){

		public boolean visit(IResourceDelta delta)
				throws CoreException {
			//System.out.println(delta +" "+ delta.getKind());
			if(  delta.getResource()!=null
					&& IResource.FILE==delta.getResource().getType()
					&& PluginConstants.JAVASCRIPT_EXT.equalsIgnoreCase(delta.getResource().getFileExtension()) ){
				
				if(delta.getKind()==IResourceDelta.MOVED_TO || delta.getKind()==IResourceDelta.REMOVED){
				  System.out.println("remove cache file:"+ PluginResourceUtil.getResourceCacheKey( delta.getResource()));
				  ResourceCacheManager.remove(PluginResourceUtil.getResourceCacheKey(delta.getResource()));
				}

				//remove lib cache time stamp
				IResource p = delta.getResource();
				while(p!=null){
					p.setPersistentProperty(PluginConstants.CACHE_QUALIFIEDNAME, null);//clear cache stamp
					p = p.getParent();
				}
			}
			return true;
		}
		
	};

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
	 */
	public void resourceChanged(IResourceChangeEvent event){
		try {
			if(event!=null && event.getDelta()!=null){
			   event.getDelta().accept(vistor);
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		//System.out.println("type:"+event.getType()+" resource:"+event.getResource()+" source:"+event.getSource());
	}

}
