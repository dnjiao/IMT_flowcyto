package flow_cytometry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

public class Sample {
	// patient info
	int mrn;
	int accession;
	String protocol;
	
	// Panel info
	String filename;
	String panelcode;
	String panelname;
	String antibodies;
	
	// Cycle info
	int collection;
	String cycle;
	String date;
	List<Gate> gates;
	
	/**
	 * Sample constructor
	 * @param filename - name of data file (xls)
	 * @param code - panel code
	 * @param name - panel name
	 * @param antibodies - panel antibodies
	 * @param row0 - first row in data file with gate column names
	 * @param sampRow - sample rows in data file
	 * @param sheet - sheet of panel dictionary
	 */
	public Sample(String filename, String code, String name, String antibodies, Row row0, Row sampRow, HSSFWorkbook workbook) {
		this.filename = filename;
		this.panelcode = code;
		this.panelname = name;
		this.antibodies = antibodies;
		String[] fields = parseSampleField(sampRow.getCell(1).getStringCellValue());
		this.date = fields[0];
		this.mrn = Integer.parseInt(fields[1].substring(3));
		this.protocol = fields[2].split("-")[0] + "-" + fields[2].split("-")[1];
		this.accession = Integer.parseInt(fields[2].split("-")[2]);
		this.cycle = fields[3];
		this.collection = cycleToColl(fields[3]);
		
		if (workbook.getSheet(code) == null) {
			System.out.println("ERROR: Sheet " + code + " missing from dictionary.");
			System.exit(1);
		}
		this.gates = parseGateDict(workbook.getSheet(code));
		
		Iterator<Cell> cellIter = row0.cellIterator();
		Cell cell;
		int cellCount = 0;
		HashMap<String, Integer> colMap = new HashMap<String, Integer>();
		// read data sheet and record column names and their indexes
		while (cellIter.hasNext()) {
			cell = cellIter.next();
			cellCount ++;
			if (cellCount > 3) {
				colMap.put(cell.getStringCellValue(), cellCount - 1);
			}
		}
		// list to store gate that needs to be deleted
		List<Integer> deleteList = new ArrayList<Integer>();
		// set gate values
		for (Gate gate : gates) {
			if (colMap.get(gate.getColumn()) == null)
				deleteList.add(gates.indexOf(gate));
			else
				gate.setValue(sampRow.getCell(colMap.get(gate.getColumn())).getNumericCellValue());
		}
/*
		if(deleteList.size() > 0) {
			for (int i = deleteList.size() - 1; i >= 0; i --) {
				int x = deleteList.get(i);
				gates.remove(x);
			}
		}
*/		
		
		// sort gates based on Gate_Name
		Collections.sort(gates, new Comparator<Gate>() {
			public int compare(Gate g1, Gate g2) {
				if (g1.getName().compareToIgnoreCase(g2.getName()) > 0)
					return 1;
				if (g1.getName().compareToIgnoreCase(g2.getName()) < 0)
					return -1;
				return 0;
			}
		});
		
	}
	
	/**
	 * Parse sample field and convert to String array
	 * @param str - sample field content, 2nd column in data xls
	 * @return - 4-member String array
	 */
	public static String[] parseSampleField(String str) {
		String[] out = new String[4];
		
		// split with one or more spaces
		String[] fields = str.split(" +");
		if (fields.length < 4) {
			System.out.println("ERROR: Invalid Sample Name: " + str);
			System.exit(1);
		}
		// only take the first 4 elements
		out[0] = fields[0];
		out[1] = fields[1];
		out[2] = fields[2];
		out[3] = fields[3];
		
		return out;
		
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
	 * @param sheet - sheet for the panel
	 * @return list of Gate objects
	 */
	private List<Gate> parseGateDict(HSSFSheet sheet) {
		List<Gate> gList = new ArrayList<Gate>();
		Iterator<Row> rowIter = sheet.rowIterator();
		int rowCount = 0;			
		while (rowIter.hasNext()) {
			Row row = rowIter.next();
			if (rowCount > 0) {
				if (row.getCell(0) == null) break;
				Gate gate = new Gate(row.getCell(0).getStringCellValue(), row.getCell(1).getStringCellValue(),
									 row.getCell(2).getStringCellValue(), row.getCell(3).getStringCellValue());
				gList.add(gate);
			}
			rowCount ++;
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

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

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