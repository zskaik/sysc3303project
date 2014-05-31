// TFTPClient.java 
// This class is the client side for a very simple assignment based on TFTP on
// UDP/IP. The client uses one port and sends a read or write request and gets 
// the appropriate response from the server.  No actual file transfer takes place.   

import java.io.*;
import java.net.*;
import java.util.*;
/**
 * 
 * @author Ziad Skaik
 * @since 2014-05-30
 * @version 2.0
 */
public class TFTPClient
{

   private DatagramPacket sendPacket, receivePacket;
   private DatagramSocket sendSocket, ftSocket; // sendSocket-> Sends requests to Error Simulator, ftSocket-> Handles file transfer
   private static String s;
   private static String fname;
   private int sendPort;

   // we can run in normal (send directly to server) or test
   // (send to simulator) mode
   public static enum Mode { NORMAL, TEST};

   /**
    * This is the constructor for the TFTPClient
    * It is responsible for initializing our two Sockets:
    * sendSocket, which is used to send requests to the Server,
    * and ftSocket which is used in the duration of the file transfer to
    * send/receive Acknowledge packets, and send/receive Data packets.
    */
   public TFTPClient()
   {
	
      try {
         // Construct a datagram socket and bind it to any available
         // port on the local host machine. This socket will be used to
         // send and receive UDP Datagram packets.
         sendSocket = new DatagramSocket();
         // Create this socket ( File Transfer Socket) to be for use in  file transfer.
         ftSocket = new DatagramSocket();
      } catch (SocketException se) {   // Can't create the socket.
         se.printStackTrace();
         System.exit(1);
      }
   }
   /**
    * This method will send the read/write request to the Server(Port 69).
    * Additionally, the read() or write() methods will be called according to the 
    * request type made which will run until the file transfer has been completed. 
    */
   public void sendRequest()
   {
      byte[] msg = new byte[100], // message we send
             fn, // filename as an array of bytes
             md, // mode as an array of bytes
             data; // reply as array of bytes
      String mode; // filename and mode as Strings
      int j, len;
      Mode run = Mode.TEST; // change to NORMAL to send directly to server
      
      if (run==Mode.NORMAL) 
         sendPort = 69;	  // If this is selected, all communication will be with the Error Simulator.
      else
         sendPort = 70;   // If this is selected, all communication will be with the Error Simulator.
      
         System.out.println("Client: creating packet request.");
         
         // Set the first byte of the packet data to 0 as per opcode format requirements
        msg[0] = 0;
        // If the String that the user entered matches the letter "R" or "r" indicating a read request
        // we will set the second byte of the packet data to 1 as per opcode format requirements
        if(s.equalsIgnoreCase("R")) 
           msg[1]=1;
        // If the String that user entered matches the letter "W" or "w" indicating a write request
        // we will set the second byte of the packet data to 1 as per opcode format requirements
        else if(s.equalsIgnoreCase("W")) 
           msg[1]=2;
       
        // convert the user-entered String representing the file name that is
        // to be read/written from/to to bytes
        fn = fname.getBytes();
        
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

        System.out.println("Client: sending packet ");
        if(run==Mode.NORMAL)
        System.out.println("To Server: " + sendPacket.getAddress());
        
        else
        {
        System.out.println("To Error Simulator: " + sendPacket.getAddress());	
        }
        System.out.println("Destination  port: " + sendPacket.getPort());
        System.out.println("Length: " + sendPacket.getLength());
        System.out.println("Containing: \n  ");
        data = sendPacket.getData();
        for (j=0;j<len;j++) 
        System.out.print(data[j]+" ");
        
        // Send the DatagramPacket containing the file transfer request to the server via the send/receive socket.
        try {
           ftSocket.send(sendPacket);
        } catch (IOException e) {
           e.printStackTrace();
           System.exit(1);
        }

        System.out.println("Client: Packet sent.");
       
        // We have sent the file transfer request to the Server, so we close the Socket.
       sendSocket.close(); 
        //If the String which the user entered matches the letter "R" or "r" indicating a read request
        // we will invoke the method read(sendPort) which will initiate the reading process on the client end.
        if(s.equalsIgnoreCase("R"))
        	read();

        //If the String which the user entered matches the letter "W" or "w" indicating a write request
        // we will invoke the method write(sendPort) which will initiate the writing process on the client end.
        else if(s.equalsIgnoreCase("W"))
        	write();
   }
   /**
    * This method will be responsible for reading the file from the Server
    */
   private void read()
   {
	   
	   OutputStream os= null;
	   byte[] data, data_noopcode;
     for(;;)
     { 	 
    	 // receive the data packet with the opcode included and store in byte array "data"
    	 // initialize the data_noopcode array to the same array as data, discarding the opcode
    	 data = receiveDataPacket();data_noopcode = Arrays.copyOfRange(data, 4, data.length-1);
    	 sendAck(data[3]++);
    	 File file = new File("client_files\\" +fname);
    	 
    	 try {
			 os = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.err.println("ERROR! Failed to initialize OutputSteam Object");
		}
    	 //write to the OutputStream
    	try {
			os.write(data_noopcode,0, data_noopcode.length);
			
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Failed to write to the OutputStream");
		}
  	   if(data_noopcode.length<512)
  		  break;
  	   
  	
     }//end INFINITE for loop
     
     System.out.println("Done. File was successfully read");
     try {
		os.close();
	} catch (IOException e) {
		e.printStackTrace();
		System.err.println("ERROR! Failed to close the OutputStream.");
	}
     ftSocket.close(); //Close the Socket used for the file transfer since we are done.
   }
   /**
    * This method will be responsible for handling the writing of the file to the Server
    * Upon receiving an initial ACK0, the file to be transferred will be opened, 512 bytes will be read into a byte array 
    * and inserted into a DatagramPacket to be sent to the Error Simulator which will send 
    * the packet to the Server.
    * This process will repeat until the file is fully transferred. 
    * When the transfer is complete, the ftSocket will be closed.
    */
   private void write()
   {
	   for(;;)
	   {
		   
	   }//end INFINITE for loop
   }
   /**
    * 
    * @param d the Byte array that contains our packet data 
    * (i.e file contents) that are being transferred
    * Sends a packet of file contents to the Error Simulator/Server depending on 
    * mode of operation
    * 
    */
   private void sendDataPacket(Byte[] d)
   {
	   
   }
   /**
    * 
    * @return the data byte array containing the data of the DatagramPacket we receive.
    * This packet will contain file data that is being transferred to 
    * to the Server.
    */
   private byte[] receiveDataPacket()
   {
	byte[] data= new byte[516];
	
	System.out.println("Waiting for data packet");
	DatagramPacket receivePacket = null;
	try {
		receivePacket = new DatagramPacket(data,data.length,InetAddress.getLocalHost(),sendPort);
	} catch (UnknownHostException e) {
		e.printStackTrace();
		System.err.println("ERROR! Failed to create and intialize DatagramPacket for receiving next data packet");
		
	}
	// Receive the data packet.
	// This method call will block until a packet matching the receivePacket object is received on the ftSocket
	try {
		ftSocket.receive(receivePacket);
	} catch (IOException e) {
		e.printStackTrace();
		System.err.println("ERROR! Failed to receive data packet.");
	}
	
	// set the data of the receivePacket equal to the data byte array initialized previously
	data = receivePacket.getData();
	// print a message to indicate that a data packet has been received, along with the corresponding block #
	System.out.println("Received Data packet with block #: " + data[3] );
	System.out.println("Containing: ");
	for(byte b:data)
	System.out.print(b+ " ");
	
	return data;
   }
   /**
    * 
    * @param ackCount the ACK # ( or block #) 
    * that will be inserted into the ACK packet that is to be sent.
    */
   private void sendAck(byte ackCount)
   {	
	  byte[] data = {0,4,0,ackCount};
	  DatagramPacket sendPacket = null;
	  try {
		 sendPacket = new DatagramPacket(data,data.length,InetAddress.getLocalHost(),sendPort);
	} catch (UnknownHostException e) {
		e.printStackTrace();
		System.err.println("ERROR! DatagramPacket failed to be created and initialized");
	}
	  //print what is to be sent
	  System.out.println("Sending ACK with block #: " + ackCount);
	  try {
		ftSocket.send(sendPacket);
	} catch (IOException e) {
		e.printStackTrace();
		System.err.println("ERROR! Datagram ACK packet failed to be sent");
	}
	  
	  System.out.println("ACK successfully sent");
   }
   /**
    * 
    * @return the data byte array containing the data of the DatagramPacket we receive.
    * Receives the ACK with corresponding ACK # ( or block #)
    */
   private byte[] receiveAck()
   {
	   return null;
   }
   /**
    * The main thread of execution for this program
    * This thread will create and initialize a new TFTPClient Object,
    * create a Scanner Object to read user input
    * which will ask the user to enter either "r"/"R" or "w"/"W" 
    * to indicate a read or write request(continues prompt until user correctly enters request String)
    * The user is also prompted to enter the file name that is being read/written from/to.
    * If the user has selected a write request, and has incorrectly entered a file name that 
    * does not exist on the file system, the user will be prompted to re-enter the file name.
    * 
    * Once the user input has been handled, the TFTPClient Object will invoke the sendRequest() method
    * to send a file read/write request to the Server/Error Simulator depending on the mode of operation.
    * @param args
    */
   public static void main(String args[])
   {
	   TFTPClient c = new TFTPClient();  
	   Scanner scanner = new Scanner(System.in);
	  
	  do
	  {
	   System.out.println("Read or Write? (R/W) ");
	   s = scanner.next();
		   
	  }while(!s.equalsIgnoreCase("R")&&!s.equalsIgnoreCase("W"));
	  
	  File file; boolean filexists = true ;
	   do
	   {
		   System.out.println("Enter file name");
		   fname = scanner.next();
		  
		   if(s.equalsIgnoreCase("W"))
		   {
		    	file = new File("client_files\\" +fname);
		    	filexists = file.exists();
		    	// If the file name entered by the user doesn't exist
		    	if(!filexists)
		    	{
		    		System.err.println("File does not exist, please re-enter the file name");
		    	}
		   }
	   }while(!filexists);  // loop while the file name entered doesn't exist
	   // invoke the sendRequest method
      c.sendRequest();
   }//end main
}//end class

