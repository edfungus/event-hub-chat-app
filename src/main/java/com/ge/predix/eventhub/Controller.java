/*
 * Copyright (c) 2016 GE. All Rights Reserved.
 * GE Confidential: Restricted Internal Distribution
 */
package com.ge.predix.eventhub;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {

  @Autowired
  private SimpMessagingTemplate socket;

  @MessageMapping("/connect")
  public void wireSocketToClient(@Payload ChatMessage message, @Headers Map<String, Object> headers) throws EventHubClientException {
    // Get the Event Hub Client
    String sessionID = SimpMessageHeaderAccessor.getSessionId(headers);
    if (!WebSocketConfig.clients.containsKey(sessionID)) {
      return;
    }
    EventHubClient client = WebSocketConfig.clients.get(sessionID);
    String user = (((Map<String, List<String>>) headers.get(SimpMessageHeaderAccessor.NATIVE_HEADERS)).get("user").get(0));
    client.setNameID(user);

    // Setup sender
    client.setMessageSender(socket);

    // Sends welcome message to Event Hub
    Map<String, String> tags = new HashMap<String, String>();
    tags.put("name", client.getSubscriberName());
    tags.put("type", EventHubClient.statusType);
    client.getClient().addMessage("welcome:"+client.getSubscriberName(), client.getSubscriberName() + " has joined", tags).flush();
  }

  @MessageMapping("/message")
  public void messagesFromUser(ChatMessage message, @Headers Map<String, Object> headers) throws EventHubClientException {
    // Get the Event Hub Client
    String sessionID = SimpMessageHeaderAccessor.getSessionId(headers);
    if (!WebSocketConfig.clients.containsKey(sessionID)) {
      return;
    }
    EventHubClient client = WebSocketConfig.clients.get(sessionID);

    // Create Event Hub message
    Map<String, String> tags = new HashMap<String, String>();
    tags.put("name", client.getSubscriberName());
    tags.put("type", EventHubClient.messageType);
    tags.put("origin", client.getNameID());
    String contentLength = ((Map<String, List<String>>) headers.get("nativeHeaders")).get("content-length").get(0);
    String id = sessionID + "-" + contentLength;
    String cleanMessage = Jsoup.parse(message.getMessage()).text();

    // Send message to Event Hub
    System.out.println("From WS to EH id: " + id + ", msg: " + cleanMessage + ", tags: " + tags.toString());
    client.getClient().addMessage(id, cleanMessage, tags).flush();
  }

  @SubscribeMapping("/users")
  public List<String> activeUsers() {
    List<String> users = new ArrayList<String>();

    for (Map.Entry<String, EventHubClient> entry : WebSocketConfig.clients.entrySet()) {
      users.add(entry.getValue().getSubscriberName());
    }

    return users;
  }
}
