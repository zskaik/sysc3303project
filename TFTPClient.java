// TFTPClient.java 
// This class is the client side for a very simple assignment based on TFTP on
// UDP/IP. The client uses one port and sends a read or write request and gets 
// the appropriate response from the server.  No actual file transfer takes place.   
/**
 * @author Ziad Skaik
 * @date 2014-05-21
 * @version 1.0
 */
import java.io.*;
import java.net.*;
import java.util.*;
/**
 * @author Ziad Skaik
 * @date 2014-05-21
 * @version 1.0
 */
public class TFTPClient {

   private DatagramPacket sendPacket, receivePacket;
   private DatagramSocket sendReceiveSocket;
   private static String s;
private static String fname;


   // we can run in normal (send directly to server) or test
   // (send to simulator) mode
   public static enum Mode { NORMAL, TEST};

   public TFTPClient()
   {
	
      try {
         // Construct a datagram socket and bind it to any available
         // port on the local host machine. This socket will be used to
         // send and receive UDP Datagram packets.
         sendReceiveSocket = new DatagramSocket();
      } catch (SocketException se) {   // Can't create the socket.
         se.printStackTrace();
         System.exit(1);
      }
   }

   public void sendAndReceive(int i)
   {
      byte[] msg = new byte[100], // message we send
             fn, // filename as an array of bytes
             md, // mode as an array of bytes
             data; // reply as array of bytes
      String filename, mode; // filename and mode as Strings
      int j, len, sendPort;
      Mode run = Mode.TEST; // change to NORMAL to send directly to server
      
      if (run==Mode.NORMAL) 
         sendPort = 69;
      else
         sendPort = 68;
      
      // sends 10 packets -- 4 reads, 5 writes, 1 invalid
     // for(int i=0; i<10; i++) {

         System.out.println("Client: creating packet request number:  " + i + ".");
         
         // Prepare a DatagramPacket and send it via sendReceiveSocket
         // to sendPort on the destination host (also on this machine).

         // if i even, it's a read; otherwise a write
         // opcode for read is 01, and for write 02

        msg[0] = 0;
        if(s.equals("R")) 	
           msg[1]=1;
        else if(s.equals("W")) 
           msg[1]=2;
           
       /* if(i==8) 
           msg[1]=7; // if it's the 8th time, send an invalid request
*/
        // next we have a file name -- let's just pick one
        filename = fname;
        // convert to bytes
        fn = filename.getBytes();
        
        // and copy into the msg
        System.arraycopy(fn,0,msg,2,fn.length);
        
        // now add a 0 byte
        msg[fn.length+2] = 0;

        // now add "octet" (or "netascii")
        mode = "octet";
        // convert to bytes
        md = mode.getBytes();
        
        // and copy into the msg
        System.arraycopy(md,0,msg,fn.length+3,md.length);
        
        len = fn.length+md.length+4; // length of the message

        // and end with another 0 byte 
        msg[len-1] = 0;
        

        // Construct a datagram packet that is to be sent to a specified port
        // on a specified host.
        // The arguments are:
        //  msg - the message contained in the packet (the byte array)
        //  the length we care about - k+1
        //  InetAddress.getLocalHost() - the Internet address of the
        //     destination host.
        //     In this example, we want the destination to be the same as
        //     the source (i.e., we want to run the client and server on the
        //     same computer). InetAddress.getLocalHost() returns the Internet
        //     address of the local host.
        //  69 - the destination port number on the destination host.
        try {
           sendPacket = new DatagramPacket(msg, len,
                                         InetAddress.getLocalHost(), sendPort);
        } catch (UnknownHostException e) {
           e.printStackTrace();
           System.exit(1);
        }

        System.out.println("Client: sending packet " + i + ".");
        System.out.println("To host: " + sendPacket.getAddress());
        System.out.println("Destination host port: " + sendPacket.getPort());
        System.out.println("Length: " + sendPacket.getLength());
        System.out.println("Containing: ");
        data = sendPacket.getData();
        for (j=0;j<len;j++) {
            System.out.println("byte " + j + " " + data[j]);
        }

        // Send the datagram packet to the server via the send/receive socket.

        try {
           sendReceiveSocket.send(sendPacket);
        } catch (IOException e) {
           e.printStackTrace();
           System.exit(1);
        }

        System.out.println("Client: Packet sent.");

        // Construct a DatagramPacket for receiving packets up
        // to 100 bytes long (the length of the byte array).

        data = new byte[100];
        receivePacket = new DatagramPacket(data, data.length);

        System.out.println("Client: Waiting for packet.");
        try {
           // Block until a datagram is received via sendReceiveSocket.
           sendReceiveSocket.receive(receivePacket);
        } catch(IOException e) {
           e.printStackTrace();
           System.exit(1);
        }

        // Process the received datagram.
        System.out.println("Client: Packet received:");
        System.out.println("From host: " + receivePacket.getAddress());
        System.out.println("Host port: " + receivePacket.getPort());
        System.out.println("Length: " + receivePacket.getLength());
        System.out.println("Containing: ");

        // Get a reference to the data inside the received datagram.
        data = receivePacket.getData();
        for (j=0;j<receivePacket.getLength();j++) {
            System.out.println("byte " + j + " " + data[j]);
        }
        
        // send an ACK after receiving DATA BLOCK 1 0 bytes of data
        
        
        ///////////////////////////////////////////////////////////////////
      
   
        

         
         // if request is a read, read file in 512 byte chunks from server, and send ACK back
         //to server to acknowledge receiving the packet for each packet received until data size <512
         // then we know that the file has ended.
      if(s=="R")
      {
    	  int ackcount = 1;
       for(;;)
       { 
    	  
    	   byte[] data2 = new byte[512];
           receivePacket = new DatagramPacket(data2, data2.length);
    	   try {
			sendReceiveSocket.receive(receivePacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
    	   data2 = receivePacket.getData();
    	   
    	   // Process the received datagram.
           System.out.println("Client: Packet received:");
           System.out.println("From host: " + receivePacket.getAddress());
           System.out.println("Host port: " + receivePacket.getPort());
           System.out.println("Length: " + receivePacket.getLength());
           System.out.println("Containing: ");

           // Get a reference to the data inside the received datagram.
         
           for (int k=0;k<receivePacket.getLength();k++) {
               System.out.println("byte " + k + " " + data2[k]);
           }
           
           //////////////////////////////////////////////////////////
         
          sendAck(ackcount,receivePacket.getPort());
    	   /////////////////////////////////////////////////////////////////////
    	   try {
    		   
   			String content = new String(data2);
    
   			File file = new File(fname);
    
   			// if file doesn't exist, then create it
   			if (!file.exists()) {
   				file.createNewFile();
   			}
    
   			FileWriter fw = new FileWriter(file.getAbsoluteFile());
   			BufferedWriter bw = new BufferedWriter(fw);
   			bw.write(content);
   			bw.close();
    
   			System.out.println("Done");
    
   		} catch (IOException e) {
   			e.printStackTrace();
   		}
    	   if(data2.length<512)
    		   break;
    	   
    	   
    	   ackcount++;
       }

      }//end IF
      else if(s=="W")
      {
    	  
    	  int ackcount =1;
    	  FileInputStream reader = null; 
      	int bytesRead=0;
      	try {
				 reader = new FileInputStream(fname);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
      	byte[] myBuffer = new byte[512], bdata;
      	
      	try {
				while ((bytesRead = reader.read(myBuffer,0,512)) != -1)
				{
				    this.sendPacket = new DatagramPacket(myBuffer,myBuffer.length,this.receivePacket.getAddress(), this.receivePacket.getPort());
				    try {
			            sendReceiveSocket.send(sendPacket);
			         } catch (IOException e) {
			            e.printStackTrace();
			            System.exit(1);
			         }

			         System.out.println("Client: packet sent using port " + sendReceiveSocket.getLocalPort());
			         System.out.println();
			         
			         receiveAck(ackcount,this.receivePacket.getPort());
			         ackcount++;
				}//end while
			} catch (IOException e) {
				
				e.printStackTrace();
			}
      }
   //}// end of loop

      // We're finished, so close the socket.
    //  sendReceiveSocket.close();
   }
   public void sendAck(int ackcount, int sendPort)
   {
	   try {
          	
	         byte[]  ack = {0,4,0,(byte)ackcount};
	               sendPacket = new DatagramPacket(ack, ack.length,
	                                             InetAddress.getLocalHost(), sendPort);
	            } catch (UnknownHostException e) {
	               e.printStackTrace();
	               System.exit(1);
	            }

	            System.out.println("Client: sending ACK  packet to acknowdlege data received ( in 512 byte chunks) " +ackcount + ".");
	            System.out.println("To host: " + sendPacket.getAddress());
	            System.out.println("Destination host port: " + sendPacket.getPort());
	            System.out.println("Length: " + sendPacket.getLength());
	            System.out.println("Containing: ");
	           byte[] data = sendPacket.getData();
	            for (int j=0;j<data.length;j++) {
	                System.out.println("byte " + j + " " + data[j]);
	            }

	            // Send the datagram packet to the server via the send/receive socket.

	            try {
	               sendReceiveSocket.send(sendPacket);
	            } catch (IOException e) {
	               e.printStackTrace();
	               System.exit(1);
	            }

	          System.out.println("Client: Packet sent.");

	           ackcount++;
   }
   public void receiveAck(int ackcount, int receiveport)
   {
	  byte[] data = new byte[100];
       receivePacket = new DatagramPacket(data, data.length);

       System.out.println("Client: Waiting for packet.");
       try {
          // Block until a datagram is received via sendReceiveSocket.
          sendReceiveSocket.receive(receivePacket);
       } catch(IOException e) {
          e.printStackTrace();
          System.exit(1);
       }

       // Process the received datagram.
       System.out.println("Client: Ack # " + ackcount + "received ");
       System.out.println("From host: " + receivePacket.getAddress());
       System.out.println("Host port: " + receiveport);
       System.out.println("Length: " + receivePacket.getLength());
       System.out.println("Containing: ");

       // Get a reference to the data inside the received datagram.
       data = receivePacket.getData();
       for (int j=0;j<receivePacket.getLength();j++) {
           System.out.println("byte " + j + " " + data[j]);
       }
       
	   
   }

   public static void main(String args[])
   {
	 
	   TFTPClient c = new TFTPClient();
	   int x = 1;
	   for(;;)
	   {
	   System.out.println("Read or Write? (R/W) ");
	   Scanner scan1 = new Scanner(System.in), scan2 = new Scanner(System.in);
	  
	  for(;;)
	  {
	   s = scan1.next();
	   
	   if(s.equalsIgnoreCase("R")||s.equalsIgnoreCase("W"))
		   break;
	  }
	  
	   do
	   {
		   
		   System.out.println("Enter file name");
		   fname = scan2.next();
		   
	   }while(!(fname.endsWith(".txt")));
	  
      c.sendAndReceive(x); 
      x++;
	   }//end for loop
   }//end main
}//end class

