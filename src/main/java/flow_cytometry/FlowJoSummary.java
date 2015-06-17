package flow_cytometry;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

public class FlowJoSummary {
	
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
			System.exit(1);
		}
		if (!dataDir.isDirectory()) {   // Path not a directory
			System.out.println("ERROR: " + dirStr + " is not a directory.");
			System.exit(1);
		}
		// user's default home directory
		String home = System.getProperty("user.home");
		File dictDir = new File(home, "Flow_Dict");
		
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
		
		// read panel.dict
		File panelDict = new File(dictDir, "Panel.dict");
		if (!panelDict.exists()) {
			System.out.println("ERROR: " + panelDict.getCanonicalPath() + " does not exist.");
			System.exit(1);
		}
		try {
			BufferedReader reader = new BufferedReader(new FileReader(panelDict));	
			List<String> col1 = new ArrayList<String>();
			List<String> col2 = new ArrayList<String>();
			List<String> col3 = new ArrayList<String>();
			List<String> col4 = new ArrayList<String>();
			String line;	
			int linenum = 0;
			// Looping the read block until all lines read.
			while ((line = reader.readLine()) != null) {
				linenum ++;
				if (linenum > 1) { // start parsing from 2nd row
					String split[] = line.split("\t");
					if (split.length == 4) {
						col1.add(split[0]);
						col2.add(split[1]);
						col3.add(split[2]);
						col4.add(split[3]);
						
					}
					else {
						System.out.println("ERROR: column number mismatch in Panel.dict");
						System.exit(1);
					}
				}
			}
			
			reader.close();
			
			List<Panel> panels = new ArrayList<Panel>();
			List<File> files = listXls(dirStr, col1);
			for (File file : files) {
				FileInputStream fis = null;
				// create a workbook from input excel file
				HSSFWorkbook workbook = new HSSFWorkbook(fis);
				// get the first sheet
				HSSFSheet sheet = workbook.getSheetAt(0);
				String name = file.getName().split("_", 2)[1];
				for (int i=0; i < col1.size(); i++) {
					if (col1.get(i).equals(name)) {
						Panel panel = new Panel(col2.get(i), col3.get(i), col4.get(i), dictStr, sheet);
						panels.add(panel);
					}
				}
				workbook.close();
			}
			File outfile = new File(dirStr, "summary.xls");
			// sort panels based on accession_id
			Collections.sort(panels, new Comparator<Panel>() {
				public int compare(Panel p1, Panel p2) {
					if (s1.getAccession() < s2.getAccession())
						return 1;
					if (s1.getAccession() > s2.getAccession())
						return -1;
					return 0;
				}
			});
			writeXls(outfile, panels);
		}
		catch (IOException e) {
			e.printStackTrace();
		}

	}
	

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
			String filename = f.getName();
			if (StringUtils.isNumeric(filename.split("_", 2)[0]) && list.contains(filename.split("_", 2)[1])) {
				foundFiles.add(f);
			}
		}
		return foundFiles;
	}	
	
	public static int writeXls(File file, List<Panel> panels) {
		
		try {
			
			
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