/**
 * 
 */
package com.alibaba.just.ui.cache;

/**
 * @author bruce.liz
 *
 */
public class CacheConfiguration {

	private boolean overflowToDisk = false;
	private boolean diskPersistent = false;
	private int cacheSize = 10;
	private int timeToLiveSeconds = 0;
	private int timeToIdleSeconds = 0;
	
	public boolean isOverflowToDisk() {
		return overflowToDisk;
	}
	public void setOverflowToDisk(boolean overflowToDisk) {
		this.overflowToDisk = overflowToDisk;
	}
	public boolean isDiskPersistent() {
		return diskPersistent;
	}
	public void setDiskPersistent(boolean diskPersistent) {
		this.diskPersistent = diskPersistent;
	}
	public int getCacheSize() {
		return cacheSize;
	}
	public void setCacheSize(int cacheSize) {
		this.cacheSize = cacheSize;
	}
	public int getTimeToLiveSeconds() {
		return timeToLiveSeconds;
	}
	public void setTimeToLiveSeconds(int timeToLiveSeconds) {
		this.timeToLiveSeconds = timeToLiveSeconds;
	}
	public int getTimeToIdleSeconds() {
		return timeToIdleSeconds;
	}
	public void setTimeToIdleSeconds(int timeToIdleSeconds) {
		this.timeToIdleSeconds = timeToIdleSeconds;
	}
}
