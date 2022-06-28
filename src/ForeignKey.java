public class ForeignKey {
    private String pkTable;
    private String pkColumn;
    private String fkTable;
    private String fkColumn;

    ForeignKey(String pkTable, String pkColumn, String fkTable, String fkColumn){
        this.pkTable = pkTable;
        this.pkColumn = pkColumn;
        this.fkTable = fkTable;
        this.fkColumn = fkColumn;
    }

    public String getpkTable(){
        return pkTable;
    }
    
    public String getpkColumn(){
        return pkColumn;
    }

    public String getfkTable(){
        return fkTable;
    }

    public String getfkColumn(){
        return fkColumn;
    }
}
