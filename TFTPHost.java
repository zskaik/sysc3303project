// TFTPHost.java 
// This class is the beginnings of an error simulator for a simple TFTP server 
// based on UDP/IP. The simulator receives a read or write packet from a client and
// passes it on to the server.  Upon receiving a response, it passes it on to the 
// client.
// One socket (68) is used to receive from the client, and another to send/receive
// from the server.  A new socket is used for each communication back to the client.   

/**
 * @author Ziad Skaik
 * @date 2014-05-21
 * @version 1.0
 */
import java.io.*;
import java.net.*;
import java.util.*;

public class TFTPHost {
   
   // UDP datagram packets and sockets used to send / receive
   private DatagramPacket sendPacket, sendPacket2, receivePacket, receivePacket2;
   private DatagramSocket receiveSocket, sendSocket, sendReceiveSocket;
   private int clientPort =0;
   public TFTPHost()
   {
      try {
         // Construct a datagram socket and bind it to port 68
         // on the local host machine. This socket will be used to
         // receive UDP Datagram packets from clients.
         receiveSocket = new DatagramSocket(68);
         // Construct a datagram socket and bind it to any available
         // port on the local host machine. This socket will be used to
         // send and receive UDP Datagram packets from the server.
         sendReceiveSocket = new DatagramSocket();
      } catch (SocketException se) {
         se.printStackTrace();
         System.exit(1);
      }
      

      
      byte[] data, sending;
      
      int j=0;

      for(;;) { // loop forever
         // Construct a DatagramPacket for receiving packets up
         // to 100 bytes long (the length of the byte array).
         
         data = new byte[100];
         receivePacket = new DatagramPacket(data, data.length);

         System.out.println("Simulator: Waiting for packet.");
         // Block until a datagram packet is received from receiveSocket.
         try {
            receiveSocket.receive(receivePacket);
         } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
         }

         // Process the received datagram.
         System.out.println("Simulator: Packet received:");
         System.out.println("From host: " + receivePacket.getAddress());
         clientPort = receivePacket.getPort();
         System.out.println("Host port: " + clientPort);
         System.out.println("Length: " + receivePacket.getLength());
         System.out.println("Containing: " );

         // Get a reference to the data inside the received datagram.
         data = receivePacket.getData();
         
         // print the bytes
         for (j=0;j<receivePacket.getLength();j++) {
            System.out.println("byte " + j + " " + data[j]);
         }

         // Form a String from the byte array.
         String received = new String(data,0,receivePacket.getLength());
         System.out.println(received);
         
         
         
        
          sendPacket = new DatagramPacket(data, receivePacket.getLength(),
                 receivePacket.getAddress(), 69);
        new connectionManager(data,sendPacket).start();
         
         // Now pass it on to the server (to port 69)
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

         
         // Construct a DatagramPacket for receiving packets up
         // to 100 bytes long (the length of the byte array).

     
         // Construct a datagram packet that is to be sent to a specified port
         // on a specified host.
         // The arguments are:
         //  data - the packet data (a byte array). This is the response.
         //  receivePacket.getLength() - the length of the packet data.
         //     This is the length of the msg we just created.
         //  receivePacket.getAddress() - the Internet address of the
         //     destination host. Since we want to send a packet back to the
         //     client, we extract the address of the machine where the
         //     client is running from the datagram that was sent to us by
         //     the client.
         //  receivePacket.getPort() - the destination port number on the
         //     destination host where the client is running. The client
         //     sends and receives datagrams through the same socket/port,
         //     so we extract the port that the client used to send us the
         //     datagram, and use that as the destination port for the TFTP
         //     packet.



       //  new connectionManagerC(clientPort,sendPacket2).start();
       
      } // end of loop
   }


   class connectionManager extends Thread
   {
	   byte[] data;DatagramPacket sendPacket;
	   public connectionManager (byte[] d, DatagramPacket sp)
	   {
		   this.sendPacket= sp;
	        this.data = d;
	        
	   }
	   public void run()
	   {

	         System.out.println("Simulator: sending packet.");
	         System.out.println("To Client: " + this.sendPacket.getAddress());
	         System.out.println("Destination host port: " + this.sendPacket.getPort());
	         System.out.println("Length: " + this.sendPacket.getLength());
	         System.out.println("Containing: ");
	         this.data = this.sendPacket.getData();
	         for (int j=0;j<this.sendPacket.getLength();j++) {
	             System.out.println("byte " + j + " " + this.data[j]);
	         }

	         // Send the datagram packet to the server via the send/receive socket.

	         try {
	            sendReceiveSocket.send(this.sendPacket);
	         } catch (IOException e) {
	            e.printStackTrace();
	            System.exit(1);
	         }
	         
	         
	         while(true)
	         {
	        	 
	             
	        	 passPacketClient(receivePacket(),clientPort);
	        	 
	        	 passPacketServer(receivePacket());
	        	 
	        	 
	         }
	   }
   }
   /*
   class connectionManager extends Thread
   {
	   int clientPort; DatagramPacket sendPacket;
	   public connectionManagerC (int cp, DatagramPacket sp)
	   {
		   this.clientPort = cp;
		   this.sendPacket =sp;
	   }
	   public void run()
	   {
		    System.out.println( "Simulator: Sending packet:");
	         System.out.println("To host: " + sendPacket2.getAddress());
	         System.out.println("Destination host port: " + this.sendPacket.getPort());
	         System.out.println("Length: " + this.sendPacket.getLength());
	         System.out.println("Containing: ");
	        byte[] sending = this.sendPacket.getData();
	         for (int j=0;j<this.sendPacket.getLength();j++) {
	            System.out.println("byte " + j + " " + sending[j]);
	         }

	         // Send the datagram packet to the client via a new socket.

	         try {
	            // Construct a new datagram socket and bind it to any port
	            // on the local host machine. This socket will be used to
	            // send UDP Datagram packets.
	            sendSocket = new DatagramSocket();
	         } catch (SocketException se) {
	            se.printStackTrace();
	            System.exit(1);
	         }

	         try {
	            sendSocket.send(this.sendPacket);
	         } catch (IOException e) {
	            e.printStackTrace();
	            System.exit(1);
	         }

	         
	    
	         System.out.println("Simulator: packet sent using port " + sendSocket.getLocalPort());
	         System.out.println();

	         // We're finished with this socket, so close it.
	         sendSocket.close();
	   }
   }*/
   public void passPacketServer(DatagramPacket p)
   {
	   byte[] data = p.getData();
	   
	   try {
       	
           
                DatagramPacket sendPacket = new DatagramPacket(data, data.length,
                                               InetAddress.getLocalHost(), 69);
              } catch (UnknownHostException e) {
                 e.printStackTrace();
                 System.exit(1);
              }

              System.out.println("Host: passing packet to Server ");
              System.out.println("To host: " + sendPacket.getAddress());
              System.out.println("Destination host port: " + 69);
              System.out.println("Length: " + sendPacket.getLength());
              System.out.println("Containing: ");
              data = sendPacket.getData();
              for (int j=0;j<data.length;j++) {
                  System.out.println("byte " + j + " " + data[j]);
              }

              // Send the datagram packet to the server via the send/receive socket.

              try {
                 sendSocket.send(sendPacket);
              } catch (IOException e) {
                 e.printStackTrace();
                 System.exit(1);
              }

            System.out.println("Client: Packet sent.");

           
   }
   public void passPacketClient(DatagramPacket p,int cport)
   {
	 byte[] data = p.getData();
       
	 
	 try {
	       	
         
         DatagramPacket sendPacket = new DatagramPacket(data, data.length,
                                        InetAddress.getLocalHost(), cport);
       } catch (UnknownHostException e) {
          e.printStackTrace();
          System.exit(1);
       }

       // Process the received datagram.
       System.out.println("Host: Passing packet to client: ");
       System.out.println("From host: " + receivePacket.getAddress());
       System.out.println("Length: " + receivePacket.getLength());
       System.out.println("Containing: ");
       for (int j=0;j<data.length;j++) {
           System.out.println("byte " + j + " " + data[j]);
       }
	   
       
       

       try {
          sendSocket.send(sendPacket);
       } catch (IOException e) {
          e.printStackTrace();
          System.exit(1);
       }

     System.out.println("Client: Packet sent.");

   }
   public DatagramPacket receivePacket()
   {
	   byte[] data = new byte[512];
	  DatagramPacket receivePacket = new DatagramPacket(data, data.length);
	   System.out.println("Simulator: Waiting for packet.");
       // Block until a datagram packet is received from receiveSocket.
       try {
          receiveSocket.receive(receivePacket);
       } catch (IOException e) {
          e.printStackTrace();
          System.exit(1);
       }

       // Process the received datagram.
       System.out.println("Simulator: Packet received:");
       System.out.println("From host: " + receivePacket.getAddress());
      int  clientPort = receivePacket.getPort();
       System.out.println("Length: " + receivePacket.getLength());
       System.out.println("Containing: " );

       // Get a reference to the data inside the received datagram.
       byte[] d = receivePacket.getData();
       
       // print the bytes
       for (int j=0;j<receivePacket.getLength();j++) {
          System.out.println("byte " + j + " " + d[j]);
       }

       // Form a String from the byte array.
       String received = new String(d,0,receivePacket.getLength());
       System.out.println(received);
       
       return receivePacket;
   }
   public static void main( String args[] )
   {
      TFTPHost s = new TFTPHost();
      
   }
}

