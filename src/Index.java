public class Index {
    private String IndexName;
    private String tableName;
    private String columnName;
    private String nonUnique;

    Index(String indexName, String tableName, String columnName, String nonUnique){
        this.IndexName = indexName;
        this.tableName = tableName;
        this.columnName = columnName;
        this.nonUnique = nonUnique.equals("1")?"":"UNIQUE";
    }

    public void addColumn(String columnName){
        this.columnName += ", "+ columnName;
    }

    public String getIndex(){
        return IndexName;
    }

    public String getTable(){
        return tableName;
    }

    public String getColumn(){
        return columnName;
    }

    public String getNonUnique(){
        return nonUnique;
    }
}
