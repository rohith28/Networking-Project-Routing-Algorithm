 /**  Name: Rohith Kumar Uppala
 *    Filename: Router.java
 *    Date: 3/23/2017
 *    Course: COMP 594
 *    Description: This server code will send and recevie routing information.
 *				      
 */
import java.util.*;
import java.util.logging.*;
import java.net.*; 
import java.io.*;
import java.text.*;
import java.sql.Timestamp;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

class Router extends Thread {

   private static String filename = "information_file.txt";
   private String nodeID;
   private int portNum=-1;
   private Map<String,Table> routingTable = new HashMap<String,Table>();
   private Map<Integer,String> neighborTable = new HashMap<Integer,String>();
   private Map<Integer,Integer> neighborCost = new HashMap<Integer,Integer>();
   private Map<String,Timestamp> lastUpdateTime = new HashMap<String,Timestamp>();
   private InetAddress IPAddress;
   private DatagramSocket serverSocket;
   DatagramPacket receivePacket;
   private boolean isUpdated;
   
   public Router(String node) {
      nodeID = node;
   }
   
   public void run() 
   {
      /*
      long sec = System.currentTimeMillis()/1000;
      while(true)
      {
         if ((System.currentTimeMillis()/1000 - sec) > 5)
         {
            sec = System.currentTimeMillis()/1000;
            System.out.println(System.currentTimeMillis()/1000);
         }
      }
*/
      
      
      try
      {
         if (nodeID.equals("H"))
            injectDataPacket();
         else
            handleConnection();
       
      }
      catch (SocketException skte)
      { System.out.println("SocketException: " + skte); }
      catch (Exception ex)
      { 
         System.out.println("Exception: " + ex);
         ex.printStackTrace();
      }


   }
   
   protected void injectDataPacket() throws SocketException, Exception
   {
      Thread.sleep(30000);
      
      System.out.println("Sending data packet from H to D.");
      
      serverSocket = new DatagramSocket();
      byte[] fileNameBytes = "Hello world!!".getBytes();
	  
	   // Pack file name into the packet, convert packet to bytes.
      Packet requestPacket = new Packet("H", "D", 2, 0, fileNameBytes.length, fileNameBytes);
      byte[] buffer = Packet.serialize(requestPacket);
       
      // Get server IP address.
      InetAddress receiverAddress = InetAddress.getLocalHost();

      // Pack data and data into the DatagramPacket.
      DatagramPacket packet = new DatagramPacket(
        buffer, buffer.length, receiverAddress, 10000);
        
      // Send Request.
      serverSocket.send(packet);
      
      
      // Shut down C.
      Thread.sleep(30000);
      System.out.println("Shut down packet from H to C.");
      // Pack file name into the packet, convert packet to bytes.
      requestPacket = new Packet("H", "C", 6, 0, fileNameBytes.length, fileNameBytes);
      buffer = Packet.serialize(requestPacket);

      // Pack data and data into the DatagramPacket.
      packet = new DatagramPacket(
        buffer, buffer.length, receiverAddress, 10000);
        
      // Send Request.
      serverSocket.send(packet);
      

      Thread.sleep(180000);
      System.out.println("Sending data packet from H to D.");
      // Pack file name into the packet, convert packet to bytes.
      requestPacket = new Packet("H", "D", 2, 0, fileNameBytes.length, fileNameBytes);
      buffer = Packet.serialize(requestPacket);

      // Pack data and data into the DatagramPacket.
      packet = new DatagramPacket(
        buffer, buffer.length, receiverAddress, 10000);
        
      // Send Request.
      serverSocket.send(packet);
      
      
   }
   
   protected void handleConnection() throws SocketException, Exception

   {
      
      ArrayList<String> fileInfo= new ArrayList<String>();
      
      // Read data from File
      Path file = Paths.get(filename);
      try (BufferedReader reader = Files.newBufferedReader(file)) {
         String line = null;
         while ((line = reader.readLine()) != null) {
            fileInfo.add(line);
         }
      } catch (IOException x) {
         System.err.format("IOException: %s%n", x);
      }

      String[] tupleArray;
      
      // Find assigned port number. 
      for(String infoLine : fileInfo)
      {
         tupleArray = infoLine.split(",");
         if (tupleArray[1].equals(nodeID))
         {
            portNum = Integer.parseInt(tupleArray[2]);
            break;
         }
      }

      // Get immediate neighbors DVs.
      for(String infoLine : fileInfo)
      {
         tupleArray = infoLine.split(",");
         if (tupleArray[0].equals(nodeID))
         {
            routingTable.put(tupleArray[1], new Table(
               Integer.parseInt(tupleArray[3]), 
               portNum,
               Integer.parseInt(tupleArray[2]))
               );
               
            neighborTable.put(Integer.parseInt(tupleArray[2]),tupleArray[1]);
            neighborCost.put(Integer.parseInt(tupleArray[2]),Integer.parseInt(tupleArray[3]));
         }
      }
      
      // put in DV for itself, must go after immNeighbors recorded.
      routingTable.put(nodeID, new Table(0, portNum, portNum));
      
      // Router run on known port number.
      serverSocket = new DatagramSocket(portNum);
      
      // Wait up to 5 second for ACK reply.
      serverSocket.setSoTimeout(5000);
      
      //create a datagram packet to hold incoming UDP packet
      // For reply message from Server.
      byte[] receiveData = new byte[1024];
      receivePacket = new DatagramPacket(receiveData, receiveData.length);
      
      IPAddress = InetAddress.getLocalHost(); //get local IP addr
      
      String timestamp = new SimpleDateFormat("HH.mm.ss:sss").format(new Timestamp(System.currentTimeMillis()));
      System.out.printf("[%s]Router %s: Initialize Routing Table:\n%s\n", timestamp, nodeID, routingTableToString());
      
      boolean isUpdated = false;
      
      Thread.sleep(5000);
      // generate DV message and advertisements DV to immediate neighbors.
      advertisements(createDvMessage());
      //new DelaySend(serverSocket, advertisements(createDvMessage()),5000).start();
      
            
      // Receive DV from imm neighbors.
      long sec = System.currentTimeMillis()/1000;
      while(true)
      {
         //Timestamp time = new Timestamp(System.currentTimeMillis()); 
         if ((System.currentTimeMillis()/1000 - sec) > 5)
         {
            advertisements(createDvMessage());
            sec = System.currentTimeMillis()/1000;
         }
         boolean dropDV = false;
         for(Iterator<Map.Entry<String,Timestamp>>it=lastUpdateTime.entrySet().iterator();it.hasNext();)
         {
           Map.Entry<String,Timestamp> entry = it.next();
           long diff = new Timestamp(System.currentTimeMillis()).getTime() - entry.getValue().getTime();
           if (diff > 5000*6) 
           {
               System.out.printf("Router %s: %s is disconnected\n", nodeID, entry.getKey());
               routingTable.remove(entry.getKey());
               
               
               for(Iterator<Map.Entry<String,Table>>it2=routingTable.entrySet().iterator();it2.hasNext();)
               {
                  Map.Entry<String,Table> entry2 = it2.next();
                  if (entry2.getValue().getDestPort() == neighborTable.get(entry2.getKey()))
                     it2.remove();
               }

               
               
               
               it.remove();
               dropDV = true;
           }
         }

         try
         {
            serverSocket.receive(receivePacket);
            Packet incomingPacket = Packet.deserialize(receivePacket.getData());
            if (incomingPacket.getType() == 5 && !dropDV)
               dvPkt(incomingPacket);
            else if (incomingPacket.getType() == 6 && incomingPacket.getDestinationID().equals(nodeID))
            {
               System.out.printf("Router %s: Shuting down", nodeID);
               return;
            }
            else
               dataPkt(incomingPacket);
               
         }
         
         catch (SocketTimeoutException ex) 
         {
            //timestamp = new SimpleDateFormat("HH.mm.ss:sss").format(new Timestamp(System.currentTimeMillis()));
            //System.out.printf("[%s]Router %s: routing table in stable state\n", timestamp,nodeID);
            
         }
      }
   }      
   
   private void dvPkt(Packet incomingPacket) throws Exception
   {
      String arrivedDvMsg = new String(incomingPacket.getData(),0, incomingPacket.getSize());
      int SourcePort = receivePacket.getPort(); 
      //System.out.printf("Advertisement: <%s> from %d to %d\n", arrivedDvMsg, SourcePort, portNum);
      
      // determine shortest distance path.
      String SourceName = "";
      boolean isUpdated = false;
      //for (String neighborName : immNeighbors)
      for (Map.Entry<Integer, String> neigbor : neighborTable.entrySet())
      {
         if (SourcePort == neigbor.getKey())
         {
            SourceName = neighborTable.get(neigbor.getKey());
            break;
         }
      }
      
      lastUpdateTime.put(SourceName, new Timestamp(System.currentTimeMillis()));
      
      /*
      // for debug.
      if (SourceName.equals(""))
      {
         System.out.printf("Error from SourcePort: %d at Node %s message: %s", SourcePort, nodeID, arrivedDvMsg);
         throw new Exception();
      }
      */
      
      ArrayList<String> nodeList= new ArrayList<String>();

      // Separate X:Y:Z,X:Y:Z,X:Y:Z, by ','
      String[] dvArray = arrivedDvMsg.split(",");
      for (String dv : dvArray)
      {
         // Handle each dv data set (X:Y:Z) 
         String[] dvPair = dv.split(":");
         String newNode = dvPair[0];
         int newCost = Integer.parseInt(dvPair[1]);
         int newPort = Integer.parseInt(dvPair[2]);
         
         nodeList.add(newNode);
         
         int SourceLinkCost = neighborCost.get(SourcePort);
         int totalNewCost = SourceLinkCost + newCost;
         if (routingTable.containsKey(newNode))
         {
            // shorter path || lower node Id with same cost
            if (totalNewCost < routingTable.get(newNode).getCost()||
               (totalNewCost == routingTable.get(newNode).getCost() && 
               neighborTable.get(SourcePort).compareTo(neighborTable.get(routingTable.get(newNode).getDestPort())) < 0)
               )
            {                     
               if (newPort != portNum)
               { 
                  routingTable.get(newNode).setDestPort(SourcePort);
                  routingTable.get(newNode).setCost(totalNewCost);
                  
               }
               else
               {
                  routingTable.remove(newNode);
               }
               isUpdated = true;
                  
            }
    
         }
         else
         {
            if (newPort != portNum)
            {
               routingTable.put(newNode, new Table(totalNewCost, portNum, SourcePort));
               isUpdated = true;
            }
         }
            
      }
     
      // Check unavailable Node:
      for(Iterator<Map.Entry<String,Table>>it=routingTable.entrySet().iterator();it.hasNext();)
      {
         Map.Entry<String,Table> entry = it.next();
         if (SourcePort == entry.getValue().getDestPort())
         {
            boolean isAlive = false;
            for (String node : nodeList)
            {
               if (node.equals(entry.getKey()))
               {
                  isAlive = true;
                  break;
               }
            }
            if (!isAlive)
            {
               it.remove();
               isUpdated = true;
            }
         }
      }


      // if DV changed.
      if (isUpdated)
      {
         String timestamp = new SimpleDateFormat("HH.mm.ss:sss").format(new Timestamp(System.currentTimeMillis()));
         System.out.printf("[%s]Router %s: DV message <%s> from %d(Node %s)\nUpdated Routing Table:\n%s\n", 
            timestamp, nodeID, arrivedDvMsg, SourcePort, SourceName, routingTableToString());
         
      }
      
      //Thread.sleep(1000);
      // generate DV message and advertisements DV to immediate neighbors.
      //advertisements(createDvMessage());
   
   
   }
   
   
      // Forward packet.
   private void dataPkt(Packet incomingPacket) throws Exception
   {
      
      if (incomingPacket.getDestinationID().equals(nodeID))
      {
         String message = new String(incomingPacket.getData(),0, incomingPacket.getSize());
         System.out.printf("Received data packetm message at %s: %s\n", nodeID, message);
      }
      else
      {
         // Not mine, forward it.
         
         int sendPort = routingTable.get(incomingPacket.getDestinationID()).getDestPort();  //get client's port #
         byte[] buffer = Packet.serialize(incomingPacket);
         DatagramPacket sendPacket = new DatagramPacket(buffer, buffer.length, IPAddress, sendPort);
         serverSocket.send(sendPacket);
         
         System.out.printf("Forwarding data packet from %s to %s\n", nodeID, neighborTable.get(sendPort));


      }
   }
      

   
   private String routingTableToString()
   {
      String datatable= "Dest\tCost\tOutPort\tDestPort\n";
      for (Map.Entry<String,Table> route : routingTable.entrySet())
      {
         Table table = route.getValue();
         datatable += String.format("%s\t%d\t%d\t%d\n", 
            route.getKey(), table.getCost(), table.getOutPort(), table.getDestPort());
      }
      
      return datatable;
   }
   
   private String createDvMessage() 
   {
      // generate DV message.
      String dvMessage = "";
      for (String key : routingTable.keySet())
      {
         //if (key != nodeID)
            dvMessage += key + ":" + routingTable.get(key).getCost() + ":" + routingTable.get(key).getDestPort() + ",";
      }
      return dvMessage;
   }
   
   private void advertisements(String message) throws IOException
   {
       // advertisements DV to immediate neighbors.
      int sendPort =-1;
      
      // convert DV msg to bytes.
      byte[] msgBuffer = message.getBytes();
      

      for (Map.Entry<Integer, String> neigbor : neighborTable.entrySet())
      {
         // Pack DV msg into the packet, convert packet to bytes.
         Packet requestPacket = new Packet(nodeID, neigbor.getValue(), 5, 0,msgBuffer.length, msgBuffer);
         byte[] buffer = Packet.serialize(requestPacket);
         
         sendPort = neigbor.getKey();  //get client's port #

         DatagramPacket sendPacket = new DatagramPacket(buffer, buffer.length, IPAddress, sendPort);
         serverSocket.send(sendPacket);
         
      }

   }
  
    
} 