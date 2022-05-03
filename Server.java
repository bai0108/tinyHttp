/**
 * @program: Reimplementation
 * @description:
 * @author: Rongchao
 * @create: 2022-04-13 22:46
 **/

import java.net.*;
import java.io.*;
import java.util.Map;
import java.util.concurrent.*;

public class Server {
    private int port;
    ServerSocket serverSocket;
    ExecutorService connections;
    static String SERVER_STRING = "Server: jdbhttpd/0.1.0\r\n";

    public Server(int MAX_nThreads, int port) {
        this.port = port;
        serverSocket = null;
        connections = Executors.newFixedThreadPool(MAX_nThreads);
    }

    public static void main(String[] args) {
        int port = 8080;
        int max_nThreads = 100;
        if (args.length == 1) {
            try{
                port =  Integer.parseInt(args[0]);
            }catch(NumberFormatException ignored){
                ;
            }
        }
        Server server = new Server(max_nThreads, port);
        server.startServer();
    }

    static class Handler implements Runnable{
        private Socket socket = null;
        private BufferedReader reader;
        private PrintWriter writer;

        public Handler(Socket socket){
            this.socket = socket;
        }

        @Override
        public void run() {
            String method;
            String url;
            String path;
            String query_string = "";
            boolean cgi = false;
            try{
                reader = new BufferedReader(
                        new InputStreamReader(socket.getInputStream(), "UTF-8"));
                writer = new PrintWriter(
                        new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
                String msg;
                msg = getLine(); // first line
                if (msg == null)
                    return;

                String[] split = msg.split(" ");
                if (split.length == 0)
                    return;
                method = split[0];
                if (!method.equalsIgnoreCase("GET") && !method.equals("POST")){
                    unimplemented();
                    return;
                }

                if (method.equals("POST"))
                    cgi = true;

                int i = method.length();
                while (i<msg.length() && msg.charAt(i) == ' ') {
                    i++;
                }
                url = split[1];
                if (method.equalsIgnoreCase("GET")) {
                    query_string = url;
                    i = 0;
                    while (i < query_string.length() && query_string.charAt(i) != '?')
                        i++;
                    if (i < query_string.length() && query_string.charAt(i) == '?')
                        cgi = true;
                    query_string = url.substring(i);
                    url = url.substring(0, i);
                }
                path = "httpdoc" + url;
                if (path.charAt(path.length()-1) == '/')
                    path += "index.html";

                System.out.printf("Info: %s %s %s\n%n", path, url, query_string);

                String[] split_file = path.split("\\.");
                String fileType = "";
                if (split_file.length > 0) {
                    fileType = split_file[split_file.length-1];
                }
                System.out.println(fileType);
                File file = new File(path);
                if (!file.exists()) {
                    while (msg.length() != 0) {
                        msg = getLine();
                        System.out.println(msg);
                    }
                    notFound();
                } else {
                    if (file.isDirectory())
                        path += "index.html";
                    //check the file is executable
                    if (fileType.equals("exe"))
                        cgi = true;
                    if (cgi)
                        executeCgi(path, method, query_string);
                    else
                        serveFile(path);
                }
            }catch(Exception e){
                e.printStackTrace();
            }finally{
                if(socket != null){
                    try {
                        System.out.println("socket " + socket.getRemoteSocketAddress().toString() + " closed.");
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                socket = null;
                if(reader != null){
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                reader = null;
                if(writer != null){
                    writer.close();
                }
                writer = null;
            }
        }

        private String getLine() throws Exception{
            return reader.readLine();
        }

        private void unimplemented(){
            System.out.print("unimplemented");
            writer.print("HTTP/1.0 501 Method Not Implemented\r\n");
            writer.print(SERVER_STRING);
            writer.print("Content-Type: text/html\r\n");
            writer.print("\r\n");
            writer.print("<HTML><HEAD><TITLE>Method Not Implemented\r\n");
            writer.print("</TITLE></HEAD>\r\n");
            writer.print("<BODY><P>HTTP request method not supported.\r\n");
            writer.print("</BODY></HTML>\r\n");
            writer.flush();
        }


        private void notFound(){
            System.out.print("notFound");
            writer.print("HTTP/1.0 404 NOT FOUND\r\n");
            writer.print(SERVER_STRING);
            writer.print("Content-Type: text/html\r\n");
            writer.print("\r\n");
            writer.print("<HTML><TITLE>Not Found</TITLE>\r\n");
            writer.print("<BODY><P>The server could not fulfill\r\n");
            writer.print("your request because the resource specified\r\n");
            writer.print("is unavailable or nonexistent.\r\n");
            writer.print("</BODY></HTML>\r\n");
            writer.flush();
        }

        void badRequest() throws IOException {

            System.out.print("badRequest");
            writer.print("HTTP/1.0 400 BAD REQUEST\r\n");
            writer.print(SERVER_STRING);
            writer.print("Content-Type: text/html\r\n");
            writer.print("\r\n");
            writer.print("<HTML><HEAD><TITLE>Method Not Implemented\r\n");
            writer.print("</TITLE></HEAD>\r\n");
            writer.print("<P>Your browser sent a bad request,");
            writer.print("such as a POST without a Content-Length.\\r\\n");
            writer.print("</BODY></HTML>\r\n");
            writer.flush();
        }

        /*
        execute cgi program
        Parameters: path: cgi program path
                    method: GET or POST
                    queryString: user sent variables
         */
        void executeCgi(String path, String method, String queryString) throws Exception {
            System.out.println("executeCgi func");
            int content_length = -1;

            if (method.equals("GET")) { // read and discard headers for GET
                while(getLine().length() > 0);
            } else if (method.equals("POST")) { // keep content_length and discard others for POST
                String oneLine = getLine();
                while (oneLine.length() > 0) {
                    String[] attrs = oneLine.split(": ");
                    if (attrs[0].equals("Content-Length"))
                        content_length = Integer.parseInt(attrs[1]);
                    oneLine = getLine();
                }

                if (content_length == -1) {
                    badRequest();
                    return;
                }
            } else {
                /*HEAD or other*/
            }
            ProcessBuilder builder ;

            System.out.println("path = " + path + ", method = " + method + ", queryString = " + queryString);
            builder = new ProcessBuilder(path);
            Process process= null;

            /*
            set environment var
             */
            Map<String, String> env = builder.environment();
            env.put("REQUEST_METHOD", method);
            env.put("QUERY_STRING", queryString);
            env.put("CONTENT_LENGTH", String.valueOf(content_length));
            //check runnable
            try {
                process = builder.start();
            } catch (IOException e) {
                badRequest();
                e.printStackTrace();
            }


            OutputStream stdin = process.getOutputStream();
            InputStream stdout = process.getInputStream();
            /*
            connect processes by pipe
             */
            BufferedReader process_reader = new BufferedReader(new InputStreamReader(stdout));
            BufferedWriter process_writer = new BufferedWriter(new OutputStreamWriter(stdin));
            /*
            get input data: read from socket
            POST
            */
            if (content_length != -1) {
                String input_data = getLine();
                while (input_data != "" && input_data.length() > 0){
                    process_writer.write(input_data+"\n");
                    process_writer.flush();
                    input_data = getLine();
                }
                process_writer.close();// close first and then read
            }

            headers();
            /*
            get child process output and sent to client
             */

            String output = process_reader.readLine(); // if not close writer, cannot read
            while (output != null ) {
                System.out.println(output);
                writer.println(output);
                writer.flush();
                output = process_reader.readLine();
            }
            process_reader.close();
            process.waitFor();
            process.destroy();
        }

        /*
        Parameter: filename got from url
         */
        private void serveFile(String filename){
            BufferedReader reader;
            String msg = "0";

            try {
                while (msg.length() != 0) {
                    msg = getLine();
                    System.out.println(msg);
                }
                reader = new BufferedReader(new FileReader(filename));
                if(reader == null)
                    notFound();
                else {
                    headers();
                    cat(reader);
                }
                reader.close();
            }catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void cat(BufferedReader reader) throws IOException {
        /*
        check socket is created
         */
            if (socket == null) {
                System.out.println("Connection is not already.");
                return;
            }
        /*
        read file line by line, send line by line
         */
            String line = reader.readLine();
            System.out.println(line);
            while(line != null) {
                writer.println(line);
                writer.flush();
                line = reader.readLine();
            }
        }

        private void headers() throws IOException {
            if (socket == null) {
                System.out.println("Connection is not already.");
                return;
            }
        /*
        getFileType(filename);
         */
            writer.print("HTTP/1.0 200 OK\r\n");
            writer.print(SERVER_STRING);
            writer.print("Content-type: text/html\r\n");
            writer.print("\r\n");
            writer.flush();
        }

    }

    private void startServer() {
        try{
            serverSocket = new ServerSocket(port);
            System.out.printf("server started!\nport: %d\n%n",
                    serverSocket.getLocalPort());
            while(true){
                Socket clientSocket = serverSocket.accept();

                connections.execute(new Handler(clientSocket));
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            if(serverSocket != null){
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            serverSocket = null;
        }
    }

}
