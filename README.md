# FlowGenerator
FlowGenerator for bigMAC

---------- v1.0 ----------

Main.java
The entrance of the project, including main funtion. We can run java main.Main to excute it.

RawPacketSender.java
The IP packet sender. The main funtion is sendPacket(String[] addrList, byte[] data), which receving the 4-tuple: 
(srcIP, desIP, srcMAC, desMAC), and the data stream. We can use it to send a simple IP packet just with the 4-tuple.

GetRule.java
I haven't implement it since it need the API to get rules. 
