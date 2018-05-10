package biz.netcentric;

public interface HtmlParserUtils {

	String MULTIPLE_EXPRESSION_REGEX = "([\\w\\s\\W]*)?\\$\\{(\\w+[\\.\\w+]*)\\}";
	String SINGLE_EXPRESSION_DELIM = "(?=\\$\\{(\\w+[\\.\\w+]*)\\})";
	
	/**
	 * Matches with regex ([\w\s\W]*)?\$\{(w+[.w+]*)\}
	 * Regex matches ( optional_text $ {variable(.member) optional} optional_text )
	 * @param text
	 *            String to match with regular expression
	 * @return true if expression matched
	 */
	default boolean isExpressionRegex(String text) {
		boolean matches = text.trim().matches(MULTIPLE_EXPRESSION_REGEX);
		return matches;
	}
}
