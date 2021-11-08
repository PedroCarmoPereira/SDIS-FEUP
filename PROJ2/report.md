# Projeto 2 SDIS T7G26

## Overview

Our project implements BACKUP, RESTORE and DELETE. We implement a version of the Chord algorithm, using ThreadPool's and we also address scalability and fault-tolerance, described in more detail on the following subsections.

## Protocols
All of our protocols use TCP and communicate by transmitting serializable classes filled with the needed data. The classes we are using to transmit data are Delete.java, Restore.java and Backup.java. For the BACKUP protocol, the peer that initiates that request will generate a BackupRequest (BackupRequest.java) that contains the file path, the file id used to know what peer should hold that file and the sender ChordNodeInfo, a class used to store all the necessary information of a peer that belongs to the Chord network. 
<br><br>
ChordNodeInfo.java

```
private BigInteger id;
private String internetIp;
private String localIp;
private int port;
private ChordNodeInfo predecessor;
```

This information is used for the receiving peer to know where to send the response (BackupRequestResponse.java)


Using this information, que requesting peer will proceed the backup by sending a Backup serializable class with the needed information of that file to the peer that gave the BackupRequestResponse. 

Backup.java
```
private ChordNodeInfo sender;
private byte[] file;
private String filePath;
private BigInteger fileId;
private String fileName;
```

Use also use RMI for the invocation of a peer's protocols by the TestApp (TestApp.java, line 60).

## Concurrency Design
To handle a large number of peers and requests, we use Thread Pools on various procedures. One of them is the Stabilize (Stabilize.java), it's responsable for keeping each of the peer's finger table updated with the current predecessor and following sucessors. Every 3 seconds, each peer does this procedure by requesting the imediate peer's ChordNodeInfo and use that information to update the finger table.
Also, each peer uses a thread that initiates a TCP Server Socket, listening for every receiving connection and handling the request properly. With this implementation, the handling of requests are non-blocking.
```
@Override
public void run() {
    while(true){
        try {
            Socket privateSocket = this.server.accept();
            Object obj = SocketFunctions.socketListenerTCP(privateSocket);
            Peer.getThreadPool().execute(new ProcessReceived(obj));
        } catch (IOException e) {}   
    }
}
```

## JSSE
Even though our project doesn't use SSLEngine, a big effort was made to implement it and we believe we should at least document our code and procedures. <br>
Our SSLEngine uses TLSv1.2, uses the SSLEngine class created by the set context initiated with the corresponding KeyStores and TrustStores (SSLEngine/SSLCommon.java, line 23). The SSLContext uses selfSigned keys with a 365 day key validity, located in the folder resources.
Even though the functions we developed in a test enviroment are working, we had many obstacles implementing it with the Chord algorithm and structure already implemented in the project.

## Scalability
To maximize the scalability of our project we implemented at the design level the Chord algorithm alongside asynchronous I/O using threads. The implementation of the Chord algorithm syncronizes each peer's finger table every 3 seconds (ChordNode.java, line 27).
We set a limit (m chord parameter set to 10) for the number of peers joining the network, currently suports up to 1024 peers, but theoretically should support more than that.


## Fault-tolerance
Our implementation of the Chord suports a peer leaving the network, by intention and by peer fail by transfering its files to the corresponding sucessor. No link to the network is lost or broken, our Stabilize thread handles that event. 
Our stabilize function works by asking the sucessor node for it's predecessor. If the returned id number is closer than the requesting peer sucessor, whilst not being before the requesting peer's supposed successor in the chord structure. 

We also implemented the method defined in the original Chord specification called FixFingers that periodically checks a finger table entry's successor, if needed, fixes that finger entry and for every time the method is called, it checks the next finger for it's validity until it loops around the table.

Also, to handle more catastrofic events such as several peers losing connection simultaneously, we implemented a method called RecoverSuccessor (Stabilize.java, line 6). If a peer looses the successor, this method checks the finger table, clearing the disabled peers until it finds a working peer, making it it's new successor. In the case that doesn't work, the peer's predecessor becomes it's successor. If the new successor is found, the functions Stabilize and FixFingers naturally reset the network to a stable condition. If a new successor is not found, assumes that the peer is alone in the network, then it becomes the starting peer, reseting the whole network.