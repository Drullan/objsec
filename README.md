# objsec
# Getting started
  * open 2 terminal windows and run javac Client.java in one of them and javac Server.java in the other
  * run java Server -port number in one of the windows and java Client -port number in the other
  * port number should be the same on client and server.
  * ip address is hardcoded to localhost but can easily be changed in client class and server class.
# Description
  * The client constantly asks for input from the user
  * The server will reply with "reply: original message"
# Description of system
  The project contains 5 classes: Client,EchoClient,Server,ServerThread,Utils
  ## Client
  Starts up the client and asks for input untill the user types "end". 
  ## EchoClient
  Help class for the client class which sends udp packets to the server and returns replies from the server
  ## Server
  Starts up the server
  ## ServerThread
  Waits for connections and replies to connected clients when it recieves data.
  Will terminate if it recieves "end"
  ## Utils
  Got some helper functions for building byte arrays that can be sent as packets.
  Packetsize is defined in this class, use it if you want to change packetsize to not break any code.
  
# Description of packet
  first 4 bytes contains the integer for the size of the encrypted msg. The tail contains the encrypted msg
  
   [0000][..................................]
   
  after decryption the first 4 bytes contains the integer for the size of the decrypted msg, next 4 bytes contains a nonce which is increased after every message, the next bytes contains the actual message,
  last 32 bytes contains the hash of the message.
  
  [0000][0000][............][....................]
  
# Progress of project so far
- [ ] Work on the principle of object security, ????
- [x] provide integrity, this is complete by using hash, no integritity is provided during handshake nor for the encrypted message. Only the original message sent can be garanteed to have integritiy protection.
- [x] provide confidentiality, we use AES encryption
- [x] provide replay protection, sessions are protected by negotiate different keys every time, messages within a session are protected with nonces. 
- [X] use UDP as the way to exchange data between the two parties,
- [x] work on principle of forward security, new keys will be negotiated between every session, However if a key during 
a session is compromised the atacker will be able to read and write during that entire session.
- [x] should have at least two distinct parts; handshake and (protected) data exchange, diffie hellman ecc is used for handshake and aes encryption is used to protect the data.
- [ ] actually work when we test it, ??
- [ ] document the design choices for your implementation
