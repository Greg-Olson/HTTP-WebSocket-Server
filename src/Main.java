
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;


public class Main {

    ///In my main class I start a server socket
    //Once the server accepts a new client a new socket is created
    //then a new client handler class is created
    //the client handler class is a runnable for a new thread object which will then handle the clients request
    public static void main(String[] args) {
       //Start a server
        ServerSocket server = null;
        //Try opening a serversocket at port 8080 and if it works reuse the port
        try {
            server = new ServerSocket(8080);
            server.setReuseAddress(true);
            //Once the server socket port is tested the server will listen for client requests

        while (true) {
            //A socket for the new request will accept the connection
            Socket client = server.accept();
            // Print new client connected to server
            System.out.println("New client connected"
                    + client.getInetAddress()
                    .getHostAddress());
            // create a new thread objectthat will handle the client request
            //this clienthandler is a runnable that starts a new thread
            ClientHandler clientSock = new ClientHandler(client);

            //Start the new thread
            new Thread(clientSock).start();
        }

        }
        //this handles the exception if the socket wasnt able to use port 8080
        catch (SocketException e) {
            e.printStackTrace();
        }
        //this handles any io exception from executing the client handler runnable
        catch (IOException e) {
            e.printStackTrace();
        }

    }
}