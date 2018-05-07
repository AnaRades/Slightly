package biz.netcentric;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

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

public class PersonJSoupParser {

	
	//go over the html, traverse in tree-order
	//check every tag, element body, attributes, attribute values
	//maybe use a stack or queue to hold the resulting html
	//if we find a script, evaluate it
	//maybe better to user regex to match each specified section (data-if, data-for, $-expression, etc)
	
	//TODO: check what the deal is with NodeVisitor
	//TODO: for instances like Spouse : ${expression}, replace only the expr with evaluated value
	//TODO: add automated tests
	//TODO: add optionals
	//TODO: add type safety checks
	//TODO: combine data-if with data-for
	
	
	
	
	/**
	 * Get a list of all element attributes
	 * retrieve all special attributes
	 * if multiple attrs with special keys, chain them --> ???
	 * foreach attribute with special values, evaluate and replace the value
	 * 
	 * 
	 * data-for-loopVar in arr, render if cond
	 */
	
	private static final String pattern = Pattern.quote(".*${\\w\\d}");			
	public static String parse(File resource, HttpServletRequest request) {
		try {
			Document doc = Jsoup.parse(resource, "UTF-8");
			Elements elements =  doc.getAllElements();
			
			ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");		
			Bindings globalBindings = engine.createBindings(); 
			globalBindings.put("request", request);
			engine.setBindings(globalBindings, ScriptContext.GLOBAL_SCOPE);
						
			Element peerElement = null;
			List<Element> elementsToRemove = new LinkedList<>();
			Map<Element, Attribute> attrsToRemove = new HashMap<>();
			
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
				peerElement = new Element(element.nodeName());
				Attributes attributes = element.attributes();
				
				LinkedList<Attribute> listAttrs = new LinkedList<>(attributes.asList());
				
				for (Attribute attr : listAttrs) {
					try {
						System.out.println("------------------ATTR " + attr.getKey());
						if (attr.getKey().startsWith("data-if")) {
							Object ifobj = engine.eval(attr.getValue());
							if (ifobj != null) {
								boolean render = (boolean) ifobj;
								if (!render) {
									System.out.println("Don't render, remove element and continue");
									elementsToRemove.add(element);
									break;
								} else {
									attrsToRemove.put(element, attr);
									continue;
								}
							}
						}  else if (attr.getKey().startsWith("data-for-")) {
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
							continue;
						} else if(attr.getKey().startsWith("data-set-")) {
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
								System.out.println("localStatePerson = " + localStatePerson.getName());
								localBindings.put(localVarName, localStatePerson);
								engine.setBindings(localBindings, ScriptContext.ENGINE_SCOPE);
							}
							attrsToRemove.put(element, attr);			
						}
						if(attr.getValue() == null) {
							System.out.println("Attribute " + attr.getKey() + " has NULL value");
							continue;
						}
						if (attr.getValue().contains("${")) {
							int indexStart = attr.getValue().indexOf("${") + 2;
							int endStart = attr.getValue().indexOf("}", indexStart);
							String expr = attr.getValue().substring(indexStart, endStart);
							String o = String.valueOf(engine.eval(expr));
							System.out.println("Expression attribute found in node: " + element.nodeName() + ", attr: " + attr.getKey() + ", expr: " + expr + ", value: " + o);
							if (o != null) {
								attr.setValue(o);
								peerElement.attr(attr.getKey(), o);
							} else {
								peerElement.attr(attr.getKey(), expr + " --> resulted in null");
								System.out.println(expr + " --> resulted in null");
							}
						}
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
				if(element.ownText().contains("${")) {
					String text = element.ownText(); 
					int index = text.indexOf("${") + 2;
					String expr = text.substring(index, text.length() - 1);
//					System.out.println("Expression text " + text + " found in node " + element.nodeName().toUpperCase() + " expr = " + expr);
					try {
						String o = text.substring(0, index-2) + String.valueOf(engine.eval(expr));
						System.out.println("Expression evaluated at " + o);
						
						if (o != null) {
							peerElement.text(o);
							element.text(o);
						}
					} catch(Exception ee) {
						System.out.println(" --> resulted in null");
					}
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
			
//			System.out.println("\nCONSTRUCTED DOCUMENT:\n" + doc.html());
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ScriptException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return "";
	}
	
	private void handleDataIf() {
		
	}
	
	private void handleDataFor() {
		
	}
	
	private boolean isExpression() {
		return false;
	}
}
