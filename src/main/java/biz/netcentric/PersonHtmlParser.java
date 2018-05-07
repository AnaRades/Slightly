package biz.netcentric;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class PersonHtmlParser extends DefaultHandler {
	
	String scriptText = null;
	boolean isScript;
	//TODO: add the other elements
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		System.out.println("uri = " + uri + " , localName = " + localName + ", qName = " + qName);
		
		for(int i=0; i<attributes.getLength(); i++) {
			System.out.println(attributes.getValue(i));
			System.out.println(attributes.getLocalName(i));
		}
		
		try {
			if (qName.equalsIgnoreCase("script")) {
				//TODO: include type="server/javascript"
				isScript = true;
				System.out.println("It's a script");
			} else {
				System.out.println("Tag name = " + qName);
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}

	int cnt = 0;
	@Override
	public void characters(char ch[], int start, int length) throws SAXException {
		try {
			if (isScript) {
				scriptText = new String(ch, start, length);
				isScript = false;
				System.out.println("start = " + start + " length = " + length);
				System.out.println("Script text = " + scriptText);
			} else {
				System.out.println("Something else = " + new String(ch, start, length)); 
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
}
