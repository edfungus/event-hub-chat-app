/*
 * Copyright (c) 2016 GE. All Rights Reserved.
 * GE Confidential: Restricted Internal Distribution
 */
package com.ge.predix.eventhub;

public class ChatMessage {
  private String origin;
  private String name;
  private String message;
  private String messageType;

  public ChatMessage() {
  }

  public ChatMessage(String origin, String name, String message, String messageType) {
    this.origin = origin;
    this.name = name;
    this.message = message;
    this.messageType = messageType;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getOrigin() {
    return origin;
  }

  public void setOrigin(String origin) {
    this.origin = origin;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getMessageType() {
    return messageType;
  }

  public void setMessageType(String messageType) {
    this.messageType = messageType;
  }

  @Override
  public String toString() {
    return "ChatMessage{" +
        "origin='" + origin + '\'' +
        ", name='" + name + '\'' +
        ", message='" + message + '\'' +
        ", messageType='" + messageType + '\'' +
        '}';
  }
}
