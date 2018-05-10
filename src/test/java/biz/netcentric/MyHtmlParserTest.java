package biz.netcentric;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MyHtmlParserTest {

	private static final String HTML_START = "<html><head></head><body>";
	private static final String HTML_END = "</body></html>";
	private static final String EMPTY_HTML = HTML_START+HTML_END;
	private static final String DATA_IF_HTML = "<html><head></head><body><h2 data-if=\"person.married\" title=\"${person.spouse}\">Spouse: ${person.spouse}</h2></body></html>";
	private static final String DATA_SET_HTML = "<html><head></head><body><h2 data-set-person=\"person.spouseObj\">Spouse: ${person.name}</h2></body></html>";
	private static final String DATA_FOR_HTML = "<html><head></head><body><div data-for-child=\"person.children\" > ${child}</div></body></html>";
	private static final String DATA_FOR_IF_HTML = "<div data-for-friend=\"person.bestFriends\" data-if=\"friend.married\">Couples friend ${friend.name} </div>";
	
	@Test
	public void testIfAttributeFalseCondition() {
		Person person = new Person("Kerstin", "Jose", false, 1);
		MyHtmlParser parser = new MyHtmlParser().withInitialBinding("person", person).withResource(DATA_IF_HTML);
		
		String actual = parser.parse();		
		assertEquals("Should return a HTML with the basic elements, void of content", MyHtmlParser.getFormattedHtml(EMPTY_HTML), actual);		
	}
	
	@Test
	public void testIfAttributeTrueCondition() {
		Person person = new Person("Erik", "Dora", true, 3);
		
		//expected result replaces the ${person.spouse} expression with actual value and deletes the data-if attribute
		String expected = DATA_IF_HTML.replaceAll("\\$\\{person.spouse\\}", person.getSpouse()).replace("data-if=\"person.married\"", "");
		
		MyHtmlParser parser = new MyHtmlParser().withInitialBinding("person", person).withResource(DATA_IF_HTML);
		String actual = parser.parse();
		
		assertEquals(MyHtmlParser.getFormattedHtml(expected), actual);		
	}
	
	@Test
	public void testDataSetAttribute() {
		Person person = new Person("Erik", "Dora", true, 3);
		
		//expected result replaces the ${person.name} expression with actual value and deletes the data-set attribute
		String expected = DATA_SET_HTML.replaceAll("\\$\\{person.name\\}", person.getSpouse()).replace("data-set-person=\"person.spouseObj\"", "");
		
		MyHtmlParser parser = new MyHtmlParser().withInitialBinding("person", person).withResource(DATA_SET_HTML);
		String actual = parser.parse();
		
		assertEquals(MyHtmlParser.getFormattedHtml(expected), actual);		
	}
	
	@Test
	public void testDataForAttribute() {
		Person person = new Person("Erik", "Dora", true, 3);
		StringBuilder sb = new StringBuilder(HTML_START);
		
		for(String child : person.getChildren()) {
			sb.append("<div>").append(child).append("</div>");
		}
		sb.append(HTML_END);		
		String expected = MyHtmlParser.getFormattedHtml(sb.toString());
		
		MyHtmlParser parser = new MyHtmlParser().withInitialBinding("person", person).withResource(DATA_FOR_HTML);
		String actual = parser.parse();
		
		assertEquals(expected, actual);		
	}
	
	@Test
	public void testCombinedDataForDataIf() {
		StringBuilder sb = new StringBuilder(HTML_START);
		Person person = new Person("Erik", "Dora", true, 3);
		for(Person friend : person.getBestFriends()) {
			if(friend.isMarried()) {
				sb.append("<div>").append("Couples friend").append(friend.getName()).append("</div>");
			}
		}
		sb.append(HTML_END);		
		String expected = MyHtmlParser.getFormattedHtml(sb.toString());
		
		MyHtmlParser parser = new MyHtmlParser().withInitialBinding("person", person).withResource(DATA_FOR_IF_HTML);
		String actual = parser.parse();
		
		assertEquals(expected, actual);		
	}
}
