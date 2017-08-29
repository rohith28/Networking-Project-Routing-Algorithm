 /**  Name: Rohith Kumar Uppala
 *    Filename: RoutingServer.java
 *    Date: 3/23/2017
 *    Course: COMP 594
 *    Description: This server code will send and recevie routing information.
 *				      
 */
import java.util.*;
import java.util.logging.*;
import java.net.*; 
import java.io.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

class RoutingServer {

   public static final int WINDOW_SIZE = 4;
   public static final int MAX_DATA_LENGTH = 16;
   
   private static final Logger LOGGER = Logger.getLogger( Thread.currentThread().getStackTrace()[0].getClassName() );
   
   public static void main(String args[]) throws Exception
   {

            

      System.out.println("Server is Up.");
      new Router("A").start();
      
      new Router("B").start();
      new Router("C").start();
      new Router("D").start();
      new Router("E").start();
      new Router("F").start();
      
      new Router("H").start();
      
   }//main
    
} 