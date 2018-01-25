/*
 * Copyright (c) 2016 GE. All Rights Reserved.
 * GE Confidential: Restricted Internal Distribution
 */
package com.ge.predix.eventhub;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig extends AbstractWebSocketMessageBrokerConfigurer {

  public static Map<String, EventHubClient> clients = new HashMap<String, EventHubClient>();

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/service")
        .setAllowedOrigins("*")
        .withSockJS();
  }

  public String getSubscriberName(MessageHeaders headers) {
    GenericMessage message = (GenericMessage) headers.get("simpConnectMessage");
    Map<String, List<String>> messageHeaders = (Map<String, List<String>>) message.getHeaders().get("nativeHeaders");
    return messageHeaders.get("subscriber-name").get(0);
  }

  @EventListener
  // Make Event Hub instance when a new websocket connection starts
  public void onSocketConnected(SessionConnectedEvent event) throws EventHubClientException {
    StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
    String sessionID = sha.getSessionId();
    String subscriberName = getSubscriberName(sha.getMessageHeaders());

    System.out.println("[Connected] " + sessionID + " with name: " + subscriberName);

    // Make new subscribe client for this websocket connection
    EventHubClient client = new EventHubClient(subscriberName, sessionID);

    // Map the Event Hub client with the websocket session id
    clients.put(sha.getSessionId(), client);
  }

  @EventListener
  // Clean up Event Hub instances that the websockets have closed for
  public void onSocketDisconnected(SessionDisconnectEvent event) throws EventHubClientException {
    StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
    String sessionID = sha.getSessionId();

    // Get client
    if (!clients.containsKey(sessionID)) {
      return;
    }

    EventHubClient client = clients.get(sessionID);
    System.out.println("[Disconnected] " + sessionID + " with name: " + client.getSubscriberName());


    // Sends good-bye message
    Map<String, String> tags = new HashMap<String, String>();
    tags.put("name", client.getSubscriberName());
    tags.put("type", EventHubClient.statusType);
    client.getClient().addMessage("goodbye:"+client.getSubscriberName(), client.getSubscriberName() + " has left", tags).flush();

    // Shutdown Event Hub client because websocket connection is closed
    client.getClient().shutdown();
    clients.remove(sessionID);
  }
}