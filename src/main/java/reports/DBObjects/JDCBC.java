package reports.DBObjects;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Created by gurramvinay on 8/3/15.
 */
public class JDCBC {

    private static final String MYSQL_HOST ="jdbc:mysql://localhost:3306/sellers";
    private static final String MYSQL_DB = "sellers";
    private static final String USER = "vinay";
    private static final String PASSWD = "vinay";

    private static Connection connection;

    private JDCBC(){};


    public static Connection getConnection(){

        try {
           if(connection==null ||connection.isClosed()){
               Class.forName("com.mysql.jdbc.Driver");
               connection = DriverManager.getConnection(MYSQL_HOST,USER,PASSWD);
           }
        }catch (Exception e){
            e.printStackTrace();
        }
        return connection;
    }
}
