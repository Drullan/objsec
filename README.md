# objsec
# Getting started
  open 2 terminal windows and run javac Client.java in one of them and javac Server.java in the other
  run java Server <port number> in one of the windows and java Client <port number> in the other
  port number should be the same.
  ip address is hardcoded to localhost but can easily be changed in client class and server class.
# Description
  The client constantly asks for input from the user
  The server will reply with "reply: <original message>"
# Description of system
  The project contains 5 classes: Client,EchoClient,Server,ServerThread,Utils
  ##Client
  Starts up the client and asks for input untill the user types "end". 
  ##EchoClient
  Help class for the client class which sends udp packets to the server and returns replies from the server
  ##Server
  Starts up the server
  ##ServerThread
  Waits for connections and replies to connected clients when it recieves data.
  Will terminate if it recieves "end"
  ##Utils
  Got some helper functions for building byte arrays that can be sent as packets.
  Packetsize is defined in this class, use it if you want to change packetsize to not break any code.
  
# Description of packet
  first 4 bytes contains the integer for the size of the msg + hash, following bytes contains the actual message,
  last 32 bytes contains the hash of the message.
  [0000][............][....................]
  size      msg        sha-256 hash of msg 
  
# Progress of project so far
- [ ] Work on the principle of object security, ????
- [x] provide integrity, this is complete by using hash, but we need to add encryption
- [ ] provide confidentiality, need some encryption
- [ ] provide replay protection, possibly will need some nonces or dates depending on how we implement key exchanges and sessions
- [X] use UDP as the way to exchange data between the two parties,
- [ ] work on principle of forward security
- [ ] should have at least two distinct parts; handshake and (protected) data exchange
- [ ] actually work when we test it, ??
- [ ] document the design choices for your implementation
