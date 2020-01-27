import java.io.*;
import java.net.*;
import java.util.*;

public class EchoServer
{
    public static void main(String[] args) throws IOException
    {
       // keep tracks of threads
        int count=0;

        // creates a new server socket in port 23
        try (ServerSocket serverSocket = new ServerSocket(23))
        {
            System.out.println("Listening...");

            // while the server socket is active
            while(true)
            {
                // increment counter to keep track of number of threads created
                count++;
                // crreates a client socket that the server socket has connected to
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
    private DataOutputStream toClient;
	private byte[] inData;
	private int state;

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

	// updateState method takes in a state and character, returns the state
	// resulting from processing character in state.
    public static int updateState(int state, char character )
    {
		// any time a q is seen, go to SEEN_Q state.
	    if (character == 'q')
	    {
		    return SEEN_Q;
	    }
		// if in SEEN_Q state see a q, advance to SEEN_QU state.
	    if (state == SEEN_Q && character == 'u')
	    {
		    return SEEN_QU;
	    }
		// if in SEEN_QU state and see an i, advance to SEEN_QUI state.
	    if (state == SEEN_QU && character == 'i')
	    {
	 	    return SEEN_QUI;
	    }
		// if in SEEN_QUI state and see a t, advance to FINAL_STATE.
	    if (state == SEEN_QUI && character == 't')
	    {
		    return FINAL_STATE;
	    }

		// default back to start state.
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

		// open a data input stream from the client.
        try (DataInputStream fromClient = new DataInputStream(io))
        {
			// create a data output stream to the client
            toClient = new DataOutputStream(os);

			//send connection message to client
            toClient.writeBytes("Connected\n");

			// loop while a byte is read from the client,
			// store byte in the first element of inData array.
            while(fromClient.read(inData, 0, 1) != 0)
            {

				// get the current byte from inData, convert to char.
                char charFromClient = (char) inData[0];

				// check if current char is valid
				if(isValidChar(charFromClient))
				{
					// server client connection display
					System.out.println("Client "+count+": "+ charFromClient);

			 		// send echo back to client.
                	                toClient.writeBytes("Echo: "+ charFromClient + "\n");

					// update state machine to check for 'quit' sequence.
					state = updateState(state, charFromClient);
				}

				// if current char puts us in final state (seen 'quit'),
				// send disconnection message, exit loop.
				if (state == FINAL_STATE)
				{
					toClient.writeBytes("Disconnected.\n");
					break;
				}
            }
        }
		catch (IOException e)
		{
			System.err.println(e);
		}
		// display message to server specifying that the client left.
        finally {
            System.out.println("Client " + count + " left.");
        }
    }
}
