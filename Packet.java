 /**  Name: Rohith Kumar Uppala 
 *    Filename: Packet.java
 *    Date: 3/23/2017
 *    Course: COMP 594
 *    Description: Packet Class to store data and 
      static function to convert between obj and bytes[].
 */


import java.io.*;
public class Packet implements Serializable 
{
 /* Packet Constructor:
 * t = type
 * i = seqno
 * j = size of the packet
 */
   // Helper function to Convert Packet object to byte[].
   public static byte[] serialize(Packet packet) throws IOException
   {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(out);
      oos.writeObject(packet);
      oos.flush();
      byte[] bytes = out.toByteArray();
      out.close();
      oos.close();
      return bytes;
   }
   
   // Helper function to Convert byte[] to Packet object.
   public static Packet deserialize(byte[] data) throws IOException, ClassNotFoundException
   {
      ByteArrayInputStream in = new ByteArrayInputStream(data);
      ObjectInputStream ois = new ObjectInputStream(in);
      Packet packet = (Packet)ois.readObject();
      ois.close();
      return packet;
   }

   public Packet(String source, String dest, int t, int i, int j, byte abyte[])   {
      sourceID = source;
      destinationID = dest;
      type = t;
      seqno = i; 
      size = j;
      data = new byte[size];
      data = abyte;
   }

   public String getSourceID()
   {
      return sourceID;
   }

   public String getDestinationID()
   {
      return destinationID;
   }

   public byte[] getData()
   {
      return data;
   }

   public int getSeqNo()
   {
      return seqno;
   }
   public int getSize()
   {
      return size;
   }
   public int getType()
   {
      return type;
   }
   public String toString()
   {
      return "type: " + type + "seq: " + seqno + " size: " +
         size + " data: " + data;
   }
   
   private String sourceID;
   private String destinationID;
   private int type;
   private int seqno;
   private int size;
   private byte data[];
   
   
} 