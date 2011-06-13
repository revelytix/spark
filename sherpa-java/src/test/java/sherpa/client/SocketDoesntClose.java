package sherpa.client;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.apache.avro.Protocol;
import org.apache.avro.Schema;
import org.apache.avro.Protocol.Message;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.Encoder;
import org.apache.avro.ipc.Responder;
import org.apache.avro.ipc.SaslSocketServer;
import org.apache.avro.ipc.Server;

public class SocketDoesntClose extends Responder {

  public static void main(String[] args) throws Exception {
    
    Protocol prot = new Protocol("c", "a.b");

    // start on port 9999
    InetSocketAddress addr = new InetSocketAddress(InetAddress.getLocalHost(), 9999);
    Server server = new SaslSocketServer(new SocketDoesntClose(prot), addr);
    server.start();
    
    // stop -- I would expect this to fully release the socket
    server.close();
    
    Thread.sleep(10000);
    
    // start on same socket again -> address already in use
    server = new SaslSocketServer(new SocketDoesntClose(prot), addr);
  }
  
  // dummy implementation
  public SocketDoesntClose(Protocol local) {
    super(local);
  }

  public Object readRequest(Schema actual, Schema expected, Decoder in) {
    return null;
  }

  public Object respond(Message message, Object request) {
    return null;
  }

  public void writeError(Schema schema, Object error, Encoder out) {    
  }

  public void writeResponse(Schema schema, Object response, Encoder out) {
  }
}
