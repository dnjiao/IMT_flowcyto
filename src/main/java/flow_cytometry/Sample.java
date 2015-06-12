package flow_cytometry;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;

public class Sample {
	int mrn;
	int accession;
	int collection;
	String cycle;
	String date;
	String protocol;
	List<Gate> gateList;
	
	/** 
	 * custom constructor
	 * @param row0 - first row in the data sheet
	 * @param sampRow - sample row in the data sheet
	 * @param dictFile - full path to gate dictionary file
	 */
	public Sample(Row row0, Row sampRow, String dictFile) {
		
	}
	
	public Sample(int mrn, int accession, int collection, String cycle,
			String date, String protocol, String dictFile) {
		super();
		this.mrn = mrn;
		this.accession = accession;
		this.collection = collection;
		this.cycle = cycle;
		this.date = date;
		this.protocol = protocol;
		this.gateList = parseGateDict(dictFile);
	}
	
	/**
	 * parse gate dictionary file for a panel
	 * @param dictFile - full path to dictionary file
	 * @return list of Gate objects
	 */
	private List<Gate> parseGateDict(String dictFile) {
		List<Gate> gList = new ArrayList<Gate>();
		File file = new File(dictFile);
		if (file.exists()) {
			try {
				// Create a buffered reader to read the file
				BufferedReader reader = new BufferedReader(new FileReader(file));			
				String line;	
				int linenum = 0;
				// Looping the read block until all lines read.
				while ((line = reader.readLine()) != null) {
					linenum ++;
					if (linenum > 1) { // start parsing from 2nd row
						String split[] = line.split("\t");
						if (split.length == 4) {
							Gate gate = new Gate(split[0], split[1], split[2], split[3]);
							gList.add(gate);
						}
						else {
							System.out.println("ERROR: column number mismatch in " + dictFile);
							System.exit(1);
						}
					}
				}
				reader.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		else {
			System.out.println("ERROR: File" + file.getAbsolutePath() + "does not exist.");
			System.exit(1);
		}
		return gList;
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
	public String getProtocol() {
		return protocol;
	}
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	
	
}