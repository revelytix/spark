
/*
 * Copyright 2011 Revelytix Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package spark.protocol;

import java.util.Map;

import spark.api.Command;
import spark.api.Connection;
import spark.api.DataSource;
import spark.api.Solutions;
import spark.api.credentials.NoCredentials;
import spark.api.rdf.RDFNode;
import spark.protocol.ProtocolDataSource;

public class SparqlQuery {

  public static void testQuery() throws Exception {
    DataSource myDS = new ProtocolDataSource("http://DBpedia.org/sparql");
    Connection conn = myDS.getConnection(NoCredentials.INSTANCE);
    Command query = conn.createCommand("SELECT ?p ?o WHERE { <http://dbpedia.org/resource/Terry_Gilliam> ?p ?o }");    
    Solutions solutions = query.executeQuery();
    
    System.out.println("vars = " + solutions.getVariables());
    int row = 0;
    while(solutions.next()) {
      System.out.println("Row " + (row++) + ": " + solutions.getSolution());
    }
    solutions.close();
    query.close();
    conn.close();
  }
  
  public static void testQuery2() throws Exception {
    DataSource myDS = new ProtocolDataSource("http://DBpedia.org/sparql");
    Connection conn = myDS.getConnection(NoCredentials.INSTANCE);
    Command query = conn.createCommand("SELECT ?p ?o WHERE { <http://dbpedia.org/resource/Terry_Gilliam> ?p ?o }");    
    Solutions solutions = query.executeQuery();
    
    System.out.println("vars = " + solutions.getVariables());
    int row = 0;
    for(Map<String, RDFNode> solution : solutions) {
      System.out.println("Row " + (row++) + ": " + solution);
    }
    solutions.close();
    query.close();
    conn.close();
  }
  
  public static void main(String arg[]) throws Exception {
    testQuery();
    testQuery2();
  }
}
