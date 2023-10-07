/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package godpostgresql;

import java.net.URL;

/**
 *
 * @author test
 */
public class Nodes {
    
    String Node_type= "";
    String Join_type= "";
    String Command  = "";
    String Parallel  = "";
    Double Total_cost=0.0;
    Double Io_cost=0.0;
    Double Cpu_cost=0.0;
    Double Power_cost=0.0;
    String tabname="";
    String strategie="";
    Double Start_cost=0.0;
    int Plan_wi=0;
    String Actual_st_time="";
    String Actuel_tt_time="";
    int Actual_rows=0;
    Double Actual_loop=0.0;
    int Rows=0;
    int HSblock=0;
    int RSblock=0;
    int WrPlan=-1;
    int WrLau=-1;
    String path;
    
    
    Nodes(){
        defaultimg();
    }
    
    void setPath(String name){
        if(name!=null){
        URL im=getClass().getResource("../vis/img/"+name+".png");
        path=im.toString();}
    }
    
    String getpath(){
        return path;
    }
    
    
    Nodes(String N, Double T, Double I, Double P, int R, int HS, int RS){
            Node_type= N;
            Total_cost=T;
            Io_cost=I;
            Power_cost=P;
            Rows=R;
            HSblock=HS;
            RSblock=RS; 
    }
    
    String getNode(){
        return Node_type;
    }
    void setNode(String node){
        Node_type=node;
    }
    
    Double getTotal_cost(){
        return Total_cost;
    }
    void setTotal_cost(Double total){
        Total_cost=total;
    }
    Double getIo_cost(){
        return Io_cost;
    }
    void setIo_cost(Double io){
        Io_cost=io;
    }
    
    Double getPower_cost(){
        return Power_cost;
    }
    void setPower_cost(Double po){
        Power_cost=po;
    }
    int getRows(){
        return Rows;
    }
    void setRows(int row){
        Rows=row;
    }
    
    int getHSblock(){
        return HSblock;
    }
    void setHSblock(int hs){
        HSblock=hs;
    }
            
    int getRSblock(){
        return RSblock;
    }
    void setRSblock(int rs){
        RSblock=rs;
    }
    void setCpu_cost(Double cost) {
        Cpu_cost = cost;
    }
    
    Double getCpu_cost(){
      return Cpu_cost;  
    }

    void setWrPlan(int in) {
        WrPlan=in;
    }
    int getWrPlan()
    { return WrPlan;}

    void setWrLau(int in) {
        WrLau =in;
    }
    int getWrLau(){
        return WrLau;
    }

    void setRelationname(String tb) {
            tabname=tb;
    }
    String getRelationname(){
        return tabname;
    }

    void setStrategie(String tex) {
        strategie=tex;
    }
    String getStrategie(){
        return strategie;
    }
    void setStart_cost(double par) {
       Start_cost=par;
    }
    Double getStart_cost(){
        return Start_cost;
    }
    
    void setPlan_width(int par) {
     Plan_wi=par;
    }
    int getPlan_width(){
        return Plan_wi;
    }

    void setActual_st(String par) {
    Actual_st_time=par;
    }
    
    String getActual_st(){
        return  Actual_st_time;
    }

    void setActuel_tt(String par) {
       Actuel_tt_time=par;
    }
    
    String getActuel_tt(){
        return Actuel_tt_time;
    }
    void setAct_Rows(int par) {
    Actual_rows=par;
    }
    
    int getAct_Rows(){
        return Actual_rows;
    }

    void setAct_Loop(Double par) {
     Actual_loop=par;
    }
    Double getAct_Loop(){
        return Actual_loop;
    }

    void setJoin_type(String tp){
        Join_type=tp;
    }
    String getJoin_Type() {
        return Join_type;
    }

 void setCommand(String cm) {
    Command=cm;
 }
String getCommand() {
    return Command;
 }

    void setParallelAware(String te) {
     Parallel=te;
    }
    String getParallelAware() {
     return Parallel;
    }

    private void defaultimg() {
        URL im=getClass().getResource("../vis/img/ex_unknown.png");
        path=im.toString();}
}
