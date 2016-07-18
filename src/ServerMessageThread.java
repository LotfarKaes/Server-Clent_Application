

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.DriverManager;
import java.sql.ResultSet;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;
/*
* This class is responsible for initiating the server message thread which is running throughout the server runtime
*/

public class ServerMessageThread extends Thread {

	private ServerSocket server;
	private Socket mSocket;
	  private ServerMessageThread[] mThreads;
	  private int maxCon;
	private int port = 6789;
	Javamail mail = new Javamail();

	 public ServerMessageThread(Socket mSocket, ServerMessageThread[] mThreads) {
		    this.mSocket = mSocket;
		    this.mThreads = mThreads;
		    maxCon = mThreads.length;
		  }


	public void run() {
		try {
			PrintWriter	pw = new PrintWriter(
					mSocket.getOutputStream(), true);



			//server = new ServerSocket(port);
			while (true) {
				//Socket client = server.accept();
				//BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
				BufferedReader br = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
				System.out.println("Waiting for emegency message");
				String waring = br.readLine();

				String mailMessage = null;
				System.out.println("The message is " + waring );
				if("A/W/WARNING|".equals(waring)){
					mailMessage = "house  broken -window";
				}else if("A/D/WARNING|".equals(waring)){
					mailMessage = "house  broken -door";
				}
				else if("A/OK|".equals(waring)){
					//mailMessage = "Alarm on";
				System.out.println("ALARM ON!!!");
				}


				if(mailMessage != null){
					//Javamail mail = new Javamail();
					mail.setMailBody(mailMessage);
					userEmail();
					//mail.send("lutfurkayes@yahoo.com");
					//mail.send(userEmail());
					//mail.send("buzhidao321@hotmail.com");
					//mail.send("buzhidao321@hotmail.com");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

/*
* takes user email address saved in the the databse and send mail to that address
*/

	public void userEmail(){
		String mailId="";
		try{
			try {
				Class.forName("com.mysql.jdbc.Driver");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			Connection con = (Connection) DriverManager
					.getConnection(
							"jdbc:mysql://localhost:3306/userDB",
							"root", "");


			PreparedStatement statement = (PreparedStatement) con
					.prepareStatement("select email from usertable");


			ResultSet result = statement
					.executeQuery();


			while(result.next()){
				mailId =result.getString(1);

			System.out.println("Email id are :	"+"\n" +mailId);
			mail.send(mailId);
			}

		}
		catch(Exception ex){


		}


	}
}