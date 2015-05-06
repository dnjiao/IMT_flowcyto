package flow_cytometry;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class FlowJo {
	String specimen;
	String panel;
	String protocol;
	int accession;
	int collection;
	String fullname;
	String sample;
	String staining;
	String expDate;
	int mrn;
	LinkedHashMap<String, Double> gateMap;
	
	public FlowJo() {
		gateMap = new LinkedHashMap<String, Double>();
	}
	public void setSpecimen(String str) {
		specimen = str;
	}
	public String getSpecimen() {
		String str = protocol + ":" + accession + ":" + collection;
		return str;
	}
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
	
	public void setGateMap(ArrayList<String> str, ArrayList<Double> dou) {
		if (str.size() != dou.size()) {
			System.out.println("ERROR: key/value pair mismatch.");
			System.exit(0);
		}
		else {
			for (int i=0;i<str.size();i++) {
				gateMap.put(str.get(i), dou.get(i));
			}
		}
	}
	public LinkedHashMap<String, Double> getGateMap() {
		return gateMap;
	}
	
}