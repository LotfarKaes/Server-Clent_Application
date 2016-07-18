
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
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.StringTokenizer;
import java.util.concurrent.LinkedBlockingQueue;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;
import com.sun.net.httpserver.Filter;

/*
 * author Md Lotfar Rahman Kaes
 * This class is responsible for allthe connections with the client and client command process
 */
public class ClientHandler extends Thread {

    String logInfo;
    String userName;
    String password;
    String userInfo;
    String dbUserName;
    String dbPassword;
    String dbState;
    private ClientHandler[] clientThread;
    String msgToDevice;
    private int maxClients;
    static String state;
    static String name;
    static String type;
    static String thirdtoken;
    static String secondtoken;
    static String end = "|";
    BufferedReader br;
    PrintWriter pw;
    // String msgFromClient;
    Socket clientSocket = null;
    Connection connection;
    InputStream clientIn = null;
    String msgFromClient;
    String msgFromDeviceHandler;

    public ClientHandler(Socket clientSocket, ClientHandler[] clientThread) {
        this.clientSocket = clientSocket;
        this.clientThread = clientThread;
        maxClients = clientThread.length;
    }

    public ClientHandler(String msgToDevice) {
        this.msgToDevice = msgToDevice;
        // TODO Auto-generated constructor stub
    }

    public void run() {

        int maxClients = this.maxClients;
        ClientHandler[] clientThread = this.clientThread;

        try {
            clientIn = clientSocket.getInputStream();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        br = new BufferedReader(new InputStreamReader(clientIn));
        /////////////////////
        //adding password here
        System.out.println("Waiting for username and password");
        int count = 0;
        do {
            try {
                userInfo = br.readLine();
                //splitCheckCommand(userInfo);
                StringTokenizer userToken = new StringTokenizer(
                        userInfo, "/", false);
                if (userToken.countTokens() == 3) {
                    while (userToken.hasMoreElements()) {

                        logInfo = userToken
                                .nextToken();
                        userName = userToken
                                .nextToken();

                        String passwordToken = userToken
                                .nextToken();

                        if (passwordToken.contains("|")) {
                            StringTokenizer tokenizer1 = new StringTokenizer(
                                    passwordToken, "|",
                                    false);
                            password = tokenizer1
                                    .nextToken();

                        }
                    }
                } //////////////////////////
                else if (userToken.countTokens() == 4) {
                    while (userToken.hasMoreElements()) {

                        logInfo = userToken
                                .nextToken();
                        userName = userToken
                                .nextToken();

                        String passwordToken = userToken
                                .nextToken();

                        String fourthToken = userToken
                                .nextToken();

                        if (passwordToken.contains("|")) {
                            StringTokenizer tokenizer1 = new StringTokenizer(
                                    passwordToken, "|",
                                    false);
                            password = tokenizer1
                                    .nextToken();

                        }
                    }

                }

                if (logInfo.equalsIgnoreCase("Reg")) {
                    splitCheckCommand(userInfo); //finds the registration info and spilts the command
                    PrintWriter pw = new PrintWriter(
                            clientSocket.getOutputStream(), true);
                    try {
                        try {
                            Class.forName("com.mysql.jdbc.Driver");
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        Connection con = (Connection) DriverManager
                                .getConnection(
                                        "jdbc:mysql://localhost:3306/project",
                                        "root", "5976423520");

                        PreparedStatement prest = (PreparedStatement) con
                                .prepareStatement("INSERT INTO usertable (state,email,password) VALUES(?,?,?)");

                        prest.setString(1,
                                "REGISTRY");
                        prest.setString(2, userName);
                        prest.setString(3, password);

                        prest.executeUpdate();

                        pw.println("Reg/GRANT|");
                        System.out.println("register ok");

                    } catch (SQLException ex) {

                    }

                    break;
                }

                if (logInfo.equalsIgnoreCase("Log")) {
                    splitCheckCommand(userInfo);

                    try {
                        try {
                            Class.forName("com.mysql.jdbc.Driver");
                        } catch (Exception e) {

                            e.printStackTrace();
                        }
                        Connection con = (Connection) DriverManager
                                .getConnection(
                                        "jdbc:mysql://localhost:3306/project",
                                        "root", "5976423520");

                        PreparedStatement statement = (PreparedStatement) con
                                .prepareStatement("select password,email from usertable where email=?");
                        statement.setString(1, userName);

                        ResultSet result = statement
                                .executeQuery();

                        System.out.println("Recieved user name " + userName);
                        System.out.println("Recieved password " + password);
                        while (result.next()) {
                            dbPassword = result.getString(1);
                            dbUserName = result.getString("email");

                            System.out.println("Db pass word is" + dbPassword);

                        }

                        if (userName.equals(dbUserName) && password.equals(dbPassword)) {
                            System.out.println("Successfull login");

                            PrintWriter pw1 = new PrintWriter(
                                    clientSocket.getOutputStream(), true);
                            pw1.println("Log/GRANT"
                                    + "");

                            String sql = "UPDATE usertable SET state = ? WHERE email =? AND password =?";
                            PreparedStatement prest = (PreparedStatement) con
                                    .prepareStatement(sql);
                            prest.setString(1,
                                    "LOGON");
                            prest.setString(2, userName);
                            prest.setString(3, password);

                            prest.executeUpdate();
                            break;

                        } else {
                            PrintWriter pw1 = new PrintWriter(
                                    clientSocket.getOutputStream(), true);
                            pw1.println("Log/FAILED|");
                            System.out.println("Enter the password again");
                            count++;
                            if (count == 3) {
                                pw1.println("Timed out");
                                pw1.close();

                                br.close();
                                clientSocket.close();

                            }
                        }

                    } catch (SQLException ex) {

                    }

                    break;

                } ///////////////////////////////////////////////
                else if (!logInfo.equalsIgnoreCase("Log")) {
                    splitCheckCommand(userInfo);
                    try {

                        msgFromClient = splitCommand(userInfo);

                        //msgFromClient = br.readLine();// change in here
                        //splitCheckCommand(msgFromClient);
                        System.out
                                .println("Message received from client :"
                                        + msgFromClient);

                        pw = new PrintWriter(
                                clientSocket.getOutputStream(), true);

                        String ansMsg = msgFromClient;
                        TCPServer.threadMessage.put(ansMsg);

                        String ackMsg = TCPServer.threadMessage2.take();

                        pw.println(ackMsg);//sending the acknowledgement message to client

                        StringTokenizer tokenizer = new StringTokenizer(
                                msgFromClient, "/", false);
                        if (tokenizer.countTokens() == 3
                                || tokenizer.countTokens() == 2) {

                            // start of 3 token
                            if (tokenizer.countTokens() == 3) {
                                while (tokenizer.hasMoreElements()) {

                                    name = tokenizer
                                            .nextToken(); //  LI/ON/riaj|
                                    type = tokenizer
                                            .nextToken();

                                    thirdtoken = tokenizer
                                            .nextToken();

                                    System.out.println("This is type " + type);
                                    if (thirdtoken.contains("|")) {
                                        StringTokenizer tokenizer1 = new StringTokenizer(
                                                thirdtoken, "|",
                                                false);
                                        state = tokenizer1
                                                .nextToken();

                                    }

                                    //adding media player commands in here
                                    if (name.equalsIgnoreCase("M") && (type.equalsIgnoreCase("VOLUMEUP") || type.equalsIgnoreCase("VOLUMEDOWN"))) {
                                        System.out.println("Media player vol u/d commad have been acesssed!!!!");
                                        break;

                                    }

                                    //end of adding media player condition
                                }

                                if (msgFromClient != null
                                        && !msgFromClient
                                        .equalsIgnoreCase(name) && !msgFromClient
                                        .equalsIgnoreCase(type)) {

                                    OutputStream clientOut = clientSocket.getOutputStream();

                                    if (!name.equalsIgnoreCase("M") && !type.equalsIgnoreCase("VOLUMEUP")) {

                                        try {
                                            Class.forName("com.mysql.jdbc.Driver");
                                            Connection con = (Connection) DriverManager
                                                    .getConnection(
                                                            "jdbc:mysql://localhost:3306/project",
                                                            "root", "5976423520");

                                            try {
                                                String sql = "UPDATE state SET device_state = ? WHERE device_symbol =? ";
                                                PreparedStatement prest = (PreparedStatement) con
                                                        .prepareStatement(sql);
                                                prest.setString(1,
                                                        type);
                                                prest.setString(2, name);
                                                //prest.setString(3, type);
                                                prest.executeUpdate();
                                                System.out
                                                        .println("Updating Database....");
                                                PreparedStatement statement = (PreparedStatement) con
                                                        .prepareStatement("select * from state");
                                                ResultSet result = statement
                                                        .executeQuery();
                                                while (result.next()) {
                                                    System.out
                                                            .println(result
                                                                    .getString(1)
                                                                    + "    "
                                                                    + result.getString(3)
                                                            );
                                                }

                                            } catch (SQLException ex) {
                                                System.out
                                                        .println("Error Found!!!:D"
                                                                + ex);

                                            }

                                        } catch (Exception ex) {
                                            System.out
                                                    .println("Error!!!:D"
                                                            + ex);

                                        }
                                    }/////end of media player request

                                }

                            }
                            // end for the 3 token

                            if (tokenizer.countTokens() == 2) {
                                System.out
                                        .println("It's in the 2nd");
                                while (tokenizer.hasMoreElements()) {
                                    name = tokenizer
                                            .nextToken();
                                    secondtoken = tokenizer
                                            .nextToken();
                                    //thirdtoken =tokenizer
                                    //	.nextToken();

                                    System.out
                                            .println("The first token is "
                                                    + name);

                                    if (secondtoken.contains("|")) {
                                        StringTokenizer tokenizer2 = new StringTokenizer(
                                                secondtoken, "|",
                                                false);
                                        state = tokenizer2
                                                .nextToken();

                                    }

                                    if (name.equalsIgnoreCase("M") && (secondtoken.equalsIgnoreCase("REWIND") || secondtoken.equalsIgnoreCase("FORWARD"))) {
                                        System.out.println("Media player forward /rewind commad have been acesssed!!!!");
                                        break;

                                    }

                                    if (name.equalsIgnoreCase("ALARM") && (secondtoken.equalsIgnoreCase("ON"))) {
                                        System.out.println("ALARM is on!!!!");
                                        break;

                                    }
                                    if (name.equalsIgnoreCase("ALARM") && (secondtoken.equalsIgnoreCase("OFF"))) {
                                        System.out.println("ALARM is off!!!!");
                                        break;

                                    }

                                }

                                if (msgFromClient != null
                                        && !msgFromClient
                                        .equalsIgnoreCase(name) && !msgFromClient
                                        .equalsIgnoreCase(type)) {

                                    OutputStream clientOut = clientSocket.getOutputStream();

                                }

                                if (!msgFromClient.equalsIgnoreCase("M/FORWARD|") && !msgFromClient.equalsIgnoreCase("M/REWIND|")
                                        && !msgFromClient.equalsIgnoreCase("ALARM/ON|") && !msgFromClient.equalsIgnoreCase("ALARM/OFF|")) {
                                    try {
                                        Class.forName("com.mysql.jdbc.Driver");
                                        Connection con = (Connection) DriverManager
                                                .getConnection(
                                                        "jdbc:mysql://localhost:3306/project",
                                                        "root", "5976423520");

                                        System.out
                                                .println("Waiting.....");
                                        try {
                                            String sql = "UPDATE state SET device_state = ? WHERE device_symbol =?";
                                            PreparedStatement prest = (PreparedStatement) con
                                                    .prepareStatement(sql);
                                            prest.setString(1,
                                                    state);
                                            prest.setString(2, name);
                                            prest.executeUpdate();
                                            System.out
                                                    .println("Updating Database....");
                                            PreparedStatement statement = (PreparedStatement) con
                                                    .prepareStatement("select device_state,device_name from state");
                                            ResultSet result = statement
                                                    .executeQuery();

                                            while (result.next()) {
                                                System.out
                                                        .println(result
                                                                .getString(1)
                                                                + "    "
                                                                + result.getString(2)
                                                        );
                                            }

                                        } catch (SQLException ex) {
                                            System.out
                                                    .println("Error Found!!!:D"
                                                            + ex);

                                        }

                                    } catch (Exception ex) {
                                        System.out
                                                .println("Error!!!:D"
                                                        + ex);

                                    }

                                }

                            }

                        }
                        
                        catch (IOException e) {
			e.printStackTrace();
                        }
                        catch (Exception e) {
			e.printStackTrace();
			}

                    }

                }catch (IOException e1) {

				e1.printStackTrace();
			}

            }
            while (count < 3);

            try {

                msgFromClient = br.readLine();// change in here

                splitCheckCommand(msgFromClient);

                System.out
                        .println("Message received from client :"
                                + msgFromClient);

                pw = new PrintWriter(
                        clientSocket.getOutputStream(), true);

                String ansMsg = msgFromClient;
                TCPServer.threadMessage.put(ansMsg);

                String ackMsg = TCPServer.threadMessage2.take();
                //ansMsg = msgFromClient+"OK";
                System.out.print("This is I'm going to send" + ackMsg);
                pw.println(ackMsg);

                //if(!msgFromClient.equalsIgnoreCase("M/PAUSE|") &&!msgFromClient.equalsIgnoreCase("M/REWIND|") &&
                //	!msgFromClient.equalsIgnoreCase("M/FORWARD|"))
                StringTokenizer tokenizer = new StringTokenizer(
                        msgFromClient, "/", false);
                if (tokenizer.countTokens() == 3
                        || tokenizer.countTokens() == 2) {

                    // start of 3 token
                    if (tokenizer.countTokens() == 3) {
                        while (tokenizer.hasMoreElements()) {

                            name = tokenizer
                                    .nextToken();
                            type = tokenizer
                                    .nextToken();

                            thirdtoken = tokenizer
                                    .nextToken();

                            if (thirdtoken.contains("|")) {
                                StringTokenizer tokenizer1 = new StringTokenizer(
                                        thirdtoken, "|",
                                        false);
                                state = tokenizer1
                                        .nextToken();

                            }

                            //adding media player conditions from here
                            if (name.equalsIgnoreCase("M") && type.equalsIgnoreCase("VOLUME")) {
                                System.out.println("Media player volumn commad has been acesssed!!!!");
                                break;

                            }

                            //end of adding media player condition
                        }

                        if (msgFromClient != null
                                && !msgFromClient
                                .equalsIgnoreCase(name) && !msgFromClient
                                .equalsIgnoreCase(type)) {

                            OutputStream clientOut = clientSocket.getOutputStream();

                            //String ansMsg2 = msgFromClient+"ACK";
                            //pw.println(ansMsg2);
                            /////conditions for media player starts here
                            if (!name.equalsIgnoreCase("M") && !type.equalsIgnoreCase("VOLUME")) {

                                try {
                                    Class.forName("com.mysql.jdbc.Driver");
                                    Connection con = (Connection) DriverManager
                                            .getConnection(
                                                    "jdbc:mysql://localhost:3306/house",
                                                    "root", "");

                                    try {
                                        String sql = "UPDATE state SET device_state = ? WHERE device_symbol =? AND light_device =?";
                                        PreparedStatement prest = (PreparedStatement) con
                                                .prepareStatement(sql);
                                        prest.setString(1,
                                                state);
                                        prest.setString(2, name);
                                        prest.setString(3, type);
                                        prest.executeUpdate();
                                        System.out
                                                .println("Updating Database....");
                                        PreparedStatement statement = (PreparedStatement) con
                                                .prepareStatement("select * from state");
                                        ResultSet result = statement
                                                .executeQuery();
                                        while (result.next()) {
                                            System.out
                                                    .println(result
                                                            .getString(1)
                                                            + "    "
                                                            + result.getString(3)
                                                    );
                                        }

                                    } catch (SQLException ex) {
                                        System.out
                                                .println("Error Found!!!:D"
                                                        + ex);

                                    }

                                } catch (Exception ex) {
                                    System.out
                                            .println("Error!!!:D"
                                                    + ex);

                                }
                            }/////end of media player request

                        }

                    }
                    // end for the 3 token

                    if (tokenizer.countTokens() == 2) {
                        System.out
                                .println("It's in the 2nd");
                        while (tokenizer.hasMoreElements()) {
                            name = tokenizer
                                    .nextToken();
                            secondtoken = tokenizer
                                    .nextToken();

                            System.out
                                    .println("The first token is "
                                            + name);

                            if (secondtoken.contains("|")) {
                                StringTokenizer tokenizer2 = new StringTokenizer(
                                        secondtoken, "|",
                                        false);
                                state = tokenizer2
                                        .nextToken();

                            }

                            //adding media player commands in here
                            if (name.equalsIgnoreCase("M") && (state.equalsIgnoreCase("REWIND") || state.equalsIgnoreCase("FORWARD"))) {
                                System.out.println("Media player forward /rewind commad have been acesssed!!!!");
                                break;

                            }
                            if (name.equalsIgnoreCase("M") && (state.equalsIgnoreCase("VOLUMEUP") || state.equalsIgnoreCase("VOLUMEDOWN"))) {
                                System.out.println("Media player vol u/d commad have been acesssed!!!!");
                                break;

                            }

                            if (name.equalsIgnoreCase("ALARM") && (state.equalsIgnoreCase("ON"))) {
                                System.out.println("ALARM is on!!!!");
                                break;

                            }
                            if (name.equalsIgnoreCase("ALARM") && (state.equalsIgnoreCase("OFF"))) {
                                System.out.println("ALARM is off!!!!");
                                break;

                            }

                            //end of adding media player command
                        }

                        if (msgFromClient != null
                                && !msgFromClient
                                .equalsIgnoreCase(name) && !msgFromClient
                                .equalsIgnoreCase(type)) {

                            OutputStream clientOut = clientSocket.getOutputStream();

                            //ansMsg=TCPServer.threadMessage2.take();
                            //ansMsg = msgFromClient+"OK";
                            //System.out.print("This is I'm going to send" + ansMsg);
                            //pw.println(ansMsg);
                        }

                        //media player condition starts here
                        if (!msgFromClient.equalsIgnoreCase("M/FORWARD|") && !msgFromClient.equalsIgnoreCase("M/REWIND|")
                                && !msgFromClient.equalsIgnoreCase("ALARM/ON|") && !msgFromClient.equalsIgnoreCase("ALARM/OFF|")) {
                            try {
                                Class.forName("com.mysql.jdbc.Driver");
                                Connection con = (Connection) DriverManager
                                        .getConnection(
                                                "jdbc:mysql://localhost:3306/house",
                                                "root", "");

                                System.out
                                        .println("Waiting.....");
                                try {
                                    String sql = "UPDATE state SET device_state = ? WHERE device_symbol =?";
                                    PreparedStatement prest = (PreparedStatement) con
                                            .prepareStatement(sql);
                                    prest.setString(1,
                                            state);
                                    prest.setString(2, name);
                                    prest.executeUpdate();
                                    System.out
                                            .println("Updating Database....");
                                    PreparedStatement statement = (PreparedStatement) con
                                            .prepareStatement("select device_state,device_name from state");
                                    ResultSet result = statement
                                            .executeQuery();

                                    //String ansMsg1 = msgFromClient+"OK";
                                    //pw.println(ansMsg1);
                                    while (result.next()) {
                                        System.out
                                                .println(result
                                                        .getString(1)
                                                        + "    "
                                                        + result.getString(2)
                                                );
                                    }

                                } catch (SQLException ex) {
                                    System.out
                                            .println("Error Found!!!:D"
                                                    + ex);

                                }

                            } catch (Exception ex) {
                                System.out
                                        .println("Error!!!:D"
                                                + ex);

                            }

                        }//end of media player condition
                    }

                }

                //run =false;
                //}
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
              
        
      
       
    

    

    

    public void userCommandCheck(String command) {

        String first = "";
        String second = "";
        String third = "";
        String fourth = "";
        StringTokenizer commandToken = new StringTokenizer(
                command, "/", false);
        if (commandToken.countTokens() == 4) {
            while (commandToken.hasMoreElements()) {
                first = commandToken.nextToken();
                second = commandToken.nextToken();
                third = commandToken.nextToken();
                fourth = commandToken.nextToken();

            }
            if (fourth.contains("|")) {
                StringTokenizer tokenizer1 = new StringTokenizer(
                        fourth, "|",
                        false);
                fourth = tokenizer1
                        .nextToken();

            }
        }
        //M/VOLUMEUP/12/riaj|
        msgFromClient = first + "/" + second + "/" + third + "|";

    }
    /*
     *@param command takes user command
     *check the log status
     */

    public void splitCheckCommand(String command) {
        String first = "";
        String second = "";
        String third = "";
        String fourth = "";
        StringTokenizer commandToken = new StringTokenizer(
                command, "/", false);

        if (commandToken.countTokens() == 3) {
            while (commandToken.hasMoreElements()) {

                first = commandToken
                        .nextToken();
                second = commandToken
                        .nextToken();

                third = commandToken
                        .nextToken();

                if (third.contains("|")) {
                    StringTokenizer tokenizer1 = new StringTokenizer(
                            third, "|",
                            false);
                    third = tokenizer1
                            .nextToken();

                }

            } //LI/ON/riaj|

            if (!first.equalsIgnoreCase("Log")) {

                try {
                    PrintWriter pw3 = new PrintWriter(
                            clientSocket.getOutputStream(), true);
                    Class.forName("com.mysql.jdbc.Driver");
                    Connection con1 = (Connection) DriverManager
                            .getConnection(
                                    "jdbc:mysql://localhost:3306/project",
                                    "root", "5976423520");
                    PreparedStatement statement = (PreparedStatement) con1
                            .prepareStatement("select state from usertable where userID=?");
                    statement.setString(1, third);

                    ResultSet result2 = statement
                            .executeQuery();
                    while (result2.next()) {
                        dbState = result2.getString(1);
                        System.out.println("The current state  of the user is " + dbState);
                    }
                    if (dbState.equalsIgnoreCase("LOGON")) {
                        msgFromClient = first + "/" + second + "|";

                        System.out.println("Command has been accessed");

                    } else if (dbState.equalsIgnoreCase("LOGO")) {
                        pw3.println("User is not logged  in");
                        clientSocket.close();

                    }
                } catch (Exception ex) {

                }

            }

        } else if (commandToken.countTokens() == 4) {
            while (commandToken.hasMoreElements()) {

                first = commandToken
                        .nextToken();
                second = commandToken
                        .nextToken();

                third = commandToken
                        .nextToken();
                fourth = commandToken
                        .nextToken();

                if (fourth.contains("|")) {
                    StringTokenizer tokenizer1 = new StringTokenizer(
                            fourth, "|",
                            false);
                    fourth = tokenizer1
                            .nextToken();

                }

            } // M/VOLUMEUP/12/riaj|

            if (!first.equalsIgnoreCase("Log")) {

                try {
                    PrintWriter pw3 = new PrintWriter(
                            clientSocket.getOutputStream(), true);
                    Class.forName("com.mysql.jdbc.Driver");
                    Connection con1 = (Connection) DriverManager
                            .getConnection(
                                    "jdbc:mysql://localhost:3306/project",
                                    "root", "5976423520");
                    PreparedStatement statement = (PreparedStatement) con1
                            .prepareStatement("select state from usertable where userID=?");
                    statement.setString(1, fourth);

                    ResultSet result2 = statement
                            .executeQuery();
                    while (result2.next()) {
                        dbState = result2.getString(1);
                        System.out.println("The current state  of the user is " + dbState);
                    }
                    if (dbState.equalsIgnoreCase("LOGON")) {
                        msgFromClient = first + "/" + second + "/" + third + "|";

                        System.out.println("Command has been accessed");

                    } else if (dbState.equalsIgnoreCase("LOGO")) {
                        pw3.println("User is not logged  in");

                    }
                } catch (Exception ex) {

                }

            }

        }

    }
    /*
     *@param takes a command
     *adds different part of command for sending the command to device or house as per the house requirement
     */

    public String splitCommand(String command) {
        String first = "";
        String second = "";
        String third = "";
        String fourth = "";
        StringTokenizer commandToken = new StringTokenizer(
                command, "/", false);
        while (commandToken.hasMoreElements()) {
            if (commandToken.countTokens() == 4) {

                first = commandToken
                        .nextToken();
                second = commandToken
                        .nextToken();

                third = commandToken
                        .nextToken();
                fourth = commandToken
                        .nextToken();

                if (fourth.contains("|")) {
                    StringTokenizer tokenizer1 = new StringTokenizer(
                            fourth, "|",
                            false);
                    fourth = tokenizer1
                            .nextToken();

                }
                msgFromClient = first + "/" + second + "/" + third + "|";

            } else if (commandToken.countTokens() == 3) {
                first = commandToken
                        .nextToken();
                second = commandToken
                        .nextToken();

                third = commandToken
                        .nextToken();

                if (third.contains("|")) {
                    StringTokenizer tokenizer1 = new StringTokenizer(
                            third, "|",
                            false);
                    third = tokenizer1
                            .nextToken();

                }

                msgFromClient = first + "/" + second + "|";
            }

        }

        return msgFromClient;

    }

}
