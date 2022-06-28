import java.util.ArrayList;

public class Table {

    private String name;
    private ArrayList<Column> columns = new ArrayList<>();
    private ArrayList<String> primaryKeys = new ArrayList<>();
    private ArrayList<ForeignKey> foreignKeys = new ArrayList<>();
    private ArrayList<Index> indexs = new ArrayList<>();


    Table(String tableName){
        this.name = "'"+tableName+"'";
    }

    public void addColumn(String name, String type, String size, String isNullable){
        columns.add(new Column(name, type, isNullable));
    }

    public void addColumn(Column column){
        columns.add(column);
    }

    public void addPrimaryKey(String primaryKey){
        primaryKeys.add(primaryKey);
    }

    public void addForeignKey(String pkTable, String pkColumn, String fkTable, String fkColumn){
        foreignKeys.add(new ForeignKey(pkTable, pkColumn, fkTable, fkColumn));
    }

    public void addIndex(String indexName, String tableName, String columnName, String nonUnique){
        indexs.add(new Index(indexName, tableName, columnName, nonUnique));
    }

    public void addIndex(Index index){
        indexs.add(index);
    }

    public void addIndexColumn(String columnName, int indexNum){
        indexs.get(indexNum).addColumn(columnName);
    }

    public String getName(){
        return name;
    }

    public ArrayList<Column> getColumns(){
        return columns;
    }

    public ArrayList<String> getPrimaryKeys(){
        return primaryKeys;
    }

    public ArrayList<ForeignKey> getForeignKeys(){
        return foreignKeys;
    }

    public ArrayList<Index> getIndex(){
        return indexs;
    }



}
