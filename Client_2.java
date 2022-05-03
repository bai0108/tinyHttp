/**
 * @program: Reimplementation
 * @description:
 * @author: Rongchao
 * @create: 2022-04-13 22:47
 **/
import java.net.*;
import java.io.*;
import java.util.*;

class Client2 {
    private List<List<String>> Windows;
    private Deque<String> history;
    private static final int MAX_HISTORY_NUM = 5;
    private static final int defaultPort = 8080;
    private static final String defaultHost = "127.0.0.1";
    private static final String staticHeader = "Host: www.hootina.org\r\nAccept-Encoding: gzip, deflate\r\nAccept-Language: zh-CN,zh;q=0.9,en;q=0.8\r\n";
    private int currPosi;
    private static final String defaultDownloadPath = "./download/";

    public static void main(String[] args) {
        Client2 client = new Client2();
        client.startup(args);

    }

    public Client2() {
        history = new LinkedList<>();
        currPosi = -1;
        Windows = new LinkedList<>();
    }

    private void startup(String[] args) {
        String host;
        StringBuilder msg = new StringBuilder();
        String cmd;
        String url;
        String data;
        int port = defaultPort;
        Scanner input = new Scanner(System.in);
        while (true) {
            System.out.print("> ");
            cmd = input.nextLine();
            if (cmd.equals("exit")) {
                break;
            }else if (cmd.equalsIgnoreCase("get") || cmd.equalsIgnoreCase("post")) {
                msg.setLength(0);
                msg.append(cmd.toUpperCase()).append(" ");
                System.out.print("What's your host address?\n> ");
                host = input.nextLine();
                if (host.length() == 0)
                    host = defaultHost;

                do {
                    System.out.print("What's your url?\n> ");
                    url = input.nextLine();
                }while (!checkUrl(cmd, url));
                System.out.print("Your url is valid now.\n");
                msg.append(url);
                msg2history(msg.toString());
                msg.append(" HTTP/1.0\r\n").append(staticHeader);

                if (cmd.equals("post")) {
                    System.out.print("Do you have andy data to transmit?\n> ");
                    data = input.nextLine();
                    msg.append(String.format("Content-Length: %d\r\n", data.length()));
                    msg.append("\r\n");
                    msg.append(data);
                    msg.append("\r");
                    msg.append("\r");
                }else
                    msg.append("\r\n");
                requestHandler(msg.toString(), host, port);

            }else if (cmd.equals("switch")) {
                System.out.printf("There are/is %d window(s). Select the window please.\n> ", Windows.size());
                int num = Integer.parseInt(input.nextLine());
                switchWindow(num);
            }else if (cmd.equals("close")) {
                System.out.printf("There are/is %d window(s). You are at %d. Select the window please.\n> ", Windows.size(), currPosi);
                int num = Integer.parseInt(input.nextLine());
                closeWindow(num);
            }else if (cmd.equals("download")) {
                System.out.printf("Enter your path please.\n> ", Windows.size());
                String path = input.nextLine();
                download(defaultDownloadPath + path);
            }else if (cmd.equals("history")) {
                returnHistory();
            }else
                System.out.println("I am sorry. Your command is not valid.");
        }
        System.out.println("Goodbye!");
    }

    private boolean checkUrl(String cmd, String url) {
        if (cmd.equals("post")) {
            for (int i=0; i<url.length(); i++) {
                if (url.charAt(i) == '?') {
                    return false;
                }
            }
        }
        return true;
    }

    private void msg2history(String msg) {
        if (history.size() >= MAX_HISTORY_NUM)
            history.poll();
        history.offer(msg);
    }

    private void requestHandler(String msg, String host, int port) {
        System.out.println("Your http request message is:\n" + msg);
        ArrayList<String> win = new ArrayList<>();
        Socket socket = null;
        BufferedReader reader = null;
        PrintWriter writer = null;
        try {
            socket = new Socket(host, port);
            reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), "UTF-8"));
            writer = new PrintWriter(
                    socket.getOutputStream(), true);
            writer.print(msg);
            writer.flush();
            currPosi++;
            System.out.println(String.format("*********************************  %d  *************************************", currPosi));
            String response = reader.readLine();
            boolean isBody = false;
            while (response != null) {
                System.out.println(response);
                if (response.equals(""))
                    isBody = true;
                if (isBody)
                    win.add(response);
                response = reader.readLine();
            }
            Windows.add(currPosi, win);
            System.out.println(String.format("*********************************  %d  *************************************", currPosi));
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

    private void showWindow() {
        List<String> window = Windows.get(currPosi);
        System.out.println(String.format("*********************************  %d  *************************************", currPosi));
        for (String s : window) {
            System.out.println(s);
        }
        System.out.println(String.format("*********************************  %d  *************************************", currPosi));
    }

    private void switchWindow(int num) {
        if (num >= Windows.size()) {
            System.out.printf("Window %d doesn't appear? %n", num);
            return;
        }
        currPosi = num;
        showWindow();
    }

    private void closeWindow(int num) {
        if (num >= Windows.size()) {
            System.out.printf("Window %d doesn't appear? %n", num);
            return;
        }

        if (currPosi == num) {
            if (currPosi == Windows.size()-1) {
                currPosi--;
            }
            Windows.remove(num);
            if (currPosi == -1) {
                System.out.println("No pages.");
                return;
            }
            showWindow();
        }else {
            Windows.remove(num);
            if (currPosi > num){
                currPosi--;
                showWindow();
            }
        }
    }

    private void download(String path) {
        List<String> window = Windows.get(currPosi);
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(path));
            for (String s : window) {
                out.write(s+"\n");
            }
            System.out.println("download to: " + path);
            out.close();
        } catch (IOException e) {
            System.out.println("Download fails！");
        }
    }

    private void returnHistory() {
        int i = 0;
        for (String msg : history) {
            System.out.printf("%d: %s%n", i++, msg);
        }
    }
}
