
public class Main {
    public static void main(String[] args) {
        String dbName = "Northwind.db";
        
        if(args.length == 1) {
            dbName = args[0];
            Backup backup = new Backup();
            backup.doBackup(dbName);
        }
        else{
            System.out.println("Usage: java -jar 19722055.jar \"Database Name[XXX.db]\" ");
            System.exit(0);
        }
    }
}
