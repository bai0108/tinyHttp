# tinyHttp
Implement a http server in Java
## Server.java
Create a children thread to process clients' request: GET or POST.
Base on the request, server create a new process to run cgi script.
### Server workflow
![Alt text](https://github.com/bai0108/tinyHttp/blob/main/img/serverFlowchart.png?raw=true "serverFlowchart")
### Server pipe
![Alt text](https://github.com/bai0108/tinyHttp/blob/main/img/PipeInServer.png?raw=true "pipe")
## Client.java
Simple client program
### Client workflow
![Alt text](https://github.com/bai0108/tinyHttp/blob/main/img/clientFlowChart.png?raw=true "clientFlowchart")
## Client_2.java
Interactive client program
