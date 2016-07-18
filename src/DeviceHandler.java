

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/*
* This class is responsible for all the device connection and device command process
*/
public class DeviceHandler extends Thread {

	 private Socket deviceSocket ;
	  private DeviceHandler[] threads;
	  private int maxDevice;
	  BufferedReader br ;
	 // PrintWriter pw ;
	  static String state;
		static String name;
		static String type;
		static String thirdtoken;
		static String secondtoken;
		static String end = "|";
		String ansMsg;
	String messageFromClientHandler;
		 InputStream clientIn;
	  String messageToClientHandler;
	  //String msgFromClientHandler;
	  PrintWriter pw;


	 // ClientHandler ch = new ClientHandler(messageFromClientHandler);

	  public DeviceHandler(Socket clientSocket, DeviceHandler[] threads) {
		    this.deviceSocket = clientSocket;
		    this.threads = threads;
		    maxDevice = threads.length;
		  }


	  public DeviceHandler(String messageToClientHandler) {
		  this.messageToClientHandler=messageToClientHandler;
		// TODO Auto-generated constructor stub
	}
	 // public DeviceHandler( ClientHandler ch) {
		//this.ch= ch;
		// TODO Auto-generated constructor stub
	//}



	public void run(){

		  int maxClients = this.maxDevice;
		  DeviceHandler[] threads = this.threads;



			try {

				//clientIn = clientSocket.getInputStream();
				br = new BufferedReader(new InputStreamReader(deviceSocket.getInputStream()));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			/*
			 String messageFromDevice;
			try {
				messageFromDevice = br.readLine();
				System.out.println("Message received from Device = "+ messageFromDevice);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			  */
		//PrintWriter pw;
		try {
			pw = new PrintWriter(deviceSocket.getOutputStream(), true);
			//pw.println("HD");

			System.out.println("-----------------------------------------------------");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		  while(true){
			  try{
				  //making change in here



				 //changes ends in here

		//String ansMsg = messageFromDevice+"OK";


		//pw1.write(ansMsg);
		//pw.println(ansMsg);
		System.out.println("Waiting for client command");
		//String messageFromClientHandler = ch.getMessage();


          String msgFromClientHandler = TCPServer.threadMessage.take();

            System.out.println("This is from client handler" +  msgFromClientHandler );

           //adding tokenizer in here
            String deviceType = splitCommand(msgFromClientHandler);

            if(deviceType.equalsIgnoreCase("LI") || deviceType.equalsIgnoreCase("LO")|| deviceType.equalsIgnoreCase("F") ||deviceType.equalsIgnoreCase("TI")
            		||deviceType.equalsIgnoreCase("TO")||deviceType.equalsIgnoreCase("ALARM")){
            	 System.out.println("Sendig msg to HD device");
                 pw.println("HD/"+msgFromClientHandler);

            }
            else {
            	 System.out.println("Sendig msg to simulated device");
                 pw.println("SS/"+msgFromClientHandler);
            	// pw.println(msgFromClientHandler);

            }
            //end of tokennizer



            System.out.println("Waiting for Device ACK");

            try {
				String ackMsg = br.readLine();
				System.out.println("This is ack message "+ ackMsg);
				System.out.println("Sending ack to client");
				TCPServer.threadMessage2.put(ackMsg);




			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		//String ansMsg = msgFromClientHandler;








		  }

		  catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		  }






	  }
/*
* @param command takes the command and checks the device type
*/

	public String splitCommand(String command){
		StringTokenizer tokenizer = new StringTokenizer(
				command, "/", false);


		type = tokenizer.nextToken();


System.out.println("This is the device type" + type);




		return type;


	}





}