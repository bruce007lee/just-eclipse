package com.alibaba.just.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtil {
	private static final String ENCODE_GBK = "GBK";
	private static final String ENCODE_UTF8 = "UTF-8";

	private FileUtil(){}

	/**
	 * 
	 * @param inputStream
	 * @return
	 */
	public static String guessEncoding(InputStream inputStream){
		try{
			byte[] rs = getFileByte(inputStream);
			String str = new String(rs,ENCODE_UTF8);
			if(str.equals(new String(str.getBytes(),ENCODE_UTF8))){
				return ENCODE_UTF8;
			}else{
				str = new String(rs,ENCODE_GBK);
				if(str.equals(new String(str.getBytes(),ENCODE_GBK))){
					return ENCODE_GBK;
				}
			}
		}catch(Exception e){
		}
		return null;
	}

	/**
	 * 
	 * @param file
	 * @param encode
	 * @return
	 * @throws Exception
	 */
	public static String guessEncoding(File file){
		FileInputStream fis = null;
		try{
			fis = new FileInputStream(file);
			return FileUtil.guessEncoding(fis);
		}catch(Exception e){
			return null;
		}finally{
			if(fis!=null){
				try{fis.close();}catch(Exception ex){};
				fis=null;
			}
		}
	}

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
		}catch(Exception e){
			throw e;
		}finally{
			if(os!=null){
				os.close();
				os= null;
			}
		}

	}

	/**
	 * 
	 * @param inputStream
	 * @return
	 * @throws Exception
	 */
	public static byte[] getFileByte(InputStream inputStream) throws Exception {
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
			byte[] rs = sb.toByteArray();
			return rs;
		}catch(Exception e){
			throw e;
		}finally{
			if(fis!=null){
				fis.close();
				fis= null;
			}
			if(sb!=null){
				sb.close();
				sb=null;
			}
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
		return new String(getFileByte(inputStream),encode==null?ENCODE_GBK:encode);		
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
			throw e;
		}finally{
			if(fis!=null){
				fis.close();
				fis=null;
			}
		}
	}
}
