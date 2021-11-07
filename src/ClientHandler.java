import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

//This class handles a new client request by starting a new thread
//The class is constructed and then started
//this class catches most of the exceptions when processing a client request
public class ClientHandler implements Runnable {
    private final Socket clientSocket; //The socket that accepted the client and is the link to the client
    private InputStream inStream_;//This is the inputstream from the client socket used to read in the request from the client or the message
    private Scanner Scanner_; //The scanner used in the Request handler class

    // Constructor
    //this is a simple constructor not all of the variables are created
    public ClientHandler(Socket socket) {
        this.clientSocket = socket;

    }

    @Override
    public void run() {
        //read from socket to InputStream object


        try {
            //Try creating an input stream and scanner
            this.inStream_ = clientSocket.getInputStream();
            this.Scanner_ = new Scanner(inStream_);
            //Try creating an output stream that can be passied the request handler
            OutputStream oos = clientSocket.getOutputStream();
            //Construct request handler
            RequestHandler ClientRequest = new RequestHandler(Scanner_);
            //Then handle the request that is stored in its variables
            ClientRequest.HandleRequest();
            //Then send response
            ClientRequest.Respond(oos, inStream_);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            //if the program cannot run and exceptions are still being thrown
            try {
                //if the scanner can't start close the scanner
                if (this.Scanner_ != null) {
                    this.Scanner_.close();
                }
                //if the inputstream can't start close the input stream and the client socket
                if (this.inStream_ != null) {
                    this.inStream_.close();
                    this.clientSocket.close();
                }
                //catch the exceptions for closing those things
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
