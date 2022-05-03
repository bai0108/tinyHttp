/**
 * @program: Reimplementation
 * @description:
 * @author: Rongchao
 * @create: 2022-04-13 22:47
 **/
import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        String host = null;
        int port = 0;
        if(args.length > 2){
            host = args[0];
            port = Integer.parseInt(args[1]);
        }else{
            host = "127.0.0.1";
            port = 8080;
        }

        Socket socket = null;
        BufferedReader reader = null;
        PrintWriter writer = null;
        Scanner s = new Scanner(System.in);
        try{
            socket = new Socket(host, port);
            String message = null;
            reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), "UTF-8"));
            writer = new PrintWriter(
                    socket.getOutputStream(), true);
            //message = "GET /check.exe?a&b&c\r\nHost: 127.0.0.1\r\n\r\n";
            message = "POST /check.exe\r\nHost: 127.0.0.1\r\nContent-Length: 7\r\n\r\nname = aa\r\nbb\r\n";

            writer.println(message);
            writer.flush();
            String st = reader.readLine();
            while (st != null) {
                System.out.println(st);
                st = reader.readLine();
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            if(socket != null){
                System.out.println("Closing socket!");
                try {
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
}