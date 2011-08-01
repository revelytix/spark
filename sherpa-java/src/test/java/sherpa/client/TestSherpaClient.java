/*
 * Copyright 2011 Revelytix, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sherpa.client;

import static spark.spi.TestCursor.AFTER_LAST;
import static spark.spi.TestCursor.BEFORE_FIRST;
import static spark.spi.TestCursor.FIRST;
import static spark.spi.TestCursor.LAST;
import static spark.spi.TestCursor.NONE;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.avro.AvroRemoteException;
import org.apache.avro.util.Utf8;
import org.junit.Assert;
import org.junit.Test;

import sherpa.protocol.BNode;
import sherpa.protocol.IRI;
import sherpa.protocol.PlainLiteral;
import sherpa.protocol.QueryRequest;
import sherpa.protocol.QueryResponse;
import sherpa.protocol.TypedLiteral;
import sherpa.server.DummyQueryResponder;
import sherpa.server.DummySherpaServer;
import spark.api.Command;
import spark.api.Connection;
import spark.api.DataSource;
import spark.api.Solutions;
import spark.api.credentials.NoCredentials;
import spark.api.rdf.Literal;
import spark.api.rdf.NamedNode;
import spark.api.rdf.RDFNode;
import spark.api.uris.XsdTypes;
import spark.spi.TestCursor;
import spark.spi.rdf.BlankNodeImpl;
import spark.spi.rdf.NamedNodeImpl;
import spark.spi.rdf.PlainLiteralImpl;
import spark.spi.rdf.TypedLiteralImpl;
import spark.spi.util.DateTime;


public class TestSherpaClient {

  public Solutions helpExecuteQuery(DummySherpaServer server, int batchSize) {
    InetSocketAddress serverAddress = server.getAddress();
    DataSource ds = new SHPDataSource(serverAddress.getHostName(),
        serverAddress.getPort());
    Connection conn = ds.getConnection(NoCredentials.INSTANCE);
    Command command = conn
        .createCommand("SELECT ?x ?y WHERE { this should be a real query but the test doesn't actually do anything real.");
    ((SHPCommand) command).setBatchSize(batchSize);
    return command.executeQuery();
  }
  
  public void helpTestQueryCursor(int resultRows, int batchSize) {
    DummySherpaServer server = new DummySherpaServer(resultRows);

    try {
      Solutions solutions = helpExecuteQuery(server, batchSize);

      TestCursor.assertCursor(solutions, BEFORE_FIRST);
      
      int counter = 0;
      while (solutions.next()) {
        Map<String,RDFNode> solution = solutions.getResult();
        Assert.assertNotNull(solution);
        Assert.assertEquals(++counter, solutions.getRow());
        int state = ((counter == 1) ? FIRST : NONE) | ((counter == resultRows) ? LAST : NONE);
        TestCursor.assertCursor(solutions, state);
      }

      TestCursor.assertCursor(solutions, AFTER_LAST);
      
      System.out.println("Read " + counter + " rows");
      Assert.assertEquals(resultRows, counter);

    } finally {
      server.shutdown();
    }
  }
  
  public void helpCheckRows(List<Map<String,RDFNode>> data, int size) {
    Assert.assertEquals(size, data.size());
    for (Map<String,RDFNode> s : data) {
      Assert.assertNotNull(s);
    }
    for (int i = 0; i < size - 1; i++) {
      Assert.assertFalse(data.get(i).equals(data.get(i + 1)));
    }
  }

  public void helpTestIteratorNormal(int rows, int batchSize) {
    DummySherpaServer server = new DummySherpaServer(rows);
    try {
      Solutions solutions = helpExecuteQuery(server, batchSize);
      Iterator<Map<String,RDFNode>> iter = solutions.iterator();
      Assert.assertNotNull(iter);
      List<Map<String,RDFNode>> data = new ArrayList<Map<String,RDFNode>>(rows);
      // Traverse the iterator in a normal fashion.
      while (iter.hasNext()) {
        data.add(iter.next());
      }
      helpCheckRows(data, rows);
    } finally {
      server.shutdown();
    }
  }
  
  public void helpTestIteratorParanoid(int rows, int batchSize) {
    DummySherpaServer server = new DummySherpaServer(rows);
    try {
      Solutions solutions = helpExecuteQuery(server, batchSize);
      Iterator<Map<String,RDFNode>> iter = solutions.iterator();
      Assert.assertNotNull(iter);
      List<Map<String,RDFNode>> data = new ArrayList<Map<String,RDFNode>>(rows);
      // Traverse the iterator, with lots of extra checks to hasNext();
      Assert.assertEquals(rows > 0, iter.hasNext());
      Assert.assertEquals(rows > 0, iter.hasNext());
      while (iter.hasNext()) {
        data.add(iter.next());
        Assert.assertEquals(data.size() < rows, iter.hasNext());
      }
      helpCheckRows(data, rows);
    } finally {
      server.shutdown();
    }
  }
  
  public void helpTestIteratorCount(int rows, int batchSize) {
    DummySherpaServer server = new DummySherpaServer(rows);
    try {
      Solutions solutions = helpExecuteQuery(server, batchSize);
      Iterator<Map<String,RDFNode>> iter = solutions.iterator();
      Assert.assertNotNull(iter);
      List<Map<String,RDFNode>> data = new ArrayList<Map<String,RDFNode>>(rows);
      // Traverse the iterator without calling hasNext()
      for (int i = 0; i < rows; i++) {
        data.add(iter.next());
      }
      Assert.assertFalse(iter.hasNext());
      helpCheckRows(data, rows);
    } finally {
      server.shutdown();
    }
  }
  
  @Test
  public void testCursor() {
    helpTestQueryCursor(0, 10);
    helpTestQueryCursor(1, 10);
    helpTestQueryCursor(9, 10);
    helpTestQueryCursor(10, 10);
    helpTestQueryCursor(11, 10);
    helpTestQueryCursor(20, 10);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testTimeoutPassedDown() {
    final List<Object> results = new ArrayList<Object>();    
    DummySherpaServer server = new DummySherpaServer(
        new DummyQueryResponder(10) {
          public QueryResponse query(QueryRequest query)
              throws AvroRemoteException {
            results.add(query.properties);
            return super.query(query);
          }
        });
    InetSocketAddress serverAddress = server.getAddress();
    
    try {
      DataSource ds = new SHPDataSource(serverAddress.getHostName(), serverAddress.getPort());
      Connection conn = ds.getConnection(NoCredentials.INSTANCE);
      Command command = conn.createCommand("SELECT ?x ?y WHERE { this should be a real query but the test doesn't actually do anything real. }");
      command.setTimeout(1234);
      command.executeQuery();
      
      // Kind of tricky here - the keys and values are now Avro Utf8 instances which don't compare equal to Strings
      Map<CharSequence,CharSequence> serverProps = (Map<CharSequence,CharSequence>)results.get(0);
      Assert.assertEquals(new Utf8("1234"), serverProps.get(new Utf8(QueryExecution.TIMEOUT)));
      
    } finally {
      server.shutdown();
    }

  }
  
  @Test
  public void testData() {
    NamedNodeImpl uri1 = new NamedNodeImpl(URI.create("http://example.org/foo"));
    NamedNodeImpl uri2 = new NamedNodeImpl(URI.create("http://example.org/bar"));
    TypedLiteralImpl lit1 = new TypedLiteralImpl("10", XsdTypes.INT);
    TypedLiteralImpl lit2 = new TypedLiteralImpl("20", XsdTypes.INT);
    List<List<Object>> data = toList(
        new Object[][] {
            new Object[] { iri(uri1), iri(uri2), toInt(lit1) },
            new Object[] { iri(uri2), iri(uri1), toInt(lit2) }
        });
    
    DummySherpaServer server = new DummySherpaServer(data);
    try {
      Solutions s = helpExecuteQuery(server, 5);
      
      Assert.assertTrue(s.next());
      Map<String, RDFNode> solution = s.getResult();
      Assert.assertEquals(3, solution.size());
      Assert.assertEquals(uri1, solution.get("a"));
      Assert.assertEquals(uri2, solution.get("b"));
      Assert.assertEquals(lit1, solution.get("c"));
      
      Assert.assertTrue(s.next());
      Assert.assertEquals(uri2, s.getBinding("a"));
      Assert.assertEquals(uri2, s.getNamedNode("a"));
      Assert.assertEquals(uri1, s.getBinding("b"));
      Assert.assertEquals(uri1.getURI(), s.getURI("b"));
      Assert.assertEquals(lit2, s.getBinding("c"));      
      Assert.assertEquals(lit2, s.getLiteral("c"));      
      Assert.assertEquals(20, s.getInt("c"));
      
      Assert.assertFalse(s.next());
    } finally {
      server.shutdown();
    }
  }
  
  @Test
  public void testDatatypes() {
    Date d = new Date();
    NamedNodeImpl uri = new NamedNodeImpl(URI.create("http://example.org/foo"));
    PlainLiteralImpl lit1 = new PlainLiteralImpl("bar");
    PlainLiteralImpl lit2 = new PlainLiteralImpl("baz", "en");
    TypedLiteralImpl lit3 = new TypedLiteralImpl(DateTime.format(d, TimeZone.getDefault()), XsdTypes.DATE_TIME);
    TypedLiteralImpl aInt = new TypedLiteralImpl("20", XsdTypes.INT);
    TypedLiteralImpl aLong = new TypedLiteralImpl("54687323427654", XsdTypes.LONG);
    TypedLiteralImpl aBool = new TypedLiteralImpl("true", XsdTypes.BOOLEAN);
    TypedLiteralImpl aFloat = new TypedLiteralImpl("3.14", XsdTypes.FLOAT);
    TypedLiteralImpl aDouble = new TypedLiteralImpl("98.6", XsdTypes.DOUBLE);
    TypedLiteralImpl aString = new TypedLiteralImpl("abcd", XsdTypes.STRING);
    BlankNodeImpl bn = new BlankNodeImpl("node0");
    
    List<List<Object>> data = toList(
        new Object[][] {
            new Object[] { iri(uri) },
            new Object[] { plainLit(lit1) },
            new Object[] { plainLit(lit2) },
            new Object[] { typedLit(lit3) },
            new Object[] { Integer.valueOf(aInt.getLexical()) },
            new Object[] { Long.valueOf(aLong.getLexical()) },
            new Object[] { Boolean.valueOf(aBool.getLexical()) },
            new Object[] { Float.valueOf(aFloat.getLexical()) },
            new Object[] { Double.valueOf(aDouble.getLexical()) },
            new Object[] { aString.getLexical() },
            new Object[] { bNode(bn) },
            new Object[] { null },
        });
    
    DummySherpaServer server = new DummySherpaServer(data);
    try {
      Solutions s = helpExecuteQuery(server, 10);
      String var = "a";
      
      Assert.assertTrue(s.next());
      Assert.assertEquals(uri, s.getBinding(var));
      Assert.assertEquals(uri.getURI(), s.getURI(var));
      
      Assert.assertTrue(s.next());
      Assert.assertEquals(lit1, s.getBinding(var));
      Assert.assertEquals(lit1, s.getLiteral(var));
      
      Assert.assertTrue(s.next());
      Assert.assertEquals(lit2, s.getBinding(var));
      Assert.assertEquals(lit2, s.getLiteral(var));
      
      Assert.assertTrue(s.next());
      Assert.assertEquals(lit3, s.getBinding(var));
      Assert.assertEquals(lit3, s.getLiteral(var));
      Assert.assertEquals(d, s.getDateTime(var));
      
      Assert.assertTrue(s.next());
      Assert.assertEquals(aInt, s.getBinding(var));
      Assert.assertEquals(aInt, s.getLiteral(var));
      Assert.assertEquals(20, s.getInt(var));
      
      Assert.assertTrue(s.next());
      Assert.assertEquals(aLong, s.getBinding(var));
      Assert.assertEquals(aLong, s.getLiteral(var));
      
      Assert.assertTrue(s.next());
      Assert.assertEquals(aBool, s.getBinding(var));
      Assert.assertEquals(aBool, s.getLiteral(var));
      Assert.assertEquals(true, s.getBoolean(var));
      
      Assert.assertTrue(s.next());
      Assert.assertEquals(aFloat, s.getBinding(var));
      Assert.assertEquals(aFloat, s.getLiteral(var));
      Assert.assertTrue(Float.valueOf(aFloat.getLexical()).equals(s.getFloat(var)));
      
      Assert.assertTrue(s.next());
      Assert.assertEquals(aDouble, s.getBinding(var));
      Assert.assertEquals(aDouble, s.getLiteral(var));
      Assert.assertTrue(Double.valueOf(aDouble.getLexical()).equals(s.getDouble(var)));
      
      Assert.assertTrue(s.next());
      Assert.assertEquals(aString, s.getBinding(var));
      Assert.assertEquals(aString, s.getLiteral(var));
      Assert.assertEquals("abcd", s.getString(var));
      
      Assert.assertTrue(s.next());
      Assert.assertEquals(bn, s.getBinding(var));
      Assert.assertEquals(bn, s.getBlankNode(var));
      
      Assert.assertTrue(s.next());
      Assert.assertNull(s.getBinding(var));
      Assert.assertFalse(s.isBound(var));
      Assert.assertNull(s.getNamedNode(var));
      Assert.assertNull(s.getLiteral(var));
      Assert.assertNull(s.getBlankNode(var));
      
      Assert.assertFalse(s.next());
    } finally {
      server.shutdown();
    }
  }
  
  @Test
  public void testIterator() {
    helpTestIteratorNormal(0, 5);
    helpTestIteratorNormal(1, 5);
    helpTestIteratorNormal(4, 5);
    helpTestIteratorNormal(5, 5);
    helpTestIteratorNormal(6, 5);
    helpTestIteratorNormal(10, 5);
    helpTestIteratorParanoid(0, 5);
    helpTestIteratorParanoid(1, 5);
    helpTestIteratorParanoid(4, 5);
    helpTestIteratorParanoid(5, 5);
    helpTestIteratorParanoid(6, 5);
    helpTestIteratorParanoid(10, 5);
    helpTestIteratorCount(0, 5);
    helpTestIteratorCount(1, 5);
    helpTestIteratorCount(4, 5);
    helpTestIteratorCount(5, 5);
    helpTestIteratorCount(6, 5);
    helpTestIteratorCount(10, 5);
  }
  
  public static List<List<Object>> toList(Object[][] data) {
    List<List<Object>> list = new ArrayList<List<Object>>(data.length);
    for (Object[] row : data) {
      list.add(Arrays.asList(row));
    }
    return list;
  }
  
  public static PlainLiteral plainLit(PlainLiteralImpl l) {
    PlainLiteral lit = new PlainLiteral();
    lit.lexical = l.getLexical();
    lit.language = l.getLanguage();
    return lit;
  }
  
  public static TypedLiteral typedLit(TypedLiteralImpl l) {
    TypedLiteral lit = new TypedLiteral();
    lit.lexical = l.getLexical();
    lit.datatype = l.getDataType().toString();
    return lit;
  }
  
  public static BNode bNode(BlankNodeImpl n) {
    BNode bn = new BNode();
    bn.label = n.getLabel();
    return bn;
  }
  
  public static IRI iri(NamedNode n) {
    IRI i = new IRI();
    i.iri = n.getURI().toString();
    return i;
  }
  
  public static Integer toInt(Literal l) {
    return Integer.valueOf(l.getLexical());
  }
}
