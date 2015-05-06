package flow_cytometry;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

public class FlowSummary {
	
	public static void main(String[] args) {
		System.out.println("Path to the directory with data files:");
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
			System.exit(0);
		}
		if (!dataDir.isDirectory()) {   // Path not a directory
			System.out.println("ERROR: " + dirStr + " is not a directory.");
			System.exit(0);
		}
		// user's default home directory
		String home = System.getProperty("user.home");
		File dictDir = new File(home, "Flow_Dict");
		
		try {
			String dictStr = dictDir.getCanonicalPath();
			System.out.println("Path to flow cytometry data dictionary files [" + dictStr + "]");

			if (!dictDir.exists()) {  // Path does not exist
				System.out.println("ERROR: Dictionary folder " + dictStr + " does not exist.");
				System.exit(0);
			}
			if (!dictDir.isDirectory()) {   // Path not a directory
				System.out.println("ERROR: " + dictStr + " is not a directory.");
				System.exit(0);
			}
			
			HSSFWorkbook workbook = new HSSFWorkbook();
			HSSFSheet sheet = workbook.createSheet();
			Row row = sheet.createRow(0);
			Cell cell;
			String[] firstrow = {"PrometheusSpecimenID", "Umbrella_Protocol_ID","Umbrella_Protocol_Core_Accession_Number",
					             "Umbrella_Protocol_Collection_Number","Panel_Code","Panel_Name","Panel_Antibodies",
					             "Gate_Code","Gate_Name","Definition_Gate_Population","Gate_Value(%)","Parent_Gate",
					             "Record_Insert_Date","Record_Modified_Date","Delete_Flag"};
			writeXlsRow(row, 0, firstrow);
			int rowID = 1;
			
			
			String[] attributes = new String[6];
			File[] dataFiles = dataDir.listFiles();
			for (File file: dataFiles) {
				if (file.getName().toLowerCase().endsWith("xls") && !file.getName().equals("summary.xls")) {
					List<FlowJo> list = new ArrayList<FlowJo>();
					list = readXls(file.getAbsolutePath());
					if (list.size() != 0) {
						String panel = list.get(0).getPanel();
						int colID;
						for (FlowJo f : list) {
							for (String gate : f.getGateMap().keySet()) {
								colID = 0;
								row = sheet.createRow(rowID++);
								cell = row.createCell(colID++);
								cell.setCellValue(f.getSpecimen());
								cell = row.createCell(colID++);
								cell.setCellValue(f.getProtocol());
								cell = row.createCell(colID++);
								cell.setCellValue(f.getAccession());
								cell = row.createCell(colID++);
								cell.setCellValue(f.getCollection());
								attributes = readDict(dictStr, panel, gate);
								colID = writeXlsRow(row, colID, Arrays.copyOfRange(attributes, 0, attributes.length - 1));
								cell = row.createCell(colID++);
								cell.setCellValue(f.getGateMap().get(gate));
								cell = row.createCell(colID++);
								cell.setCellValue(attributes[attributes.length - 1]);
								// getting the last modified timestamp of the file
								Path path = file.toPath();
								BasicFileAttributeView view = Files.getFileAttributeView(path, BasicFileAttributeView.class);
								BasicFileAttributes attr = view.readAttributes();
								cell = row.createCell(colID);
								cell.setCellValue(attr.lastModifiedTime().toString());
								cell = row.createCell(colID+2);
								cell.setCellValue("N");
							}
						}
					}
				}
			}
			FileOutputStream out = new FileOutputStream(new File(dirStr, "summary.xls"));
            workbook.write(out);
            workbook.close();
            System.out.println("summary.xls is generated.");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	
	/****
	 * Read in a raw data file, usually in xls format
	 * @param path: full path to the input file
	 * @return FlowJo object with element parsed from input
	 */
	public static List<FlowJo> readXls(String xlsPath) {
		List<FlowJo> flowList = new ArrayList<FlowJo>();
		ArrayList<String> gateList = new ArrayList<String>();
		ArrayList<Double> valueList = new ArrayList<Double>();
		FileInputStream fis = null;
		try {
			File file = new File(xlsPath);
			//String fName = file.getName();
			// get the panel name from filename
			//String panel = fName.split(" ")[1].substring(0, -4);
			String panel = "Costimulatory";
			fis = new FileInputStream(file);
			
			// create a workbook from input excel file
			HSSFWorkbook workbook = new HSSFWorkbook(fis);
			// get the first sheet
			HSSFSheet sheet = workbook.getSheetAt(0);			
			Row row;
			Cell cell;
			
			// iterate on the rows
			Iterator<Row> rowIter = sheet.rowIterator();
			
			int rowIndex = 0;
			int cellIndex = 0;
			int col = 0;
			while (rowIter.hasNext()) {
				row = rowIter.next();
				
				if (rowIndex == 0) { // first row in spreadsheet
					col = 3;
					cell = row.getCell(col);
					// read column names into a list
					while (cell != null) {
						gateList.add(cell.getStringCellValue());
						col ++;
						cell = row.getCell(col);
					}
				}
				else {  // rows other than first
					if (row.getCell(2) != null) {
					// Only keep the "com" rows
						if (row.getCell(2).getStringCellValue().endsWith("com")) {
							FlowJo flowData = new FlowJo();
							Iterator<Cell> cellIter = row.cellIterator();
							while (cellIter.hasNext()) {
								cell = cellIter.next();
								flowData.setPanel(panel);
								if (cellIndex == 0) {
									String str = cell.getStringCellValue();
									String[] splits = str.split(" ");
									flowData.setFullname(str);
									flowData.setExpDate(splits[2]);
									flowData.setMrn(Integer.parseInt(splits[3].substring(3)));
									flowData.setProtocol(String.join("-", splits[4].split("-")[0], splits[4].split("-")[1]));
									flowData.setAccession(Integer.parseInt(splits[4].split("-")[2]));
									flowData.setCollection(Integer.parseInt(splits[4].split("-")[3]));
									
								}
								if (cellIndex == 1)
									flowData.setSample(cell.getStringCellValue());
								if (cellIndex == 2)
									flowData.setStaining(cell.getStringCellValue());
								if (cellIndex > 2 && cellIndex <= col) {
									//flowData.setGateMap(gateList.get(cellIndex - 3), cell.getNumericCellValue());
									valueList.add(cell.getNumericCellValue());
								}
								cellIndex ++;
							}
							flowData.setGateMap(gateList, valueList);
							valueList.clear();
							flowList.add(flowData);
						}
					}
				}
				rowIndex ++;
				cellIndex = 0;
			}
			workbook.close(); 
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return flowList;
	}
	
	/**
	 * Obtain panel info from a dictionary
	 * @param dictPath - path to the dictionary file
	 * @param panel - name of the panel
	 * @param gate - name of the gate
	 * @return string array of the information
	 */
	public static String[] readDict (String dictPath, String panel, String gate) {
		String sArray[] = new String[7];
		File dictFile = new File(dictPath, panel + ".dict");
		if (dictFile.exists()) {
			try {
				// Create a buffered reader to read the file
				BufferedReader bReader = new BufferedReader(new FileReader(dictFile));			
				String line;			
				// Looping the read block until all lines read.
				while ((line = bReader.readLine()) != null) {
					String lineSplit[] = line.split("\t");
					if (lineSplit[0].equals(gate)) {
						System.arraycopy(lineSplit, 1, sArray, 0, 7);
						break;
					}
				}
				bReader.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		else {
			System.out.println("ERROR: File" + dictFile.getAbsolutePath() + "does not exist.");
			System.exit(0);
		}
		return sArray;
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