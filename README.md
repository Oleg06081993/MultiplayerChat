#Multiplayer chat (Java)

It's a simple desktop application, of course multithreading, written only on Java 8 without any frameworks. Project structure is built according to MVC pattern (Model-View-Controller).

GUI itself is written on Swing. The connection between server and client is set via HTTP sockets.

The transmission of text messages is implemented using serialized objects, which are transmitted through input- and output object streams.

Messages are processed with listener methods, different types of messages are implemented through enums.