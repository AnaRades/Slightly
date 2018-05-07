package biz.netcentric;

import java.io.File;

import javax.servlet.http.HttpServletRequest;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class HtmlRenderer {

	public static void parse(File resource, HttpServletRequest request) {
		try {
			Document doc = Jsoup.parse(resource, "UTF-8");
			Elements elements =  doc.getAllElements();
			
			StringBuilder sb = new StringBuilder();
			
			for(Element e : elements) {
				sb.append(e.nodeName());
//				e.
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private static String getHtmlElement(String nodeName) {return null;}
}
