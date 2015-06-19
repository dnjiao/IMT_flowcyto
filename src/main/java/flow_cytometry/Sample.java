package flow_cytometry;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

public class Sample {
	int collection;
	String cycle;
	String date;
	List<Gate> gates;
	
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
			cellCount ++;
			if (cellCount > 3) {
				colMap.put(cell.getStringCellValue(), cellCount - 1);
			}
		}
		String str = sampRow.getCell(1).getStringCellValue();
		String[] fields = Panel.parseSampleField(str);
		this.date = fields[0];
		this.cycle = fields[3];
		this.collection = cycleToColl(fields[3]);
		this.gates = parseGateDict(dictFile);
		
		// set gate values
		for (Gate gate : gates) {
			try {
//				if (colMap.get(gate.getColumn()) != null)
					gate.setValue(sampRow.getCell(colMap.get(gate.getColumn())).getNumericCellValue());
			} catch (Exception e) {
				System.out.println("Error in " + gate.getColumn());
				System.exit(1);
			}
			
		}
		
		// sort gates based on Gate_Code
		Collections.sort(gates, new Comparator<Gate>() {
			public int compare(Gate g1, Gate g2) {
				if (g1.getCode().compareTo(g2.getCode()) > 0)
					return 1;
				if (g1.getCode().compareTo(g2.getCode()) < 0)
					return -1;
				return 0;
			}
		});
		
	}
	
	/**
	 * Translate cycle "CxDy" format to collection number
	 * @param str - cycle string
	 * @return - collection number
	 */
	private int cycleToColl(String str) {
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
	public List<Gate> getGates() {
		return gates;
	}
	public void setGates(List<Gate> gates) {
		this.gates = gates;
	}
}