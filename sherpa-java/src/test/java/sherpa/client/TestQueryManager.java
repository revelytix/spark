package sherpa.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avro.AvroRemoteException;
import org.junit.Assert;
import org.junit.Test;

import sherpa.protocol.DataRequest;
import sherpa.protocol.DataResponse;
import sherpa.protocol.ErrorResponse;
import sherpa.protocol.QueryRequest;
import sherpa.protocol.QueryResponse;
import sherpa.protocol.ReasonCode;
import sherpa.protocol.ServerException;
import sherpa.server.DummyQueryResponder;
import spark.api.exception.SparqlException;

public class TestQueryManager {
  
  public static void helpTestCursor(int rows, int batchSize) {
    DummyQueryResponder queryResponder = new DummyQueryResponder(rows);
    
    QueryExecution mgr = new QueryExecution(queryResponder);
    Map<String,String> params = Collections.emptyMap();
    Map<String,String> props = new HashMap<String,String>();
    props.put(QueryExecution.BATCH_SIZE, Integer.toString(batchSize));
    
    mgr.query("SELECT foo", params, props);
    
    Assert.assertEquals(0, mgr.getCursor());
    Assert.assertFalse(mgr.isLast());
    Assert.assertFalse(mgr.isAfterLast());
    
    int counter = 0;
    while (mgr.incrementCursor()) {
      List<Object> row = mgr.getRow();
      Assert.assertNotNull(row);
      Assert.assertEquals(DummyQueryResponder.DEFAULT_WIDTH, row.size());
      Assert.assertEquals(++counter, mgr.getCursor());
      Assert.assertEquals(counter == rows, mgr.isLast());
      Assert.assertFalse(mgr.isAfterLast());
    }
    
    Assert.assertFalse(mgr.isLast());
    Assert.assertTrue(mgr.isAfterLast());
    Assert.assertEquals(rows + 1, mgr.getCursor());
    Assert.assertEquals(counter, rows);
  }
  
  @Test
  public void testQuery() {
    DummyQueryResponder queryResponder = new DummyQueryResponder(20);
  
    QueryExecution mgr = new QueryExecution(queryResponder);
    Map<String,String> params = Collections.emptyMap();
    Map<String,String> props = new HashMap<String,String>();
    props.put(QueryExecution.BATCH_SIZE, "10");
    
    mgr.query("SELECT foo", params, props);
    List<String> vars = mgr.getVars();
    List<String> expectedVars = new ArrayList<String>();
    expectedVars.add("a");
    expectedVars.add("b");
    Assert.assertEquals(expectedVars, vars);        
    Assert.assertEquals(0, mgr.getCursor());
    
    // advance iterator; this blocks until the first page of results is ready, so first data request is guaranteed to have happened
    Assert.assertTrue(mgr.incrementCursor());
    Assert.assertEquals(1, mgr.getCursor());
    
    // verify messages to server
    Assert.assertTrue(queryResponder.messages.size() >= 2);
    Assert.assertEquals("Message=query sparql=SELECT foo params={} props={batchSize=10} ", queryResponder.messages.get(0));
    Assert.assertEquals("Message=data queryId=1 startRow=1 maxSize=10 ", queryResponder.messages.get(1));

    // cancel 
    mgr.cancel();
    int cancelMsg = queryResponder.messages.indexOf("Message=cancel queryId=1 ");
    Assert.assertTrue(cancelMsg > 1); // Should be at least 2 messages before cancel request (query request and first data request)
    
    // close 
    mgr.close();
    int closeMsg = queryResponder.messages.indexOf("Message=close queryId=1 ");
    Assert.assertTrue(closeMsg > cancelMsg); // Cancel and close are synchronous, so cancel must have happend after.
  }
  
  @Test
  public void testCursor() {
    helpTestCursor(0, 5);
    helpTestCursor(1, 5);
    helpTestCursor(4, 5);
    helpTestCursor(5, 5);
    helpTestCursor(6, 5);
    helpTestCursor(10, 5);
    helpTestCursor(11, 5);
  }
  
  @Test
  public void testExceptionOnQuery() {
    DummyQueryResponder queryResponder = new DummyQueryResponder(20) {
      private ErrorResponse err() {
        ErrorResponse resp = new ErrorResponse();
        resp.code = ReasonCode.Error;
        resp.serverException = new ServerException();
        resp.serverException.message = "foo";
        return resp;
      }
      @Override
      public QueryResponse query(QueryRequest query)
          throws AvroRemoteException, ErrorResponse {
        throw err();
      }
    };
    
    try {
      QueryExecution mgr = new QueryExecution(queryResponder);
      Map<String,String> empty = Collections.emptyMap();
      mgr.query("SELECT foo", empty, empty);
      Assert.fail("Should have thrown an error response!");
    } catch(SparqlException e) {
      Throwable cause = e.getCause();
      Assert.assertTrue(cause instanceof ErrorResponse);
      ErrorResponse er = (ErrorResponse) cause;
      Assert.assertEquals(ReasonCode.Error, er.code);
      Assert.assertEquals("foo", er.serverException.message);
    }
  }
  
  @Test
  public void testExceptionOnData() {
    DummyQueryResponder queryResponder = new DummyQueryResponder(20) {
      private ErrorResponse err() {
        ErrorResponse resp = new ErrorResponse();
        resp.code = ReasonCode.Error;
        resp.serverException = new ServerException();
        resp.serverException.message = "foo";
        return resp;
      }
      @Override
      public DataResponse data(DataRequest req)
          throws AvroRemoteException, ErrorResponse {
        throw err();
      }
    };

    QueryExecution mgr = new QueryExecution(queryResponder);
    Map<String,String> empty = Collections.emptyMap();
    mgr.query("SELECT foo", empty, empty);
    try {
      mgr.incrementCursor();
      mgr.getRow();
      
      Assert.fail("Should have thrown an error response!");
    } catch(SparqlException e) {
      Throwable cause = e.getCause();
      Assert.assertTrue(cause instanceof ErrorResponse);
      ErrorResponse er = (ErrorResponse) cause;
      Assert.assertEquals(ReasonCode.Error, er.code);
      Assert.assertEquals("foo", er.serverException.message);
    }
  }
  
  @Test
  public void testParamsAndPropsTransferredThrough() {
    final List<Object> outVals = new ArrayList<Object>();
    DummyQueryResponder queryResponder = new DummyQueryResponder(10) {
      public QueryResponse query(QueryRequest req) throws AvroRemoteException {
        outVals.add(req.parameters);
        outVals.add(req.properties);
        return super.query(req);
      }
    };
    
    QueryExecution mgr = new QueryExecution(queryResponder);
    Map<String,String> params = new HashMap<String,String>();
    params.put("abc", "def");
    Map<String,String> props = new HashMap<String,String>();
    props.put("ghi", "jkl");
    mgr.query("SELECT foo", params, props);
    
    Assert.assertEquals(2, outVals.size());
  }  
  
}