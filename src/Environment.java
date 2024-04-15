import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * Handles the main flow of the application standalone CSV generation and diff creation tool.
 * Fetches the original file 
 * Triggers the generation of the new CSV file from view query, using ResultsetCSVWriter.
 * Starts the generation of original diff and then the highlighted diff.  
 * @author 1303540
 *
 */
public final class Environment {
	
	private Environment(){
		/* Environment class cannot be instantiated */
	}
	public static Properties config;
	
	/** 
	 * Initialise the configuration properties
	 *  
	 **/
	public static void initialise(String propertiesFileName) {
		//System.out.println("args :: infile outfile1 outfile2 -outputmode \n-outputmode is -a for appending to existing output files.");
		/*
		 * Input original file url -
		 * http://nfs.gdc.standardchartered.com:8001/cgi-bin/pos_web_reports.cgi?disposition=attachment&download=DATED/MXG_REPORTS/spirit_mtm4_09072008.txt&date=2008_07_10 
		 */
		config = new Properties();
		try {
			config.load(new FileInputStream(propertiesFileName));
		}
		catch (FileNotFoundException e) {
			System.err.println("Property file not found or read access is denied.");
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
