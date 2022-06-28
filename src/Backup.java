import java.io.*;
import java.nio.file.Files;
import java.sql.*;
import java.util.*;


public class Backup{

    private ArrayList<Table> tables = new ArrayList<>();
    private ArrayList<Table> sortedTables = new ArrayList<>();
    private ArrayList<String> queries = new ArrayList<>();

    public void doBackup(String dbName){
        Read(dbName);
        CreateSql();
        WriteSql(dbName);
        BackupDB(dbName+"_backup");
    }

    public void Read(String dbName){
        Connection con = getConnection(dbName);
        try {
            DatabaseMetaData databaseMetaData = con.getMetaData();
            ResultSet resultSet = databaseMetaData.getTables(null, null, null, new String[]{"TABLE"});
            while(resultSet.next()) { 
                String tableName = resultSet.getString("TABLE_NAME");
                tables.add(new Table(tableName));
            }

            for(Table table: tables){
                ResultSet columns = databaseMetaData.getColumns(null, null, table.getName().replace("'", ""), null);
                while(columns.next()) {
                    String columnName = columns.getString("COLUMN_NAME");
                    String datatype = columns.getString("TYPE_NAME");
                    String isNullable = columns.getString("IS_NULLABLE");

                    Column column = new Column(columnName, datatype, isNullable);
                    table.addColumn(column);

                    ResultSet data = con.createStatement().executeQuery("SELECT " + columnName + " FROM " +table.getName());
                    while(data.next()){
                        String content;
                        if(data.getString(1)==null) content = "null";

                        else if(column.getType().equals("BLOB")) {
                            byte [] byteValue = data.getBytes(1);
                            StringBuffer hexValue = new StringBuffer();
                            String temp;
                            if(byteValue == null) {
                                content = "";
                            }else {
                                for (byte b : byteValue) {
                                                    temp = Integer.toHexString(b & 0xFF);
                                                    hexValue.append( (temp.length() == 1)? "0" + temp : temp);
                                                }
                                content = "\""+hexValue+"\"";
                            }
                        }

                        else content = (datatype.indexOf("INT")!=-1 && data.getString(1).equals("\\N"))?"-1":data.getString(1).replace("\"", "'");
                        column.addData(content);
                    }
                }

                
                ResultSet primaryKeys = databaseMetaData.getPrimaryKeys(null, null, table.getName().replace("'", ""));
                while(primaryKeys.next()){ 
                    String primaryKeyColumnName = primaryKeys.getString("COLUMN_NAME");

                    table.addPrimaryKey(primaryKeyColumnName);
                }

                ResultSet foreignKeys = databaseMetaData.getImportedKeys(null, null, table.getName().replace("'", ""));
                while(foreignKeys.next()){
                    String pkTableName = foreignKeys.getString("PKTABLE_NAME");
                    String fkTableName = foreignKeys.getString("FKTABLE_NAME");
                    String pkColumnName = foreignKeys.getString("PKCOLUMN_NAME");
                    String fkColumnName = foreignKeys.getString("FKCOLUMN_NAME");

                    table.addForeignKey(pkTableName, pkColumnName, fkTableName, fkColumnName);
                }

                ResultSet indexInfo = databaseMetaData.getIndexInfo(null, null, table.getName().replace("'", ""), false, false);
                String currentIndexName = "";
                Index currentIndex;
                int count = -1;
                while(indexInfo.next()){
                    String indexName = indexInfo.getString("INDEX_NAME");
                    String tableName = indexInfo.getString("TABLE_NAME");
                    String columnName = indexInfo.getString("COLUMN_NAME");
                    String nonUnique = indexInfo.getString("NON_UNIQUE");

                    if(indexName.indexOf("auto")==-1){
                        if(!indexName.equals(currentIndexName)){
                            currentIndex = new Index(indexName, tableName, columnName, nonUnique);
                            table.addIndex(currentIndex);
                            count++;
                        }
                        else{
                            table.addIndexColumn(columnName, count);
                        }
                        currentIndexName = indexName;
                    }
                    
                }

            }

        } catch(Exception e) {
            System.out.println( e.getClass().getName() + " : " + e );
		    e.printStackTrace ( );
		    System.exit( 0 );
        }
    }

    public void CreateSql(){

        sortTables(tables);

        for(Table table: sortedTables){

            queries.add("DROP TABLE IF EXISTS " + table.getName() +";");

            String query = "CREATE TABLE " + table.getName() + " ( ";
            for(Column column: table.getColumns()){
                query += column.getName() +" "+ column.getType() + column.getNullable() +" , ";
            }

            if(table.getPrimaryKeys().size() != 0){
                query += "PRIMARY KEY (";
                for(String pk: table.getPrimaryKeys()){
                    query += pk+",";
                }
                query = query.substring(0, query.length()-1) + "),";
            }

            if(table.getForeignKeys().size() != 0){
                for(ForeignKey fk: table.getForeignKeys()){
                    query += "FOREIGN KEY (" + fk.getfkColumn() + ")" + " REFERENCES " +fk.getpkTable() +"("+fk.getpkColumn()+"), ";
                }
                query = query.substring(0, query.length()-1);
            }
            query = query.substring(0, query.length()-1) + " );";
            queries.add(query);
            
            if(table.getIndex().size() != 0){
                for(Index index: table.getIndex())
                queries.add("CREATE "+ index.getNonUnique()+" INDEX "+index.getIndex()+" ON " +table.getName() +" ("+index.getColumn()+ " ASC);");
            }
        }

        //Create INSERT statement
        for(Table table: sortedTables){
            for(int i=0; i<table.getColumns().get(0).getData().size(); i++ ){
                String query = "INSERT INTO " +table.getName() + " VALUES(";
                for(Column column: table.getColumns()){
                    if(column.getData().get(i).equals("null")) query += column.getData().get(i) + ",";
                    else if(column.getType().indexOf("CHAR") != -1 || 
                            column.getType().indexOf("TEXT")!=-1 ||
                            column.getType().indexOf("DATE")!=-1 ||
                            column.getType().indexOf("TIME")!=-1) query += "\"" + column.getData().get(i) + "\",";
                    else query += column.getData().get(i) + ",";
                }
                queries.add(query.substring(0, query.length()-1)+");");
            }
        }
    }


    public void WriteSql(String dbName){
        try {
            Files.deleteIfExists(new File(dbName + ".sql").toPath());
            File file = new File(dbName+".sql");
            FileOutputStream fos = new FileOutputStream(file);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
            for(String query: queries){
                bw.write(query);
                bw.newLine();
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }

    public void BackupDB(String dbName) {
        try {
            Files.deleteIfExists(new File(dbName + ".db").toPath());
            Connection con = getConnection(dbName+".db");
            try{
                Statement st = con.createStatement();
                int line =0;
                for(String query: queries) {
                    
                    st.executeUpdate(query);
                    line++;
                }
                st.close();
            } catch (SQLException e){
                e.printStackTrace();
            }
            closeConnection(con);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }



    
    /**
     * Sort table based on their foreign keys
     * @param tables
     */
    private void sortTables(ArrayList<Table> tables){
        if(tables.size()==0) return;
        else{
            for(int i=tables.size()-1;i>=0;i--){
                Table table = tables.get(i);
                //put tables without foreign keys in the front
                if(table.getForeignKeys().size() == 0){
                    sortedTables.add(table);
                    tables.remove(table);
                }
                //put tables which's foreign keys are already in the list into the list
                else{
                    boolean isConstrained = false;
                    ArrayList<String> sortedName = new ArrayList<>();
                    for(Table sortedTable: sortedTables){
                        sortedName.add(sortedTable.getName().replace("'", ""));
                    }
                    for(ForeignKey fk: table.getForeignKeys()){
                        if(!sortedName.contains(fk.getpkTable()) && !fk.getpkTable().equals(table.getName().replace("'", ""))) isConstrained = true;
                    }
                    if(!isConstrained){
                        sortedTables.add(table);
                        tables.remove(table);
                    }
                }
            }
            sortTables(tables);
            return;
        }
    }

    /**
     * Establish JDBC connection with database
	 * <p>
	 * Autocommit is turned off delaying updates
	 * until commit( ) is called
     * @param dbName
     * @return
     */
    private static Connection getConnection(String dbName){
        Connection con = null;
        try {
			Class.forName("org.sqlite.JDBC");
            try {
                con = DriverManager.getConnection(
                        "jdbc:sqlite:"
                        + dbName);

                /*
                * Turn off AutoCommit:
                * delay updates until commit( ) called
                */
                con.setAutoCommit(false);
                
            }
            catch ( SQLException sqle ) {
                System.out.println(sqle);
                closeConnection(con);
            }
		}
		catch ( ClassNotFoundException cnfe ) {
            System.out.println("DB.open: "+cnfe);
		}
        return con;
    }

    /**
     * Close database
     * <p>
     * Commits any remaining updates to database and
     * closes connection
     * @param con
     */
    private static void closeConnection(Connection con){
        try {
            con.commit( );
            con.close ( );
        }
        catch ( Exception e ) {
            System.out.println("DB.close: "+e);
        };
    }

    public ArrayList<String> getQueries(){
        return queries;
    }


}
