/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package godpostgresql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


/**
 *
 * @author test
 */
public class DBcon {
    
static String url = "jdbc:postgresql://";
static Connection con=null;
//String user, pass, port;

//String url = "jdbc:postgresql://localhost/test?user=fred&password=secret&ssl=true";

  Connection DBcon() throws SQLException{
    con= DriverManager.getConnection(url+"localhost/postgres");
    return DriverManager.getConnection(url+"localhost/postgres");
}

 static Connection DBcon(String url) throws SQLException{
    con=DriverManager.getConnection("jdbc:postgresql://"+url);
    return DriverManager.getConnection("jdbc:postgresql://"+url);
}

 static boolean checkConDB(String url) throws SQLException{
    if(DBcon(url)!=null) return true;
    return false;
}
 static Connection getConn(){
    return con;
}
}
