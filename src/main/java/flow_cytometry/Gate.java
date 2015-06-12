package flow_cytometry;

public class Gate {
	String column;  // column name of gate in raw file
	String code;
	String name;
	String definition;
	String parent;
	double value;
	
	public Gate(String column, String code, String definition, String parent) {
		super();
		this.column = column;
		this.code = code;
		this.definition = definition;
		this.parent = parent;
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