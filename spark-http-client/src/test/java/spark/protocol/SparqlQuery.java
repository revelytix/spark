
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

import junit.framework.Assert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spark.api.Command;
import spark.api.Connection;
import spark.api.DataSource;
import spark.api.Solutions;
import spark.api.credentials.NoCredentials;
import spark.api.rdf.RDFNode;

public class SparqlQuery {

  private static final Logger logger = LoggerFactory.getLogger(SparqlQuery.class);
  
  public static void testQuery() throws Exception {
    DataSource myDS = new ProtocolDataSource("http://DBpedia.org/sparql");
    Connection conn = myDS.getConnection(NoCredentials.INSTANCE);
    Command query = conn.createCommand("SELECT ?p ?o WHERE { <http://dbpedia.org/resource/Terry_Gilliam> ?p ?o }");    
    Solutions solutions = query.executeQuery();
    
    logger.debug("vars = {}", solutions.getVariables());
    int row = 0;
    while(solutions.next()) {
      logger.debug("Row {}: {}", ++row, solutions.getResult());
    }
    solutions.close();
    query.close();
    conn.close();
    myDS.close();
  }
  
  public static void testQuery2() throws Exception {
    DataSource myDS = new ProtocolDataSource("http://DBpedia.org/sparql");
    Connection conn = myDS.getConnection(NoCredentials.INSTANCE);
    Command query = conn.createCommand("SELECT ?p ?o WHERE { <http://dbpedia.org/resource/Terry_Gilliam> ?p ?o }");    
    Solutions solutions = query.executeQuery();
    
    logger.debug("vars = {}", solutions.getVariables());
    int row = 0;
    for(Map<String, RDFNode> solution : solutions) {
      logger.debug("Row {}: {}", ++row, solution);
    }
    solutions.close();
    query.close();
    conn.close();
    myDS.close();
  }
  
  static int iterateResults(Solutions s) {
    int c = 0;
    while (s.next()) {
      c++;
      Assert.assertNotNull(s.getBinding("s"));
      Assert.assertNotNull(s.getBinding("p"));
      Assert.assertNotNull(s.getBinding("o"));
    }
    return c;
  }
  
  public static void main(String arg[]) throws Exception {
    testQuery();
    testQuery2();
  }
}
