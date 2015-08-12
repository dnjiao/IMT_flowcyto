package flow_cytometry;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

public class FlowJoSummary {
	
	public static void main(String[] args) {
		System.out.println("Path to the directory with flowcytometry files:");
		String cwd = System.getProperty("user.dir");  // get current working directory
		System.out.print(cwd + "[Y/N]:");
		String pathCorrect, dirStr = "";
		Scanner scan = new Scanner(System.in);
		pathCorrect = scan.nextLine().trim().toLowerCase();
		if (pathCorrect.equals("y")) {
			dirStr = cwd;
		}
		else if (pathCorrect.equals("n")) {  //user needs to provide the working directory with full path
			System.out.println("Please enter the full path to data directory: ");
			dirStr = scan.nextLine();
		}
		else {
			System.out.println("ERROR: Invalid input. Try again.");
			System.exit(0);
		}
		
		File dataDir = new File(dirStr);
		if (!dataDir.exists()) {  // Path does not exist
			System.out.println("ERROR: Data folder " + dirStr + " does not exist.");
			System.exit(1);
		}
		if (!dataDir.isDirectory()) {   // Path not a directory
			System.out.println("ERROR: " + dirStr + " is not a directory.");
			System.exit(1);
		}
		// user's default home directory
		String home = System.getProperty("user.home");
		File dictDir = new File(home, "Flow_Dict");
		try {
			String dictStr = dictDir.getCanonicalPath();
			System.out.println("Path to flow cytometry data dictionary files [" + dictStr + "]");
	
			if (!dictDir.exists()) {  // Path does not exist
				System.out.println("ERROR: Dictionary folder " + dictStr + " does not exist.");
				System.exit(1);
			}
			if (!dictDir.isDirectory()) {   // Path not a directory
				System.out.println("ERROR: " + dictStr + " is not a directory.");
				System.exit(1);
			}
			
			// read dictionary file
			File dictFile = new File(dictDir, "FlowJo_Dict.xls");
			if (!dictFile.exists()) {
				System.out.println("ERROR: " + dictFile.getCanonicalPath() + " does not exist.");
				System.exit(1);
			}
			
			FileInputStream dictfis = new FileInputStream(dictFile);
			HSSFWorkbook dictbook = new HSSFWorkbook(dictfis);
			HSSFSheet dictsheet = dictbook.getSheetAt(0);

			List<String> col1 = new ArrayList<String>();
			List<String> col2 = new ArrayList<String>();
			List<String> col3 = new ArrayList<String>();
			List<String> col4 = new ArrayList<String>();
			
			Iterator<Row> rowIter = dictsheet.rowIterator();
			
			int rowCount = 0;			
			while (rowIter.hasNext()) {
				Row row = rowIter.next();
				if (rowCount > 0) {
					col1.add(row.getCell(0).getStringCellValue().toLowerCase());
					col2.add(row.getCell(1).getStringCellValue());
					col3.add(row.getCell(2).getStringCellValue());
					col4.add(row.getCell(3).getStringCellValue());
				}
				rowCount ++;
			}
			
			List<Sample> samples = new ArrayList<Sample>();
			List<File> files = listXls(dirStr, col1);
			
			// read data files one by one
			for (File file : files) {
				FileInputStream fis = new FileInputStream(file);
				// create a workbook from input data excel file
				HSSFWorkbook workbook = new HSSFWorkbook(fis);
				// get the first sheet from data file
				HSSFSheet sheet = workbook.getSheetAt(0);
				String name = file.getName().substring(0, file.getName().length() - 4).split("_", 2)[1];
				for (int i=0; i < col1.size(); i++) {
					if (col1.get(i).equalsIgnoreCase(name)) {
						rowIter = sheet.rowIterator();
						rowCount = 0;
						List<Integer> comList = new ArrayList<Integer>();
						List<Integer> isoList = new ArrayList<Integer>();
						List<Integer> nonList = new ArrayList<Integer>();
						// count the rows that have "com" keyword
						while (rowIter.hasNext()) {
							Row row = rowIter.next();
							rowCount ++;
							if (row.getCell(0) != null && row.getCell(0).getStringCellValue().toLowerCase().contains("mean")) {
								break;
							}
							String cell2 = row.getCell(2).getStringCellValue().toLowerCase();
							if (rowCount != 1) {
								if (cell2.contains("com")) {
									comList.add(row.getRowNum());
								}
								else if (cell2.contains("iso")) {
									isoList.add(row.getRowNum());
								}
								else {
									nonList.add(row.getRowNum());
								}
							}
						}
						if (comList.size() == 0 && nonList.size() != 0) {
							comList.clear();
							comList = nonList;
						}
						
						if (comList.size() == 0) {  // no "com" specified in column "Staining", all rows are accounted for
							if (rowCount > 2 && (rowCount - 2) < 5) {  // total lines <= 4
								for (int j = 1; j < rowCount - 1; j ++) {
									try {
										Sample samp = new Sample(col1.get(i), col2.get(i), col3.get(i), col4.get(i), sheet.getRow(0), sheet.getRow(j), dictbook);
										samples.add(samp);
									} catch (Exception e) {
										System.out.println("Error: Row " + Integer.toString(j) + " in " + file.getCanonicalPath());
										System.exit(1);
									}	
								}
							}
							else { 
								System.out.println("ERROR: Invalid number of rows in file.");
								System.exit(1);
							}
						}
						else { // only read the rows with "com" in "staining" column
							for (int j : comList) {
								try {
									Sample samp = new Sample(col1.get(i), col2.get(i), col3.get(i), col4.get(i), sheet.getRow(0), sheet.getRow(j), dictbook);
									samples.add(samp);
								} catch (Exception e) {
									System.out.println("Error: Row " + Integer.toString(j) + " in " + file.getCanonicalPath());
									System.exit(1);
								}
							}
						}
					}
				}
				workbook.close();
			}
			
			HashMap<Integer, List<Sample>> sampMap = new HashMap<Integer, List<Sample>>();
			
			for (Sample s : samples) {
				if (!sampMap.containsKey(s.getAccession())) {
					List<Sample> slist = new ArrayList<Sample>();
					slist.add(s);
					sampMap.put(s.getAccession(), slist);
				}
				else {
					List<Sample> slist = sampMap.get(s.getAccession());
					slist.add(s);
					sampMap.put(s.getAccession(), slist);
				}
			}
			
			// sort HashMap using TreeMap
			TreeMap<Integer, List<Sample>> sortedMap = new TreeMap<Integer, List<Sample>>(sampMap);
			
			// create workbook and print the first row with column names
			HSSFWorkbook workbook = new HSSFWorkbook();
			HSSFSheet sheet = workbook.createSheet();
			Row row = sheet.createRow(0);
			String[] firstrow = {"PrometheusSpecimenID", "Umbrella_Protocol_ID","Umbrella_Protocol_Core_Accession_Number",
					             "Umbrella_Protocol_Collection_Number","Panel_Code","Panel_Name","Panel_Antibodies",
					             "Gate_Code","Gate_Name","Definition_Gate_Population","Gate_Value(%)","Parent_Gate",
					             "Record_Insert_Date","Record_Modified_Date","Delete_Flag"};
			writeXlsRow(row, 0, firstrow);
			int rowID = 1;
			
			// iterate through map, sort based on cycle and write to file
			for (Map.Entry<Integer, List<Sample>> entry : sortedMap.entrySet()) {
			    List<Sample> slist = entry.getValue();
			    Collections.sort(slist, new Comparator<Sample>() {
			    	public int compare(Sample s1, Sample s2) {
				    	if (s1.getCollection() > s2.getCollection()) {
							return 1;
						}
						if (s1.getCollection() < s2.getCollection()) {
							return -1;
						}
						if (s1.getCollection() == s2.getCollection()) {
							if (s1.getPanelname().compareToIgnoreCase(s2.getPanelname()) > 0)
								return 1;
							if (s1.getPanelname().compareToIgnoreCase(s2.getPanelname()) < 0)
								return -1;
						}
						return 0;
				    }
			    });
			    rowID = writeXls(sheet, rowID, slist);   
			}	
			
			// create file and output
			File outfile = new File(dirStr, dataDir.getName() + "_summary.xls");
			FileOutputStream out;
			try {
				out = new FileOutputStream(outfile);
				workbook.write(out);
		        workbook.close();
		        System.out.println(outfile.getAbsolutePath() + " is generated with " + Integer.toString(sortedMap.size()) + " patients");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} 
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}


	/**
	 * 
	 * @param path - path to data files
	 * @param list - list of qualified filenames from Panel.dict
	 * @return - list of legit files
	 */
	public static List<File> listXls(String path, List<String> list) {
		
		File dataDir = new File(path);
		List<File> foundFiles = new ArrayList<File>();
		// list all xls files
		File[] files = dataDir.listFiles(new FilenameFilter() {
		    public boolean accept(File dir, String name) {
		        return name.toLowerCase().endsWith(".xls");
		    }
		});
		
		for (File f : files) {
			String filename = f.getName().toLowerCase();
			if (filename.indexOf("_") >= 0)
				if (StringUtils.isNumeric(filename.split("_", 2)[0]) && list.contains(filename.substring(0,filename.length() - 4).split("_", 2)[1])) {
					foundFiles.add(f);
				}
		}
		return foundFiles;
	}	
	
	/**
	 * 
	 * @param sheet - output sheet
	 * @param rid - row ID
	 * @param samples - list of sorted Sample objects
	 * @return - current row number
	 */
	public static int writeXls(HSSFSheet sheet, int rid, List<Sample> samples) {
		int rowID = rid;
		//get current date and time as Record_Insert_Date
		DateFormat dateFormat = new SimpleDateFormat("MM/dd/yy hh:mm a");
		Date date = new Date();
		List<Integer> accList = new ArrayList<Integer>();
		for (Sample samp : samples) {
			// create list of unique accession numbers (patient count)
			if (!accList.contains(samp.getAccession()))
				accList.add(samp.getAccession());
			
			for (Gate gt : samp.getGates()) {
				String specimen = samp.getProtocol() + ":" + samp.getAccession() + ":" + samp.getCollection();
				String[] cells = {specimen, samp.getProtocol(), Integer.toString(samp.getAccession()), 
								  Integer.toString(samp.getCollection()),samp.getPanelcode(), samp.getPanelname(), samp.getAntibodies(),
						  		  gt.getCode(), gt.getName(), gt.getDefinition(), Double.toString(gt.getValue()), gt.getParent(),
						  		  dateFormat.format(date), "", "N"};
				Row row = sheet.createRow(rowID++);
				writeXlsRow(row, 0, cells);
				
			}
		}
        return rowID;
	}
	
	/**
	 * write a string array to a particular row of a spreadsheet
	 * @param row - row in excel to write to
	 * @param col - starting point in the row
	 * @param array - String type array
	 * @return - column index of the next cell
	 */
	public static int writeXlsRow(Row row, int col, String[] array) {
		int colIndex = col;
		for (String str : array) {
			Cell cell = row.createCell(colIndex++);
			cell.setCellValue(str);
		}
		return colIndex;
	}				
}