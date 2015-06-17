package flow_cytometry;

public class Gate {
	String column;  // column name of gate in raw file
	String code;
	String name;
	String definition;
	String parent;
	double value;
	
	/**
	 * Custom constructor
	 * @param column - column title appears in data xls
	 * @param code - Gate_Code
	 * @param definition - Gate_Definition
	 * @param parent - Parent_Gate
	 */
	public Gate(String column, String code, String definition, String parent) {
		super();
		this.column = column;
		this.code = code;
		this.definition = definition;
		this.parent = parent;
		// Gate_Name is combination of Gate_Code and Gate_Definition
		this.name = code + " (" + definition + ")";
	}
	
	public void setColumn(String str) {
		column = str;
	}
	public String getColumn() {
		return column;
	}
	public void setCode(String str) {
		code = str;
	}
	public String getCode() {
		return code;
	}
	public String getName() {
		return name;
	}
	public void setDefinition(String str) {
		definition = str;
	}
	public String getDefinition() {
		return definition;
	}
	public void setParent(String str) {
		parent = str;
	}
	public String getParent() {
		return parent;
	}
	public void setValue(double d) {
		value = d;
	}
	public double getValue() {
		return value;
	}
	
}