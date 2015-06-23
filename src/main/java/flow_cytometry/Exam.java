package flow_cytometry;

import java.util.List;

public class Exam {
	// from Panel class
	String panelcode;
	String panelname;
	String antibodies;
	int mrn;
	int accession;
	String protocol;
	List<Sample> samples;
	
	// from Sample class
	int collection;
	String cycle;
	String date;
	
	public String getPanelcode() {
		return panelcode;
	}
	public void setPanelcode(String panelcode) {
		this.panelcode = panelcode;
	}
	public String getPanelname() {
		return panelname;
	}
	public void setPanelname(String panelname) {
		this.panelname = panelname;
	}
	public String getAntibodies() {
		return antibodies;
	}
	public void setAntibodies(String antibodies) {
		this.antibodies = antibodies;
	}
	public int getMrn() {
		return mrn;
	}
	public void setMrn(int mrn) {
		this.mrn = mrn;
	}
	public int getAccession() {
		return accession;
	}
	public void setAccession(int accession) {
		this.accession = accession;
	}
	public String getProtocol() {
		return protocol;
	}
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	public List<Sample> getSamples() {
		return samples;
	}
	public void setSamples(List<Sample> samples) {
		this.samples = samples;
	}
	public int getCollection() {
		return collection;
	}
	public void setCollection(int collection) {
		this.collection = collection;
	}
	public String getCycle() {
		return cycle;
	}
	public void setCycle(String cycle) {
		this.cycle = cycle;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	
	
}