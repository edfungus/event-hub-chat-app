class websockets {
    constructor(endpoint, subscribeAddress, sendAddress, messageCallback, disconnectCallback) {
        this.endpoint = endpoint;
        this.subscribeAddress = subscribeAddress;
        this.sendAddress = sendAddress;
        this.messageCallback = messageCallback;
        this.disconnectCallback = disconnectCallback;

        this.connected = false;
        this.messageType = "message";
    }

    connect(subscriberName, chat) {
        var that = this;

        this.uniqueName = subscriberName + "-" + String(parseInt(Math.random() * (1000 - 0) + 0));
        var headers = {}
        headers["subscriber-name"] = subscriberName;
        headers.user = this.uniqueName;
        
        this.connected = true;    
        
        this.socket = new SockJS(this.endpoint);               
        this.stompClient = Stomp.over(this.socket);   

        this.stompClient.connect(headers, function (frame) {
            that.stompClient.send("/connect", headers, "{}");
            that.stompClient.subscribe("/messages/" + headers.user, function(msg) {
                that.messageCallback(msg);
            });
        }, function(msg) {
            that.disconnectCallback(msg);
        });        
        chat.getElementsByTagName("input")[0].placeholder = "Send as " + subscriberName;
    }

    send(msg){
        var message = {};
        message.message = msg;
        message.type = this.messageType;

        this.stompClient.send(this.sendAddress, {}, JSON.stringify(message));
    }

    disconnect() {
        this.connected = false;        
        this.stompClient.disconnect();
    }

    getUsers(callback) {
        var that = this;
        this.userSocket = new SockJS(this.endpoint);               
        this.userStompClient = Stomp.over(this.userSocket);   
        this.users;

        var subscription = this.stompClient.subscribe("/users", function(msg) {    
            console.log(msg);                
            users = JSON.parse(msg.body);
            console.log(users);      
            callback(users); 
            subscription.unsubscribe();         
        });
    }
}