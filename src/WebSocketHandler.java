import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;

//This class handles a websocket request
//It is first constructed with only the websocket key from the request handler
//It then does a handshake response


public class WebSocketHandler {  
    private String HandShakeResponse_;//This is the handshake key that goes back to the websocket server
    private int SocketMessageLength_;//This is the length of the message recieved from the websocket
    private boolean isMask_; //This is if the websockets message is masked
    private String WSOpcode_;
    private DataInputStream WSInStream_; //The instream from the websocket
    private OutputStream CSOutStream_; //The outputStream from the clientsocket

    private String Command_; //the command given from the client request
    private String Data_; //the first part of the message from the client
    private byte[] MessageBack_;//the message to all of the connect clients
    private Room theRoom_; //The room that this websocket belongs too
    private String RoomName_; //The room name

    public WebSocketHandler (String WebSocketSecKey){

        try{
            String MagicString =  WebSocketSecKey + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11" ;
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] hashed = md.digest( MagicString.getBytes() );
            HandShakeResponse_ = Base64.getEncoder().encodeToString( hashed );
        }
        catch(NoSuchAlgorithmException e){
                System.out.println("No Such Algorithm Exception");
                e.printStackTrace();
        }

    }

    public void RespondHandShake (PrintWriter PW){
                    //Send websocket response
                    PW.print("HTTP/1.1 101 Switching Protocols" + "\r\n");
                    PW.print("Connection: Upgrade" + "\r\n");
                    PW.print("Upgrade: websocket" + "\r\n");
                    PW.print("Sec-WebSocket-Accept: " + HandShakeResponse_); //plus the magic thing
                    PW.print("\r\n");
                    PW.print("\r\n");
                    PW.flush();
    }

    public void ParseWSMessage (InputStream InStream, OutputStream OStream) throws IOException {
        WSInStream_ = new DataInputStream(InStream);
        CSOutStream_ = OStream;
        //Parse message from the websocket
        //read the first 2 bytes
        byte[] FirstTwo = WSInStream_.readNBytes(2);
        byte OpCode = (byte) (FirstTwo[0] & 0x0F);
        WSOpcode_ = new String(Byte.toString(OpCode));
        isMask_ = (FirstTwo[1] & 0x080) != 0;
        int LengthClue = FirstTwo[1] & 0x7F;
        int Length = 0;
        if(LengthClue == 127){
            //This will never happen
            //At this point in time I'm not going to deal with it
        }
        else if(LengthClue == 126){
            //This loop is probably rarely used for this messaging app
            byte [] LengthArray = WSInStream_.readNBytes(2);
            for (int i = 0; i < LengthArray.length; i++) {
                Length = (Length *10) + ((LengthArray[i] & 0xff));
            }
        }
        else {
            SocketMessageLength_ = LengthClue;
        }

        if( isMask_ == true){
            byte [] MaskBytes = WSInStream_.readNBytes(4);
            byte [] EncodedPayload = WSInStream_.readNBytes(SocketMessageLength_);
            byte [] Decoded = new byte[EncodedPayload.length];
            for(int i = 0; i<Decoded.length; i ++){
                Decoded[i] = (byte) (EncodedPayload[i]^MaskBytes[i%4]);
            }
            String WSMessage = new String(Decoded, StandardCharsets.UTF_8);
            String [] Pieces = WSMessage.split(":", 2);
            Data_ = Pieces[0];
            Command_ = Pieces[0];
            String Message = Pieces[1];

            if(Data_.equals("Join")){
                RoomName_ = Message;
            }
            else if(Data_.equals("Leave")){
                String [] Pieces2 = Message.split(":", 2);
                Data_ = Pieces2[0];
                Message = Pieces2[1];
            }
            else if(Data_.equals("GetRoom")){
                ArrayList <String> RoomsList = theRoom_.GetRooms();
                StringBuilder sb = new StringBuilder();
                for (String s : RoomsList)
                {
                    sb.append(s);
                    sb.append("\t");
                }
                Message = sb.toString();
                System.out.println(Message);
            }
            System.out.println(Data_);
            System.out.println(Message);

            Message = "{\"user\":\"" + Data_ +"\","+ "\"message\":\"" + Message + "\"}";
            System.out.println(Message);

            byte [] byteMessage = Message.getBytes(StandardCharsets.UTF_8);
            MessageBack_= new byte[2 + byteMessage.length];
            MessageBack_[0] = (byte) 0x81;
            MessageBack_[1] = (byte) byteMessage.length;
            //Move the message back to
            for(int i = 0; i < byteMessage.length; i++ ) {
                MessageBack_[i + 2] = byteMessage[i];
            }

        }
        else{
            System.out.println("Clients message unmasked there is an error");
        }

    }
    public void ServerOutPut() throws IOException {
            if(Data_.equals("Join")){
                theRoom_ = Room.getRoom(this.RoomName_);
                theRoom_.addClient(this);
                //send join room message to all of the members
                //if already joined this shouldn't be runnable
            }
            else if(Command_.equals("Leave")){

                theRoom_.LeaveRoom(this, this.MessageBack_);
            }
            else if(Command_.equals("GetRoom")){

            }
            else {
                /// SEND MESSAGE BACK
                if (SocketMessageLength_ <= 125) {

                    theRoom_.sendMessage(MessageBack_);

                } else if (SocketMessageLength_ == 126) {
                    System.out.println("I didnt handle longer messages so message back to client is wrong");
                }
            }




    }


    public OutputStream getCSOutStream() {
        return CSOutStream_;
    }
}

