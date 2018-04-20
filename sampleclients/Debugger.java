package sampleclients;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Debugger {

    private boolean errReady = false;
    private boolean outReady = false;
    private boolean inReady = false;

    private ServerSocket errServerSocket;
    private ServerSocket outServerSocket;
    private ServerSocket inServerSocket;

    private PrintStream oldOut = System.out;
    private PrintStream oldErr = System.err;
    private InputStream oldIn = System.in;

    private  OutputStream newOut = null;
    private  OutputStream newErr = null;
    private  InputStream newIn = null;

    private String level;

    public Debugger(String level) {

        this.level = level;

        try {
            errServerSocket = new ServerSocket(0);
            outServerSocket = new ServerSocket(0);
            inServerSocket = new ServerSocket(0);
        } catch (IOException e) {
            e.printStackTrace();
        }


        ErrServerThread errServerThread = new ErrServerThread(errServerSocket);
        OutServerThread outServerThread = new OutServerThread(outServerSocket);
        InServerThread inServerThread = new InServerThread(inServerSocket);

        errServerThread.start();
        outServerThread.start();
        inServerThread.start();

        ChildProcessThread childProcessThread = new ChildProcessThread();

        childProcessThread.start();

        while(!errReady || !outReady || !inReady) {

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        oldErr.println("Let's debug now !");
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


    private class ChildProcessThread extends Thread {


        public ChildProcessThread() {
            super();
        }

        public void run() {

            ProcessBuilder pb = new ProcessBuilder("cmd", "/c node debugger.js --launcher " + level + " " + errServerSocket.getLocalPort() + " " + outServerSocket.getLocalPort() + " " + inServerSocket.getLocalPort());
            System.out.println("Run echo command");
            Process process = null;
            try {
                process = pb.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
            int errCode = 0;
            try {
                errCode = process.waitFor();
            } catch (InterruptedException e) {

                e.printStackTrace();
            }
            System.out.println("Echo command executed, any errors? " + (errCode == 0 ? "No" : "Yes"));
            try {
                System.out.println("Echo Output:\n" + output(process.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            oldErr.println(process.pid());

        }

        private String output(InputStream inputStream) throws IOException {
            StringBuilder sb = new StringBuilder();
            BufferedReader br = null;
            try {
                br = new BufferedReader(new InputStreamReader(inputStream));
                String line = null;
                while ((line = br.readLine()) != null) {
                    sb.append(line + System.getProperty("line.separator"));
                }
            } finally {
                br.close();
            }
            return sb.toString();
        }
    }
}
