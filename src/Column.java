import java.util.ArrayList;

public class Column {
    private String name;
    private String type;
    private String isNullable;
    private ArrayList<String> data = new ArrayList<>();

    Column(String name, String type, String isNullable){
        this.name = name;
        this.type = type;
        this.isNullable = isNullable.equals("N")?"NOT NULL":"";
    }

    public void addData(String content){
        data.add(content);
    }

    public String getName(){
        return name;
    }

    public String getType(){
        return type;
    }


    public String getNullable(){
        return isNullable;
    }

    public ArrayList<String> getData(){
        return data;
    }

}
