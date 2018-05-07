package biz.netcentric;

import java.io.File;
import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.NodeTraversor;

public class HtmlTraverser {

	public static void traverse(File resource) throws IOException {
		Document doc = Jsoup.parse(resource, "UTF-8");
		MyNodeVisitor visitor = new MyNodeVisitor();
		
		NodeTraversor.traverse(visitor, doc.getAllElements());
	}
}
