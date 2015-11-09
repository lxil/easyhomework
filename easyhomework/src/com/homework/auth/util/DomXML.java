package com.homework.auth.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;



public class DomXML {

	@SuppressWarnings("unchecked")
	public static Map<String, String> readDoc(String xml) {
		Map<String, String> map = new HashMap<String, String>();
		Document document;
		try {
			document = DocumentHelper.parseText(xml);
			Element root = document.getRootElement();
			// 得到根元素的所有子节点
			List<Element> elementList = root.elements();

			// 遍历所有子节点
			for (Element e : elementList) {
				map.put(e.getName(), e.getText());
			}
		} catch (DocumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return map;
	}

	public static String getValue(Map<String, String> map, String key) {
		return map.get(key);

	}

}
