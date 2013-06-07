package com.alibaba.just.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtil {
	
	private FileUtil(){}
	
	/**
	 * 
	 * @param file
	 * @param encode
	 * @return
	 * @throws Exception
	 */
	public static void saveFileContent(String file,String content,String encode) throws Exception {
		OutputStream os = null;
		try{
			os = new FileOutputStream(file);
			
			if(encode!=null){
			   os.write(content.getBytes(encode));
			}else{
			   os.write(content.getBytes());
			}
			os.flush();
			os.close();
		}catch(Exception e){
			if(os!=null){
				os.close();
				os= null;
			}

			throw e;
		}
		
	}
	
	
	/**
	 * 
	 * @param file
	 * @param encode
	 * @return
	 * @throws Exception
	 */
	public static String getFileContent(InputStream inputStream,String encode) throws Exception {
		InputStream fis = null;
		ByteArrayOutputStream sb = null;
		try{
			fis = inputStream;
			sb = new ByteArrayOutputStream();
			byte[] bytes = new byte[128];
			int n = -1;
			n=fis.read(bytes);
			while(n>-1){
				sb.write(bytes,0,n);
				n=fis.read(bytes);				
			}
			String content = new String(sb.toByteArray(),encode==null?"GBK":encode);
			
			fis.close();
			sb.close();
			
			return content;
		}catch(Exception e){
			if(fis!=null){
				fis.close();
				fis= null;
			}
			if(sb!=null){
				sb.close();
				sb=null;
			}
			throw e;
		}
	}
	

	/**
	 * 
	 * @param file
	 * @param encode
	 * @return
	 * @throws Exception
	 */
	public static String getFileContent(File file,String encode) throws Exception {
		FileInputStream fis = null;
		try{
			fis = new FileInputStream(file);
			return FileUtil.getFileContent(fis, encode);
		}catch(Exception e){
			if(fis!=null){
				fis.close();
				fis=null;
			}
			throw e;
		}
	}
}
