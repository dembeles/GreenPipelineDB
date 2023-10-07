/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package godpostgresql;

import java.sql.*;
import java.util.ArrayList;

/**
 *
 * @author test
 */
public class MetaData {
    
  private  String Dataname=null;
  private  int table_number=0;
  // Firt column :table name, secont: attribute number, ....
  private  ArrayList<ArrayList<String>> Table_info = new ArrayList<ArrayList<String>>();
  private  String Database_size, BLCKSZ, Encoding, Table_name;
  int[]   MLD= { 1, 0, 0 }; 

    MetaData()
    {}
    MetaData(String dbname) throws SQLException
    { Dataname=dbname;
      extrat(dbname);}

   void extrat(String dbname) throws SQLException {
        //connect to database
        Connection con= DBcon.getConn();
        Statement stmt = con.createStatement();
        ResultSet rs;
        String qry=null;
        // 
        qry= "SELECT pg_size_pretty( pg_database_size('"+dbname+"')), current_setting('block_size');";
        rs=stmt.executeQuery(qry);
        while(rs.next()){
            setDataSize(rs.getString(1));
            setBlockS(rs.getString(2));

        }

        qry= "SELECT pg_encoding_to_char(encoding) FROM pg_database WHERE datname = '"+dbname+"';";
        rs=stmt.executeQuery(qry);
        while(rs.next()){
            setEncoding(rs.getString(1));
        }

        
        qry= "SELECT tablename FROM pg_catalog.pg_tables WHERE schemaname != 'pg_catalog' AND schemaname != 'information_schema';";
        rs=stmt.executeQuery(qry);
        int i=0;
        while(rs.next()){
            addtable_info(rs.getString(1));
            i++;
        }
        settableN(i);
       Table_name="";
       qry="SELECT ";
        for( i=0;i<Table_info.size();i++){
       if(i<Table_info.size()-1){
           Table_name=Table_name+Table_info.get(i).get(0)+",";
       qry= qry+" pg_size_pretty( pg_total_relation_size('"+Table_info.get(i).get(0)+"')),";
        } else {
       qry= qry+" pg_size_pretty( pg_total_relation_size('"+Table_info.get(i).get(0)+"'));"; 
       Table_name=Table_name+Table_info.get(i).get(0);
        }}
        rs=stmt.executeQuery(qry);
        while(rs.next()){
            for(i=1;i<=rs.getMetaData().getColumnCount();i++){
               Table_info.get(i-1).add(1,rs.getString(i));

            }
        }
        
        qry="select relname, relnatts , reltuples,relpages  from pg_class where relname in"
                + "(SELECT tablename FROM pg_catalog.pg_tables WHERE "
                + "schemaname != 'pg_catalog' AND schemaname != 'information_schema');";
        rs=stmt.executeQuery(qry);
        int j;
        while(rs.next()){
           
                for(j=0;j<Table_info.size();j++){
                    if(Table_info.get(j).get(0).compareTo(rs.getString(1))==0){
                        break;
                    }
                }
               Table_info.get(j).add(2,rs.getString(2));
               Table_info.get(j).add(3,rs.getString(3));
               Table_info.get(j).add(4,rs.getString(4));
     
        }
        
        
        rs.close();
        stmt.close();
        
           
    }

private void setDataSize(String size) {
       Database_size=size;
    }

 String getDataSize() {
    return Database_size;
    }

private void setBlockS(String size) {
    BLCKSZ=size;
}

 String getBlockS() {
    return BLCKSZ;
}

private void setEncoding(String encode) {
        Encoding=encode;
    }
 String getEncoding() {
       return Encoding;
    }

private void addtable_info(String string) {
    ArrayList<String> a1 = new ArrayList<String>();
    a1.add(string);
    Table_info.add(a1);
}
 ArrayList<ArrayList<String>> gettable_info() {
    return Table_info;
}

    private void settableN(int i) {
        table_number=i;
    }
 int gettableN() {
     return   table_number;
    }

    void setMLD(char c, int i) {
    if(c=='R'){
        MLD[0]=i;
    } else if (c=='N') {
        MLD[1]=i;
    } else {
        MLD[2]=i;
    }
    }
    int[] getMLD(){
        return MLD;
    }

    
}
