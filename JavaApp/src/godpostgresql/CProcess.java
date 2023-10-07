/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package godpostgresql;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import org.rosuda.JRI.REXP;
import org.rosuda.JRI.Rengine;

/**
 *
 * @author test
 */
public class CProcess {
    
    private static ArrayList<Integer> NodesParend;
    private static final ArrayList<ArrayList<Nodes>> nodesplan = new ArrayList<ArrayList<Nodes>>();
    private static ArrayList<String> AllNodes=new ArrayList<>();
    private static ArrayList<String>  Alledges  =new ArrayList<>();
    private static  String nodes,edges ;
    private static ArrayList<QueryStatistic> Qst=new ArrayList<>();
    private static ArrayList<ArrayList<Double>> measurep=new ArrayList<ArrayList<Double>>();
    static Rengine re=null;
    static REXP resultR=null;
    public static double Cqpower=0.0;
    public static boolean notlauch=true;
    
    static void InitRlearning() {

    if (!Rengine.versionCheck()) {
    System.err.println("Java version mismatch.");
    System.exit(1);
    }
    String my[] = {"--vanilla"};
    //re=new Rengine(my,false,new TextConsole());
    re=new Rengine(my,false,null);
    if (!re.waitForR()) {
    System.out.println("Cannot load R");
    System.exit(1);
    }
    //REXP result = re.eval("7+10");
    //System.out.println("rexp: " + (result.asDouble()+3.0));
    
    //   initialisation
    re.eval("library(neuralnet)");
    re.eval("library(caret)");
    re.eval("library(randomForest)");
    re.eval("library(ISLR)");
    re.eval("library(e1071)");
    re.eval("library(tree)");
    re.eval("trControl <- trainControl(method = \"cv\", number = 10, search = \"grid\")");
    //
    
    }
    
    static boolean setTradeoff(int a, int b) throws SQLException{
        Connection con= DBcon.getConn();
        Statement stmt = con.createStatement();
        stmt.execute("SET power_weight to "+a+";");
        stmt.execute("SET time_weight to "+b+";");
        if(stmt.getWarnings()==null) return true;
        return false;
    }

    static void setOptimizer(boolean b) throws SQLException {
        Connection con= DBcon.getConn();
        Statement stmt = con.createStatement();
        if(b==true) stmt.execute("set max_parallel_workers_per_gather = 2;");
        if(b==false) stmt.execute("set max_parallel_workers_per_gather = 0;");
       
    }

    static void setOptimizer1(String function, String cmd) throws SQLException {
        Connection con= DBcon.getConn();
        Statement stmt = con.createStatement();
        stmt.execute("SET "+function+" TO "+cmd+";");       
    }

static void setDOP(int dop) throws SQLException {
        Connection con= DBcon.getConn();
        Statement stmt = con.createStatement();
        stmt.execute("set max_parallel_workers_per_gather = "+dop+";");  
    }

static ArrayList<ResultSet> ProcessQuery(String text, int i) throws SQLException, IOException, InterruptedException {
        //To change body of generated methods, choose Tools | Templates.
        Connection con= DBcon.getConn();
        Statement stmt = con.createStatement();
        ArrayList<ResultSet> rs=new ArrayList<ResultSet>();
        ResultSet rq=null;
        Thread t=new Thread();
        ThreadFactory dft= Executors.defaultThreadFactory();
        
        //Apply opti;izer configuration
        if(NFrame.Pscan==1){
            stmt.execute("set enable_seqscan to on;");
        }else {
            stmt.execute("set enable_seqscan to off;");
        }
        
        if(NFrame.PIscan==1){
            stmt.execute("set enable_indexscan to on;");
        }else {
            stmt.execute("set enable_indexscan to off;");
        }
        
        if(NFrame.PHjoin==1){
        //    stmt.execute("set enable_parallel_hash to on;");
        }else {
          //  stmt.execute("set enable_parallel_hash to off;");
        }
        
        if(NFrame.Mjoin==1){
            stmt.execute("set enable_mergejoin to on;");
        }else {
            stmt.execute("set enable_mergejoin to off;");
        }
        
        if(NFrame.Hjoin==1){
            stmt.execute("set enable_hashjoin to on;");
        }else {
            stmt.execute("set enable_hashjoin to off;");
        }
        
        if(NFrame.Esort==1){
            stmt.execute("set enable_sort to on;");
        }else {
            stmt.execute("set enable_sort to off;");
        }
        
        if(NFrame.Enlj==1){
            stmt.execute("set enable_mergejoin to on;");
        }else {
            stmt.execute("set enable_mergejoin to off;");
        }
        
        if(NFrame.PBhead==1){
            stmt.execute("set enable_bitmapscan to on;");
        }else {
            stmt.execute("set enable_bitmapscan to off;");
        }
        
        
        if(i==0){
        if(NFrame.app){
             //System.out.println("test");
            if(notlauch){
                  t = dft.newThread(new WattsUpWorker()); 
                  WattsUpWorker.i=0;
                  WattsUpWorker.b=true; 
                  t.start();
                  notlauch=false;
                  Thread.sleep(2000);
               }
            WattsUpWorker.brun=true; 
            WattsUpWorker.tp=0; 
            Cqpower=0;
            
            rs.add(stmt.executeQuery(text));

            WattsUpWorker.brun=false; 
            ArrayList<Double> aa=new ArrayList<>();
                System.out.println("Cqpower."+Cqpower+"   Watt:"+WattsUpWorker.tp);
            aa.add(Cqpower/(1+WattsUpWorker.tp));
            aa.add(Cqpower);
            measurep.add(aa);
               //Save value in query stat
            }else {
            rs.add(stmt.executeQuery(text));
               }
        //Save value in query stat
        }
        else
        {  // Multiple Query Process
            File f=new File(text);
            BufferedReader  br = new BufferedReader(new FileReader(f));
            String st,queries="";
            String [] Qlist;
            while((st=br.readLine())!=null){
                queries=queries+" "+st;}
            Qlist=queries.split(";");
            
            if(FenP.app){
                  if(notlauch){
                  t = dft.newThread(new WattsUpWorker()); 
                  WattsUpWorker.i=0;
                  WattsUpWorker.b=true; 
                  t.start();
                  notlauch=false;
                  Thread.sleep(2000);
                  }
             for (String Qlist1 : Qlist) {
                 WattsUpWorker.brun=true; 
                 WattsUpWorker.tp=0; 
                 Cqpower=0;
        //Papi  init
        //Papi.init();
        // EventSet evset=
        
        //Mesasure Prower from tool
        //t=dft.newThread(t);  begin capture
        //  t.start();
        //  
        // evset.start() pai start
                rs.add(stmt.executeQuery(Qlist1));
                WattsUpWorker.brun=false; 
                ArrayList<Double> aa=new ArrayList<>();
                aa.add(Cqpower/(1+WattsUpWorker.tp));
                aa.add(Cqpower);
                measurep.add(aa);
                
        //evset.stop() papi stop
        //  t.stop();
        //Save value
            }} else
            {
                for (String Qlist1 : Qlist) {
                rs.add(stmt.executeQuery(Qlist1));
            }}
            
        }
        
        return rs;
    }
static void ExplainQuery(String text, int i) throws SQLException, FileNotFoundException, IOException, SAXException, ParserConfigurationException, XMLStreamException {
        Connection con= DBcon.getConn();
        Statement stmt = con.createStatement();
        ArrayList<ResultSet> rs=new ArrayList<ResultSet>();
        //ResultSet rq=null;
        
                //Apply opti;izer configuration
        if(NFrame.Pscan==1){
            stmt.execute("set enable_seqscan to on;");
        }else {
            stmt.execute("set enable_seqscan to off;");
        }
        
        if(NFrame.PIscan==1){
            stmt.execute("set enable_indexscan to on;");
        }else {
            stmt.execute("set enable_indexscan to off;");
        }
        
        if(NFrame.PHjoin==1){
            //stmt.execute("set enable_parallel_hash to on;");
        }else {
           // stmt.execute("set enable_parallel_hash to off;");
        }
        
        if(NFrame.Mjoin==1){
            stmt.execute("set enable_mergejoin to on;");
        }else {
            stmt.execute("set enable_mergejoin to off;");
        }
        
        if(NFrame.Hjoin==1){
            stmt.execute("set enable_hashjoin to on;");
        }else {
            stmt.execute("set enable_hashjoin to off;");
        }
        
        if(NFrame.Esort==1){
            stmt.execute("set enable_sort to on;");
        }else {
            stmt.execute("set enable_sort to off;");
        }
        
        if(NFrame.Enlj==1){
            stmt.execute("set enable_mergejoin to on;");
        }else {
            stmt.execute("set enable_mergejoin to off;");
        }
        
        if(NFrame.PBhead==1){
            stmt.execute("set enable_bitmapscan to on;");
        }else {
            stmt.execute("set enable_bitmapscan to off;");
        }
          
      
        if(i==0){
            rs.add(stmt.executeQuery("EXPLAIN (FORMAT XML) "+text));
        } else {
            File f=new File(text);
            BufferedReader  br = new BufferedReader(new FileReader(f));
            String st,queries="";
            String [] Qlist;
            while((st=br.readLine())!=null){
                queries=queries+" "+st;}
            Qlist=queries.split(";");
            for (String Qlist1 : Qlist) {
                rs.add(stmt.executeQuery("EXPLAIN (FORMAT XML)"+Qlist1));
             }   
        }
        
     //  ExtractNodes(rs);
       ParseXMLFormat(rs);  
    }
private static void ParseXMLFormat(ArrayList<ResultSet> rs) throws SQLException, ParserConfigurationException, SAXException, IOException, XMLStreamException {
   // QStringList queriesPath;
   // QStringList queriesStr;
   // QMutex mutex;

   //  QList<bool> isLeafList;
   //  QList<int> parentIdList;
   //  QStringList opNameList;
    int id=0; 
    for(int i=0; i<rs.size();i++){
      String qry="";
      while(rs.get(i).next()){ qry= qry+rs.get(i).getString(1); }
    String xmlstr=qry.replaceAll("\t", "");
      //LinkNoeud(xmlstr);
    ExtractNodes1(xmlstr);
    LinkNoeud1(xmlstr);
    
    nodes = "var nodes = new vis.DataSet([";
    edges = "var edges = new vis.DataSet([";
    Reader reader1 = new StringReader(xmlstr);
    XMLInputFactory factory = XMLInputFactory.newInstance();
    XMLStreamReader reader = factory.createXMLStreamReader(reader1);
    
    while(reader.hasNext()){
        int e=reader.next();
        if(e==XMLEvent.START_ELEMENT){
            if (reader.getLocalName().equals("Plan"))
            {   NodesEdges(NodesParend.get(id), id,i);
                id++;
             }
       }
       // else if (e==XMLEvent.END_ELEMENT)
        }
    
    StringBuilder sb = new StringBuilder(nodes); 
    sb.deleteCharAt(sb.length()-1);
    nodes=sb.toString();
    
    sb = new StringBuilder(edges); 
    sb.deleteCharAt(sb.length()-1);
    edges=sb.toString();
    nodes += "]);";
    edges += "]);";

    id=0; AllNodes.add(nodes); Alledges.add(edges);}}
static void NodesEdges(int parent, int child, int i)
{
    String title = getNodeLabel(child,i);
    //String image =", image:'../vis/img/"+getNodeImg(child,i)+".png"+"', brokenImage:'../vis/img/ex_unknown.png', shape: 'image'";
   
   nodesplan.get(i).get(child).setPath(getNodeImg(child,i));
   String src=nodesplan.get(i).get(child).getpath();
   // String image =", image:'"+getClass().getResource("../vis/img/"+getNodeImg(child,i)+".png")+"', brokenImage:'../vis/img/ex_unknown.png', shape: 'image'";
   String image =", image:'"+src+"', brokenImage:'"+src+"', shape: 'image'";
  // System.out.println("Title :"+System.getProperty("user.dir"));
   // System.out.println("Title :"+child+" :"+image);
   // System.out.println("image :"+image);
    
  nodes+="{id: "+child+", label: '"+nodesplan.get(i).get(child).getNode()+"'"+title+image+"},";
    if(parent != -1){
  edges+="{from: "+parent+", to: "+child+"},";}

    //isLeafList.append(opName.contains("Scan"));
    //opNameList.append(opName); */
}
private static void ExtractNodes(ArrayList<ResultSet> rs) throws SQLException, SAXException, IOException, ParserConfigurationException {
     
     for(int i=0; i<rs.size();i++){
         ArrayList<Nodes> node= new ArrayList<Nodes>();
         String qry="";
         while(rs.get(i).next()){
             qry= qry+rs.get(i).getString(1);
             
         }
         String xmlstr=qry;
         //System.out.println(xmlstr);
         //String xmlstr=qry.replaceAll("\n|\r", ""); 
         DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
         DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
         Document doc = dBuilder.parse(new InputSource(new StringReader(xmlstr)));
         doc.getDocumentElement().normalize();
         NodeList nList = doc.getElementsByTagName("Plan");
         
         for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);
            Nodes nd=new Nodes();
          if (nNode.getNodeType() == Node.ELEMENT_NODE) {
             Element eElement = (Element) nNode;
              nd.setNode(eElement.getElementsByTagName("Node-Type").item(0).getTextContent());
             if(eElement.getElementsByTagName("Total-Cost").getLength()>0){
             nd.setTotal_cost(Double.parseDouble(eElement.getElementsByTagName("Total-Cost").item(0).getTextContent()));
             }if(eElement.getElementsByTagName("IO-Cost").getLength()>0){
             nd.setIo_cost(Double.parseDouble(eElement.getElementsByTagName("IO-Cost").item(0).getTextContent()));
             }if(eElement.getElementsByTagName("CPU-Cost").getLength()>0){
             nd.setCpu_cost(Double.parseDouble(eElement.getElementsByTagName("CPU-Cost").item(0).getTextContent()));
             }if(eElement.getElementsByTagName("Power-Cost").getLength()>0){
             nd.setPower_cost(Double.parseDouble(eElement.getElementsByTagName("Power-Cost").item(0).getTextContent()));
             }if(eElement.getElementsByTagName("Plan-Rows").getLength()>0){
             nd.setRows(Integer.parseInt(eElement.getElementsByTagName("Plan-Rows").item(0).getTextContent()));
             }if(eElement.getElementsByTagName("Relation-Name").getLength()>0){
             nd.setRelationname(eElement.getElementsByTagName("Relation-Name").item(0).getTextContent());
             }if(eElement.getElementsByTagName("Strategy").getLength()>0){
             nd.setStrategie(eElement.getElementsByTagName("Strategy").item(0).getTextContent());
             }if(eElement.getElementsByTagName("Startup-Cost").getLength()>0){
             nd.setStart_cost(Double.parseDouble(eElement.getElementsByTagName("Startup-Cost").item(0).getTextContent()));
             }if(eElement.getElementsByTagName("Plan-Width").getLength()>0){
             nd.setPlan_width(Integer.parseInt(eElement.getElementsByTagName("Plan-Width").item(0).getTextContent()));
             }if(eElement.getElementsByTagName("Actual-Startup-Time").getLength()>0){
              nd.setActual_st(eElement.getElementsByTagName("Actual-Startup-Time").item(0).getTextContent());
             }if(eElement.getElementsByTagName("Actual-Total-Time").getLength()>0){
             nd.setActuel_tt(eElement.getElementsByTagName("Actual-Total-Time").item(0).getTextContent());
             }if(eElement.getElementsByTagName("Actual-Rows").getLength()>0){
              nd.setAct_Rows(Integer.parseInt(eElement.getElementsByTagName("Actual-Rows").item(0).getTextContent()));
              }if(eElement.getElementsByTagName("Actual-Loops").getLength()>0){
              nd.setAct_Loop(Double.parseDouble(eElement.getElementsByTagName("Actual-Loops").item(0).getTextContent()));
              }if(eElement.getElementsByTagName("Join-Type").getLength()>0){
              nd.setJoin_type(eElement.getElementsByTagName("Join-Type").item(0).getTextContent());
              }if(eElement.getElementsByTagName("Command").getLength()>0){
             nd.setCommand(eElement.getElementsByTagName("Command").item(0).getTextContent());
              }if(eElement.getElementsByTagName("Parallel-Aware").getLength()>0){
              }nd.setParallelAware(eElement.getElementsByTagName("Parallel-Aware").item(0).getTextContent());     
             if(eElement.getElementsByTagName("Shared-Hit-Blocks").getLength()>0){
             nd.setHSblock(Integer.parseInt(eElement.getElementsByTagName("Shared-Hit-Blocks").item(0).getTextContent()));
             }if(eElement.getElementsByTagName("Shared-Read-Blocks").getLength()>0){
             nd.setRSblock(Integer.parseInt(eElement.getElementsByTagName("Shared-Read-Blocks").item(0).getTextContent()));
             }if(eElement.getElementsByTagName("Workers-Planned").getLength()>0){
             nd.setWrPlan(Integer.parseInt(eElement.getElementsByTagName("Workers-Planned").item(0).getTextContent()));
             }if(eElement.getElementsByTagName("Workers-Launched").getLength()>0)
             {nd.setWrLau(Integer.parseInt(eElement.getElementsByTagName("Workers-Launched").item(0).getTextContent()));
             }   }
             node.add(nd);
         }   nodesplan.add(node); 

     }
     
 }

private static void ExtractNodes1(String qry) throws SQLException, SAXException, IOException, ParserConfigurationException, XMLStreamException {
        //System.out.println(qry);
    Double EstimatedTotalCost=0.0;
    Double EstimatedPower=0.0;
    Double EstimatedEnergy=0.0;
    Double EstimatedIO=0.0;
    Double EstimatedCPU=0.0;
    //Double Realpower;
    //Double Realenergy;
    Double Time;
        ArrayList<Nodes> node= new ArrayList<Nodes>();
         String xmlstr=qry;
         DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
         DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
         Document doc = dBuilder.parse(new InputSource(new StringReader(xmlstr)));
         doc.getDocumentElement().normalize();
         NodeList nList = doc.getElementsByTagName("Plan");
         for (int temp = nList.getLength()-1; temp >=0; temp--) {
            Node nNode = nList.item(temp);
            Nodes nd=new Nodes();
            Element eElement = (Element) nNode;
            if(eElement.getElementsByTagName("Node-Type").getLength()>0){
              nd.setNode(eElement.getElementsByTagName("Node-Type").item(0).getTextContent());
              nNode.removeChild(eElement.getElementsByTagName("Node-Type").item(0));
            }if(eElement.getElementsByTagName("Total-Cost").getLength()>0){
                if(temp==0) EstimatedTotalCost=Double.parseDouble(eElement.getElementsByTagName("Total-Cost").item(0).getTextContent());
             nd.setTotal_cost(Double.parseDouble(eElement.getElementsByTagName("Total-Cost").item(0).getTextContent()));
             nNode.removeChild((Node) eElement.getElementsByTagName("Total-Cost").item(0));
            }if(eElement.getElementsByTagName("IO-Cost").getLength()>0){
             nd.setIo_cost(Double.parseDouble(eElement.getElementsByTagName("IO-Cost").item(0).getTextContent()));
             EstimatedIO+=Double.parseDouble(eElement.getElementsByTagName("IO-Cost").item(0).getTextContent());
             nNode.removeChild((Node) eElement.getElementsByTagName("IO-Cost").item(0));
            }if(eElement.getElementsByTagName("CPU-Cost").getLength()>0){
             nd.setCpu_cost(Double.parseDouble(eElement.getElementsByTagName("CPU-Cost").item(0).getTextContent()));
             EstimatedCPU+=Double.parseDouble(eElement.getElementsByTagName("CPU-Cost").item(0).getTextContent());
             nNode.removeChild((Node) eElement.getElementsByTagName("CPU-Cost").item(0));
            }if(eElement.getElementsByTagName("Power-Cost").getLength()>0){
             nd.setPower_cost(Double.parseDouble(eElement.getElementsByTagName("Power-Cost").item(0).getTextContent()));
             EstimatedPower+=Double.parseDouble(eElement.getElementsByTagName("Power-Cost").item(0).getTextContent());
             nNode.removeChild((Node) eElement.getElementsByTagName("Power-Cost").item(0));
            }if(eElement.getElementsByTagName("Plan-Rows").getLength()>0){
             nd.setRows(Integer.parseInt(eElement.getElementsByTagName("Plan-Rows").item(0).getTextContent()));
             nNode.removeChild((Node) eElement.getElementsByTagName("Plan-Rows").item(0));
            }if(eElement.getElementsByTagName("Relation-Name").getLength()>0){
             nd.setRelationname(eElement.getElementsByTagName("Relation-Name").item(0).getTextContent());
             nNode.removeChild((Node) eElement.getElementsByTagName("Relation-Name").item(0));
            }if(eElement.getElementsByTagName("Strategy").getLength()>0){
             nd.setStrategie(eElement.getElementsByTagName("Strategy").item(0).getTextContent());
             nNode.removeChild((Node) eElement.getElementsByTagName("Strategy").item(0));
            }if(eElement.getElementsByTagName("Startup-Cost").getLength()>0){
             nd.setStart_cost(Double.parseDouble(eElement.getElementsByTagName("Startup-Cost").item(0).getTextContent()));
             nNode.removeChild((Node) eElement.getElementsByTagName("Startup-Cost").item(0));
            }if(eElement.getElementsByTagName("Plan-Width").getLength()>0){
             nd.setPlan_width(Integer.parseInt(eElement.getElementsByTagName("Plan-Width").item(0).getTextContent()));
             nNode.removeChild((Node) eElement.getElementsByTagName("Plan-Width").item(0));
            }if(eElement.getElementsByTagName("Actual-Startup-Time").getLength()>0){
              nd.setActual_st(eElement.getElementsByTagName("Actual-Startup-Time").item(0).getTextContent());
              nNode.removeChild((Node) eElement.getElementsByTagName("Actual-Startup-Time").item(0));
            }if(eElement.getElementsByTagName("Actual-Total-Time").getLength()>0){
             nd.setActuel_tt(eElement.getElementsByTagName("Actual-Total-Time").item(0).getTextContent());
             nNode.removeChild((Node) eElement.getElementsByTagName("Actual-Total-Time").item(0));
            }if(eElement.getElementsByTagName("Actual-Rows").getLength()>0){
              nd.setAct_Rows(Integer.parseInt(eElement.getElementsByTagName("Actual-Rows").item(0).getTextContent()));
              nNode.removeChild((Node) eElement.getElementsByTagName("Actual-Rows").item(0));
            }if(eElement.getElementsByTagName("Actual-Loops").getLength()>0){
              nd.setAct_Loop(Double.parseDouble(eElement.getElementsByTagName("Actual-Loops").item(0).getTextContent()));
              nNode.removeChild((Node) eElement.getElementsByTagName("Actual-Loops").item(0));
            }if(eElement.getElementsByTagName("Join-Type").getLength()>0){
              nd.setJoin_type(eElement.getElementsByTagName("Join-Type").item(0).getTextContent());
              nNode.removeChild((Node) eElement.getElementsByTagName("Join-Type").item(0));
            }if(eElement.getElementsByTagName("Command").getLength()>0){
             nd.setCommand(eElement.getElementsByTagName("Command").item(0).getTextContent());
              nNode.removeChild((Node) eElement.getElementsByTagName("Command").item(0));
            }if(eElement.getElementsByTagName("Parallel-Aware").getLength()>0){
              nd.setParallelAware(eElement.getElementsByTagName("Parallel-Aware").item(0).getTextContent());     
             nNode.removeChild((Node) eElement.getElementsByTagName("Parallel-Aware").item(0));
            } if(eElement.getElementsByTagName("Shared-Hit-Blocks").getLength()>0){
             nd.setHSblock(Integer.parseInt(eElement.getElementsByTagName("Shared-Hit-Blocks").item(0).getTextContent()));
             nNode.removeChild((Node) eElement.getElementsByTagName("Shared-Hit-Blocks").item(0));
            }if(eElement.getElementsByTagName("Shared-Read-Blocks").getLength()>0){
             nd.setRSblock(Integer.parseInt(eElement.getElementsByTagName("Shared-Read-Blocks").item(0).getTextContent()));
            nNode.removeChild((Node) eElement.getElementsByTagName("Shared-Read-Blocks").item(0));
            }if(eElement.getElementsByTagName("Workers-Planned").getLength()>0){
             nd.setWrPlan(Integer.parseInt(eElement.getElementsByTagName("Workers-Planned").item(0).getTextContent()));
             nNode.removeChild((Node) eElement.getElementsByTagName("Workers-Planned").item(0));
            }if(eElement.getElementsByTagName("Workers-Launched").getLength()>0)
             {nd.setWrLau(Integer.parseInt(eElement.getElementsByTagName("Workers-Launched").item(0).getTextContent()));
             nNode.removeChild((Node) eElement.getElementsByTagName("Workers-Launched").item(0));
             }   node.add(nd); }

       
        /*        Reader reader1 = new StringReader(qry);
                XMLInputFactory factory = XMLInputFactory.newInstance();
                XMLStreamReader reader = factory.createXMLStreamReader(reader1);
                Nodes nd=null;
               while(reader.hasNext()){
                     int e=reader.next();
                        if(e==XMLEvent.START_ELEMENT){
                           if (reader.getLocalName().equals("Plan"))
                               {    if(nd==null){  nd=new Nodes();
                                       } else { node.add(nd);nd=new Nodes();}
                               } else {
                               if(e==XMLEvent.CHARACTERS){
             if(reader.getName().equals("Node-Type")){
             nd.setNode(reader.getElementText());}
             System.out.println(reader.getElementText());
             if(reader.getName().equals("Total-Cost")){
             nd.setTotal_cost(Double.parseDouble(reader.getElementText()));
             }if(reader.getName().equals("IO-Cost")){
             nd.setIo_cost(Double.parseDouble(reader.getElementText()));
             }if(reader.getName().equals("CPU-Cost")){
             nd.setCpu_cost(Double.parseDouble(reader.getElementText()));
             }if(reader.getName().equals("Power-Cost")){
             nd.setPower_cost(Double.parseDouble(reader.getElementText()));
             }if(reader.getName().equals("Plan-Rows")){
             nd.setRows(Integer.parseInt(reader.getElementText()));
             }if(reader.getName().equals("Relation-Name")){
             nd.setRelationname(reader.getElementText());
             }if(reader.getName().equals("Strategy")){
             nd.setStrategie(reader.getElementText());
             }if(reader.getName().equals("Startup-Cost")){
             nd.setStart_cost(Double.parseDouble(reader.getElementText()));
             }if(reader.getName().equals("Plan-Width")){
             nd.setPlan_width(Integer.parseInt(reader.getElementText()));
             }if(reader.getName().equals("Actual-Startup-Time")){
              nd.setActual_st(reader.getElementText());
             }if(reader.getName().equals("Actual-Total-Time")){
             nd.setActuel_tt(reader.getElementText());
             }if(reader.getName().equals("Actual-Rows")){
              nd.setAct_Rows(Integer.parseInt(reader.getElementText()));
              }if(reader.getName().equals("Actual-Loops")){
              nd.setAct_Loop(Double.parseDouble(reader.getElementText()));
              }if(reader.getName().equals("Join-Type")){
              nd.setJoin_type(reader.getElementText());
              }if(reader.getName().equals("Command")){
             nd.setCommand(reader.getElementText());
              }if(reader.getName().equals("Parallel-Aware")){
              nd.setParallelAware(reader.getElementText()); }    
             if(reader.getName().equals("Shared-Hit-Blocks")){
             nd.setHSblock(Integer.parseInt(reader.getElementText()));
             }if(reader.getName().equals("Shared-Read-Blocks")){
             nd.setRSblock(Integer.parseInt(reader.getElementText()));
             }if(reader.getName().equals("Workers-Planned")){
             nd.setWrPlan(Integer.parseInt(reader.getElementText()));
             }if(reader.getName().equals("Workers-Launched"))
             {nd.setWrLau(Integer.parseInt(reader.getElementText()));
             } }}
               } else if(e==XMLEvent.END_ELEMENT){
                      if (reader.getLocalName().equals("Plan"))
                          { if(nd!=null){node.add(nd);}}}
                } */
      /*   for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);
            Nodes nd=new Nodes();
          if (nNode.getNodeType() == Node.ELEMENT_NODE) {
             Element eElement = (Element) nNode;
              nd.setNode(eElement.getElementsByTagName("Node-Type").item(0).getTextContent());
             if(eElement.getElementsByTagName("Total-Cost").getLength()>0){
             nd.setTotal_cost(Double.parseDouble(eElement.getElementsByTagName("Total-Cost").item(0).getTextContent()));
             }if(eElement.getElementsByTagName("IO-Cost").getLength()>0){
             nd.setIo_cost(Double.parseDouble(eElement.getElementsByTagName("IO-Cost").item(0).getTextContent()));
             }if(eElement.getElementsByTagName("CPU-Cost").getLength()>0){
             nd.setCpu_cost(Double.parseDouble(eElement.getElementsByTagName("CPU-Cost").item(0).getTextContent()));
             }if(eElement.getElementsByTagName("Power-Cost").getLength()>0){
             nd.setPower_cost(Double.parseDouble(eElement.getElementsByTagName("Power-Cost").item(0).getTextContent()));
             }if(eElement.getElementsByTagName("Plan-Rows").getLength()>0){
             nd.setRows(Integer.parseInt(eElement.getElementsByTagName("Plan-Rows").item(0).getTextContent()));
             }if(eElement.getElementsByTagName("Relation-Name").getLength()>0){
             nd.setRelationname(eElement.getElementsByTagName("Relation-Name").item(0).getTextContent());
             }if(eElement.getElementsByTagName("Strategy").getLength()>0){
             nd.setStrategie(eElement.getElementsByTagName("Strategy").item(0).getTextContent());
             }if(eElement.getElementsByTagName("Startup-Cost").getLength()>0){
             nd.setStart_cost(Double.parseDouble(eElement.getElementsByTagName("Startup-Cost").item(0).getTextContent()));
             }if(eElement.getElementsByTagName("Plan-Width").getLength()>0){
             nd.setPlan_width(Integer.parseInt(eElement.getElementsByTagName("Plan-Width").item(0).getTextContent()));
             }if(eElement.getElementsByTagName("Actual-Startup-Time").getLength()>0){
              nd.setActual_st(eElement.getElementsByTagName("Actual-Startup-Time").item(0).getTextContent());
             }if(eElement.getElementsByTagName("Actual-Total-Time").getLength()>0){
             nd.setActuel_tt(eElement.getElementsByTagName("Actual-Total-Time").item(0).getTextContent());
             }if(eElement.getElementsByTagName("Actual-Rows").getLength()>0){
              nd.setAct_Rows(Integer.parseInt(eElement.getElementsByTagName("Actual-Rows").item(0).getTextContent()));
              }if(eElement.getElementsByTagName("Actual-Loops").getLength()>0){
              nd.setAct_Loop(Double.parseDouble(eElement.getElementsByTagName("Actual-Loops").item(0).getTextContent()));
              }if(eElement.getElementsByTagName("Join-Type").getLength()>0){
              nd.setJoin_type(eElement.getElementsByTagName("Join-Type").item(0).getTextContent());
              }if(eElement.getElementsByTagName("Command").getLength()>0){
             nd.setCommand(eElement.getElementsByTagName("Command").item(0).getTextContent());
              }if(eElement.getElementsByTagName("Parallel-Aware").getLength()>0){
              }nd.setParallelAware(eElement.getElementsByTagName("Parallel-Aware").item(0).getTextContent());     
             if(eElement.getElementsByTagName("Shared-Hit-Blocks").getLength()>0){
             nd.setHSblock(Integer.parseInt(eElement.getElementsByTagName("Shared-Hit-Blocks").item(0).getTextContent()));
             }if(eElement.getElementsByTagName("Shared-Read-Blocks").getLength()>0){
             nd.setRSblock(Integer.parseInt(eElement.getElementsByTagName("Shared-Read-Blocks").item(0).getTextContent()));
             }if(eElement.getElementsByTagName("Workers-Planned").getLength()>0){
             nd.setWrPlan(Integer.parseInt(eElement.getElementsByTagName("Workers-Planned").item(0).getTextContent()));
             }if(eElement.getElementsByTagName("Workers-Launched").getLength()>0)
             {nd.setWrLau(Integer.parseInt(eElement.getElementsByTagName("Workers-Launched").item(0).getTextContent()));
             }   }
             node.add(nd);
         }*/
         Collections.reverse(node);
         nodesplan.add(node); 
         QueryStatistic it=new QueryStatistic();
         it.setNbreop(node.size());
         it.setEstimatedCPU(EstimatedCPU);
         it.setEstimatedPower(EstimatedPower);
         it.setEstimatedIO(EstimatedIO);
         it.setEstimatedTotalCost(EstimatedTotalCost);
         it.setEstimatedEnergy(EstimatedEnergy);
         // it.setTime(Time);
         Qst.add(it);it=null;
         //System.out.println("time:"+Time+"tt:"+EstimatedTotalCost);
     }
private static void LinkNoeud(String xmlstr) {
    String planline[];
    planline=xmlstr.split("\n");
    ArrayList<Integer> parentIdlist=new ArrayList<>();
    ArrayList<Integer> idIdent=new ArrayList<>();
    idIdent.add(-1);
    int ident;

    for(int i=0,k=0;i<planline.length;i++){
        if(planline[i].contains("<Plan>")){
            ident = planline[i].indexOf("<Plan>");
            idIdent.add(ident);
            k=idIdent.size()-2;
            while(k<idIdent.size() && idIdent.get(k)>=ident){
                k--;
                if(k<0){
                    break;
                }
            } parentIdlist.add(k-1);
        }
    }
 /*   for(int j=0;j<idIdent.size();j++){
       System.out.print(" "+idIdent.get(j));
    }
    System.out.println();
    for(int j=0;j<parentIdlist.size();j++){
       System.out.print(" "+parentIdlist.get(j));
    }*/
    }
private static void LinkNoeud1(String xmlstr) throws ParserConfigurationException, SAXException, IOException {
            
         DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
         DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
         Document doc = dBuilder.parse(new InputSource(new StringReader(xmlstr)));
         doc.getDocumentElement().normalize();
         NodeList nList = doc.getElementsByTagName("Plan");
         NodesParend =new  ArrayList(nList.getLength());

         int j=0;
         for(int i=0;i<nList.getLength();i++){
             if(!nList.item(i).getParentNode().hasAttributes()){
                 Element elt=(Element) nList.item(i).getParentNode();
                 elt.setAttribute("ParentID",String.valueOf(i-1));
             }
        }

        for(int i=0;i<nList.getLength();i++){
            NodesParend.add(Integer.parseInt(nList.item(i).getParentNode().getAttributes().getNamedItem("ParentID").getNodeValue()));
        }
     /*   for(int i=0;i<nList.getLength();i++){            
        System.out.println(": Parent:" +NodesParend.get(i));
        }*/
    }
private static String getNodeLabel(int child,int i)
       {
    String title = ", title: '";
    if(nodesplan.get(i).get(child).getRelationname().length()>0){
    title += "<b>Table:</b> "+nodesplan.get(i).get(child).getRelationname()+"<br>";}

    title += "<b>Parallel-Aware:</b> "+nodesplan.get(i).get(child).getParallelAware()+"<br>"+
             "<b>Startup-Cost:</b> "+nodesplan.get(i).get(child).getStart_cost()+"<br>"+
             "<b>Total-Cost:</b> "+nodesplan.get(i).get(child).getTotal_cost()+"<br>"+
             "<b>Plan-Rows:</b> "+nodesplan.get(i).get(child).getRows()+"<br>"+
             "<b>IO-Cost:</b> "+nodesplan.get(i).get(child).getIo_cost()+"<br>"+
             "<b>CPU-Cost:</b> "+nodesplan.get(i).get(child).getCpu_cost()+"<br>"+
             "<b>Power-Cost:</b> "+(nodesplan.get(i).get(child).getPower_cost()-FenP.B0)+"<br>";
    if(nodesplan.get(i).get(child).getWrPlan()>-1){
        title += "<b>Workers-Planned:</b> "+nodesplan.get(i).get(child).getWrPlan()+"<br>";}
    if(nodesplan.get(i).get(child).getWrLau()>-1){
             title += "<b>Workers-Launched:</b> "+nodesplan.get(i).get(child).getWrLau()+"<br>";
    }
    title += "'";
    return title;
}

private static String getNodeImg(int child, int i) {

    String[] nodesList1 = nodesplan.get(i).get(child).getNode().split(" ");
    String nod="";
    
    String token  = (nodesList1.length> 0) ? nodesList1[0] : "";
    String token2 = (nodesList1.length> 1) ? nodesList1[1] : "";
    String token3 = (nodesList1.length> 2) ? nodesList1[2] : "";
    String token4 = (nodesList1.length> 3) ? nodesList1[3] : "";
    
    
         if (token.compareTo("Total")==0)          nod = "";
    else if (token.compareTo("Trigger")==0)        nod = "";
    else if (token.compareTo("Settings")==0)       nod = "";
    else if (token.compareTo("Result")==0)         nod = "ex_result";
    else if (token.compareTo("Append")==0)         nod = "ex_append";
    else if (token.compareTo("Aggregate")==0)      nod = "ex_aggregate";
    else if (token.compareTo("BitmapAnd")==0)      nod = "ex_bmp_and";
    else if (token.compareTo("BitmapOr")==0)       nod = "ex_bmp_or";
    else if (token.compareTo("Group")==0)          nod = "ex_group";
    else if (token.compareTo("GroupAggregate")==0)              nod = "ex_aggregate";
    else if (token.compareTo("Table")==0)                       nod = "ex_table_func_scan"; 
    else if (token.compareTo("HashAggregate")==0)               nod = "ex_aggregate";
    else if (token.compareTo("Limit")==0)                       nod = "ex_limit";
    else if (token.compareTo("LockRows")==0)                    nod = "ex_lock_rows";
    else if (token.compareTo("Materialize")==0)                 nod = "ex_materialize";
    else if (token.compareTo("ProjectSet")==0)                  nod = "ex_projectset";
    else if (token.compareTo("Scan")==0)                        nod = "ex_scan";
    else if (token.compareTo("Seek")==0)                        nod = "ex_seek";
    else if (token.compareTo("Sort")==0)                        nod = "ex_sort";
    else if (token.compareTo("Unique")==0)          nod = "ex_unique";
    else if (token.compareTo("WindowAgg")==0)       nod = "ex_window_aggregate";
    else if (token.compareTo("Undefined")==0)       nod = "ex_unknown";
    else if (token.compareTo("Subquery")==0)        nod = "ex_subplan";
    else if (token.compareTo("Function")==0)        nod = "ex_result";
    else if (token.compareTo("Tid")==0)             nod = "ex_tid_scan";
    else if (token.compareTo("WorkTable")==0)       nod = "ex_worktable_scan";
    else if (token.compareTo("Foreign")==0)         nod = "ex_foreign_scan";
    else if (token.compareTo("Recursive")==0)       nod = "ex_recursive_union";
    else if (token.compareTo("Insert")==0)          nod = "ex_insert";
    else if (token.compareTo("Update")==0)          nod = "ex_update";
    else if (token.compareTo("Delete")==0)          nod = "ex_delete";
    else if (token.compareTo("Values")==0)          nod = "ex_values_scan";
    else if (token.compareTo("CTE")==0)             nod = "ex_cte_scan";
    else if (token.compareTo("Named")==0)           nod = "ex_named_tuplestore_scan";
    else if (token.compareTo("Sample")==0)          nod = "ex_scan";
    else if (token.compareTo("Seq")==0)             nod = "ex_scan";
    else if (token.compareTo("Bitmap")==0)    {
             if(token2.compareTo("Index")==0)       nod = "ex_bmp_index";
             else                                   nod = "ex_bmp_heap";
         }
    else if (token.compareTo("Gather")==0)    {
             if(token2.compareTo("Merge")==0)       nod = "ex_gather_merge";
             else                                   nod = "ex_gather_motion";
         }
    
    else if (token.compareTo("Index")==0)  {
             if(token2.compareTo("Only")==0)                 nod = "ex_index_only_scan";
             else if(token3.compareTo("Backword")==0)        nod = "ex_gather_motion";
                  else                                       nod = "ex_index_scan";
         }
    
    else if (token.compareTo("Merge")==0)      {
            if(token2.compareTo("Append")==0)    nod="ex_merge_append";
            else {
                  switch (nodesplan.get(i).get(child).getJoin_Type()) {
                    case "Anti": nod = "ex_merge_anti_join";
                    case "Semi": nod = "ex_merge_semi_join";
                        default: nod = "ex_merge";
                     }
            }
        }
    else if (token.compareTo("Nested")==0){
              switch (nodesplan.get(i).get(child).getJoin_Type()) {
                case "Anti": nod = "ex_nested_loop_anti_join";
                case "Semi": nod = "ex_nested_loop_semi_join";
                default:     nod = "ex_nested";
                }
            }
    else if (token.compareTo("Hash")==0) {
            if(token2.compareTo("")==0)   nod = "ex_hash";
            else {
               switch (nodesplan.get(i).get(child).getJoin_Type()) {
                case "Anti": nod = "ex_hash_anti_join";
                case "Semi": nod = "ex_hash_semi_join";
                default:     nod = "ex_hash";   
            } }
    }  
    else if (token.compareTo("SetOp")==0) {
            if(nodesplan.get(i).get(child).getStrategie().compareTo("Hashed")==0) {
                if(nodesplan.get(i).get(child).getCommand().startsWith("Intersect")){
                    if(nodesplan.get(i).get(child).getCommand().compareTo("Intersect All")==0){
                        nod = "ex_hash_setop_intersect_all";
                    } else {nod = "ex_hash_setop_intersect";}
                } else if(nodesplan.get(i).get(child).getCommand().startsWith("Except")){
                  if(nodesplan.get(i).get(child).getCommand().compareTo("Except All")==0){
                        nod = "ex_hash_setop_except_all";
                    } else{nod = "ex_hash_setop_except"; }
                } else {
                       nod="ex_hash_setop_unknown";
                }
            }
            else { nod = "ex_setop";}
    }  
//System.out.println(child+"token:"+token+":"+token2+":"+token3+":"+token4+"node:"+nod);
return nod; }
static ArrayList<String> getAllnodes(){
return AllNodes;
}
static ArrayList<String> getAlledges(){
 return Alledges;
}
static ArrayList<ArrayList<Nodes>> getnodesplan (){
    return nodesplan;
}
static ArrayList<QueryStatistic> getQuerySt() {
   return  Qst;
    }
static ArrayList<Integer> getNodesParends(){
    return NodesParend;
}

    static Double estimatemrpm(double cpu, double io, double mem) {
 
        return 0.0;
    }

    static Double estimatemann(double cpu, double io, double mem) {

    return 0.0;
    }

    static Double estimatemrf(double cpu, double io, double mem) {

    return 0.0;
    }

    
        
static public void initRpost(int ml){
   
    re.eval("dataml1<- data.frame(CPU=c(4032814.33, 2207238.84, 2064490.32, 1060864.82, 1373974.97, 1208079.19, 2488100.25, 1212814.16, 2044170.85, 1498535.96, 2081532.94, 1225607.52, 2006588, 2108708.62, 1285345.94, 1206056.5, 2277452.49, 1210093.2, 2698739.33, 1217493.82, 3471098.54, 1222876.09, 3757891.35, 1185758.41, 1042861.86, 752677.28, 942302.84, 903330.63, 1614448.16, 906694.68, 2328929.74, 910731.52, 3186307.6, 914768.49, 751324.16, 4043685.61, 914077.66, 901983.92, 1328654.44, 905347.84, 2043135.88, 909384.54, 2900513.61, 913421.24, 2935525.54, 9289291.11, 15794922.37, 1566875.65, 1127555.83, 916057.6),\n" +
"                      IO=c(2753010, 2947494, 2448912, 2011874, 2041980, 2036388, 2961890, 2124806, 3285437, 1847013, 3118758, 2125330, 2006588, 3118758, 2075758, 2034286, 2515340, 2064108, 2641372, 2064634, 2753010, 2064634, 1328536, 1328536, 1328536, 1328536, 1328536, 1328536, 1328536, 1328536, 1328536, 1328536, 1328536, 1328536, 1328536, 1328536, 1328536, 1328536, 1328536, 1328536, 1328536, 1328536, 1328536, 1328536, 2341991, 11741836, 19729612, 4015091, 4014995, 2038816),\n" +
"                   Power=c(49.12777778 ,45.38 ,45.00691489 ,42.14518072 ,43.10466667 ,42.48509317 ,46.18902954 ,43.11028571 ,45.40512821 ,41.94486486 ,45.04292237 ,42.3486631 ,43.42 ,44.64732143 ,42.8373494 ,43.26363636 ,45.58510638 ,42.50222222 ,46.53910891 ,42.95324675 ,48.53719008 ,43.37307692 ,64.40666667 ,48.92857143 ,44.03461538 ,41.67211538 ,41.86826923 ,40.96153846 ,47.61714286 ,40.91509434 ,53.30769231 ,40.70380952 ,60.83962264 ,40.95142857 ,40.8 ,62.74677419 ,40.66407767 ,40.91904762 ,42.77864078 ,41.20849057 ,47.34095238 ,40.76261682 ,58.18190476 ,41.06442308 ,67.73 ,61.79 ,60.89 ,59.8 ,53.75 ,65.15)\n" +
"                   );");
    
    re.eval("dataml2<- data.frame(Power=c(77.8044934445767 ,70.5571141336489 ,68.1009782608696 ,68.1009782608696 ,65.0517732646258 ,62.3507954290834 ,61.1461409043113 ,60.6151764705882 ,60.6057192374349 ,60.8001384083044 ,60.5894543973941 ,60.5894543973941 ,60.732414553473 ,60.5573463007989 ,60.8537200504413 ,58.2900696864112 ,58.2900696864112 ,57.8207565789474 ,57.8207565789474 ,57.8207565789474 ,57.8207565789474 ,55.8932668329178 ,55.8932668329178 ,55.8932668329178 ,55.8932668329178 ,55.8932668329178 ,55.8932668329178 ,55.8932668329178 ,55.8932668329178 ,55.8932668329178 ,55.8932668329178 ,50.7407085346213 ,50.7407085346213 ,50.7407085346213 ,50.7407085346213 ,50.7407085346213 ,49 ,49.6142222222222 ,49.04 ,49 ,49 ,49 ,49 ,49 ,49 ,49 ,49 ,49 ,49 ,49 ,49 ,49 ,49 ,49 ,49 ,49 ,49 ,49 ,49 ,49 ,49 ,49 ,49 ,49 ,49 ,49 ,49 ,49 ,49 ,49 ,49 ,49 ,49 ,47.5644865925442 ,47.5644865925442 ,47.5644865925442 ,47.5644865925442 ,47.5644865925442 ,47.5644865925442 ,47.5644865925442 ,47.5644865925442 ,47.5644865925442 ,47.5644865925442 ,47.5644865925442 ,47.5644865925442 ,47.5644865925442 ,47.5644865925442 ,47.5644865925442 ,47.5644865925442 ,47.5644865925442 ,47.5644865925442 ,47.5644865925442 ,47.5644865925442 ,47.5644865925442 ,47.5644865925442 ,47.5644865925442 ,47.5644865925442 ,47.5644865925442 ,47.5644865925442 ,47.5644865925442 ,47.5644865925442 ,47.5644865925442 ,47.5644865925442 ,47.5644865925442 ,47.5644865925442 ,47.5644865925442 ,47.5644865925442 ,47.5644865925442 ,47.5644865925442 ,47.5644865925442 ,47.5644865925442 ,47.5644865925442 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,45.1216958911281 ,43.7986688851914 ,43.7986688851914 ,43.7986688851914 ,43.7986688851914 ,43.7986688851914 ,43.7986688851914 ,43.7986688851914 ,43.7986688851914 ,43.7986688851914 ,43.7986688851914 ,43.7986688851914 ,43.7986688851914 ,43.7986688851914 ,43.7986688851914 ,43.7986688851914 ,43.7986688851914 ,43.7986688851914 ,43.7986688851914 ,43.7986688851914 ,43.7986688851914 ,43.7986688851914 ,43.7986688851914 ,43.7986688851914 ,43.7986688851914 ,41.4252499999999 ,41.4252499999999 ,41.4252499999999 ,41.4252499999999 ,41.4252499999999 ,41.4252499999999 ,41.4252499999999 ,40.3519580419581 ,39.3519580419581 ,39.3519580419581 ,39.3519580419581 ,39.3519580419581 ,39.3519580419581 ,39.3519580419581 ,39.3519580419581 ,39.3519580419581 ,39.3519580419581 ,39.3519580419581 ,39.3519580419581 ,39.3519580419581 ,39.3519580419581 ,39.3519580419581 ,39.3519580419581 ,39.3519580419581 ,39.3519580419581 ,39.3519580419581 ,39.3519580419581 ,39.3519580419581 ,39.3519580419581 ,39.3519580419581 ,39.3519580419581 ,39.3519580419581 ,39.3519580419581 ,39.3519580419581 ,39.3519580419581 ,39.3519580419581 ,39.3519580419581 ,39.3519580419581 ,39.3519580419581 ,69.9331481481482 ,45.6083333333333 ,46.1 ,43.3093749999999 ,42.9731249999999 ,44.3869565217391 ,49.2818749999999 ,47.1343749999999 ,44.8285714285714 ,45.7993333333333 ,45.8584661783713 ,54.4820498301245 ,61.3806249999999 ,42.0444444444444 ,45.06 ,47.4499999999999 ,45.925 ,45.625 ,47.925 ,42 ,42 ,50 ,51 ,52 ,53 ,43 ,41.860625 ,41.860626 ,41.860627 ,41.860628 ,41.860629 ,41.860630 ,41.860631 ,41.860632 ,41.860633 ,41.860634 ,41.860635 ,46.8304347826086 ,45.8304347826086 ,44.8304347826086 ,43.8304347826086 ,42.8304347826086 ,38.8304347826086 ,44.8304347826086 ,47.8304347826086 ,48.8304347826086 ,39.8304347826086 ,40.8304347826086 ,42.8304347826086),\n" +
"				    CPU=c(21180656.66 ,14615147.36 ,12882178.9299999 ,12828816.4299999 ,12796141.4299999 ,11721505.4699999 ,10162521.38 ,9380707.53 ,9351611.71999999 ,9309092.5 ,9301082.7 ,9238460.18 ,9154688.82 ,9061411.09 ,8679361.46999999 ,8577779.46999999 ,8410424.02 ,7930697.39 ,7375685.56 ,7199689.41 ,7032781.77999999 ,6888687.62 ,6871500.99 ,6869955.81 ,6859618.61 ,6850083.62 ,6840093.39 ,6784205.21 ,6739740.84 ,6513006.1 ,6086301.52 ,5845805.06 ,5650326.64 ,5328138.73 ,5265497.76 ,5259358.47 ,4931427.09 ,4787647.53 ,4787647.46 ,4783628.35999999 ,4771505.13 ,4753788.79 ,4750451.35999999 ,4689453.92 ,4688421.45 ,4660733.92 ,4650588.18999999 ,4643867.27 ,4528588.26 ,4524849.07 ,4518245.95 ,4516595.16 ,4514337.63 ,4511642.82 ,4508341.27 ,4506690.48 ,4504218.52 ,4504218.52 ,4504218.52 ,4503389.77 ,4503388.93 ,4502563.81 ,4502563.81 ,4502563.81 ,4502563.81 ,4502563.53 ,4491400.84 ,4379344.93 ,4179458.15 ,4093423.77 ,4080353.77 ,4080353.77 ,4034739.92 ,3968053.01 ,3946889.73 ,3874877.05 ,3756212.09 ,3751607.61999999 ,3750072.89 ,3737481.79 ,3685989.75 ,3541301.73999999 ,3470177.32 ,3462903.4 ,3452273.55999999 ,3450271.13 ,3413672.69 ,3410959.17999999 ,3390353.21 ,3294840.8 ,3227609.06 ,3107676.1 ,3073202.61 ,2924934.88 ,2870286.72 ,2863125.62 ,2858174.63 ,2854201.72 ,2850039.12 ,2842489.37 ,2826752.38 ,2815311.11 ,2713752.79 ,2646575.81 ,2624962.75 ,2462715.79 ,2368102.35 ,2354303 ,2131171.21 ,2125184.74 ,2061446.07999999 ,2054761.51 ,2034287.04999999 ,1994859.56999999 ,1994859.56999999 ,1994859.56999999 ,1994859.43 ,1976947.29 ,1936319.26 ,1934944.9 ,1910450.12 ,1906041.57 ,1899321.71 ,1898941.34 ,1886918.21 ,1885010.23 ,1882602.78999999 ,1881914.96 ,1880974.22 ,1879851.44 ,1878475.78999999 ,1877787.94 ,1876774.86999999 ,1876774.86999999 ,1876774.86999999 ,1876415.42 ,1876412.29999999 ,1876069.11999999 ,1876069.11999999 ,1876068.36999999 ,1875304.59 ,1864903.42 ,1860388.93 ,1843922 ,1798066.85 ,1746297.96 ,1722172.33 ,1717875.67 ,1714905.07 ,1712521.33 ,1710023.76 ,1708838.81 ,1696775.83 ,1696051.72 ,1672116.02 ,1628252.06 ,1588538.08 ,1580413.69 ,1578709.23 ,1568307 ,1567013.78 ,1563170.06 ,1563170.05 ,1562531.96 ,1543770.24 ,1480237.44 ,1478905.83 ,1461491.73 ,1417138.24 ,1412582.15 ,1386270.38 ,1383625.29 ,1379593.33999999 ,1379365.13 ,1365183.08999999 ,1356233.68 ,1318840.43 ,1302480.48 ,1287780.88 ,1265303.57 ,1257031.21 ,1243270.82 ,1234037.52 ,1232857.22 ,1224508.14 ,1196924.4 ,1196924.4 ,1196924.4 ,1196924.4 ,1196924.4 ,1193262.22 ,1166270.79 ,1161791.98 ,1160967.23 ,1148320.4 ,1144527.52 ,1143241.17 ,1141668.75 ,1140156.79 ,1132159.58 ,1131859.72 ,1131006.64 ,1130671.94 ,1129562.1 ,1129149.4 ,1128584.82 ,1127911.24 ,1127085.84 ,1126766.19 ,1126766.19 ,1126766.19 ,1126673.11 ,1126399.17 ,1126396.04999999 ,1126082.99 ,1126082.99 ,1126082.99 ,1125852.28 ,1125847.71 ,1125642.28 ,1125642.28 ,1125642.28 ,1125642.28 ,1125641.33 ,1124180.62 ,1122417.24 ,1119729.27 ,1119577.10999999 ,1110714.38 ,1110122.45 ,1079227.31 ,1071459.38 ,1058523.08 ,1043536.05 ,1028848.64 ,987006.1 ,986718.73 ,977524.09 ,958964.11 ,948248.59 ,941014.929999999 ,940218.32 ,938185.33 ,937902.32 ,937902.309999999 ,937585.4 ,937521.28 ,937513.14 ,937513.1 ,937513.039999999 ,937513.039999999 ,937513.039999999 ,937513.039999999 ,937513.039999999 ,937513.02 ,937513.01 ,937510.64 ,927920.19 ,914306.6 ,868608.96 ,868608.96 ,868608.96 ,868608.96 ,868608.82 ,813857.44 ,809285 ,774528.5 ,761807.74 ,759221.539999999 ,756987.77 ,756349.559999999 ,755884.389999999 ,754434.899999999 ,753158.49 ,752520.26 ,751581.62 ,751246.98 ,750925.49 ,750925.49 ,750925.49 ,750925.49 ,750924.74 ,749393.51 ,734705.23 ,688992.54 ,686710.91 ,685944.99 ,685001.54 ,684094.37 ,678403.46 ,632166.159999999 ,626266.48 ,625616.57 ,624974.28 ,592031.23 ,580825.32 ,562498.03 ,556752.43 ,521174.02 ,521174.02 ,507472.48 ,502812.459999999 ,502405.49 ,485571.28 ,480089.329999999 ,477853.68 ,476708.57 ,475070.15 ,457093.29 ,456668.05 ,456063.26 ,455533.45 ,454193.11 ,453810.18 ,453530.92 ,452661.33 ,452269.32 ,451895.47 ,451512.51 ,450967.04 ,450967.04 ,450967.04 ,450751.22 ,450746.65 ,450556.1 ,450556.1 ,450556.1 ,450556.1 ,450555.16 ,449364.089999999 ,436513.01 ,436484.49 ,390636.92 ,390636.92 ,390636.92 ,390636.92 ,390636.92 ,390636.899999999 ,390636.889999999 ,390635.61 ,387090.449999999 ,378502.88 ,377701.25 ,375370.22 ,374986.68 ,373628.36 ,372496.98),\n" +
"                                   mem=c(59522703 ,57512835 ,47128712 ,45888258 ,45815377 ,53743019 ,35891189 ,22055301 ,22055077 ,22055621 ,16869489 ,35178302 ,16869201 ,22053861 ,22054085 ,10447544 ,22054533 ,22054309 ,11248184 ,22054757 ,36190685 ,11246584 ,11246456 ,22064761 ,11246712 ,11246776 ,11246648 ,11246520 ,35891541 ,11246904 ,22055557 ,26244918 ,11247032 ,31505213 ,28986086 ,22055909 ,11247160 ,16871700 ,11248120 ,27021655 ,27094933 ,27101373 ,20590441 ,20588947 ,37950716 ,9973844 ,26860631 ,11248056 ,16871892 ,18151335 ,18151335 ,11246840 ,11247288 ,11246968 ,18151335 ,11247096 ,24201780 ,24201780 ,24201780 ,24201736 ,18151323 ,16871796 ,16871604 ,16871412 ,16871220 ,11247224 ,26752505 ,29087139 ,28929781 ,18220404 ,18327041 ,18228038 ,25478520 ,31262757 ,27706694 ,29567710 ,16871988 ,11247416 ,22491500 ,28474215 ,32108313 ,7941972 ,25374743 ,26058995 ,25095963 ,19106535 ,19103267 ,32689258 ,24913910 ,24845655 ,26358888 ,26384310 ,14963028 ,26624909 ,15005228 ,15000858 ,15005626 ,15008920 ,14986352 ,28268413 ,14987734 ,27824614 ,14998060 ,30436513 ,10071189 ,28121911 ,16748849 ,14986158 ,14567948 ,7210224 ,17609412 ,14969476 ,5153621 ,16870452 ,16831455 ,16795473 ,16908027 ,2914180 ,5955690 ,15021790 ,11870929 ,12200563 ,8237171 ,11926169 ,16872024 ,22182630 ,22193844 ,15008742 ,14981738 ,14985904 ,22198676 ,14991838 ,28253268 ,28227366 ,28223208 ,28218548 ,22194022 ,20636006 ,20595674 ,14983298 ,8251577 ,4789970 ,12804029 ,13496264 ,12620918 ,13435166 ,13471540 ,13467514 ,13483032 ,13492708 ,13479408 ,15496895 ,17070781 ,13488990 ,13721929 ,13489676 ,12627130 ,6025904 ,19039305 ,29402260 ,20618702 ,23115967 ,15004502 ,18732878 ,13377853 ,14823587 ,16917221 ,7553013 ,4700823 ,13506254 ,14612965 ,15703460 ,7633261 ,14920455 ,7634466 ,18089851 ,16912997 ,13197463 ,16357153 ,18767662 ,8132209 ,16582755 ,17822143 ,13496194 ,5992472 ,14645133 ,14620925 ,14618900 ,14615912 ,14604367 ,4793099 ,14998015 ,5404018 ,13495884 ,5986828 ,5994258 ,5994776 ,6004468 ,6007648 ,14617895 ,18498183 ,20568788 ,5993946 ,20567758 ,13492166 ,13508728 ,13471102 ,20568492 ,17455560 ,17443228 ,17428262 ,13487720 ,17416622 ,13692598 ,26628414 ,26621078 ,26616560 ,26627704 ,20563431 ,19137552 ,19120618 ,19108778 ,19106146 ,13503906 ,9743825 ,9828587 ,7374311 ,9853139 ,4415245 ,7368389 ,11103230 ,18801403 ,10430348 ,22576242 ,11986622 ,11351334 ,7196218 ,10138273 ,11761353 ,5398922 ,27717180 ,19122296 ,14367154 ,21431794 ,13498362 ,6481265 ,15752392 ,3899958 ,3887724 ,5183632 ,5183632 ,5183632 ,5183508 ,3887724 ,5199816 ,3887721 ,5199432 ,6016952 ,17185905 ,6750066 ,6749403 ,6748029 ,6693927 ,6754452 ,4495006 ,6012792 ,5147464 ,6722856 ,9390263 ,9373625 ,6013920 ,5997650 ,5984316 ,9385819 ,5995740 ,11972222 ,11937222 ,8235756 ,8265944 ,8227742 ,8273842 ,5989862 ,1587932 ,5388974 ,5389906 ,10759143 ,5386096 ,5390528 ,5389784 ,5385318 ,5133502 ,6733221 ,5994890 ,7499478 ,4313919 ,1304258 ,5150028 ,5387910 ,5858803 ,5838221 ,3616221 ,3231600 ,8045969 ,5394218 ,4435626 ,3975657 ,3809327 ,1952333 ,5849975 ,5131614 ,5133330 ,8702924 ,8696810 ,5394396 ,5396632 ,5391822 ,5130682 ,8706945 ,5390580 ,11251274 ,11255954 ,11258876 ,11262530 ,8694759 ,7635274 ,7661190 ,7651968 ,7648268 ,5394382 ,3771155 ,4107740 ,848452 ,3888445 ,3850143 ,3463490 ,3890293 ,3904681 ,3902208 ,3453320 ,3903406 ,3755744 ,11837956 ,7659552 ,5392620 ,6285398 ,3835215 ,4129584),\n" +
"                                   ES=c(41997190 ,21002450 ,16751433.74 ,16725252.49 ,16708914.99 ,16510542 ,12920914 ,8983586 ,8956046 ,8939406 ,8936294 ,26053644 ,9025962 ,8839186 ,8958468 ,8077057.8 ,10998090 ,9466226 ,5624124 ,10148670 ,9906087 ,5624124 ,5624124 ,9268638 ,5624124 ,5624124 ,5624124 ,5624124 ,10102666 ,5624124 ,9010342 ,8400548 ,5624124 ,10528288 ,8839710 ,8824122 ,5624124 ,5624124 ,5624124 ,8887768 ,8876292 ,8869360 ,8868062 ,8990874 ,6603900 ,5368791.96 ,8827600 ,5624124 ,5624124 ,6593985 ,6593985 ,5624124 ,5624124 ,5624124 ,6593985 ,5624124 ,6603013 ,6603013 ,6602369 ,6600435 ,6593985 ,5629928 ,5630574 ,5631218 ,5631218 ,5624124 ,8877302 ,9727144 ,9174316 ,6718936.24 ,6712401.24 ,6712401.24 ,5705578 ,12723302 ,9844682 ,9373220 ,5633718 ,5624124 ,5624124 ,9006538 ,9524950 ,4594722.01 ,8860390 ,8853506 ,8849346 ,8848568 ,8980848 ,8984364 ,8824290 ,8854110 ,9364016 ,9090914 ,5624124 ,9151662 ,5624124 ,5624124 ,5624124 ,5624124 ,5624124 ,8931654 ,5624124 ,9360922 ,5624124 ,8976942 ,80878293 ,10086534 ,5906649 ,5624124 ,4211114 ,2757233.51 ,5398175 ,5624124 ,5294467 ,5624124 ,5624124 ,5624124 ,5624124 ,5312349 ,2249502 ,5624124 ,3553688 ,3549394 ,3546764 ,3546616 ,5624124 ,6208803 ,6208803 ,5624124 ,5624124 ,5624124 ,6208803 ,5624124 ,6212565 ,6212297 ,6212565 ,6211491 ,6208803 ,5627080 ,5627080 ,5624124 ,3596038 ,2148125.71 ,3530958 ,5624124 ,3551274 ,3885826 ,5624124 ,5624124 ,5624124 ,5624124 ,5624124 ,3712940 ,5620441 ,5624124 ,3670030 ,5624124 ,5089008 ,2249502 ,3937690 ,6712199 ,5628122 ,6708201 ,5624124 ,5624124 ,3742330 ,3812606 ,6051751 ,1880505 ,1838502.26 ,5624124 ,3543242 ,3540666 ,3539088 ,3539000 ,3592090 ,3529604 ,3541794 ,3800976 ,3742524 ,3638794 ,3337699 ,3636484 ,3536122 ,5624124 ,2249502 ,5624124 ,5624124 ,5624124 ,5624124 ,5624124 ,1683694.48 ,3656428 ,2249502 ,5624124 ,2249502 ,2249502 ,2249502 ,2249502 ,2249502 ,5624124 ,3746502 ,6098751 ,2249502 ,6098751 ,5624124 ,5624124 ,5624124 ,6098751 ,3961737 ,3961737 ,3961461 ,5624124 ,3960637 ,3957891 ,6101009 ,6100847 ,6101009 ,6100365 ,6098751 ,5625738 ,5625576 ,5625898 ,5625898 ,5624124 ,3538020 ,3536302 ,3535250 ,3535190 ,2058841 ,3590116 ,3537054 ,3597262 ,3670874 ,3601720 ,3619712 ,4034372 ,2702929 ,3613476 ,3713450 ,2249502 ,6505957 ,5626524 ,4519688 ,6503557 ,5624124 ,1401707 ,5624124 ,1304789 ,1304789 ,1304789 ,1304789 ,1304789 ,1304789 ,1304789 ,1304789 ,1304789 ,1304670 ,2249502 ,3593564 ,2249502 ,2249502 ,2249502 ,2249502 ,2249502 ,1603050.12 ,2249502 ,2249502 ,2249502 ,2764537 ,2764537 ,2249502 ,2249502 ,2249502 ,2764537 ,2249502 ,2767779 ,2767031 ,2252246 ,2251746 ,2252246 ,2251996 ,2249502 ,562395 ,2249502 ,2249502 ,3507048 ,2249502 ,2249502 ,2249502 ,2249502 ,2249502 ,2249502 ,2249502 ,2249502 ,1467951 ,562395 ,3374371 ,2249502 ,2249502 ,2249502 ,969705 ,1164185 ,3215674 ,2249502 ,1212487 ,888537 ,887437 ,886797 ,2249502 ,2249502 ,2249502 ,2662425 ,2662425 ,2249502 ,2249502 ,2249502 ,2249502 ,2662425 ,2249502 ,2664521 ,2664371 ,2664521 ,2663921 ,2662425 ,2250850 ,2251148 ,2251148 ,2250998 ,2249502 ,887783 ,971409 ,532339.99 ,1304788 ,1304788 ,1304788 ,1304788 ,1304788 ,1304788 ,1304788 ,1304669 ,936857 ,3074986 ,2251916 ,2249502 ,2249502 ,915221 ,953375)\n" +
");");
    
        switch (ml) {
            case 1:
                re.eval("postml01<-lm(Power ~ poly(IO, CPU, degree = 2, raw=TRUE),    data = dataml1)");
                //re.eval("postml21<-lm(Power ~ poly(CPU,ES,mem, degree=2, raw=TRUE), data = dataml2)");
                break;
            case 2:
                re.eval("postml02<-neuralnet(Power ~ CPU + IO, data = dataml1, hidden = c(4), threshold = 0.08, stepmax = 50000, rep = 40, learningrate.limit = NULL, \n" +
                        "    learningrate.factor = list(minus = 0.5, plus = 1.2), lifesign = \"minimal\", \n" +
                        "    algorithm = \"rprop+\", act.fct = \"logistic\", linear.output = TRUE)");
                
                /*            re.eval("postml22<-neuralnet(Power ~ CPU + mem + ES, data = dataml2, hidden = c(4, \n" +
                "    3), threshold = 0.08, stepmax = 50000, rep = 40, learningrate.limit = NULL, \n" +
                "    learningrate.factor = list(minus = 0.5, plus = 1.2), lifesign = \"minimal\", \n" +
                "    algorithm = \"rprop+\", act.fct = \"logistic\", linear.output = TRUE)");
                */            break;
            default:
                re.eval("postml03<-train(Power ~ ., data = dataml1, method = \"rf\", trControl = trControl, importance = TRUE, nodesize = 14, ntree = 8)");
                
                /* re.eval("postml23<-train(Power ~ ., data = dataml2, method = \"rf\", \n" +
                "    trControl = trControl, importance = TRUE, nodesize = 14, \n" +
                "    ntree = 500)"); */
                break;
        }
    
}   
static public  void initRMon(){
    
    re.eval("dataml3<- data.frame(Power=c(62.13103448 ,76.575 ,72.725 ,61.125 ,70.75 ,64.05 ,64.55 ,65.55 ,62.025 ,65.4 ,65.825 ,63.35 ,60.475 ,66.725 ,64.65 ,55.675 ,56.125 ,60.85 ,69.35 ,69.7 ,66.2 ,62.075 ,61.7 ,65.62727273 ,63.75 ,66.15 ,65.95 ,62.4 ,67.425 ,67.1 ,62.525 ,62.675 ,66.875 ,59.9 ,71.175 ,73.075 ,61.725 ,54.775 ,63.6 ,57.575 ,63.5 ,64.4 ,57.75 ,60.875 ,65.3 ,54.8 ,65.15 ,69.05 ,62.35 ,62.425 ,71.65 ,60.55 ,65.025 ,63.45 ,64.775 ,64.125 ,64.525 ,62.6 ,65.375 ,65.075 ,63.25 ,68.25 ,69.825 ,64.875 ,70.525 ,55.4 ,64.625 ,64.95 ,63.05 ,65.45 ,60.225 ,65.225 ,64.725 ,67.625 ,60.5 ,64.26 ,62.525 ,61.25 ,61.8 ,62.375 ,67.825 ,37.3 ,39.9 ,40.95 ,40.25 ,43.85 ,41.6 ,39.825 ,40.825 ,39.45 ,39.425 ,41.575 ,46.8 ,45.525 ,39.65 ,42.15 ,42.25 ,37.9 ,39.275 ,47.775 ,47.025 ,41.125 ,40.075 ,40.475 ,39.1 ,38.9 ,43.725 ,38 ,43.225 ,37.375 ,45.775 ,39.35 ,43.1 ,39.85 ,39.3 ,40.025 ,40.65 ,38.95 ,39.225 ,39.55 ,45.95 ,42.325 ,45.375 ,40.675 ,37.65 ,41.45 ,37.775 ,42.35 ,36.95 ,47.35 ,37.55 ,45.5 ,39.15 ,37.2 ,43.175 ,37.4 ,40.35 ,39.475 ,38.925 ,42.3 ,40.45 ,51.95 ,39.7 ,39.95 ,39.675 ,47.225 ,47.075 ,43.9 ,40.125 ,40.1 ,40.775 ,42.775 ,42.675 ,41.2 ,48.875 ,41.75 ,40.575 ,41.075 ,41.275 ,42 ,40.15 ,41.225 ,41.975 ,39.325 ,41 ,40.975 ,38.975 ,41.925 ,51.95 ,42.55 ,53.6 ,49.325 ,43.05 ,41.475 ,39.75 ,38.975 ,43.125 ,41.7 ,39.625 ,41.3 ,42.975 ,40.75 ,41.375 ,39.775 ,43.625 ,40.55 ,41.725 ,39.025 ,39.25 ,43.65 ,41.65 ,37.275 ,43.325 ,45.325 ,41.825 ,45.7 ,40.225 ,45.65 ,49.58 ,40.2 ,40.49 ,46.17142857 ,39.725 ,41.25 ,51.4 ,48 ,53.2 ,44.925 ,45.35 ,43.75 ,42.22 ,48.925 ,46.525 ,45.975 ,39.6 ,39.01428571 ,54.6 ,41.34 ,45.62857143 ,42.5 ,40.54444444 ,39.38780488 ,42.49574468 ,40.36 ,41.5390625 ,46.52222222 ,43.76206897 ,45.06666667 ,40.64333333 ,41.53333333 ,40.79772727 ,49.53793103 ,41.17857143 ,43.0483871 ,40.09384615 ,41.83030303 ,39.62258065 ,40.87727273 ,40.57727273 ,40.6516129 ,49.17111111 ,42.2 ,42.70588235 ,48.34651163 ,41.19354839 ,41.63921569 ,39.42407407 ,41.85 ,40.47788462 ,41.38333333 ,40.88492063 ,40.64098361 ,42.18730159 ,40.24666667 ,40.86388889 ,39.92063492 ,40.078125 ,41.12444444 ,39.50833333 ,40.25904255 ,43.8295082 ,40.46 ,40.34193548 ,40.05923567 ,41.30091743 ,40.64 ,40.7893617 ,41.57760417 ,40.26068376 ,45.18402778 ,39.22876712 ,41.62960526 ,39.17109375 ,38.90727273 ,39.83791469 ,39.8626556 ,39.75479452 ,40.96228571 ,41.0379845 ,41.67675439 ,40.34936709 ,41.14606414 ,40.76511628 ,41.16778846 ,40.82913043 ,39.88914027 ,40.75815385 ,40.8833935 ,40.61731602 ,41.08347339 ,40.75355932 ,41.09636364 ,40.97707641 ,39.3908284 ,41.5411215 ,41.0406015 ,40.55436893 ,40.96379822 ,40.62944444 ,39.87283105 ,43.38356941 ,50.30692042 ,39.83700234 ,39.97878788 ,39.91243243 ,40.32246575 ,41.16120996 ,40.55671642 ,39.96687898 ,40.91754967 ,40.15358974 ,40.36925 ,40.55238095 ,40.62910217 ,40.00227273 ,40.43739316 ,39.61630219 ,40.85880682 ,39.81353965 ,40.66047904 ,40.36349206 ,40.29533981 ,39.99303483 ,41.6389049 ,40.10928144 ,40.10360531 ,39.70087566 ,40.14943609 ,40.00330435 ,40.35921053 ,40.06789555 ,40.13797678 ,43.69927667 ,40.34388646 ,40.93255814 ,40.25728155 ,40.10540254 ,40.18218452 ,40.66791188 ,40.33941532 ,40.16312261 ,40.63294118 ,40.14371047 ,40.43269862 ,40.26895833 ,40.42695093 ,40.09812458 ,39.97442002 ,40.55937282 ,39.87488756 ,40.44638868 ,41.68076023),\n" +
"				    CPU=c(13645281 ,13652422 ,13668818 ,7119558 ,1312185 ,1721521 ,1146996 ,1142958 ,7102514 ,7030494 ,1140617 ,6750579 ,775234 ,772613 ,688726 ,1140512 ,1141255 ,1147253 ,169382 ,1143135 ,594741 ,206076 ,222211 ,266737 ,594641 ,219765 ,433588 ,217947 ,265473 ,181056 ,205883 ,588208 ,1899287 ,588028 ,266732 ,266885 ,125185 ,174306 ,144933 ,124312 ,208238 ,226356 ,349762 ,349812 ,243897 ,146916 ,226288 ,132356 ,147102 ,98116 ,365060 ,98141 ,244062 ,95604 ,95969 ,146923 ,198967 ,193225 ,98418 ,269567 ,128881 ,129565 ,364818 ,193118 ,107792 ,243897 ,270651 ,128513 ,115629 ,141190 ,99732 ,151088 ,141431 ,138197 ,261259 ,182345 ,87053 ,95591 ,87033 ,85253 ,264553 ,388544 ,196121 ,210951 ,201117 ,220689 ,339455 ,260046 ,335837 ,473427 ,461618 ,478172 ,461295 ,433598 ,278827 ,424065 ,457335 ,536417 ,545352 ,542634 ,458072 ,531690 ,426227 ,441782 ,450162 ,455381 ,442776 ,440670 ,786828 ,446683 ,543117 ,802130 ,781451 ,784009 ,796811 ,696457 ,685265 ,851656 ,656132 ,634788 ,736249 ,798705 ,644805 ,853344 ,416281 ,749190 ,810814 ,721662 ,808691 ,739561 ,812278 ,705434 ,770384 ,773775 ,753466 ,750364 ,799174 ,970354 ,849192 ,731388 ,864812 ,830679 ,725415 ,832450 ,838377 ,850342 ,874372 ,847296 ,909688 ,885237 ,785155 ,743540 ,771408 ,739848 ,806567 ,910408 ,1181701 ,1153845 ,849134 ,829798 ,918855 ,953823 ,1141303 ,818735 ,747906 ,766653 ,912740 ,724090 ,1143941 ,849403 ,761670 ,866843 ,723632 ,829320 ,845487 ,890898 ,786743 ,826796 ,964680 ,869576 ,824511 ,930749 ,900491 ,897824 ,1153010 ,1202967 ,2724506 ,1216021 ,2421107 ,756056 ,1073693 ,1204482 ,1071370 ,2484456 ,2657690 ,1056638 ,708275 ,501213 ,837829 ,805954 ,448808 ,911096 ,886891 ,800683 ,726741 ,829242 ,789786 ,662405 ,798256 ,889120 ,704204 ,901711 ,452908 ,912164 ,1376718 ,1169192 ,1152102 ,793220 ,769389 ,734861 ,1405973 ,356810 ,1111346 ,758328 ,776662 ,450629 ,696077 ,1183895 ,256217 ,716743 ,205518 ,897230 ,780391 ,883761 ,839949 ,2255213 ,370113 ,695240 ,1327416 ,1526629 ,1047689 ,757512 ,827018 ,930463 ,1070564 ,2324123 ,377559 ,1542995 ,892907 ,2315012 ,844600 ,1085412 ,746525 ,1181486 ,824336 ,769914 ,753014 ,1133988 ,2355658 ,1521701 ,678331 ,881250 ,767400 ,784565 ,967505 ,860770 ,1366789 ,884730 ,2225968 ,985438 ,852800 ,857597 ,767869 ,777119 ,475832 ,478030 ,1050633 ,841620 ,708729 ,475293 ,477529 ,754873 ,477181 ,850117 ,747415 ,769953 ,871296 ,883534 ,785967 ,533239 ,752312 ,767065 ,804177 ,855139 ,960041 ,879937 ,802338 ,800221 ,837732 ,883325 ,704496 ,680131 ,854046 ,889478 ,835967 ,832091 ,754261 ,676058 ,711580 ,779655 ,875463 ,876126 ,701378 ,762286 ,697972 ,781947 ,828880 ,696679 ,693659 ,853315 ,838372 ,766747 ,825360 ,720596 ,996167 ,825029 ,842685 ,757379 ,948988 ,884888 ,821537 ,834790 ,809223 ,720573 ,766701 ,791705 ,782223 ,790906 ,764084 ,805917 ,792163 ,900440 ,874835 ,897184 ,893187 ,876675 ,754012 ,798256 ,858664 ,806826 ,812075 ,807959),\n" +
"                                   mem=c(26821 ,26073 ,26057 ,12200 ,11478 ,11177 ,10677 ,10436 ,10005 ,9832 ,9796 ,9437 ,9112 ,9083 ,8209 ,7033 ,6833 ,6505 ,6219 ,6203 ,6199 ,6179 ,6111 ,6083 ,6058 ,6028 ,5996 ,5977 ,5972 ,5850 ,5837 ,5829 ,5727 ,5725 ,5712 ,5663 ,5607 ,5528 ,5519 ,5510 ,5497 ,5488 ,5487 ,5468 ,5444 ,5424 ,5377 ,5347 ,5342 ,5336 ,5330 ,5318 ,5312 ,5309 ,5304 ,5304 ,5296 ,5287 ,5281 ,5271 ,5261 ,5242 ,5231 ,5192 ,5182 ,5162 ,5153 ,5151 ,5095 ,5090 ,5090 ,5006 ,4997 ,4987 ,4929 ,4874 ,4841 ,4750 ,4748 ,4712 ,4517 ,4279 ,4110 ,4263 ,4003 ,4090 ,5017 ,4533 ,5410 ,5325 ,7353 ,5335 ,4641 ,4358 ,4172 ,4903 ,3961 ,5397 ,5628 ,5123 ,4238 ,5676 ,4378 ,3889 ,4715 ,5318 ,4095 ,3626 ,5416 ,4283 ,5075 ,5361 ,5019 ,4733 ,5287 ,4900 ,4343 ,6265 ,5185 ,5010 ,4506 ,4993 ,4809 ,5018 ,3560 ,4823 ,4556 ,4155 ,4721 ,3908 ,4993 ,4433 ,5583 ,4767 ,4577 ,4358 ,4338 ,7515 ,5655 ,4014 ,6356 ,5374 ,4664 ,5176 ,6076 ,4527 ,4980 ,5114 ,6182 ,6133 ,5677 ,5092 ,4124 ,4529 ,4156 ,4060 ,6773 ,6137 ,4247 ,3987 ,5181 ,7111 ,6564 ,5488 ,4619 ,5151 ,6462 ,5153 ,5831 ,5427 ,5573 ,5494 ,4349 ,4913 ,5675 ,5204 ,5205 ,4957 ,5779 ,4901 ,4860 ,5645 ,4769 ,4729 ,6192 ,6946 ,11057 ,8240 ,10617 ,4559 ,6055 ,6489 ,5356 ,7623 ,10796 ,5672 ,4598 ,5594 ,5883 ,4639 ,3766 ,5188 ,5316 ,4484 ,4720 ,4013 ,3609 ,4815 ,4377 ,4705 ,3038 ,4128 ,4702 ,4911 ,7170 ,6964 ,7175 ,5934 ,3704 ,4381 ,8650 ,5525 ,5638 ,3274 ,4640 ,4329 ,4223 ,6098 ,4263 ,4711 ,4038 ,4060 ,4409 ,4896 ,4722 ,8680 ,4883 ,4554 ,6388 ,6405 ,5447 ,4826 ,4070 ,3945 ,5468 ,9166 ,5659 ,6371 ,5527 ,10777 ,4726 ,5852 ,4018 ,6417 ,5118 ,3915 ,5062 ,6802 ,8624 ,6842 ,4958 ,6089 ,4184 ,4619 ,5852 ,4268 ,6152 ,6618 ,10968 ,5446 ,5459 ,4965 ,4543 ,5072 ,4297 ,3790 ,5744 ,3542 ,4278 ,3791 ,4285 ,6925 ,4033 ,4011 ,3453 ,3703 ,4396 ,5455 ,3596 ,4797 ,5020 ,6990 ,3878 ,6583 ,5871 ,6102 ,4437 ,3874 ,5838 ,5827 ,3721 ,4286 ,5273 ,5186 ,4343 ,5312 ,3450 ,3110 ,5514 ,3077 ,4960 ,4847 ,4457 ,3628 ,4475 ,4874 ,4874 ,4215 ,5359 ,4510 ,5008 ,4502 ,5378 ,4449 ,6943 ,4549 ,6318 ,3812 ,4505 ,5949 ,4630 ,6087 ,3639 ,4001 ,4143 ,6170 ,4254 ,6384 ,4371 ,6238 ,6079 ,5221 ,5054 ,5799 ,5829 ,5478 ,5544 ,4204 ,4918 ,4702 ,5017 ,5154),\n" +
"                                    ES=c(0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,224 ,264 ,272 ,280 ,288 ,296 ,296 ,304 ,352 ,360 ,384 ,384 ,384 ,384 ,424 ,424 ,432 ,440 ,440 ,440 ,448 ,448 ,448 ,456 ,464 ,464 ,464 ,472 ,472 ,480 ,504 ,512 ,512 ,520 ,520 ,520 ,528 ,536 ,536 ,552 ,560 ,568 ,576 ,576 ,600 ,608 ,616 ,624 ,624 ,632 ,632 ,640 ,664 ,664 ,664 ,664 ,672 ,672 ,672 ,704 ,704 ,704 ,712 ,720 ,720 ,728 ,736 ,744 ,744 ,744 ,744 ,744 ,752 ,776 ,776 ,784 ,784 ,792 ,800 ,808 ,848 ,856 ,864 ,880 ,896 ,920 ,936 ,944 ,952 ,968 ,968 ,1000 ,1024 ,1032 ,1088 ,1096 ,1120 ,1144 ,1152 ,1168 ,1280 ,1280 ,1304 ,1344 ,1368 ,1464 ,1496 ,1656 ,1688 ,1744 ,1760 ,1864 ,1928 ,1944 ,2000 ,4496 ,4880 ,9224 ,12176 ,34832 ,48304 ,57768 ,58792 ,64608 ,71640 ,117680 ,174528 ,229992 ,234216 ,235536 ,263536 ,337512 ,349128 ,467928 ,489896 ,627744 ,778424 ,803472 ,938608 ,1169944 ,1199136 ,1258120 ,1321272 ,1340136 ,1347760 ,1438880 ,1472304 ,1475664 ,1564320 ,1693888 ,1782032 ,1897512 ,1927288 ,1961616 ,2163728 ,2205616 ,2237992 ,2333336 ,2340880 ,2668056 ,2698248 ,2716584 ,2978752 ,3166488 ,3246120 ,3386632 ,3511856 ,3659704 ,3735752 ,3739000 ,4005120 ,4380960 ,4686136 ,4784728 ,4881288 ,4992344 ,5560344 ,5619776 ,5856704 ,6575608 ,6592024 ,6679544 ,7081416 ,7118848 ,7382168 ,7614072 ,8333552 ,8798808 ,9028504 ,9338288 ,9756512 ,10132264 ,10270696 ,10466576 ,10591328 ,10847424 ,11688232 ,11763200 ,11942560 ,11959680 ,12190368 ,13147160 ,13180112 ,13285376 ,14913008 ,15051784 ,15217640 ,15821512 ,16582984 ,17018144 ,17848504 ,17858480 ,17925784 ,18201992 ,18350936 ,18369072 ,18427680 ,18872584 ,20547432 ,21056200 ,21223560 ,21635000 ,23204816 ,23456624 ,24606720 ,26024280 ,26455896 ,26975496 ,28088544 ,28635976 ,29790496 ,30017520 ,30300720 ,31110432 ,31629016 ,34448344 ,34697864 ,36513400 ,37099560 ,38773256 ,39401440 ,39691568 ,40940328 ,41777088 ,42233976 ,42618120 ,42812976 ,43328952 ,44260456 ,44261608 ,45053048 ,49458904 ,58476840 ,58855568 ,60921808 ,62160400 ,64335280 ,64856008 ,65432864 ,66369320 ,70692328 ,76585456 ,78771512 ,81531632 ,85355360 ,85941488 ,87486160 ,87541024 ,88781128 ,89675576 ,112261904)\n" +
");");
    
    re.eval("montml01<-lm(Power ~ poly(CPU, ES, mem, degree = 3, raw = TRUE), \n" +
"    data = dataml3)");

    re.eval("montml02<-neuralnet(Power ~ CPU + mem + ES, data = dataml3, hidden = c(6), \n" +
"    threshold = 0.08, stepmax = 5e+05, rep = 40, learningrate.limit = NULL, \n" +
"    learningrate.factor = list(minus = 0.5, plus = 1.2), lifesign = \"minimal\", \n" +
"    algorithm = \"rprop+\", err.fct = \"sse\", act.fct = \"logistic\", \n" +
"    linear.output = TRUE)");
    
    re.eval("montml03<-train(Power ~ ., data = dataml3, method = \"rf\", \n" +
"    trControl = trControl, importance = TRUE, nodesize = 10, \n" +
"    ntree = 30)");
    
    
}

    static public  REXP getRoutput() {
      return resultR;
    }

    static void runR(String frameR, int i) {
    
        resultR = re.eval(frameR);
        switch (i) {
            case 1:
                resultR=re.eval("predict.lm(postml01, newdata=t)");
                //resultR=re.eval("predict.lm(postml21, newdata=t)");
                break;
            case 2:
                resultR=re.eval("predict(postml02, newdata=t)");
                //resultR=re.eval("predict(postml22, newdata=t)");
                break;
            default:
                resultR=re.eval("predict(postml03, newdata=t)");
                //resultR=re.eval("predict(postml22, newdata=t)");
                break;
        }
    }

    static ArrayList<ArrayList<Double>> getRealp() {
     return measurep;
    }

      
    
}
