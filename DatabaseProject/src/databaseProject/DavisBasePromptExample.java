package databaseProject;

import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;
import java.io.FileReader;
import java.util.Scanner;
import java.util.SortedMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import static java.lang.System.out;

/**
 *  @author Chris Irwin Davis
 *  @version 1.0
 *  <b>
 *  <p>This is an example of how to create an interactive prompt</p>
 *  <p>There is also some guidance to get started wiht read/write of
 *     binary data files using RandomAccessFile class</p>
 *  </b>
 *
 */
public class DavisBasePromptExample {

	/* This can be changed to whatever you like */
	static String prompt = "davisql> ";
	static String version = "v1.0b(example)";
	static String copyright = "©2016 Chris Irwin Davis";
	static boolean isExit = false;
	/*
	 * Page size for alll files is 512 bytes by default.
	 * You may choose to make it user modifiable
	 */
	static final int pageSize = 512; 
	
	static final int TABLE_LEAF_PAGE = 13;
	static final int TABLE_INTERIOR_PAGE = 5;
	static final int INDEX_LEAF_PAGE = 10;
	static final int INDEX_INTERIOR_PAGE = 2;
	
	static String workingDirectory = System.getProperty("user.dir"); // gets current working directory
	static String data_directory = workingDirectory + File.separator + "data" ;
	static String user_data_directory = data_directory + File.separator + "user_data" ;
	static String catalog_directory = data_directory + File.separator + "catalog" ;

	static String tablesCatalogPath = catalog_directory + File.separator+ "davisbase_tables.tbl";
	static String columnsCatalogPath = catalog_directory + File.separator+ "davisbase_columns.tbl";

	/* 
	 *  The Scanner class is used to collect user commands from the prompt
	 *  There are many ways to do this. This is just one.
	 *
	 *  Each time the semicolon (;) delimiter is entered, the userCommand 
	 *  String is re-populated.
	 */
	static Scanner scanner = new Scanner(System.in).useDelimiter(";");
	
	/** ***********************************************************************
	 *  Main method
	 */
    public static void main(String[] args) {
    	
    	initializeDataStore();
    	
		/* Display the welcome screen */
		splashScreen();

		/* Variable to collect user input from the prompt */
		String userCommand = ""; 

		while(!isExit) {
			System.out.print(prompt);
			/* toLowerCase() renders command case insensitive */
			userCommand = scanner.next().replace("\n", " ").replace("\r", "").trim().toLowerCase();
			// userCommand = userCommand.replace("\n", "").replace("\r", "");
			parseUserCommand(userCommand);
		}
		System.out.println("Exiting...");


	}

	static void initializeDataStore() {

		/** Create data directory at the current OS location to hold */
		File dataDir = new File(data_directory);
		File userDataDir = new File(user_data_directory);
		File catalogDir = new File(catalog_directory);
		try {
			if(!dataDir.isDirectory()) {
				System.out.println("No data dir");
				dataDir.mkdir();
				System.out.println("data directory made");
				userDataDir.mkdir();
				System.out.println("User data directory made");
				catalogDir.mkdir();
				System.out.println("catalog directory made");
				
			}else {
				System.out.println("there is a data directory");
				if(!userDataDir.isDirectory()) {
					userDataDir.mkdir();
					System.out.println("User data directory made");
				}else {
					System.out.println("User data directory already there");				
				}
				if(!catalogDir.isDirectory()) {
					catalogDir.mkdir();
					System.out.println("catalog directory made");
				}else {
					System.out.println("catalog directory made");					
				}
			}
			
			/*
			String[] oldTableFiles;
			oldTableFiles = dataDir.list();
			for (int i = 0; i < oldTableFiles.length; i++) {
				File anOldFile = new File(dataDir, oldTableFiles[i]);
				anOldFile.delete();
			}
			*/
		} catch (SecurityException se) {
			out.println("Unable to create data container directory");
			out.println(se);
		}

		/** Create davisbase_tables system catalog */
		if(Files.notExists(Paths.get(tablesCatalogPath))) {
			try {
				RandomAccessFile davisbaseTablesCatalog = new RandomAccessFile(tablesCatalogPath, "rw");
				davisbaseTablesCatalog.setLength(pageSize);
				davisbaseTablesCatalog.seek(0);
				davisbaseTablesCatalog.write(TABLE_LEAF_PAGE);
				davisbaseTablesCatalog.write(0x00);
				davisbaseTablesCatalog.writeShort(512);
				davisbaseTablesCatalog.writeInt(-1);
				davisbaseTablesCatalog.close();
			} catch (Exception e) {
				out.println("Unable to create the database_tables file");
				out.println(e);
			}
			
		}

		/** Create davisbase_columns systems catalog */
		if(Files.notExists(Paths.get(columnsCatalogPath))) {
			try {
				RandomAccessFile davisbaseColumnsCatalog = new RandomAccessFile(columnsCatalogPath, "rw");
				davisbaseColumnsCatalog.setLength(pageSize);
				davisbaseColumnsCatalog.seek(0); // Set file pointer to the beginnning of the file
				davisbaseColumnsCatalog.write(TABLE_LEAF_PAGE);			
				davisbaseColumnsCatalog.write(0x00);
				davisbaseColumnsCatalog.writeShort(512);
				davisbaseColumnsCatalog.writeInt(-1);			
				davisbaseColumnsCatalog.close();
			} catch (Exception e) {
				out.println("Unable to create the database_columns file");
				out.println(e);
			}
		}
		
	}

	/** ***********************************************************************
	 *  Static method definitions
	 */

	/**
	 *  Display the splash screen
	 */
	public static void splashScreen() {
		System.out.println(line("-",80));
        System.out.println("Welcome to DavisBaseLite"); // Display the string.
		System.out.println("DavisBaseLite Version " + getVersion());
		System.out.println(getCopyright());
		System.out.println("\nType \"help;\" to display supported commands.");
		System.out.println(line("-",80));
	}
	
	/**
	 * @param s The String to be repeated
	 * @param num The number of time to repeat String s.
	 * @return String A String object, which is the String s appended to itself num times.
	 */
	public static String line(String s,int num) {
		String a = "";
		for(int i=0;i<num;i++) {
			a += s;
		}
		return a;
	}
	
	public static void printCmd(String s) {
		System.out.println("\n\t" + s + "\n");
	}
	public static void printDef(String s) {
		System.out.println("\t\t" + s);
	}
	
		/**
		 *  Help: Display supported commands
		 */
		public static void help() {
			out.println(line("*",80));
			out.println("SUPPORTED COMMANDS\n");
			out.println("All commands below are case insensitive\n");
			out.println("SHOW TABLES;");
			out.println("\tDisplay the names of all tables.\n");
			//printCmd("SELECT * FROM <table_name>;");
			//printDef("Display all records in the table <table_name>.");
			out.println("SELECT <column_list> FROM <table_name> [WHERE <condition>];");
			out.println("\tDisplay table records whose optional <condition>");
			out.println("\tis <column_name> = <value>.\n");
			out.println("DROP TABLE <table_name>;");
			out.println("\tRemove table data (i.e. all records) and its schema.\n");
			out.println("UPDATE TABLE <table_name> SET <column_name> = <value> [WHERE <condition>];");
			out.println("\tModify records data whose optional <condition> is\n");
			out.println("VERSION;");
			out.println("\tDisplay the program version.\n");
			out.println("HELP;");
			out.println("\tDisplay this help information.\n");
			out.println("EXIT;");
			out.println("\tExit the program.\n");
			out.println(line("*",80));
		}

	/** return the DavisBase version */
	public static String getVersion() {
		return version;
	}
	
	public static String getCopyright() {
		return copyright;
	}
	
	public static void displayVersion() {
		System.out.println("DavisBaseLite Version " + getVersion());
		System.out.println(getCopyright());
	}
		
	public static void parseUserCommand (String userCommand) {
		
		/* commandTokens is an array of Strings that contains one token per array element 
		 * The first token can be used to determine the type of command 
		 * The other tokens can be used to pass relevant parameters to each command-specific
		 * method inside each case statement */
		// String[] commandTokens = userCommand.split(" ");		
		
		ArrayList<String> commandTokens = new ArrayList<String>(Arrays.asList(userCommand.split(" ")));
		
		/*
		*  This switch handles a very small list of hardcoded commands of known syntax.
		*  You will want to rewrite this method to interpret more complex commands. 
		*/
		switch (commandTokens.get(0)) {
			case "create":
				System.out.println("CASE: CREATE");
				parseCreateTable(userCommand);
				break;
			case "insert":
				/*
				1) INSERT INTO table_name (column1, column2, column3, ...)
				   VALUES (value1, value2, value3, ...);
				2) INSERT INTO table_name
				   VALUES (value1, value2, value3, ...);
				*/
				System.out.println("CASE INSERT");
				parseInsertTable(userCommand);
				break;
			case "select":
				//select * from table_name where condition;
				System.out.println("CASE: SELECT");
				parseQuery(userCommand);
				break;
			case "update":
				/*
				 UPDATE table_name
				 SET column1 = value1, column2 = value2, ...
				 WHERE condition;
				*/
				System.out.println("CASE: UPDATE");
				parseUpdate(userCommand);
				break;
			case "delete":
				//DELETE FROM table_name WHERE condition;
				break;
			case "drop":
				//DROP TABLE table_name;
				//drop table delete table_file also
				System.out.println("CASE: DROP");
				dropTable(userCommand);
				break;
			case "help":
				help();
				break;
			case "version":
				displayVersion();
				break;
			case "exit":
				isExit = true;
				break;
			case "quit":
				isExit = true;
			default:
				System.out.println("I didn't understand the command: \"" + userCommand + "\"");
				break;
		}
	}
	

	public static void parseInsertTable(String userCommand) {
		// TODO Auto-generated method stub
		
	}

	/**
	 *  Stub method for dropping tables
	 *  @param dropTableString is a String of the user input
	 */
	public static void dropTable(String dropTableString) {
		System.out.println("STUB: This is the dropTable method.");
		System.out.println("\tParsing the string:\"" + dropTableString + "\"");
	}
	
	/**
	 *  Stub method for executing queries
	 *  @param queryString is a String of the user input
	 */
	public static void parseQuery(String queryString) {
		System.out.println("STUB: This is the parseQuery method");
		System.out.println("\tParsing the string:\"" + queryString + "\"");

		boolean isAll = false;
		boolean isCondition = false;
		String condition = "", select_coloumns = "";

		String where[], from[], select[];
		String table_name = "";

		if (queryString.indexOf("where") != -1) {
			isCondition = true;
			where = queryString.split("where");
			condition = where[1].trim();

			from = where[0].trim().split("from");
			table_name = from[1].trim();

			select = from[0].trim().split("select");
			if (select[1].trim().equalsIgnoreCase("*"))
				isAll = true;
			else
				select_coloumns = select[1];
		} else {
			from = queryString.split("from");
			table_name = from[1].trim();
			select = from[0].trim().split("select");
			if (select[1].trim().equalsIgnoreCase("*")) {
				isAll = true;
			} else
				select_coloumns = select[1];
		}
		// all is for columns & condition is for rows
		executeSelect(select_coloumns, table_name, condition, isAll, isCondition);

	}
	
	public static void executeSelect(String select_coloumns, String table_name, String condition, boolean isAll,
			boolean isCondition) {

		if (!isAll) {
			String columns[] = select_coloumns.split(",");
			for (int i = 0; i < columns.length; i++) {
				columns[i] = columns[i].trim();
			}
			// something to do here
		}

		if (isAll && !isCondition) {
			// display everything in the table
			System.out.println("isAll n no condition");
		} else if (isAll && isCondition) {
			// display all that satisfy the 'condition'
			System.out.println("* with where");
		} else if (!isAll && !isCondition) {
			// display only 'select_columns' and all rows
			System.out.println("some cols with NO where");
		} else if (!isAll && isCondition) {
			// display 'select_columns' and those rows that satisfy the 'condition'
			System.out.println("some cols with where");
		}

	}

	/**
	 *  Stub method for updating records
	 *  @param updateString is a String of the user input
	 */
	public static void parseUpdate(String updateString) {
		System.out.println("STUB: This is the dropTable method");
		System.out.println("Parsing the string:\"" + updateString + "\"");
	}

	
	/**
	 *  Stub method for creating new tables
	 *  @param queryString is a String of the user input
	 */
	public static void parseCreateTable(String createTableString) {
		
		//when there is a PK in a table create a index file for that table on this PK
		
		System.out.println("STUB: Calling your method to create a table");
		System.out.println("Parsing the string:\"" + createTableString + "\"");
		
		String createTableTokens[] = createTableString.replace(")", " ").trim().split("\\(");
		
		/* Define table file name */
		String table_name = createTableTokens[0].split(" ")[2];
		String insert="";
		//check if the table already exists
		if(!tableExists(table_name)) {
			if(createTableTokens.length==1) {
				System.out.println("Invalid command: Attribute List Missing");
			}else {
				String attributeList = createTableTokens[1];
				String[] columns_dataTypes = attributeList.split(",");
				boolean error=false;
				for(int i=0;i<columns_dataTypes.length;i++) {
					String cd[]=columns_dataTypes[i].trim().split(" ");
					String column_name=cd[0];
					String data_type = cd[1];
					System.out.println(column_name+" "+data_type);
					if(!validDataType(data_type)) {
						System.out.println("Invalid command: Wrong data type of column "+(i+1));
						error=true;
						break;
					}			
				}
				if(!error) {
					//create file of table
					createFile(user_data_directory, table_name);
					
					//insert table_name in davisbase_tables
					insert ="insert into davisbase_tables values ("+table_name+")";
					parseInsertTable(insert);//to implement
					
					//insert columns in davisbase_columns
					for(String t: columns_dataTypes) {
						String cd[] = t.trim().split(" ");
						String column_name=cd[0];
						String data_type = cd[1];
						insert = "insert into davisbase_columns(table_name, column_name, data_type) values ("+table_name+", "+column_name+", "+data_type+")";
						parseInsertTable(insert);//to implement										
					}
					System.out.println("Table created successfully");
				}
			}
		}else {
			System.out.println("Table already exists!!");
		}
		
	}

	public static void createFile(String file_path, String table_name) {

		String tableFileName = file_path + File.separator + table_name + ".tbl";			
		
		/*  Code to create a .tbl file to contain table data */
		try {
			RandomAccessFile tableFile = new RandomAccessFile(tableFileName, "rw");
			tableFile.setLength(pageSize);
			tableFile.seek(0);//go to start of file to write from here
			tableFile.writeByte(TABLE_LEAF_PAGE);//type of page
			tableFile.writeByte(0);//no of records in this page
			tableFile.writeShort(512);//start of content at this address 
			tableFile.writeInt(-1);//leaf page & the rightmost page		
			tableFile.close();
		}
		catch(Exception e) {
			System.out.println(e);
		}

	}

	public static boolean validDataType(String data_type) {
		//check if the data_type entered is valid dataType
		HashSet<String> data_types = new HashSet<>();
		data_types.add("int");
		data_types.add("integer");
		data_types.add("tinyint");
		data_types.add("smallint");
		data_types.add("bigint");
		data_types.add("float");
		data_types.add("double");
		data_types.add("real");
		data_types.add("bool");
		data_types.add("boolean");
		data_types.add("date");
		data_types.add("datetime");
		data_types.add("char");
		data_types.add("varchar");
		data_types.add("text");
		
		if(data_types.contains(data_type))
			return true;	
		else if(data_types.contains(data_type.split("\\(")[0]))//for varchar(80), char(20), etc
			return true;
		return false;
	}

	public static boolean tableExists(String table_name) {
		//drop table delete tables file also
		//check in davisbase_tables table if a table with name table_name exists
		File file = new File(user_data_directory+File.separator+table_name+".tbl");
		if(file.exists() && !file.isDirectory())
			return true;
		return false;
	}
}