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
	int mrn;
	int accession;
	String protocol;
	List<Sample> samples;
	
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
		this.samples = new ArrayList<Sample>();
		
		try {
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
			
			String[] fields = new String[5];
			if (comList.size() == 0) {  // no "com" specified in column "Staining", all rows are accounted for
				if (rowCount > 2 && (rowCount - 2) < 5) {  // total lines <= 4
					for (int i = 1; i < rowCount - 1; i ++) {
						
						Sample samp = new Sample(sheet.getRow(0), sheet.getRow(i), dictfile.getCanonicalPath());
						samples.add(samp);
					}
					fields = parseSampleField(sheet.getRow(1).getCell(1).getStringCellValue());
					
				}
				else { 
					System.out.println("ERROR: Invalid number of rows in file.");
					System.exit(1);
				}
			}
			else { // only read the rows with "com" in "staining" column
				for (int i : comList) {
					Sample samp = new Sample(sheet.getRow(0), sheet.getRow(i), dictfile.getCanonicalPath());
					samples.add(samp);
				}
				fields = parseSampleField(sheet.getRow(comList.get(0)).getCell(1).getStringCellValue());
			}
			this.mrn = Integer.parseInt(fields[1].substring(3));
			this.protocol = fields[2].split("-")[0] + "-" + fields[2].split("-")[1];
			this.accession = Integer.parseInt(fields[2].split("-")[2]);
		
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// sort samples based on the collection ID
		Collections.sort(samples, new Comparator<Sample>() {
			public int compare(Sample s1, Sample s2) {
				if (s1.getCollection() < s2.getCollection())
					return 1;
				if (s1.getCollection() > s2.getCollection())
					return -1;
				return 0;
			}
		});

	}
	
	/**
	 * Parse sample field and convert to String array
	 * @param str - sample field content, 2nd column in data xls
	 * @return - 5-member String array
	 */
	public static String[] parseSampleField(String str) {
		String[] out = new String[5];
		String[] fields = str.split(" ");
		if (fields.length != 5) {
			System.out.println("ERROR: Invalid Sample Name.");
			System.exit(1);
		}
		out[0] = fields[0];
		out[1] = fields[1];
		out[2] = fields[2];
		out[3] = fields[3];
		out[4] = fields[4];
		return out;
		
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
	
}