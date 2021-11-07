"use strict";

function EnterRoom (){
    //Check to make sure a user name has been entered
    //Make sure there is a user name
    if(UserNameField.value == ''){
        UserNameField.value = "<ENTER USER NAME>";
        return;
    }
    //Make sure there is roomname value
    if(RoomNameField.value == ''){
        RoomNameField.value = "<ENTER ROOM NAME>";
        return;
    }
    //make sure all letters are lower case for room name
    let RoomName = RoomNameField.value;
    for(let i = 0; i < RoomName.length; i ++){
        if(RoomName[i] === RoomName[i].toUpperCase() || RoomName[i] === ' ' ){
            RoomNameField.value = "Enter A room with all lowercase letters";
            return;
        }

        }
    ///Send Join room message to server
    let user = UserNameField.value;
    ws.send("Join:" + RoomName + ":" + user);
}
function RequestAvailableRooms() {
    ws.send("GetRoom: NeedRooms");
}

function handleOpen(){
    console.log("Websocket is open");
}

function HandleMessage (event){
    let Message = event.data;
    let data = JSON.parse(Message);
    let command = data.command;
    if(command != "GetRoom"){
        console.log("Recieved non GetRoom Command");
        return;
    }

}

//declaring fields
let UserNameField = document.getElementById('UserName');
let RoomNameField = document.getElementById('RoomName');
let enterRoombtn = document.getElementById('EnterRoom');
//add event listeners
document.addEventListener("load", RequestAvailableRooms);
enterRoombtn.addEventListener('click', EnterRoom)

//websocket stuff
let ws = new WebSocket("ws://localhost:8080");
ws.onopen = handleOpen();

ws.onmessage = HandleMessage;