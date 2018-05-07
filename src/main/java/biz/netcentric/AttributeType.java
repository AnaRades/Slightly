package biz.netcentric;

public enum AttributeType {
	DATA_IF,
	DATA_FOR,
	DATA_SET,
	REGULAR;
	
	public static AttributeType getType(String key) {
		if(key.equalsIgnoreCase("data-if")) {
			return DATA_IF;
		} else if(key.startsWith("data-for-")) {
			return DATA_FOR;
		} else if(key.startsWith("data-set")) {
			return DATA_SET;
		}
		return REGULAR;
	}
}
