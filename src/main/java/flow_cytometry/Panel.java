package flow_cytometry;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class Panel {
	String filename;
	String code;
	String name;
	String antibodies;
	List<Sample> sList;
	

	/**
	 * constructor of Panel class
	 * @param filename - name of panel data file
	 * @param path - path to dictionaries files
	 */
	public Panel(int mrn, int accession, int collection, String cycle,
			String date, String protocol, String filename, String path) {
		super();
		this.filename = filename;
		File pDict = new File(path, "Panel.dict");
		// parse Panel.dict and initiate Panel members
		if (pDict.exists()) {
			try {
				// Create a buffered reader to read the file
				BufferedReader reader = new BufferedReader(new FileReader(pDict));			
				String line;	
				int linenum = 0;
				// Looping the read block until all lines read.
				while ((line = reader.readLine()) != null) {
					linenum ++;
					if (linenum > 1) { // start parsing from 2nd row
						String split[] = line.split("\t");
						if (split.length == 4) {
							this.filename = split[0];
							this.code = split[1];
							this.name = split[2];
							this.antibodies = split[3];
							new File(path, this.code + ".dict").toString();
						}
						else {
							System.out.println("ERROR: column number mismatch in Panel.dict");
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
			System.out.println("ERROR: File" + pDict.getAbsolutePath() + "does not exist.");
			System.exit(1);
		}
		
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAntibodies() {
		return antibodies;
	}
	public void setAntibodies(String antibodies) {
		this.antibodies = antibodies;
	}
	public List<Sample> getsList() {
		return sList;
	}
	public void setsList(List<Sample> sList) {
		this.sList = sList;
	}
	
}