
package tcp.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import model.ObjectWrapper;
import model.User;


public class ClientCtr {

    private String serverName;
    private int portNumber;
    private Socket socket;
    private User user;

    public boolean connect() {
        try {
            InetAddress address = InetAddress.getByName(serverName);
            this.socket = new Socket(address, portNumber);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public ClientCtr() {
    }
    public ClientCtr(String serverName, int portNumber){
        this.serverName = serverName;
        this.portNumber = portNumber;
    }
    public void setUser( User user ){
        this.user = user;
    }
    public void sendData( ObjectWrapper o ) {
        try {
            ObjectOutputStream oos = new ObjectOutputStream( socket.getOutputStream() );
            oos.writeObject( o );
        } catch (Exception e) {
           // e.printStackTrace();
        }
    }
    public ObjectWrapper revData() {
        try {
            ObjectInputStream ois = new ObjectInputStream( socket.getInputStream() );
            return (ObjectWrapper) ois.readObject();
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return null;
    }
      public boolean login(User user)  {
        ObjectWrapper sendData = new ObjectWrapper( ObjectWrapper.LOGIN, user);
          sendData( sendData );
        ObjectWrapper revData =   (ObjectWrapper)revData();
        if( revData.getPerformative()== ObjectWrapper.REPLY_LOGIN ){
            String res = (String) revData.getData();
            String[] tokens  = res.split(" ",2);
            if( tokens[0].equals("ok")){
                user.setId( Integer.parseInt(tokens[1]));
                return true;
            }
            else return false;
        }    
        
        return false;          
    }
      public User getUser(){
          return user;
      }
      public Socket getSocket(){
          return socket;
      }
      public void disconnect(){
          try {
              socket.close();
          } catch (Exception e) {
              e.printStackTrace();
          }
          
      } 
//      public static void main(String[] args) throws IOException, ClassNotFoundException {
//        ClientCtr ctr = new ClientCtr();
//        ctr.connect();
//        System.out.println( ctr.login("duc68","duc68") );
//          System.out.println(ctr.getUser().getId());
//    }
}
