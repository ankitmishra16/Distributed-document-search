<h1> Distributed-document-search</h1>
This repository contains code and JAR file of distributed document search using zookeeper for cluster managment


<h3>Requirements</h3>
<h5> 1. JDK </h5>
<h5> 2. Zookeeper </h5>
<h5> 3. Maven </h5>
<h5> 4. Protoc compliler </h5>

<h3> To run</h3>
Before execution, download zookeeper, and start the zookeeper server using following command, to be run at bin directory of zookeeper<br>
<command>./zkServer.sh start</command><br>
To stop the server, command will be,<br>
<command>./zkServer.sh stop</command><br>
To track all the znodes, you can also run following command<br>
<command>./zkCli.sh</command><br>
Use JARs to run with following commands
<h5> Backend server</h5>
<command>java -jar /your/jar/address port_number</command>
<h5> Front-end </h5>
<command>java -jar /your/jar/address</command>

<h3>Update code</h3>
If you wish to update the directory of books/documents for the application, just go and update CoordinatorNode.java and worker.java in Distributed-document-search/Backend/src/main/java/worker/ for their BOOK_DIRECTORY value


