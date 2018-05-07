package biz.netcentric;

import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Node;
import org.jsoup.select.NodeVisitor;

public class MyNodeVisitor implements NodeVisitor{

	@Override
	public void head(Node node, int depth) {
		System.out.print(node.nodeName() + " : ");
		Attributes attrs = node.attributes();
		
		for(Attribute attr : attrs) {
			System.out.print(attr.html() + ", ");
		}
		System.out.println();
	}

	@Override
	public void tail(Node node, int depth) {
		System.out.print(node.nodeName() + " : ");
		Attributes attrs = node.attributes();
		
		for(Attribute attr : attrs) {
			System.out.print(attr.html() + ", ");
		}
		System.out.println();
	}

}
