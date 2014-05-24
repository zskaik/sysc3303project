// TFTPServer.java 
// This class is the server side of a simple TFTP server based on
// UDP/IP. The server receives a read or write packet from a client and
// sends back the appropriate response without any actual file transfer.
// One socket (69) is used to receive (it stays open) and another for each response. 

import java.io.*;
import java.net.*;
import java.util.*;

public class TFTPServer  {

   // types of requests we can receive
   public static enum Request { READ, WRITE, ERROR};
   Request req; // READ, WRITE or ERROR
   // responses for valid requests
   // UDP datagram packets and sockets used to send / receive
   private DatagramPacket sendPacket, receivePacket;
 //  ArrayList<DatagramPacket> recvpackets = new ArrayList<DatagramPacket>();
   private DatagramSocket receiveSocket, sendSocket;
   byte[] data = null;
   int ackcount =1, x= 70;
   public TFTPServer()
   {
      try {
    	  
         // Construct a datagram socket and bind it to port 69
         // on the local host machine. This socket will be used to
         // receive UDP Datagram packets.
         receiveSocket = new DatagramSocket(69);
      } catch (SocketException se) {
         se.printStackTrace();
         System.exit(1);
      }
   
      Thread serverThread = new Thread(){
   public void run()
   {
	  for(;;)
	  {
		  receive();
		  try {
			Thread.sleep(5000);
		} catch (InterruptedException e1) {

			e1.printStackTrace();
		}
		  new clientConnectionThread(data,receivePacket,req,x).start();
		try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		
		x++;
	  }//end FOR loop
	      
   }
      };serverThread.start();
   }
   public void receive()
   {
	      
	      String filename, mode;
	      int len,j=0, k=0;

	       // loop forever
	         // Construct a DatagramPacket for receiving packets up
	         // to 100 bytes long (the length of the byte array).
	         
	         data = new byte[100];
	         receivePacket = new DatagramPacket(data, data.length);
	         //recvpackets.add(receivePacket);

	         System.out.println("Server: listening  for requests.");
	         // Block until a datagram packet is received from receiveSocket.
	         try {
	            receiveSocket.receive(receivePacket);
	         } catch (IOException e) {
	            e.printStackTrace();
	            System.exit(1);
	         }

	         // Process the received datagram.
	         System.out.println("Server: Packet received:");
	         System.out.println("From host: " + receivePacket.getAddress());
	         System.out.println("Host port: " + receivePacket.getPort());
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
	         System.out.println("asdfaf");
	         System.out.println(received);

	         // If it's a read, send back DATA (03) block 1
	         // If it's a write, send back ACK (04) block 0
	         // Otherwise, ignore it
	         if (data[0]!=0) req = Request.ERROR; // bad
	         else if (data[1]==1) req = Request.READ; // could be read 
	         else if (data[1]==2) req = Request.WRITE; // could be write
	         else req = Request.ERROR; // bad
	         
	         len = receivePacket.getLength();

	         if (req!=Request.ERROR) { // check for filename
	             // search for next all 0 byte
	             for(j=2;j<len;j++) {
	                 if (data[j] == 0) break;
	            }
	            if (j==len) req=Request.ERROR; // didn't find a 0 byte
	            // otherwise, extract filename
	            filename = new String(data,2,j-2);
	         }
	 
	         if(req!=Request.ERROR) { // check for mode
	             // search for next all 0 byte
	             for(k=j+1;k<len;k++) { 
	                 if (data[k] == 0) break;
	            }
	            if (k==len) req=Request.ERROR; // didn't find a 0 byte
	            mode = new String(data,j,k-j-1);
	         }
	         
	         if(k!=len-1) req=Request.ERROR; // other stuff at end of packet
   }
   
  
         /*
         // Create a response.
         if (req==Request.READ) { // for Read it's 0301
            response = readResp;
         } else if (req==Request.WRITE) { // for Write it's 0400
            response = writeResp;
         } else { // for invalid it's 05
            response = invalidResp;
         }
*/
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


   
   class clientConnectionThread extends Thread
   {
	   byte[] data; DatagramPacket receivePacket, sendPacket; Request req;
	   				DatagramSocket receiveSocket;
	   public clientConnectionThread(byte[] d, DatagramPacket rp, Request rq, int recvport)
	   {
		   this.data = d;
		   this.receivePacket = rp;
		   this.req=rq;
		   try {
			this.receiveSocket = new DatagramSocket(recvport);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	   }
	   
	   public void run()
	   {
		   System.out.println("Entered thread");
		   
		 //  while(!Thread.currentThread().isInterrupted())
		   //{
			   
		   System.out.println("Reached start of while loop");
		   byte[]  response = new byte[4],
		             sending;
		
		   final byte[] readResp = {0, 3, 0, 1};
		   final byte[] writeResp = {0, 4, 0, 0};
		   final byte[] invalidResp = {0, 5};
		  
		   
	         if (this.req==Request.READ) { // for Read it's 0301
	             response = readResp;
	          } else if (this.req==Request.WRITE) { // for Write it's 0400
	             response = writeResp;
	          }// else { // for invalid it's 05
	           //  response = invalidResp;
	        // }
		   
	     
	             System.out.println("request");
	             
	            
	             
	             for(byte b:response)
	             {
	            	 System.out.println("" + b);
	             }
	             System.out.println(this.req);
	            
	      
	       
	       // DatagramPacket rp = recvpackets.get(recvpackets.size()-1);
	         
	         if(response==writeResp)
	        	 
	         {
	         this.sendPacket = new DatagramPacket(response, response.length,
	                           this.receivePacket.getAddress(), this.receivePacket.getPort());
	 

	         System.out.println("Created receive packet");
	         System.out.println( "Server: Sending ACK  packet 0 :");
	         System.out.println("To host: " + sendPacket.getAddress());
	         System.out.println("Destination host port: " + sendPacket.getPort());
	         System.out.println("Length: " + sendPacket.getLength());
	         System.out.println("Containing: ");
	         sending = sendPacket.getData();
	         for (int j=0;j<sendPacket.getLength();j++) {
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
	            sendSocket.send(sendPacket);
	         } catch (IOException e) {
	            e.printStackTrace();
	            System.exit(1);
	         }

	         System.out.println("Server: packet sent using port " + sendSocket.getLocalPort());
	         System.out.println();
	         
	     	int ackcount = 1;
       	 for(;;)
            {
         	   byte[] data2 = new byte[512];
                receivePacket = new DatagramPacket(data2, data2.length);
         	   try {
     			this.receiveSocket.receive(receivePacket);
     		} catch (IOException e) {
     			e.printStackTrace();
     		}
         	   data2 = receivePacket.getData();
         	   
         	   // Process the received datagram.
                System.out.println("Server: Packet received:");
                System.out.println("From host: " + receivePacket.getAddress());
                System.out.println("Host port: " + receivePacket.getPort());
                System.out.println("Length: " + receivePacket.getLength());
                System.out.println("Containing: ");

                // Get a reference to the data inside the received datagram.
              
                for (int k=0;k<receivePacket.getLength();k++) {
                    System.out.println("byte " + k + " " + data2[k]);
                }
                
              sendAck(ackcount,this.receivePacket.getPort());
              
              
              
          	
	   	         String s, packetdata = "";
	   			try {
	   				s = "";
	   				packetdata = new String(this.data, "UTF-8");
	   			} catch (UnsupportedEncodingException e1) {
	   				e1.printStackTrace();
	   			}
	   	         //char[] c = packetdata.toCharArray();
	   	       
	   			
	   	       //  System.out.println(packetdata);
	   	        	s= packetdata.substring(2,packetdata.indexOf("o")-1);
	   	        	
          	File file = new File("server_files\\" + s);
            
    		// if file doesn't exist, then create it
    		/*if (!file.exists()) {
    			file.createNewFile();
    		}*/
    			
     			try {
    			OutputStream os = new FileOutputStream(file);
    			
    			try {
    				os.write(data2);
    			} catch (IOException e) {
    				e.printStackTrace();
    			}//end TRY2
    			
    			}//end TRY1
     			catch (FileNotFoundException e) {
    				e.printStackTrace();
    			} 
     			
    		/*FileWriter fw = new FileWriter(file.getAbsoluteFile());
    		BufferedWriter bw = new BufferedWriter(fw);
    		bw.write(content);
    		bw.close();*/
       
    		//System.out.println("Done");
        	   if(data2.length<512)
        		   break;
              
              ackcount++;

            }//end for loop
	         
	         
	         }//END IF

	        
	        else if(response==readResp)
	        {
	        	try {
					sendSocket= new DatagramSocket();
				} catch (SocketException e2) {
					e2.printStackTrace();
				}
	        	// System.out.println("Entered read method");
	        		
	   	         String s, packetdata = "";
	   			try {
	   				s = "";
	   				packetdata = new String(this.data, "UTF-8");
	   			} catch (UnsupportedEncodingException e1) {
	   				e1.printStackTrace();
	   			}
	   	         //char[] c = packetdata.toCharArray();
	   	       
	   		//	System.out.println("Thread");
	   	     //    System.out.println(packetdata);
	   	        	s= packetdata.substring(2,packetdata.indexOf("o")-1);
	   	        	
	   	        	
	        	int ackcount  = 1;
	        	InputStream reader = null; 
	        	int bytesRead=0;

	        	File file = new File("server_files\\" +s);
	        	
	        	try {
					reader = new FileInputStream(file);
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				}
	        	byte[] myBuffer = new byte[(int) file.length()], bdata;
	        	
	        	try {
					do 
					{
						
						if(file.length()>=512)
						bytesRead = reader.read(myBuffer,0,512);
						
						else
						{
							bytesRead = reader.read(myBuffer,0,(int) file.length());
						}
					    this.sendPacket = new DatagramPacket(myBuffer,myBuffer.length,this.receivePacket.getAddress(),this.receivePacket.getPort());
					    try {
					    	
					    	String bufferstr = new String(myBuffer,0,myBuffer.length);
					    	System.out.println(bufferstr);
					    	/*for(byte b:myBuffer)
					    	{
					    	System.out.println(""+ b);
					    	}*/
				            sendSocket.send(this.sendPacket);
				            
			
				         } catch (IOException e) {
				            e.printStackTrace();
				            System.exit(1);
				         }

				         System.out.println("Server: packet sent using port " + sendSocket.getLocalPort());
				         System.out.println("To port " + this.receivePacket.getPort());
				         
				         
				         receiveAck(this.receivePacket.getPort());
				         
					}while(bytesRead!=-1);//end while
				} catch (IOException e) {
					
					e.printStackTrace();
				}
	        	
	        }//END ELSE-IF STATEMENT

		//   }
	      
	   }//end run
   }//end class
   public void sendAck(int ackcount, int sendport)
   {
	   DatagramPacket  sendPacket =null;
	   
	   try {
       	
           byte[]  ack = {0,4,0,(byte)ackcount};
               sendPacket = new DatagramPacket(ack, ack.length,
                                               InetAddress.getLocalHost(), sendport);
              } catch (UnknownHostException e) {
                 e.printStackTrace();
                 System.exit(1);
              }

              System.out.println("Client: sending ACK  packet to acknowdlege data received ( in 512 byte chunks) " +ackcount + ".");
              System.out.println("To host: " + sendPacket.getAddress());
              System.out.println("Destination host port: " + sendport);
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

            System.out.println("Server: ACK Packet sent.");

           
   }
   public void receiveAck( int receiveport)
   {
	   byte[] data = new byte[4];
      DatagramPacket receivePacket = new DatagramPacket(data, data.length);

       System.out.println("Server: Waiting for ACK packet.");
       try {
          // Block until a datagram is received via sendReceiveSocket.
          this.receiveSocket.receive(receivePacket);
       } catch(IOException e) {
          e.printStackTrace();
          System.exit(1);
       }
int a= data[3]&0xFF;
       // Process the received datagram.
       System.out.println("Server: ACK # " + a + "received ");
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

   public static void main( String args[] )
   {
      TFTPServer c = new TFTPServer();
   }
}
