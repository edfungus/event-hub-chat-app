/*
 * Copyright (c) 2016 GE. All Rights Reserved.
 * GE Confidential: Restricted Internal Distribution
 */
package com.ge.predix.eventhub;

import java.util.List;
import java.util.Map;

import io.grpc.Status;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.ge.predix.eventhub.client.Client;
import com.ge.predix.eventhub.configuration.EventHubConfiguration;
import com.ge.predix.eventhub.configuration.SubscribeConfiguration;

public class EventHubClient {

  public static final String messageType = "message";
  public static final String statusType = "status";
  public static final String socketAddress = "/messages/";

  private SimpMessagingTemplate messageSender;

  private Client client;
  private String subscriberName;
  private String sessionID;
  private String nameID;

  private ChatSubscribeCallback subCallback;
  private ChatPublishCallback pubCallback;

  EventHubClient(String subscriberName, String sessionID) throws EventHubClientException {
    EventHubConfiguration configuration = new EventHubConfiguration.Builder()
        .fromEnvironmentVariables()
        .subscribeConfiguration(new SubscribeConfiguration.Builder()
            .subscriberName(subscriberName)
            .build())
        .build();

    this.subscriberName = subscriberName;
    this.sessionID = sessionID;
    this.client = new Client(configuration);

    // Register callbacks
    this.subCallback = new ChatSubscribeCallback(this.subscriberName);
    this.client.subscribe(subCallback);
    this.pubCallback = new ChatPublishCallback(this.subscriberName);
    this.client.registerPublishCallback(pubCallback);

    System.out.println("Made client with " + subscriberName + " and " + sessionID);

    // Publish creates connection when flush is called. We want to connect now
    client.flush();
  }

  public Client getClient() {
    return client;
  }

  public String getSubscriberName() {
    return this.subscriberName;
  }

  public void setSessionID(String sessionID) {
    this.sessionID = sessionID;
  }

  public String getSessionID() {
    return sessionID;
  }

  public String getNameID() {
    return nameID;
  }

  public void setNameID(String nameID) {
    this.nameID = nameID;
    this.subCallback.setNameID(nameID);
    this.pubCallback.setNameID(nameID);
  }

  public void setMessageSender(SimpMessagingTemplate messageSender) {
    this.messageSender = messageSender;
  }

  class ChatSubscribeCallback implements Client.SubscribeCallback{
    private String subscriberName;
    private String nameID;

    ChatSubscribeCallback(String subscriberName) {
      this.subscriberName = subscriberName;
    }

    public void setNameID(String nameID) {
      this.nameID = nameID;
    }

    // Getting messages from Event Hub
    public void onMessage(Message message) {
      Map<String, String> map = message.getTags();

      // Make sure this message is one of ours
      if (map.containsKey("name") && map.containsKey("type")) {
        String fullMessage = message.getBody().toStringUtf8();

        // send to websocket
        ChatMessage chatMessage = new ChatMessage(map.get("origin"), map.get("name"), fullMessage, map.get("type"));
        System.out.println("On "+this.subscriberName+" client From EH to WS:" + socketAddress + this.nameID + ", from: " + map.get("name") + ", msg:" + fullMessage + ", type: " + map.get("type"));
        messageSender.convertAndSend(socketAddress + this.nameID, chatMessage);
      }
    }

    public void onFailure(Throwable e) {
      // send to websocket
      Status status = Status.fromThrowable(e).getCode().toStatus(); // Get only the status
      ChatMessage chatMessage = new ChatMessage(this.nameID, this.subscriberName, "An error ["+status.toString()+"] occurred, please check server logs.", statusType);
      messageSender.convertAndSend(socketAddress + this.nameID, chatMessage);
    }
  }

  class ChatPublishCallback implements Client.PublishCallback {
    private String subscriberName;
    private String nameID;

    ChatPublishCallback(String subscriberName) {
      this.subscriberName = subscriberName;
    }

    public void setNameID(String nameID) {
      this.nameID = nameID;
    }

    public void onAck(List<Ack> acks){
      // don't really care about these
    }

    public void onFailure(Throwable e){
      // send to websocket
      Status status = Status.fromThrowable(e).getCode().toStatus(); // Get only the status
      ChatMessage chatMessage = new ChatMessage(this.nameID, this.subscriberName, "An error ["+status.toString()+"] occurred, please check server logs.", statusType);
      messageSender.convertAndSend(socketAddress + this.nameID, chatMessage);
    }
  }


}
