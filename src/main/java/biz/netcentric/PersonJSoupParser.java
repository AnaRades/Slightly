package biz.netcentric;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.servlet.http.HttpServletRequest;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class PersonJSoupParser implements HtmlConstants {

	private static ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");	
	private static Bindings globalBindings = engine.createBindings(); 
	
	private static List<Element> elementsToRemove = new LinkedList<>();
	private static Map<Element, Attribute> attrsToRemove = new HashMap<>();

	public static String parse(File resource, HttpServletRequest request) {
		try {
			Document doc = Jsoup.parse(resource, "UTF-8");
			Elements elements =  doc.getAllElements();
				
			globalBindings.put("request", request);
			engine.setBindings(globalBindings, ScriptContext.GLOBAL_SCOPE);
			
			for (Element element : elements) {
				Bindings localBindings = engine.createBindings(); 
				//reset the engine bindings for each element
				engine.setBindings(globalBindings, ScriptContext.ENGINE_SCOPE);
				
				if (element.nodeName().equalsIgnoreCase("script")) {
					String js = "load(\"nashorn:mozilla_compat.js\");\n" + element.data();
					engine.eval(js);					
					element.remove();
					continue;
				}
				Attributes attributes = element.attributes();				
				LinkedList<Attribute> listAttrs = new LinkedList<>(attributes.asList());
				
				for (Attribute attr : listAttrs) {
					try {
						if(attr.getValue() == null) {
							//we don't care about this attribute
							continue;
						}
						System.out.println("------------------ATTR " + attr.getKey());
						AttributeType attrType = AttributeType.getType(attr.getKey());
						boolean willDeleteElement = false;
						switch(attrType) {
						case DATA_IF: {
							//if the condifition is false, the element will be deleted
							willDeleteElement = !handleDataIf(attr, element);
						} break;
						case DATA_FOR: {
							handleDataFor(attr, element);
							willDeleteElement = true;
						} break;
						case DATA_SET: {
							handleDataSet(attr, element, localBindings);
						}break;
						default:{}
						}
						
						//if the element will be deleted, we don't care about the rest of attributes
						if(willDeleteElement) {
							break;
						}
						
						if(isExpressionRegex(attr.getValue())) {
							System.out.println("Expression attribute found in node: " + element.nodeName() + ", attr: " + attr.getKey() + ", expr: " + attr.getValue());
							attr.setValue(getExpressionValue(attr.getValue()));
						}
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
				if(isExpressionRegex(element.ownText())) {
					element.text(getExpressionValue(element.ownText()));					
				}
			}
			
			for(Entry<Element, Attribute> a : attrsToRemove.entrySet()) {
				Element e = a.getKey();
				Attribute at = a.getValue();
				e.removeAttr(at.getKey());
			}
			for(Element e : elementsToRemove) {
				e.remove();
			}
			
			System.out.println("\nORIGINAL DOCUMENT:\n" + doc.html());
			
			
			return doc.html();
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ScriptException e) {
			e.printStackTrace();
		}
		
		return "";
	}
	
	private static boolean handleDataIf(Attribute attr, Element element) {
		boolean willRender = true;
		try {
			Object ifobj = engine.eval(attr.getValue());
			if (ifobj != null && (ifobj instanceof Boolean)) {
				boolean render = (boolean) ifobj;
				if (!render) {
					System.out.println("Don't render, remove element and continue");
					elementsToRemove.add(element);
					willRender = false;
				} else {
					attrsToRemove.put(element, attr);
				}
			}
		} catch(ScriptException e) {
			e.printStackTrace();
		}
		return willRender;
	}
	
	private static void handleDataFor(Attribute attr, Element element) {		
		try {
			Object list = engine.eval(attr.getValue());
			if (list instanceof List<?>) {
				for (Object o : (List<?>)list) {
					Element listEl = new Element(element.nodeName());
					listEl.text(o.toString());
					Attributes attrPersistent =element.attributes().clone();
					attrPersistent.remove(attr.getKey());
					listEl.attributes().addAll(attrPersistent);
					element.before(listEl);
				}
			}
			element.remove();
		} catch(ScriptException e) {
			e.printStackTrace();
		}
	}
	
	private static void handleDataForWithCondition(Attribute listAttr, Attribute condAttr, Element element) {
		try {
			Object list = engine.eval(listAttr.getValue());
			Object condition = engine.eval(condAttr.getValue());
			boolean conditionTrue;
			
			if(condition == null || !(condition instanceof Boolean)) {
				conditionTrue = false;
			} else {
				conditionTrue = (boolean) condition;
			}
			
			if (list instanceof List<?>) {
				for (Object dataForObject : (List<?>)list) {
					if(!conditionTrue) {
						continue;
					}
					Element listEl = new Element(element.nodeName());
					listEl.text(dataForObject.toString());
					Attributes attrPersistent = element.attributes().clone();
					attrPersistent.remove(listAttr.getKey());
					listEl.attributes().addAll(attrPersistent);
					element.before(listEl);
				}
			}
		} catch(ScriptException e) {
			e.printStackTrace();
		}
	}
	
	private static void handleDataSet(Attribute attr, Element element, Bindings localBindings) {
		try {
			String localVarName = attr.getKey().substring(9);
			Object globalObj = engine.get(localVarName);
			
			if(globalObj == null) {
				System.out.println("No variable with the same name found");
			} else {
				System.out.println("GlobalStatePerson = " + globalObj.toString());
				globalBindings.put(localVarName, globalObj);	
			}
			Person localStatePerson = (Person) engine.eval(attr.getValue(), localBindings);
			if(localStatePerson == null) {
				System.out.println("Local variable couldn't be created");
			} else {
				localBindings.put(localVarName, localStatePerson);
				engine.setBindings(localBindings, ScriptContext.ENGINE_SCOPE);
			}
			attrsToRemove.put(element, attr);
		} catch(ScriptException e) {
			e.printStackTrace();
		}
	}
	
	private static void handleSpecialAttributes(List<Attribute> attrs, Element element) {
		for(Attribute attr : attrs) {
			if(isDataIf(attr)) {
				
			}
		}
	}
	
	//[\w*${\w}]
	//(\w+)?\${(\w+)}
	private static boolean isExpressionRegex(String text) {
		boolean matches = text.trim().matches("([\\w\\s\\W]*)?\\$\\{(\\w+\\.\\w+)\\}"); 
		return matches;
	}
	
	private static String getExpressionValue(String text) {
		String result = "";
		try {
			int indexStart = text.indexOf("${") + 2;
			int endStart = text.indexOf("}", indexStart);			
			String expr = text.substring(indexStart, endStart);
			Object expressionObj = engine.eval(expr);
			
			if(expressionObj != null) {
				result = text.substring(0, indexStart-2) + String.valueOf(engine.eval(expr)) + text.substring(endStart+1); 
			}
		} catch(IndexOutOfBoundsException | ScriptException e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	private static boolean isDataIf(Attribute attr) {
		if(attr == null || attr.getValue().isEmpty()) {
			return false;
		}
		return (attr.getKey().equalsIgnoreCase(DATA_IF));
	}
}
