package flow_cytometry;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

public class FlowSummary {
	
	/****
	 * Read in a raw data file, usually in xls format
	 * @param path: full path to the input file
	 * @return FlowJo object with element parsed from input
	 */
	public static List<FlowJo> readXls(String xlsPath) {
		List<FlowJo> flowList = new ArrayList<FlowJo>();
		List<String> gateList = new ArrayList<String>();
		FileInputStream fis = null;
		try {
			File file = new File(xlsPath);
			String fName = file.getName();
			// get the panel name from filename
			String panel = fName.split(" ")[1];
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
			
			while (rowIter.hasNext()) {
				row = rowIter.next();
				int col = 0;
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
								flowData.setExpDate(splits[1]);
								flowData.setMrn(Integer.parseInt(splits[2].substring(2)));
								flowData.setProtocol(String.join("-", splits[3].split("-")[0], splits[3].split("-")[1]));
								flowData.setAccession(Integer.parseInt(splits[3].split("-")[2]));
								flowData.setCollection(Integer.parseInt(splits[3].split("-")[3]));
								
							}
							if (cellIndex == 1)
								flowData.setSample(cell.getStringCellValue());
							if (cellIndex == 2)
								flowData.setStaining(cell.getStringCellValue());
							if (cellIndex > 2 && cellIndex <= col) 
								flowData.setGateMap(gateList.get(cellIndex - 3), Double.parseDouble(cell.getStringCellValue()));
								
							cellIndex ++;
							
						}
						flowList.add(flowData);
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
						System.arraycopy(lineSplit, 1, sArray, 0, 6);
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
					
					
}