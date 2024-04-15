import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;


public class ResultsetCSVWriter
{


	public static void main(String args[]) throws Exception
	{

		String propFile = "reporting_config.txt"; 
		Environment.initialise(propFile);
		String info = Environment.config.getProperty("INFO");
		if(! info.equalsIgnoreCase("Amit Saxena (Released 26 Feb 10)")) {
			System.out.println("Please retain INFO = Amit Saxena (Released 26 Feb 10)");
			System.exit(0);
		}
		int dbSource = Integer.parseInt(Environment.config.getProperty("DB_SOURCE"));
		Connection db = null;
		switch(dbSource){
		case 1:
			db = openDatabase(Environment.config.getProperty("JDBC_DRIVER1"), 
					Environment.config.getProperty("DB_URL1"), 
					Environment.config.getProperty("DB_USER1"), 
					Environment.config.getProperty("DB_PASSWORD1"));
			break;
		case 2:
			db = openDatabase(Environment.config.getProperty("JDBC_DRIVER2"), 
					Environment.config.getProperty("DB_URL2"), 
					Environment.config.getProperty("DB_USER2"), 
					Environment.config.getProperty("DB_PASSWORD2"));
			break;
		default:
			System.out.println("Error - No valid DB_SOURCE specified, valid values are 1 and 2");
			break;
		}
		access(db);
		closeDatabase(db);
	}
	public static void printSQLException(SQLException e) 
	{
		e.printStackTrace();
		while(e != null) {
			System.out.println(e);
			e = e.getNextException(); 
		}
	}


	public static Connection openDatabase(String strDriver, String strURL, String strUserId, String strUserPswd)
	{
		Connection conn;

		try	{
			// Load the jdbc driver.

			DriverManager.registerDriver((Driver)Class.forName(strDriver).newInstance());

			DriverManager.setLoginTimeout(900);		//15 minute timeout

			// Attempt to connect to a driver.
			conn = DriverManager.getConnection(strURL, strUserId, strUserPswd);
		}
		catch (SQLException e) {
			printSQLException(e);
			return null;
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace(); // for debugging
			return null;
		}
		catch (InstantiationException e) {
			e.printStackTrace();
			return null;
		}
		catch (IllegalAccessException e) {
			e.printStackTrace();
			return null;
		}
		return conn;
	}

	public static boolean closeDatabase(Connection conn)
	{
		if(conn == null)
			return true;
		try {
			conn.close();
		}
		catch (SQLException e) {
			return false;
		}
		return true;
	}



	public static void access(Connection db)
	{
		Statement stmt;
		ResultSet rs;

		//Statement update_stmt;
		String CSV_DELIMITER = Environment.config.getProperty("CSV_DELIMITER", ";");
		String QUERY_DELIMITER = Environment.config.getProperty("QUERY_DELIMITER", "#~#");
		String VIEWS_TXT = Environment.config.getProperty("VIEWS_TXT", "views.txt");
		String txtFilePath = Environment.config.getProperty("FILE_PATH", "");
		String SPLIT_QUERY = "\\s*"+QUERY_DELIMITER+"\\s*";
		try
		{	
			// db.setAutoCommit(true);
			BufferedReader scanner;
			String line;
			scanner = new BufferedReader(new FileReader(new File(VIEWS_TXT)));
			ArrayList<String[]> outputQueryList = new ArrayList<String[]>();
			boolean isMultiLine = false;
			StringBuffer sb = null;
			int c = 0;
			while((line=scanner.readLine())!=null){
				if(line.trim().endsWith(QUERY_DELIMITER)){  
					if(!isMultiLine){ /* Implies that the query is contained in a single line */
						String[] outputQuery = line.split(SPLIT_QUERY);
						outputQueryList.add(outputQuery);
						System.out.println("Query " + c++ + " processed.");
						System.out.println(outputQuery[0] + " # " + outputQuery[1]);
					}
					else { /* If it is the last line of a multi-line query */ 
						isMultiLine = false;   /* unset the flag */
						sb = sb.append(" " + line);
						String[] outputQuery = sb.toString().split(SPLIT_QUERY);
						outputQueryList.add(outputQuery);
						System.out.println("Query " + c++ + " processed.");
						System.out.println(outputQuery[0] + " # " + outputQuery[1]);
						sb = null;
					}
				}
				else if (line.trim().length() > 0) {
					isMultiLine = true; /* set the multi line flag */
					if(sb == null) sb = new StringBuffer();
					sb = sb.append(" " + line);
				}
				else { /* blank line, do nothing */
					
				}
			}
			scanner.close();
			for(int i=0; i<outputQueryList.size(); i++){
				stmt = db.createStatement();
				System.out.println(outputQueryList.get(i)[1]);
				rs = stmt.executeQuery(outputQueryList.get(i)[1]);
				CSVWriter tableOut =  new CSVWriter(new FileWriter(txtFilePath+File.separator+outputQueryList.get(i)[0]), CSV_DELIMITER.charAt(0), CSVWriter.NO_QUOTE_CHARACTER);
				tableOut.writeAll(rs, true);
				tableOut.flush();
				tableOut.close();
			}

		} // end try
		catch(IOException e)
		{
			e.printStackTrace();
			return;
		}
		catch(SQLException e) 
		{
			printSQLException(e);
			return;
		}
	}
}

