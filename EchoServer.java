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
	private final static int START_STATE = 1000;
    private final static int SEEN_Q = 1001;
    private final static int SEEN_QU = 1002;
    private final static int SEEN_QUI = 1003;
    private final static int FINAL_STATE = 1004;

    private Socket clientSocket;
    private int count;
    InputStream io;
    OutputStream os;
    private DataOutputStream out;
	private byte[] inData;
	private int state;
	private boolean connected;

    EchoThread(Socket clientSocket, int count)
    {
        this.clientSocket = clientSocket;
        this.count = count;
		inData = new byte[1];
		state = START_STATE;
    }

	public static boolean isValidChar(char character)
    {
	    //check if the ascii code is a part of the alphabet (upper or lowercase)
	    if ((character >= 'a' && character <= 'z') ||
		 	   (character >= 'A' && character <= 'Z'))
	    {
		    return true;
	    }

	    // if it didn't return on the if statement, it's an invalid character
	    return false;
    }

    public static int updateState(int state, char character )
    {
	    if (character == 'q')
	    {
		    return SEEN_Q;
	    }
	    if (state == SEEN_Q && character == 'u')
	    {
		    return SEEN_QU;
	    }
	    if (state == SEEN_QU && character == 'i')
	    {
	 	    return SEEN_QUI;
	    }
	    if (state == SEEN_QUI && character == 't')
	    {
		    return FINAL_STATE;
	    }
	    return START_STATE;
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
        try (DataInputStream fromClient = new DataInputStream(io))
        {
            out = new DataOutputStream(os);
			connected = true;
            out.writeBytes("Connected");

            while(fromClient.read(inData, 0, 1) != 0)
            {
                char toClient = (char) inData[0];

				if(isValidChar(toClient))
				{
					System.out.println("Client "+count+": "+ toClient);
                	out.writeBytes("Echo: "+ toClient + "\n");
					state = updateState(state, toClient);
				}

				if (state == FINAL_STATE)
				{
					connected = false;
				}

				if (!connected)
				{
					out.writeBytes("Disconnected.\n");
					break;
				}
            }
        }
		catch (IOException e)
		{
			System.err.println(e);
		}
        finally {
            System.out.println("Client " + count + " left.");
        }
    }
}
