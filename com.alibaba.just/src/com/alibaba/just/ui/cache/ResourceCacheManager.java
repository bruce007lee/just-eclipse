/**
 * 
 */
package com.alibaba.just.ui.cache;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.DiskStoreConfiguration;
import net.sf.ehcache.config.MemoryUnit;
import net.sf.ehcache.config.PersistenceConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration.Strategy;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import com.alibaba.just.ui.util.LogUtil;
import com.alibaba.just.ui.util.PluginResourceUtil;

/**
 * @author bruce.liz
 *
 */
public class ResourceCacheManager {
	private static CacheManager manager=null; 	
	private static ResourceCacheManager instance = null;
	private static final String cacheName = "moduleInfoCache"; 	

	private ResourceCacheManager(){
		init();
	}

	private void init(){
		if(manager==null){
			DiskStoreConfiguration diskCfg = new DiskStoreConfiguration();
			diskCfg.setPath(PluginResourceUtil.getWorkspacePath()+"/.justcache");
			Configuration config = new Configuration().diskStore(diskCfg);
			manager = CacheManager.create(config);

			if(getCache()==null){
				CacheConfiguration cfg = new CacheConfiguration();
				//cfg.persistence(new PersistenceConfiguration().strategy(Strategy.LOCALTEMPSWAP));
				//cfg.overflowToDisk(true).diskPersistent(true);
				cfg.overflowToDisk(false).diskPersistent(false);
				Cache cache = new Cache(cfg
						.name(cacheName)
						.maxBytesLocalHeap(20, MemoryUnit.MEGABYTES)
						.memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LFU)
						.eternal(false)
						.timeToLiveSeconds(0)
						.timeToIdleSeconds(0)
				);
				manager.addCache(cache);
			}
		}
	}

	public static CacheManager getCacheManager(){
		if(instance==null){
			instance = new ResourceCacheManager();
		}
		return manager;
	}

	public static Cache getCache(){
		return getCacheManager().getCache(cacheName);
	}

	public static void flush(){
		getCache().flush();
	}

	public static void remove(Object obj){
		//LogUtil.log("before cache size:"+ResourceCacheManager.getCache().getSize());
		getCache().remove(obj);
		//LogUtil.log("after remove cache size:"+ResourceCacheManager.getCache().getSize());
	}

	public static void put(Object obj,CacheElement cache,boolean isFlush){
		//LogUtil.log("create cache:"+obj);
		getCache().put(new Element(obj,cache));
		if(isFlush){
			flush();
		}
	}

	public static void put(Object obj,CacheElement cache){
		put(obj,cache,false);
	}

	public static CacheElement get(Object obj){
		Element el = getCache().get(obj);
		if(el!=null){
			Object cel = el.getObjectValue();
			if(CacheElement.class.isInstance(cel)){
				return (CacheElement)cel;
			}
		}
		return null;
	}

	public static void removeAll(){
		getCache().removeAll();
		//LogUtil.log("after remove all cache size:"+ResourceCacheManager.getCache().getSize());
	}

	public static void shutdown(){
		if(manager!=null){
			LogUtil.log("Shutdown resourceCacheManage ...");
			manager.shutdown();
		}
	}
}
