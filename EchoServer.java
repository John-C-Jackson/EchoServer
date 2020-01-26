import java.io.*;
import java.net.*;

public class EchoServer implements Runnable {
   Socket csocket;
   EchoServer(Socket csocket) {
      this.csocket = csocket;
   }
   public static void main(String args[]) throws Exception {
      ServerSocket ssock = new ServerSocket(Integer.parseInt(args[0]));
      System.out.println("Listening");

      while (true) {
         Socket sock = ssock.accept();
         System.out.println("Connected");
         new Thread(new EchoServer(sock)).start();
      }
   }
   public void run() {
      try {
         PrintStream pstream = new PrintStream(csocket.getOutputStream());
         BufferedReader fromClient = new BufferedReader(new InputStreamReader(csocket.getInputStream()));
         String inputLine;
         while( ( inputLine = fromClient.readLine() ) != null )
         {
           inputLine = inputLine.replaceAll("[^a-zA-Z]", "");
           pstream.println(inputLine);
         }
         pstream.close();
         csocket.close();
      } catch (IOException e) {
         System.out.println(e);
      }
   }
}
