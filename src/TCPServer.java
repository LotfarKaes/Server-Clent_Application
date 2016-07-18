

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.sun.net.httpserver.Filter;

public class TCPServer {
	static ServerSocket server = null;
	static ServerSocket server1 = null;
	static ServerSocket server3 = null;
	private static Socket clientSocket = null;
	private static Socket deviceSocket = null;
	private static Socket messageSocket = null;
	private static int maxDevice = 1;
	private static int maxClients = 100;
	private static int maxMessageCon = 1;
	static DeviceHandler[] deviceThreads = new DeviceHandler[maxDevice];
	static ClientHandler[] clientThreads = new ClientHandler[maxClients];
	static ServerMessageThread[] messageThreads = new ServerMessageThread[maxMessageCon];

	static String msgFromClient;
	static BlockingQueue<String> threadMessage = new LinkedBlockingQueue<String>();
	static LinkedBlockingQueue<String> threadMessage2 = new LinkedBlockingQueue<String>();

	public TCPServer() {

	}

	public static void main(String[] args) {
/*
*port for deviceSocket
*/
		int dPort = 1234;
		if (args.length < 1) {
			System.out.println("Port number for device is ::" + dPort);
		} else {
			dPort = Integer.valueOf(args[0]).intValue();
		}
/*
*port for clientSocket
*/
		int cPort = 3456;
		if (args.length < 1) {
			System.out.println("Port number for client is ::" + cPort);
		} else {
			cPort = Integer.valueOf(args[0]).intValue();
		}

/*
*port for receiving emergency servermessage
*/
		int smPort = 6789;
		if (args.length < 1) {
			System.out.println("Port number for device is ::" + smPort);
		} else {
			dPort = Integer.valueOf(args[0]).intValue();
		}
		/////////////////////7

		try {
			server = new ServerSocket(dPort);
		} catch (IOException e) {
			System.out.println(e);
		}

		try {
			server1 = new ServerSocket(cPort);
		} catch (IOException e) {
			System.out.println(e);
		}
		// server connection for device

		try {
			server3 = new ServerSocket(smPort);
		} catch (IOException e) {
			System.out.println(e);
		}

        /*
		*starts device thread
		*/

		int j = 0;
		for (j = 0; j < deviceThreads.length; j++) {

			try {
				System.out.println("Waiting for house connection.....");
				deviceSocket = server.accept();
				//

				//

				BufferedReader br = new BufferedReader(new InputStreamReader(
						deviceSocket.getInputStream()));
				PrintWriter pw = new PrintWriter(
						deviceSocket.getOutputStream(), true);
				// pw.println("Server has initiated device thread and running");

				String clientDevice = deviceSocket.getInetAddress()
						.getHostAddress();
				int devicePort = deviceSocket.getPort();
				System.out.println("Device host = " + clientDevice
						+ " Device port = " + devicePort);

				System.out.println("Device socket is activated");
				for (int i = 0; i < maxDevice; i++) {
					if (deviceThreads[i] == null) {
						(deviceThreads[i] = new DeviceHandler(deviceSocket,
								deviceThreads)).start();


					}
				}



			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		/*
		* starts the servermessage thread
		*/
		int k = 0;
		for (k = 0; k <messageThreads.length; k++) {

			try {
				System.out.println("Waiting for emergency connection.....");
				messageSocket = server3.accept();
				//

				//

				BufferedReader br = new BufferedReader(new InputStreamReader(
						messageSocket.getInputStream()));
				PrintWriter pw = new PrintWriter(
						messageSocket.getOutputStream(), true);
				// pw.println("Server has initiated device thread and running");

				String clientDevice = messageSocket.getInetAddress()
						.getHostAddress();
				int devicePort = messageSocket.getPort();
				System.out.println("Messageing host = " + clientDevice
						+ " Messaging port = " + devicePort);

				System.out.println("Messaging socket is activated");
				for (int i = 0; i < messageThreads.length; i++) {
					if (messageThreads[i] == null) {
						(messageThreads[i] = new ServerMessageThread(messageSocket,
								messageThreads)).start();

						break;
					}
				}



			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

       /*
		*starts clientThreads
		*each thread for each clent request and client connection
		*/

		while (true) {







			try {

				clientSocket = server1.accept();
				String clientHost = clientSocket.getInetAddress()
						.getHostAddress();
				int clientPort = clientSocket.getPort();
				System.out.println("Client host = " + clientHost
						+ " Client port = " + clientPort);

				System.out.println("Client socket is activated");
				for (int i = 0; i < maxClients; i++) {
					if (clientThreads[i] == null) {
						(clientThreads[i] = new ClientHandler(clientSocket,
								clientThreads)).start();
						System.out.println("Client thread number :" + i);

						break;
					}
				}

			} catch (IOException ex) {
				System.out.println("Error in client connection " + ex);

			}

		}

	}

}