package db;



import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DB {
	public	static Connection getConnection(String dbname) throws ClassNotFoundException, SQLException
	{
		 String driverName = "com.mysql.jdbc.Driver";
		    Class.forName(driverName);

			
		    String serverName = "db.l3s.uni-hannover.de";
		    String mydatabase = dbname;
		    String url = "jdbc:mysql://" + serverName + "/" + mydatabase+"?useCompression=true"; 

		    String username = "zerr";
		    String password = "aGSbXmmJuseznzm7";
		    return DriverManager.getConnection(url, username, password);	
	}
	
public	static Connection getConnection() throws ClassNotFoundException, SQLException
	{
		
		    return getConnection( "streetart");
		  
	}
public	static Connection getLocalConnection(String db) throws SQLException
{
	 String driverName = "com.mysql.jdbc.Driver";
	    try {
			Class.forName(driverName);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

		
	    String serverName = "localhost";
	    String mydatabase = db;
	    String url = "jdbc:mysql://" + serverName + "/" + mydatabase+"?useCompression=true"; 

	    String username = "root";
	    String password = "root";
	    return DriverManager.getConnection(url, username, password);	
}
public	static Connection getLocalConnection() throws ClassNotFoundException, SQLException
{
	
	    return getLocalConnection("citizenscience");
}
}
