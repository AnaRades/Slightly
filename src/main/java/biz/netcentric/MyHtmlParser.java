package biz.netcentric;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class MyHtmlParser implements HtmlParserUtils {

	/**
	 * The main script engine used in this parser
	 */
	private ScriptEngine engine;
	/**
	 * Bindings we want to persist
	 */
	private Bindings engineBindings;
	/**
	 * Elements we want to remove from the original HTML document
	 */
	private List<Element> elementsToRemove;
	/**
	 * Attributes we want to delete from HTML elements
	 */
	private Map<Element, Attribute> attrsToRemove;

	private Document sourceDocument;
	
	public MyHtmlParser() {
		this.engine = new ScriptEngineManager().getEngineByName("nashorn");
		this.engineBindings = engine.createBindings();
		this.elementsToRemove = new LinkedList<>();
		this.attrsToRemove = new HashMap<>();
		this.sourceDocument = null;
	}

	/**
	 * 
	 * @param key
	 *            Binding key
	 * @param obj
	 *            Binding value
	 * @return This to allow chaining
	 */
	public MyHtmlParser withInitialBinding(String key, Object obj) {
		this.engineBindings.put(key, obj);
		return this;
	}

	public MyHtmlParser withResource(File resource) {
		try {
			sourceDocument = Jsoup.parse(resource, "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return this;
	}
	
	public MyHtmlParser withResource(String html) {
		sourceDocument = Jsoup.parse(html, "UTF-8");
		return this;
	}
	
	public String parse() {
		if(sourceDocument == null) {
			//here we would usually throw a not initialized exception
			return "";
		}
		try {
			Elements elements = sourceDocument.getAllElements();
			
			for (Element element : elements) {
				// reset the engine bindings for each element
				engine.setBindings(engineBindings, ScriptContext.ENGINE_SCOPE);

				parseElement(element);
			}
			//removing elements and attributes at the end to not mess up the iteration
			for (Entry<Element, Attribute> a : attrsToRemove.entrySet()) {
				Element e = a.getKey();
				Attribute at = a.getValue();
				e.removeAttr(at.getKey());
			}
			for (Element e : elementsToRemove) {
				e.remove();
			}
			
			return sourceDocument.html();

		}catch (Exception e) {
			e.printStackTrace();
		}
		//if error, return empty String
		return "";
	}
	
	private Element parseElement(Element element) throws ScriptException {
		Bindings localBindings = engine.createBindings();
		if (element.nodeName().equalsIgnoreCase("script")) {
			String js = "load(\"nashorn:mozilla_compat.js\");\n" + element.data();
			engine.eval(js, engineBindings);
			elementsToRemove.add(element);
			//ignore attributes
			return element;
		}

		boolean willDeleteElement = false;
		Attributes attributes = element.attributes();
		
		for (Attribute attr : attributes) {
			try {
				if (attr.getValue() == null) {
					// we don't care about this attribute
					continue;
				}
				AttributeType attrType = AttributeType.getType(attr.getKey());
				switch (attrType) {
				case DATA_IF: {
					// if the condition is false, the element will be deleted
					willDeleteElement = !handleDataIf(attr, element);
					//add condition
				}
					break;
				case DATA_FOR: {
					handleDataFor(attr, element);
					willDeleteElement = true;
				}
					break;
				case DATA_SET: {
					handleDataSet(attr, element, localBindings);
				}
					break;
				default: {
				}
				}

				// if the element will be deleted, we don't care about the rest of attributes
				if (willDeleteElement) {
					break;
				}

				if (isExpressionRegex(attr.getValue())) {
					attr.setValue(getExpressionValue(attr.getValue()));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (!willDeleteElement && isExpressionRegex(element.ownText())) {
			element.text(getExpressionValue(element.ownText()));
		}
		return element;
	}
	

	public static String getFormattedHtml(String html) {
		Document format = Jsoup.parse(html);
		return format.html();
	}
	
	/**
	 * Handles the data-if element
	 * @param attr the attribute of type DATA_IF
	 * @param element The element containing the attribute
	 * @return TRUE if the condition was true and the element should be rendered</br>
	 * 		FALSE if the condition was false and the element should be deleted
	 */
	private boolean handleDataIf(Attribute attr, Element element) {
		boolean willRender = true;
		try {
			Object ifobj = engine.eval(attr.getValue());
			if (ifobj != null && (ifobj instanceof Boolean)) {
				boolean render = (boolean) ifobj;
				if (!render) {
					//Don't render, remove element and continue
					elementsToRemove.add(element);
					willRender = false;
				} else {
					attrsToRemove.put(element, attr);
				}
			}
		} catch (ScriptException e) {
			e.printStackTrace();
		}
		return willRender;
	}

	/**
	 * Handles the data-for-x element. For each element in the evaluated array, a new element is added and rendered 
	 * @param attr Attribute of type DATA_FOR
	 * @param element The element containing the attribute
	 */
	private List<Element> handleDataFor(Attribute attr, Element element) {
		ArrayList<Element> newElements = new ArrayList<>();
		//9 is length of "data-for-"
		String localVarName = attr.getKey().substring(9);
		
		try {
			Object list = engine.eval(attr.getValue());
			if (list instanceof List<?>) {
				for (Object arrItem : (List<?>) list) {
					engine.put(localVarName, arrItem);
					
					Element listEl = new Element(element.nodeName());
					if(isExpressionRegex(element.text())) {
						listEl.text(getExpressionValue(element.text()));
					} else {
						listEl.text(element.text());
					}
					
					Attributes attrPersistent = element.attributes().clone();
					attrPersistent.remove(attr.getKey());
					listEl.attributes().addAll(attrPersistent);
					
					//this element will be removed, allow special attribute chaining
					element.before(parseElement(listEl));
					
					newElements.add(listEl);
				}
			}
			elementsToRemove.add(element);
		} catch (ScriptException e) {
			e.printStackTrace();
		}
		//return to previous state
		engine.setBindings(engineBindings, ScriptContext.ENGINE_SCOPE);
		return newElements;
	}

	/**
	 * Handles the data-set-x element. With this element we create and set the value for variable 'x'  
	 * @param attr Attribute of type DATA_SET
	 * @param element The element containing the attribute
	 * @param localBindings The state representing the scope of the 'x' variable
	 */
	private void handleDataSet(Attribute attr, Element element, Bindings localBindings) {
		try {			
			//9 is length of "data-set-"
			String localVarName = attr.getKey().substring(9);
			Object globalObj = engine.get(localVarName);

			if (globalObj == null) {
				//No variable with the same name found
			} else {
				//put variable into bindings for persistence
				engineBindings.put(localVarName, globalObj);
			}
			Person localStatePerson = (Person) engine.eval(attr.getValue(), engineBindings);
			if (localStatePerson == null) {
				System.out.println("Local variable couldn't be created");
			} else {
				localBindings.put(localVarName, localStatePerson);
				engine.setBindings(localBindings, ScriptContext.ENGINE_SCOPE);
			}
			attrsToRemove.put(element, attr);
		} catch (ScriptException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Evaluates the entire text and replaces the expression value
	 * 
	 * @param text
	 *            String to parse
	 * @return the text parameter with the expression replaced by its value
	 */
	private String getExpressionValue(String text) {				
		StringBuilder result = new StringBuilder();
		
		try {
			//"(?=\\$\\{(\\w+[\\.\\w+]*)\\})"
			String[] expsr = text.split(SINGLE_EXPRESSION_DELIM);
			for(String expression : expsr) {
				try {
					//extract the javascript expression
					int startIndex = expression.indexOf("${");
					if(startIndex < 0) {
						result.append(expression);
					} else {
						//2 is length of '${'
						startIndex+=2;
						int endIndex = expression.indexOf("}", startIndex);
						String expr = expression.substring(startIndex, endIndex);
						Object expressionObj = engine.eval(expr);
						
						if (expressionObj != null) {
							result.append(String.valueOf(expressionObj));
						}
						result.append(expression.substring(endIndex+1));								
					}
				} catch (IndexOutOfBoundsException | ScriptException e) {
					e.printStackTrace();
				}
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		return result.toString();
	}
}
