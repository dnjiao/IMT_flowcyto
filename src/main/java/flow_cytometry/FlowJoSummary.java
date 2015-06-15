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
		
		List<File> files = listXls(dirStr, dictStr);
		
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
	

	public static List<File> listXls(String path1, String path2) {
		File dataDir = new File(path1);
		// list all xls files
		File[] files = dataDir.listFiles(new FilenameFilter() {
		    public boolean accept(File dir, String name) {
		        return name.toLowerCase().endsWith(".xls");
		    }
		});
		List<File> foundFiles = new ArrayList<File>();
		for (File file: files) {
			String filename = file.getName();
			if (!StringUtils.isNumeric(filename.split("_", 2)[0])) {
				System.out.println("ERROR: Invalid filename - " + filename);
				System.exit(1);
			}
			if (!)
			
			// looking for all subdirectories in the current folder
			if (file.isDirectory()) {
				String fileStr = file.getName() + "_" + fileName;
				File[] subFiles = file.listFiles();
				// looking for certain file in each subdirectories.
				for (File subfile : subFiles) {
					// if file found, add to the return list
					if (subfile.isFile() && subfile.getName().equals(fileStr)) {
						foundFiles.add(subfile);
					}
				}
			}
		}
		return foundFiles;
	}
	
	private List<Gate> parsePanelDict(String dictFile) {
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