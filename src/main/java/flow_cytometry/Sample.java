package flow_cytometry;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
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
		Iterator<Cell> cellIter = row0.cellIterator();
		Cell cell;
		int cellCount = 0;
		HashMap<String, Integer> colMap = new HashMap<String, Integer>();
		while (cellIter.hasNext()) {
			cell = cellIter.next();
			if (cellCount > 2) {
				colMap.put(cell.getStringCellValue(), cellCount);
			}
		}
		String str = sampRow.getCell(1).getStringCellValue();
		String[] fields = str.split(" ");
		if (fields.length != 5) {
			System.out.println("ERROR: Invalid Sample Name.");
			System.exit(1);
		}
		this.date = fields[0];
		this.mrn = Integer.parseInt(fields[1].substring(3));
		this.protocol = fields[2].split("-")[0] + "-" + fields[2].split("-")[1];
		this.accession = Integer.parseInt(fields[2].split("-")[2]);
		this.cycle = fields[3];
		this.collection = cycleToAcc(fields[3]);
		this.gateList = parseGateDict(dictFile);
		for (Gate gate : gateList) {
			gate.setValue(Double.parseDouble(sampRow.getCell(colMap.get(gate.getColumn())).getStringCellValue()));
		}
		
	}
	
	/**
	 * Translate cycle "CxDy" format to collection number
	 * @param str - cycle string
	 * @return - collection number
	 */
	private int cycleToAcc(String str) {
		if (str.length() != 4) {
			System.out.println("ERROR: Wrong Cycle Format.");
			System.exit(1);
		}
		int c = Character.getNumericValue(str.charAt(1));
		int d = Character.getNumericValue(str.charAt(3));
		return (c - 1) * 2 + d;
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