package flow_cytometry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFSheet;
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
	public Sample(Row row0, Row sampRow, HSSFSheet sheet) {
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
		String str = sampRow.getCell(1).getStringCellValue();
		String[] fields = Panel.parseSampleField(str);
		this.date = fields[0];
		this.cycle = fields[3];
		this.collection = cycleToColl(fields[3]);
		this.gates = parseGateDict(sheet);
		
		// list to store gate that needs to be deleted
		List<Integer> deleteList = new ArrayList<Integer>();
		// set gate values
		for (Gate gate : gates) {
			if (colMap.get(gate.getColumn()) == null)
				deleteList.add(gates.indexOf(gate));
			else
				gate.setValue(sampRow.getCell(colMap.get(gate.getColumn())).getNumericCellValue());
		}
		
		if(deleteList.size() > 0) {
			for (int i = deleteList.size() - 1; i >= 0; i --) {
				int x = deleteList.get(i);
				gates.remove(x);
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