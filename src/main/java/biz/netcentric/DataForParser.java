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

public class DataForParser {
	
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
				for (Attribute attr : attributes) {
					try {
						System.out.println("------------------ATTR " + attr.getKey());
						if (attr.getKey().startsWith("data-for-")) {
							Object list = engine.eval(attr.getValue());
							if (list instanceof List<?>) {
								//TODO:bug? check hierarchy. Remove the element with data-for and clone the one inside
								for (Object o : (List<?>)list) {
//									System.out.println("arrObj " + o);
									Element listEl = new Element(element.nodeName());
									listEl.text(o.toString());
									element.parent().appendChild(listEl);
								}
							}
							element.remove();
							continue;
						}
						if(attr.getValue() == null) {
//							System.out.println("Attribute " + attr.getKey() + " has NULL value");
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
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ScriptException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return "";
	}
}
