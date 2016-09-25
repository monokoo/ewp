package com.gz.tool.xml;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import com.gz.tool.server.tool.Utils;

public class ParseXml {
	private static ParseXml parseXml;
	
	public static ParseXml getInstance() {
    	if(null==parseXml) {
    		parseXml = new ParseXml();
    	}
    	return parseXml;
    }
	
	public boolean insertElement(String xmlPath, String nodeName, String attrName, String insertElementName, HashMap<String, String> hm) {
		boolean bFlag = true;
		try {
			SAXReader saxReader = new SAXReader();
			Document document = saxReader.read(xmlPath);
			List<Element> elementList = getElements(xmlPath, document, nodeName, attrName, null, null, null);
			for(int i=0; i<elementList.size(); i++) {
				Element element = elementList.get(i);
				Element insertElement = element.addElement(insertElementName);
				Iterator<String> itr = hm.keySet().iterator();
				while(itr.hasNext()) {
					String key = itr.next();
					insertElement.setAttributeValue(key, hm.get(key));
				}
			}
			
			if(elementList.size()>0) {
				writeXml(document);
			}
		} catch (Exception e) {
			bFlag = false;
			e.printStackTrace();
		}
		
		return bFlag;
	}
	
	public boolean removeElement(String xmlPath, String nodeName, String attrName, String removeAttrName) {
		boolean bFlag = true;
		try {
			SAXReader saxReader = new SAXReader();
			Document document = saxReader.read(xmlPath);
			List<Element> elementList = getElements(xmlPath, document, nodeName, attrName, null, null, null);
			for(int i=0; i<elementList.size(); i++) {
				Element element = elementList.get(i);
				for(int j=0; j<element.elements().size(); j++) {
					Element childElement = (Element) element.elements().get(j);
					String childAttrName = childElement.attributeValue("name");
					if(childAttrName!=null && childAttrName.equals(removeAttrName)) {
						element.remove(childElement);
						
						writeXml(document);
						
						break;
					}
				}
			}
		} catch (Exception e) {
			bFlag = false;
			e.printStackTrace();
		}
		
		return bFlag;
	}
	
	public List<Element> getElements(String xmlPath, String nodeName, String attrName) {
		return getElements(xmlPath, nodeName, attrName, null);
	}
	
	public List<Element> getElements(String xmlPath, String nodeName, String attrName, String parentName) {
		return getElements(xmlPath, null, nodeName, attrName, null, null, parentName);
	}
	
	/**
	 * 获取元素节点
	 * @param xmlPath		配置文件路径
	 * @param nodeName		要获取的元素节点名称
	 * @param element		要遍历的元素
	 * @param elementList	获取到的元素集合
	 * @return				返回获取到的元素集合
	 */
	private List<Element> getElements(String xmlPath, Document document, String nodeName, String attrName, Element element, List<Element> elementList, String parentName) {
		try {
			if(elementList==null) {
				elementList = new ArrayList<Element>();
			}
			
			if(document==null) {
				SAXReader saxReader = new SAXReader();
				document = saxReader.read(xmlPath);
			}
			
			if(element==null) {
				element = document.getRootElement();
				
				//这里是查找根节点的
				if(element.getName().equals(nodeName)) {
					elementList.add(element);
					return elementList;
				}
			}
			
			for(int i=0; i<element.elements().size(); i++) {
				Element childElement = (Element) element.elements().get(i);
				String childNodeName = childElement.getName();
				
				if(childNodeName.equals(nodeName)) {
					if(null!=attrName && !attrName.equals(childElement.attributeValue("name"))) {
						continue;
					}
					
					if(null!=parentName && !parentName.equals(element.attributeValue("name"))) {
						continue;
					}
					
					elementList.add(childElement);
				} else {
					elementList = getElements(xmlPath, document, nodeName, attrName, childElement, elementList, parentName);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return elementList;
	}
	
	public void updateXml(String xmlPath, String nodeName, Element targetElement, HashMap<String, String> hm) {
		updateXml(xmlPath, null, nodeName, targetElement, null, hm);
	}
	/**
	 * 更新配置文件节点元素属性信息
	 * @param xmlPath		配置文件路径
	 * @param document		
	 * @param nodeName		要修改的元素节点名称
	 * @param element		要遍历的元素
	 * @param hm			更新的属性值集合
	 */
	private void updateXml(String xmlPath, Document document, String nodeName, Element targetElement, Element element, HashMap<String, String> hm) {
		if(null!=hm) {
			try {
				if(null==document) {
					SAXReader saxReader = new SAXReader();
					document = saxReader.read(xmlPath);
				}
				
				if(null==element) {
					element = document.getRootElement();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			for(int i=0; i<element.elements().size(); i++) {
				Element childElement = (Element) element.elements().get(i);
				String childNodeName = childElement.getName();
				if(childNodeName.equals(nodeName)) {
					
					//当targetElement不为空时先判断当前循环节点元素是否与targetElement相同
					if(null!=targetElement) {
						String parent = targetElement.attributeValue("parent");
						if(parent!=null && !element.attributeValue("name").equals(parent)) {
							continue;
						}
							
						String childName = childElement.attributeValue("name");
						String targetName = targetElement.attributeValue("name");
						if(!targetName.equals(childName)) {	
							continue;
						}
					}
					
					boolean isUpdate = false;
					Set<String> keys = hm.keySet();
					Iterator<String> iter = keys.iterator();
					while(iter.hasNext()) {	//更新节点元素属性
						String key = iter.next();
						isUpdate = true;
						childElement.setAttributeValue(key, hm.get(key));
					}
					
					//更新配置文件
					if(isUpdate) {	
						writeXml(document);
					}
				} else {
					updateXml(xmlPath, document, nodeName, targetElement, childElement, hm);
				}
			}
		}
	}
	
	private boolean writeXml(Document document) {
		boolean bFlag = false;
		if(document!=null) {
			try {
				OutputFormat outFormat = OutputFormat.createPrettyPrint();
				outFormat.setEncoding("UTF-8");
				
				XMLWriter output = new XMLWriter(new FileOutputStream(new File(Utils.getPathDB())), outFormat);
				output.write(document);
				output.close();
				
				bFlag = true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return bFlag;
	}
}
