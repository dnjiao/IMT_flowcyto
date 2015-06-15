package flow_cytometry;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

public class Panel {
	String code;
	String name;
	String antibodies;
	List<Sample> sList;
	
	/**
	 * constructor of Panel class
	 * @param filename - name of panel data file
	 * @param path - path to dictionaries files
	 */
	public Panel(String code, String name, String antibodies, String dictpath, HSSFSheet sheet) {
		super();
		this.code = code;
		this.name = name;
		this.antibodies = antibodies;
		this.sList = new ArrayList<Sample>();
		
		File dictfile = new File(dictpath, code + ".dict");
		if (!dictfile.exists()) {
			System.out.println("ERROR: " + dictfile.getCanonicalPath() + " does not exist.");
			System.exit(1);
		}
		Iterator<Row> rowIter = sheet.rowIterator();
		int rowCount = 0;
		List<Integer> comList = new ArrayList<Integer>();
		while (rowIter.hasNext()) {
			Row row = rowIter.next();
			rowCount ++;
			if (row.getCell(0) != null && row.getCell(0).getStringCellValue().toLowerCase().contains("mean")) {
				break;
			}
			if (rowCount != 1 && row.getCell(2).getStringCellValue().toLowerCase().contains("com")) {
				comList.add(row.getRowNum());
			}
		}
		if (comList.size() == 0) {  // no "com" specified in column "Staining", all rows are accounted for
			if (rowCount > 2 && (rowCount - 2) < 5) {  // total lines <= 4
				for (int i = 1; i < rowCount - 1; i ++) {
					Sample samp = new Sample(sheet.getRow(0), sheet.getRow(i), dictfile.getCanonicalPath());
					sList.add(samp);
				}
			}
			else { 
				System.out.println("ERROR: Invalid number of rows in file.");
				System.exit(1);
			}
		}
		else { // only read the rows with "com" in "staining" column
			for (int i : comList) {
				Sample samp = new Sample(sheet.getRow(0), sheet.getRow(i), dictfile.getCanonicalPath());
				sList.add(samp);
			}
		}
		
		Collections.sort(sList, new Comparator<Sample>() {
			public int compare(Sample s1, Sample s2) {
				if (s1.getAccession() < s2.getAccession())
					return 1;
				if (s1.getAccession() > s2.getAccession())
					return -1;
				return 0;
			}
		});

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