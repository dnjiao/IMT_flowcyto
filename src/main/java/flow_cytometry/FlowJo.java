package flow_cytometry;

import java.util.HashMap;

public class FlowJo {
	String panel;
	String protocol;
	int accession;
	int collection;
	String fullname;
	String sample;
	String staining;
	String expDate;
	int mrn;
	HashMap<String, Double> gateMap;
	
	public void setPanel(String str) {
		panel = str;
	}
	public String getPanel() {
		return panel;
	}
	public void setProtocol(String str) {
		protocol = str;
	}
	public String getProtocol() {
		return protocol;
	}
	public void setAccession(int i) {
		accession = i;
	}
	public int getAccession() {
		return accession;
	}
	public void setCollection(int i) {
		collection = i;
	}
	public int getCollection() {
		return collection;
	}
	public void setFullname(String str) {
		fullname = str;
	}
	public String getFullname() {
		return fullname;
	}
	public void setExpDate(String str) {
		expDate = str;
	}
	public String getExpDate() {
		return expDate;
	}
	public void setMrn(int i) {
		mrn = i;
	}
	public int getMrn() {
		return mrn;
	}
	public void setSample(String str) {
		sample = str;
	}
	public String getSample() {
		return sample;
	}
	
	public void setStaining(String str) {
		staining = str;
	}
	public String getStaining() {
		return staining;
	}
	
	public void setGateMap(String str, Double dou) {
		gateMap = new HashMap<String, Double>();
		gateMap.put(str, dou);
	}
	public HashMap<String, Double> getGateMap() {
		return gateMap;
	}
	
}