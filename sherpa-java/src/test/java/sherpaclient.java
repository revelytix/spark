import java.util.Iterator;
import java.util.Map;

import sherpa.client.SHPDataSource;
import spark.api.Command;
import spark.api.Connection;
import spark.api.DataSource;
import spark.api.Solutions;
import spark.api.credentials.NoCredentials;
import spark.api.rdf.RDFNode;


public class sherpaclient {

  public static void main(String[] args) {
    if(args.length < 3) {
      System.out.println("Usage: sherpaclient <host> <port> <query>");
      System.exit(1);
    }
    
    String host = args[0];
    String port = args[1];
    String query = args[2];
    System.out.println("Connecting to " + host + ":" + port);
    DataSource ds = new SHPDataSource(host, Integer.parseInt(port));
    Connection conn = ds.getConnection(NoCredentials.INSTANCE);
    Command command = conn.createCommand(query);
    command.setTimeout(120000);
    
    System.out.println("Executing query....");
    Solutions results = command.executeQuery();
    
    System.out.println("\nResults:");
    int count = 0;
    Iterator<Map<String,RDFNode>> iter = results.iterator();
    for (; iter.hasNext(); ) {
      Map<String,RDFNode> tuple = iter.next();
      count++;
      for(Map.Entry<String, RDFNode> entry : tuple.entrySet()) {
        System.out.print("\t" + entry.getKey() + ": " + entry.getValue());
      }
      System.out.println();
    }
    System.out.println("Read " + count + " results");
  }

}
