package sampleclients;


import java.io.*;
import java.lang.management.ManagementFactory;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class Debugger {

    private boolean errReady = false;
    private boolean outReady = false;
    private boolean inReady = false;

    private ServerSocket errServerSocket;
    private ServerSocket outServerSocket;
    private ServerSocket inServerSocket;
    private ServerSocket ioServerSocket;

    private PrintStream oldOut = System.out;
    private PrintStream oldErr = System.err;
    private InputStream oldIn = System.in;

    private String level;

    public Debugger(String level) {

        List<String> args = ManagementFactory.getRuntimeMXBean().getInputArguments();

        if(args.size() == 0 || (!args.get(0).contains("-agentlib:") && !args.get(0).contains("-javaagent:"))) {
            return;
        }

        this.level = level;

        try {
            errServerSocket = new ServerSocket(0);
            outServerSocket = new ServerSocket(0);
            inServerSocket = new ServerSocket(0);
            ioServerSocket = new ServerSocket(0);
        } catch (IOException e) {
            e.printStackTrace();
        }


        ErrServerThread errServerThread = new ErrServerThread(errServerSocket);
        OutServerThread outServerThread = new OutServerThread(outServerSocket);
        InServerThread inServerThread = new InServerThread(inServerSocket);
        IoServerThread ioServerThread = new IoServerThread(ioServerSocket);

        errServerThread.start();
        outServerThread.start();
        inServerThread.start();
        ioServerThread.start();


        ChildProcessThread childProcessThread = new ChildProcessThread();

        childProcessThread.start();

        while (!errReady || !outReady || !inReady) {

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        oldErr.println("DEBUGGER : Let's go !");
    }

    private class ErrServerThread extends Thread {

        ServerSocket socketServer;
        Socket listenSocket;
        BufferedReader in;
        PrintWriter out;

        public ErrServerThread(ServerSocket socketServer) {
            super();
            this.socketServer = socketServer;
        }

        public void run() {

            try {
                listenSocket = socketServer.accept();

                System.setErr(new PrintStream(listenSocket.getOutputStream()));

                errReady = true;

                while (true) {

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    System.err.flush();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    private class OutServerThread extends Thread {

        ServerSocket socketServer;
        Socket listenSocket;
        BufferedReader in;
        PrintWriter out;

        public OutServerThread(ServerSocket socketServer) {
            super();
            this.socketServer = socketServer;
        }

        public void run() {

            try {
                listenSocket = socketServer.accept();

                System.setOut(new PrintStream(listenSocket.getOutputStream()));

                outReady = true;

                while (true) {

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    System.out.flush();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }



    }

    private class InServerThread extends Thread {

        ServerSocket socketServer;
        Socket listenSocket;
        BufferedReader in;
        PrintWriter out;

        public InServerThread(ServerSocket socketServer) {
            super();
            this.socketServer = socketServer;
        }

        public void run() {

            try {
                listenSocket = socketServer.accept();

                System.setIn(listenSocket.getInputStream());

                inReady = true;

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    private class IoServerThread extends Thread {

        ServerSocket socketServer;
        Socket listenSocket;
        InputStream in;
        PrintWriter out;

        public IoServerThread(ServerSocket socketServer) {
            super();
            this.socketServer = socketServer;
        }

        public void run() {

            try {
                listenSocket = socketServer.accept();

                String inputLine;

                in = listenSocket.getInputStream();

                byte[] buffer = new byte[1024];
                int read;
                while((read = in.read(buffer)) != -1) {
                    String output = new String(buffer, 0, read);
                    oldErr.print(output);
                    oldErr.flush();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }


    private class ChildProcessThread extends Thread {

        public ChildProcessThread() {
            super();
        }

        public void run() {

            ProcessBuilder processBuilder = new ProcessBuilder("cmd", "/c node debugger.js --launcher " + level + " " + errServerSocket.getLocalPort() + " " + outServerSocket.getLocalPort() + " " + inServerSocket.getLocalPort() + " " + ioServerSocket.getLocalPort());

            Process process = null;
            try {
                processBuilder.start().waitFor();

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


        }
    }

}
