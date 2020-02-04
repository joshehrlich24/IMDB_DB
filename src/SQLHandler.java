import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

/**
 * Class to handle SQL operations
 * 
 * @author Michael Norton
 * @version 1.0
 *
 */
public class SQLHandler {

    private Connection con;
    private MysqlDataSource dataSource;
    private Statement[] stmt;

    private static SQLHandler handler; // variable for the Singleton

    /**
     * constructor - Open the MySQL driver & make a connection & a default
     * statement object.
     */
    public SQLHandler() {

        if ( !init() )
            System.exit( 1 );

    } // constructor

    /**
     * Shutdown all resources
     */
    public void close() {

        try {
            if ( con != null )
                con.close();

            if ( stmt[0] != null )
                stmt[0].close();
            
            if ( stmt[1] != null )
                stmt[1].close();

        } // end try
        catch ( SQLException e ) {
            handleSQLError( e, "Closing Resources" );
            System.exit( 2 );
        }

    }

    /**
     * Execute a SQL Select statement
     * 
     * @param query
     * @return the result set
     */
    public ResultSet executeQuery( String query, int statement ) {

        ResultSet rs = null;

        try {
            rs = stmt[statement].executeQuery( query );

        } // end try

        catch ( SQLException e ) {
            handleSQLError( e, query );

        } // end catch

        return rs;

    } // method executeQuery

    /**
     * Execute an Insert/Update/Delete on a table
     * 
     * @param query
     * @return the number of rows affected
     */
    public int executeUpdate( String query ) {

        int rows = 0;

        try {
            rows = stmt[0].executeUpdate( query );

        } // end try

        catch ( SQLException e ) {
            handleSQLError( e, query );

        } // end catch

        return rows;

    } // method executeUpdate

    /**
     * Get the column heading
     * 
     * @param rs
     * @param col
     * @return the column heading
     */
    public String getHeading( ResultSet rs, int col ) {

        String heading = null;

        ResultSetMetaData rsmd = null;

        try {
            rsmd = rs.getMetaData();
            heading = rsmd.getColumnLabel( col );

        } // end try
        catch ( SQLException e ) {
            handleSQLError( e, "Get Heading" );
        }

        return heading;
    }

    /**
     * Return the number of columns in the result set
     * 
     * @param rs
     * @return the number of columns
     */
    public int getNumColumns( ResultSet rs ) {

        int numCols = 0;
        ResultSetMetaData rsmd;

        try {
            rsmd = (ResultSetMetaData) rs.getMetaData();
            numCols = rsmd.getColumnCount();

        } // end try

        catch ( SQLException e ) {
            handleSQLError( e, "Get Columns" );

        } // end catch

        return numCols;

    } // getColumns

    /**
     * Get the number of rows in the result set
     * 
     * @param rs
     * @return the number of rows
     */
    public int getNumRows( ResultSet rs ) {

        int currentRow = 0;
        int lastRow = 0;

        try {
            currentRow = rs.getRow(); // get cursor row
            rs.last(); // goto end of cursor
            lastRow = rs.getRow(); // get last row number

            // reset the cursor row
            if ( currentRow == 0 )
                rs.beforeFirst();
            else
                rs.absolute( currentRow ); // this may not work in MySQL

        } // end try

        catch ( SQLException e ) {
            handleSQLError( e, "Get Rows" );

        } // end catch

        return lastRow;

    } // method getNumRows

    /**
     * Return the value of the column in a Resultset as a String
     * 
     * @param rs
     * @param int
     * @return String
     */
    public String getString( ResultSet rs, int col ) {

        String value = null;

        try {
            value = rs.getString( col );

        } // end try
        catch ( SQLException e ) {
            handleSQLError( e, "Get String" );
        } // end catch

        return value;

    } // method getString

    /**
     * Move cursor to the next row of the resultset
     * 
     * @param rs
     * @return boolean
     */
    public boolean next( ResultSet rs ) {

        boolean valid = false;

        try {
            valid = rs.next();

        } // end try
        catch ( SQLException e ) {
            handleSQLError( e, "Next Row" );
        } // end catch

        return valid;

    } // method next

    /************************* private methods ********************************/
    
    /**
     * Generic SQL Error Handler - just print error messages
     * 
     * @param e
     * @param query
     */
    private void handleSQLError( SQLException e, String query ) {

        // handle any errors
        System.out.println( "SQLException: " + e.getMessage() );
        System.out.println( "SQLQuery: " + query );
        System.out.println( "SQLState: " + e.getSQLState() );
        System.out.println( "VendorError: " + e.getErrorCode() );

    } // method handleSQLError
    
    
    /**
     * Initialize the database connection.
     * 
     * @return true if successful, false otherwise
     */
    private boolean init() {

        boolean success = false;

        dataSource = new MysqlDataSource();
        
        dataSource.setDatabaseName( "IMDB" );
        dataSource.setUser( "kesterjw" );
        dataSource.setPassword( "cs474" );
        dataSource.setServerName( "mysql.cs.jmu.edu" );
        dataSource.setPort( 3306 );
        dataSource.setUseSSL( false );

        try {
            con = (Connection) dataSource.getConnection();
            stmt = new Statement[2];
            stmt[0] = (Statement) con.createStatement();
            stmt[1] = (Statement) con.createStatement();
            
        } catch ( Exception e ) {
            System.out.println( "Cannot load driver" ); // handle the error
            System.out.println( "Error: " + e.getMessage() );
            System.out.println( "Errno: " + e.getClass() );

        } // end catch

        success = true;
        System.out.println( "Connected" );

        return success;

    } // method init

    /****************************** static methods ****************************/
    
    /**
     * This static method will return the single instance of this class. If the
     * object has not yet been instantiated, it will be instantiated first.
     * 
     * @return this object
     */
    public static SQLHandler getSQLHandler() 
    {

    	System.out.println("in getSQLHandler ");
    	
    	System.out.println(handler);
    	
        if ( handler == null )
            handler = new SQLHandler();
        
        System.out.println("Got an instance");

        return handler;

    }

} // class SQLHandler