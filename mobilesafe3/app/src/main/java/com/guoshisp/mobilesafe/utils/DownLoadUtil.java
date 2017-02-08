package com.guoshisp.mobilesafe.utils;

import android.app.ProgressDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 下载的工具类 1.下载文件的路径 2.下载文件后保存的路径 3.关心 进度条 4.上下文
 */
public class DownLoadUtil {

	/**
	 * 下载一个文件
	 *
	 * @param urlpath
	 *            路径
	 * @param filepath
	 *            保存到本地的文件路径
	 * @param pd
	 *            进度条对话框
	 * @return	  下载后的apk
	 */
	public static File getFile(String urlpath, String filepath,
							   ProgressDialog pd) {
		try {
			URL url = new URL(urlpath);
			File file = new File(filepath);
			FileOutputStream fos = new FileOutputStream(file);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			//下载的请求是GET方式，conn的默认方式也是GET请求
			conn.setRequestMethod("GET");
			//服务端的响应的时间
			conn.setConnectTimeout(5000);
			//获取到服务端的文件的总长度
			int max = conn.getContentLength();
			//将进度条的最大值设置为要下载的文件的总长度
			pd.setMax(max);
			//获取到要下载的apk的文件的输入流
			InputStream is = conn.getInputStream();
			//设置一个缓存区
			byte[] buffer = new byte[1024];
			int len = 0;
			int process = 0;
			while ((len = is.read(buffer)) != -1) {
				fos.write(buffer, 0, len);
				//每读取一次输入流，就刷新一次下载进度
				process+=len;
				pd.setProgress(process);
				//设置睡眠时间，便于我们观察下载进度
				Thread.sleep(30);
			}
			//刷新缓存数据到文件中
			fos.flush();
			//关流
			fos.close();
			is.close();
			return file;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 获取一个路径中的文件名。例如：mobilesafe.apk
	 *
	 * @param urlpath
	 * @return
	 */
	public static String getFilename(String urlpath) {
		return urlpath
				.substring(urlpath.lastIndexOf("/") + 1, urlpath.length());
	}
}