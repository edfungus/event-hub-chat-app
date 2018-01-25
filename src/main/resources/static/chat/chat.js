class Chat {
    constructor(container, userInputCallback, customActions, classes, options) {
        this.classes = classes != null ? classes : this.defaultClasses();
        this.options = options != null ? options : this.defaultOptions();

        this.container = this.addClasses(container, [this.classes.container]);
        customActions = customActions!= null ? customActions : [];
        this.actions = new Actions(this, this.options.triggerChar, userInputCallback, customActions);

        this.setupInput();
        this.setupConversation();
    }

    setupInput() {
        this.input = this.addClasses(document.createElement("input"), [this.classes.input]);
        this.container.appendChild(this.input);
        var that = this;

        document.onkeydown = function(e) {
            that.input.focus(); // for now map everything to input                     
        };

        this.input.addEventListener("keypress", function(e){
            var key = e.which || e.keyCode;
            var value = that.input.value.replace(/^(\s)|(\s+)$/g, ""); // removes whitespace before and after
            if (key === 13 && value != "") { 
                that.actions.parse(value);
            }
        });
        
        setTimeout(function() {
            that.addClasses(that.input, [that.classes.inputDropIn]);  
            that.input.focus();               
        },1);
    }

    setupConversation() {
        this.conversation = this.addClasses(document.createElement("div"), [this.classes.conversation]);
        this.container.appendChild(this.conversation);

        var top = document.createElement("div")
        top.style.height = this.options.conversationTopMargin;
        this.bottom = document.createElement("div")
        this.bottom.style.height = this.options.conversationBottomMargin;
        this.addClasses(this.bottom, ["bottom-last-chat-element"]);

        this.conversation.appendChild(top);
        this.conversation.appendChild(this.bottom);
    }
    
    addMessage(messageType, text) {
        var message = this.addClasses(document.createElement("div"), [this.classes.message, this.classes.messageTypes[messageType]]);
        var clearfix = this.addClasses(document.createElement("div"), [this.classes.clearfix]);

        this.conversation.insertBefore(message, this.bottom);
        this.conversation.insertBefore(clearfix, this.bottom);

        var that = this;
        setTimeout(function() {
            that.addClasses(message, [that.classes.messageShow]);
            message.innerHTML = text;            
            that.conversation.scrollTop = that.conversation.scrollHeight;                 
        },1);
    }

    clearMessages() {
        var messages = this.conversation.getElementsByClassName(this.classes.message);
        var clearfix = this.conversation.getElementsByClassName(this.classes.clearfix);
        
        while(messages.length > 0) {
            this.conversation.removeChild(messages[0]);
        }
        while(clearfix.length > 0) {
            this.conversation.removeChild(clearfix[0]);
        }
    }

    clearInput() {
        this.input.value = "";
    }

    addClasses(element, classList) {
        for(var i = 0; i < classList.length; i++) {
            element.classList.add(classList[i]);
        }
        return element;
    }

    defaultClasses() {
        return {
            container: "conversation-container",
            conversation: "conversation",
            input: "input",
            inputDropIn: "dropIn",
            clearfix: "clearfix",
            message: "msg",
            messageShow: "show",
            messageTypes: {
                user: "user",
                others: "others",
                status: "status",
                general: "general"
            }
        };
    } 
    defaultOptions() {
        return {
            conversationTopMargin: "10px",
            conversationBottomMargin: "85px",
            defaultMessageType: "user",
            triggerChar: "/"
        };
    } 
}

class Actions {
    constructor(chat, triggerChar, userInputCallback, actions) {
        this.chat = chat;
        this.triggerChar = triggerChar;
        this.actionMessageType = "general";
        this.userInputCallback = userInputCallback;
        this.actions = actions;

        // Put in some default actions
        var clear = function() {
            chat.clearMessages(); 
        };
        this.actions.clear = clear;
    }

    parse(message) {
        if(message[0] != this.triggerChar) {
            if (this.userInputCallback != null) {
                this.userInputCallback(message);
            } else {
                this.chat.addMessage(this.chat.options.defaultMessageType, message);                
            }
            this.chat.clearInput();                    
            return;
        }

        var command = message.substring(1).split(" ")[0]
        if(this.actions[command] == null) {
            this.chat.addMessage(this.actionMessageType, "Unknown command: " + command);
            this.chat.clearInput();                    
            return;
        }

        var params= message.substring(1).split(" ");
        params.shift();
        params = params.join(" ");

        this.actions[command](params);
        this.chat.clearInput();        
    }
}