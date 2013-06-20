package com.alibaba.just.ui.util;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class ImageManager {
	
	public static final String IMG_MODULE_ICON = "img_module_icon";
	public static final String IMG_LIB = "img_lib_icon";
	public static final String IMG_LIB_FOLDER = "img_lib_folder_icon";
	public static final String IMG_LIB_PROJECT = "img_lib_project_icon";
	public static final String IMG_ROOT_PATH = "img_root_icon";
	
	public static final String IMG_FLAT_LAYOUT = "img_flat_layout";
	public static final String IMG_HIERARCHICAL_LAYOUT = "img_hierarchical_layout";
	public static final String IMG_REFRESH = "img_refresh";
	public static final String IMG_USED_LIST = "img_used_list";
	
	private static ImageRegistry imageRegistry = null;
	private static URL baseURL = null;
	private ImageManager(){}
	
	public static void init(AbstractUIPlugin plugin){
		baseURL = plugin.getBundle().getEntry("/");
		imageRegistry = plugin.getImageRegistry();
		ImageManager.putImage(IMG_MODULE_ICON, "icons/module.gif");
		ImageManager.putImage(IMG_LIB, "icons/lib.gif");
		ImageManager.putImage(IMG_LIB_FOLDER, "icons/lib_f.gif");
		ImageManager.putImage(IMG_LIB_PROJECT, "icons/lib_p.gif");
		ImageManager.putImage(IMG_ROOT_PATH, "icons/root.gif");
		
		ImageManager.putImage(IMG_FLAT_LAYOUT, "icons/flatLayout.gif");
		ImageManager.putImage(IMG_HIERARCHICAL_LAYOUT, "icons/hierarchicalLayout.gif");
		ImageManager.putImage(IMG_REFRESH, "icons/refresh.gif");
		ImageManager.putImage(IMG_USED_LIST, "icons/usedList.gif");
	}
	
	public static void dispose(){
		imageRegistry = null;
	}
	
	public static void putImage(String key,String imagePath){
		try {
			imageRegistry.put(key,ImageDescriptor.createFromURL(new URL(baseURL,imagePath)));
		} catch (MalformedURLException e) {
		}
	}
	
	public static void putImage(String key,Image image){
		imageRegistry.put(key, image);
	}
	
	public static Image getImage(String key){
		return imageRegistry.get(key);
	}
}
