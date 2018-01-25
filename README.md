# Event Hub Chat App
A chat messenger using Event Hub as the message distributor. This could be a great demo to show off Event Hub's features and help customers understand what "subscriber name" is, message ordering and how to use the Java SDK. (This was built just over a weekend so it could use some refactoring 😁)

### Architecture
The app utilizes the Event Hub Java SDK which talks to Event Hub. The messages are passed to the front end via websockets (specifically sockJS). The frontend is just statically served. The application creates a new Event Hub client per websocket connection identified by the sessionID. 

When a message is received from user over websockets, the name of the user who sent the message and the message type is set in the EH message tags and the message is set in body before sending to Event Hub. When the message is received from Event Hub, the complete message that will show up on the user's screen is constructed and sent via websockets to user.

### Running the app
This can run locally with:
```bash
mvn spring-boot:run
``` 
and the following env variables:
```bash
CLIENT_ID=???
CLIENT_SECRET=????
UAA_INSTANCE_NAME=uaa-instance-name
EVENTHUB_INSTANCE_NAME=event-hub-instance-name
VCAP_SERVICES=<<grab a copy from CF and edit as necessary>>
```
and visit localhost:8080 

This can also run in cloud foundry (assuming UAA and Event Hub is set up). Here is a sample manifest.yml
```yaml
---
applications:
  - name: event-hub-chat
    memory: 512M
    instances: 1
    timeout: 180
    path: target/predix-event-hub-chat-1.0-SNAPSHOT.jar
    buildpack: java-buildpack
    env:
     CLIENT_ID: ????
     CLIENT_SECRET: ?????
     EVENTHUB_INSTANCE_NAME: event-hub-instance-name
     UAA_INSTANCE_NAME: uaa-instance-name
    services:
        - uaa-instance-name
        - event-hub-instance-name
```

### Future Ideas
* Make a chat like guide to show users how Event Hub works and perhaps how they can use the SDK effectively. 
* Adding more chat like features like message ordering via timestamps???
