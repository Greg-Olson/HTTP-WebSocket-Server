

import java.io.*;

import java.util.*;

//This class handles the request that has been sent
//It is constructed with pieces of the request
//Then the hanndle request function further parses the message and determines
//Then the respond function is used to either send the requested files or start a websocket connection
//If the connection needs to be upgraded to a websocket the websocket class is constructed and used
public class RequestHandler {


    public RequestHandler(Scanner  ServerScanner){

        //This constructor reads in the header that has been recieved via the server scanner
        //and constructs the request handler class
        ClientHeaderCommand_ = ServerScanner.next();

        ClientFileNameRequest_ = ServerScanner.next();
        if(ClientFileNameRequest_.equals("/")){
            ClientFileNameRequest_ = "/index.html";

        }
        ClientHeaderProtocol_ = ServerScanner.next();
        ServerScanner.nextLine();

        String Headerline = ServerScanner.nextLine();

        HashMap <String, String> HeaderFromClient_ = new HashMap<>();
        //Strings for keys and values for the rest of the client sent header
        String HeaderKey = null;
        String HeaderValue = null;

        while(!(Headerline.isEmpty())){
            //Read a headerline in until the next line is null
//                String HeaderLine = new String(ServerScanner.nextLine());
            int colonIndex = 0;
            //Grab the colon index so that we can seperate the keys from the values
            for(int i = 0; i < Headerline.length(); i ++){
                if(Headerline.charAt(i) == ':'){
                    colonIndex = i;
                    break;
                }
            }
            HeaderKey = Headerline.substring(0, colonIndex);
            HeaderValue = Headerline.substring(colonIndex + 2);
            HeaderFromClient_.put(HeaderKey, HeaderValue);
            Headerline = ServerScanner.nextLine();

        }
        //Filling in the fields
        ContentType_ = HeaderFromClient_.get("Content-Type");
        Connection_ = HeaderFromClient_.get("Connection");
        Sec_WebSocket_Key_ = HeaderFromClient_.get("Sec-WebSocket-Key");

    }

    public void HandleRequest () throws Exception{
        if(Sec_WebSocket_Key_ != null){
            isWebSocket_ = true;
        }
        else{
            isWebSocket_ = false;
        }
        File[] Resources = new File("resources/").listFiles();
        ArrayList<String> ResourceFileNames = new ArrayList<String>();
//If this pathname does not denote a directory, then listFiles() returns null.
        for (File file : Resources) {
            if (file.isFile()) {
                ResourceFileNames.add("/" + file.getName());
            }
        }
        if(!ClientHeaderCommand_.equals("GET")){
            throw new Exception();
        }
        else if(isWebSocket_ == false){
                if (ResourceFileNames.contains(ClientFileNameRequest_)) {
                    StatusCode_ = 200;
                    StatusMessage_ = "OK";
                } else {
                    StatusCode_ = 400;
                    StatusMessage_ = "NOT FOUND";

                }
            }
        //update request so that the file can be access through the proper directory
        ClientFileNameRequest_= "./resources" + ClientFileNameRequest_;
        }

    public void Respond (OutputStream OStream, InputStream InStream) throws Exception {
        Ostream_ = OStream;
        //Put the stream into printwriter
        PrintWriter PW = new PrintWriter(OStream);
        //Grab the requested file
        File OutputFile = new File(ClientFileNameRequest_);

        if(!isWebSocket_){//if not a websocket do this

            //Put the stream into printwriter
            //This should be in the try block
            PW.print(ClientHeaderProtocol_ + " " + StatusCode_ + " " + StatusMessage_ + "\r\n");
            PW.println("Server: GregO-Server");
            PW.println("Content-Type :" + ContentType_);
            PW.println("Content-length: " + OutputFile.length());
            PW.println("r\n");
            PW.flush();

            try{
                FileInputStream MyFileInStream = new FileInputStream(OutputFile);
                MyFileInStream.transferTo(OStream);
                OStream.flush();
                OStream.close();
                PW.flush();
                PW.close();

            }
            catch (IOException FileNotWorking){
                FileNotWorking.printStackTrace();
                //This was technically already caught with the if statement Get statement
                System.out.println("This is from the RequestHandler response function");
            }
        }
        else{ //if websocket is true
            
            WebSocketHandler HandleWS = new WebSocketHandler(Sec_WebSocket_Key_);
            HandleWS.RespondHandShake(PW);

            //Loop forever (until client disconnects or the clients sends opcode 8 (opcode 8 is more graceful)
            while(true){
                //Set up the the stream to client

                try{
                    HandleWS.ParseWSMessage(InStream, Ostream_);
                    HandleWS.ServerOutPut();
                }catch (IOException e){
                    e.printStackTrace();
                    System.out.println("Wasn't able to read bytes somewhere");
                }


            }//WHile TRUE LOOP


           }


    }

    //Constructor Variables
    private String ClientHeaderCommand_;
    private String ClientFileNameRequest_;
    private String ClientHeaderProtocol_;
    private String ContentType_;
    private String Connection_;
    public String Sec_WebSocket_Key_;
    //Handle Request Variables
    private int StatusCode_;
    private String StatusMessage_;
    private boolean isWebSocket_;
    private OutputStream Ostream_;
}
