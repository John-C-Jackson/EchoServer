import java.io.*;
import java.net.*;
import java.util.*;

public class EchoServer
{
    public static void main(String[] args) throws IOException
    {
        int count=0;


        try (ServerSocket serverSocket = new ServerSocket(23))
        {
            System.out.println("Listening...");

            while(true)
            {
                count++;
                Socket cSocket = serverSocket.accept();
                // creates a run of the program with clientSocket (cSocket)
                Runnable runHolder = new EchoThread(cSocket, count);
                // creates a new thread with the runHolder
                Thread threadHolder = new Thread(runHolder);
                // starts the thread
                threadHolder.start();

                // gives and update when a new thread is performed
                System.out.println("Thread Spawned connected: "+count);
                        }
        }
    }
}


class EchoThread implements Runnable
{
    private Socket clientSocket;
    private int count;
    InputStream io;
    OutputStream os;
    private PrintWriter out;

    EchoThread(Socket clientSocket, int count)
    {
        this.clientSocket = clientSocket;
        this.count = count;
    }

    @Override
    public void run()
    {
        try
        {
          // assigns io to the input of client
          io = clientSocket.getInputStream();
          // assigns os to the output to server 
          os = clientSocket.getOutputStream();
        }
        catch (IOException e) {System.err.println(e);}
        try (Scanner fromClient = new Scanner(io))
        {
            out = new PrintWriter(os, true);
            out.println("Connected");
            while(fromClient.hasNextLine())
            {
                String toClient = fromClient.nextLine();
                System.out.println("Client "+count+": "+ toClient);
                out.println("Echo: "+toClient);
            }
        }
        finally {
            System.out.println("Client" + count + "left.");
        }
    }
}
