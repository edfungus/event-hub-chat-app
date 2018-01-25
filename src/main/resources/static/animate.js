var chatInstance;
var websocketInstance;


window.onload = function(){
    intro();
    setTimeout(function() {
        initChat();
    }, 1500);
}

var intro = function(){
    var backdrop = document.getElementById("backdrop");
    var html = document.getElementsByTagName("html");
    setTimeout(function() {
        backdrop.classList.add("dropIn");
        setTimeout(function() {
            html[0].classList.add("fill");
        }, 1500);
    }, 500);
}

var initChat = function(){
    var chat = document.getElementById("conversation-container");    
    
    // Make new websocket objects    
    websocketInstance = new websockets("/service", "/messages", "/message", function(msg) {
        // show message on chat when receiving from websockets
        message = JSON.parse(msg.body);
        if (message.messageType == "status") {
            chatInstance.addMessage(message.messageType, message.message);                                
        } else if (message.messageType = "message") {
            if (message.origin == websocketInstance.uniqueName) {
                chatInstance.addMessage("user", message.message);    
            } else {
                newMessage = "<span class=name>" + message.name +":</span> " + message.message;
                chatInstance.addMessage("others", newMessage);   
            }                           
        }                
    }, function(msg) {
        chatInstance.addMessage("status", "Uh oh, websocket disconnected. Please reconnect with \"/connect [username]\"");           
    });

    // Prep some actions and callback for the chat
    var actions = {};
    actions.help = function(){
        chatInstance.addMessage("general",`
        <span class=head>Help</span> <br>
        <br>
        Actions:<br>
        "/connect [username]" \t Connects to chat with username<br>
        "/clear" \t Clears the chat window<br>
        "/users" \t See who is online this instance (must connect first!)<br>
        "/learn" \t Learn more about Event Hub and the Java SDK
        <br><br>
        Notes:<br>
        <ul>
        <li>This chat is PUBLIC</li>
        <li>Beware, messages are not scanned and websocket connections are not encrypted</li>
        <li>User will not get all messages if a username is shared</li>
        <li>Past messages may arrive out of order</li>
        </ul>`);
    }
    actions.learn = function(){
        chatInstance.addMessage("general",`
        <span class=head>Learn</span> <br>
        <br>
        Coming soon! :)`);
    }
    actions.connect = function(name){
        if(websocketInstance.connected) {
            websocketInstance.disconnect();
        }
        
        chatInstance.addMessage("status","Connecting as " + name + " ... ");                            
        websocketInstance.connect(name, chat);
    }
    actions.users = function() {
        users = websocketInstance.getUsers(function(users) {
            chatInstance.addMessage("general",`
            <span class=head>Online Users (this instance)</span><br>` + users.join("<br>"));
        });
    }
    actions.about = function() {
        chatInstance.addMessage("general",`
            <span class=head>About</span> <br>
            <br>
            Event Hub is a pub/sub messaging service on Predix. It is useful for .... This chat demo utilizes Event Hub to deliver messages to multiple clients.<br>
            <br>
            Enter \"/learn\" for more information about Event Hub and the Java SDK`);  
    }
    
    var chatInstance = new Chat(chat, function(msg){
        // send msg to websockets after user input
        websocketInstance.send(msg);
    }, actions);

    // Some fun things for now
    setTimeout(function() {
        chatInstance.addMessage("general","<div class=welcome><b>Welcome to the Predix Event Hub chat demo!</b></div>");
        setTimeout(function() {
              actions.about();                            
            setTimeout(function() {
                chatInstance.addMessage("status","Enter \"/help\" for chat help");                                    
                chatInstance.addMessage("status","Enter \"/connect [username]\" to chat");                    
            } ,1000);                      
        } ,1000);
    } ,500);
}
