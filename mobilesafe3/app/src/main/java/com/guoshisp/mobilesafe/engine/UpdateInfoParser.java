package com.guoshisp.mobilesafe.engine;

import android.util.Xml;

import com.guoshisp.mobilesafe.domain.UpdateInfo;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * 解析XML数据
 *
 */
public class UpdateInfoParser {
	/**
	 * @param is xml文件的输入流
	 * @return updateinfo的对象
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	public static UpdateInfo getUpdateInfo(InputStream is)
			throws XmlPullParserException, IOException {
		//获得一个Pull解析的实例
		XmlPullParser parser = Xml.newPullParser();
		//将要解析的文件流传入
		parser.setInput(is, "UTF-8");
		//创建UpdateInfo实例，用于存放解析得到的xml中的数据，最终将该对象返回
		UpdateInfo info = new UpdateInfo();
		//获取当前触发的事件类型
		int type = parser.getEventType();
		//使用while循环，如果获得的事件码是文档结束的话，那么就结束解析
		while (type != XmlPullParser.END_DOCUMENT) {
			if (type == XmlPullParser.START_TAG) {//开始元素
				if ("version".equals(parser.getName())) {//判断当前元素是否是读者需要检索的元素，下同
					//因为内容也相当于一个节点，所以获取内容时需要调用parser对象的nextText()方法才可以得到内容
					String version = parser.nextText();
					info.setVersion(version);
				} else if ("description".equals(parser.getName())) {
					String description = parser.nextText();
					info.setDescription(description);
				} else if ("apkurl".equals(parser.getName())) {
					String apkurl = parser.nextText();
					info.setApkurl(apkurl);
				}
			}
			type = parser.next();
		}
		return info;
	}
}