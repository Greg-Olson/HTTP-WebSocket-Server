import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.SimpleTimeZone;

//This class ensures that requesting a room, adding a client to a room, and sending message is synchronized
public class Room {
   private static HashMap <String, Room> Rooms_;
   private ArrayList <WebSocketHandler> Clients_;
   ///If there are no rooms create a hash map of rooms
    //else Check if room already exists and return that room or create that room and return it
    public synchronized static Room getRoom (String name){

        boolean Exists;
        if(Rooms_ == null)
        {
            Rooms_ = new HashMap<>();
            Room firstRoom = new Room();
            Rooms_.put(name, firstRoom);
            return Rooms_.get(name);
        }
        else{
            Exists = Rooms_.containsKey(name);
            if(Exists == true){
                return Rooms_.get(name);
            }

            else{
                Room newRoom = new Room ();
                Rooms_.put(name, newRoom);
                return Rooms_.get(name);
            }
        }

    }

    public synchronized void addClient (WebSocketHandler Client){
        if(Clients_ == null){
            Clients_ = new ArrayList<>();
        }
        //Send a message to the joining client its username and room number
        //Send message to all the other clients that someone has joined the room
        this.Clients_.add(Client);
    }

    ///SEND MESSAGE FUNCTION
    //In this class so that only one message can be sent at a time across a thread "Syncrhonized"
   //Send message once to every client
    public synchronized void sendMessage (byte [] Message) throws IOException {
        //for each client in array list of clients send message
        for(int i = 0; i < this.Clients_.size(); i++){
            Clients_.get(i).getCSOutStream().write(Message);
        }
    }

    public synchronized void LeaveRoom (WebSocketHandler leavingWebSocket, byte[] Message) throws IOException {
        //remove client from room
        Clients_.remove(leavingWebSocket);
        System.out.println("The Client list is now" + Clients_);
        //send message to everyone in room that client left
        for(int i = 0; i < this.Clients_.size(); i++){
            Clients_.get(i).getCSOutStream().write(Message);
        }
    }

    public synchronized ArrayList <String> GetRooms (){
        ArrayList <String> CurrentRooms = new ArrayList<>(Rooms_.keySet());
        return CurrentRooms;
    }
}
