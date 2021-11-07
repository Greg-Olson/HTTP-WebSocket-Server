"use strict";

function EnterRoomFunc (){
    if(UserNameField.value == ''){
        UserNameField.value = "<ENTER USER NAME>";
    }
    if(RoomNameField.value == ''){
        RoomNameField.value = "<ENTER ROOM NAME>";
    }
    let RoomName = RoomNameField.value;
    for(let i = 0; i < RoomName.length; i ++){
        if(RoomName[i] === RoomName[i].toUpperCase() || RoomName[i] === ' ' ){
            let parent = document.createElement('blockquote');
            let message = document.createTextNode("ENTER A VALID ROOM NAME\r");
            parent.appendChild(message);
            ChatBox.appendChild(parent).scrollIntoView();
            return;
        }

    }

    //Check for User name here in a for loop
    //For now room entered message is going here


    //Send message to server
    sendRoomEnterMessage(RoomName);

}

function sendRoomEnterMessage(nameofRoom){
    let UserName = UserNameField.value;
    ws.send("Join" + ":" + nameofRoom );
}

//function for recieving message that you have entered room
//this is where the print entered room message will be appended to the div

function sendMessage(event){

    if(event.keyCode == 13){
        event.preventDefault();
        let UserName = UserNameField.value;
        let Message = MessageBox.value;
        ws.send(UserName + ":" + Message);
        console.log(UserName)
        MessageBox.value = '';
    }
}



function handleMessageCB(event){
    let serverMessage =event.data;
    let data = JSON.parse(serverMessage);
    //Parse Message
    let command = data.command;
    let user = data.user;
    let theMessage =data.message;



    let userP = document.createElement('p');
    let userName = document.createTextNode(user + ":");
    userP.appendChild(userName);
    ChatBox.appendChild(userP);

    let MessageText = document.createTextNode(theMessage);
    let BQParent = document.createElement('blockquote');
    BQParent.appendChild(MessageText);
    ChatBox.appendChild(BQParent).scrollIntoView();
}

function handleOpen(){
    console.log("Websocket is open");
}

function handleCloseCB(){
    let ChatMessage = document.createTextNode("The server is offline...");
    let myP = document.createElement('p');
    myP.appendChild(ChatMessage);
    ChatBox.appendChild(myP).scrollIntoView();

}
function LeavetheRoom(){
    let UserName = UserNameField.value;
    ws.send("Leave:" + UserName + ": Left the room.");
    window.location.href='/EnterRoom.html'
}

let ChatBox = document.getElementsByClassName('ChatBox')[0];
let MessageBox = document.getElementById("MessageBox");
MessageBox.addEventListener("keydown", sendMessage);
let UserNameField = document.getElementById("UserName");
let RoomNameField = document.getElementById("RoomName");

let EnterRoom = document.getElementById('EnterRoom');
let LeaveRoombtn = document.getElementById('LeaveRoom');
EnterRoom.addEventListener('click', EnterRoomFunc);
LeaveRoombtn.addEventListener('click', LeavetheRoom);


let ws = new WebSocket("ws://localhost:8080");
ws.onopen = handleOpen;
ws.onmessage = handleMessageCB;
ws.onclose = handleCloseCB;

