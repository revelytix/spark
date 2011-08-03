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

import static org.apache.http.protocol.HTTP.UTF_8;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import spark.api.Command;
import spark.api.Solutions;
import spark.api.exception.SparqlException;
import spark.protocol.parser.XMLResults;

public class SparqlCall {

  @SuppressWarnings("unused")
  private static final int INFORMATIONAL_MIN = 100;
  @SuppressWarnings("unused")
  private static final int INFORMATIONAL_MAX = 199;
  private static final int SUCCESS_MIN = 200;
  private static final int SUCCESS_MAX = 299;
  @SuppressWarnings("unused")
  private static final int REDIRECT_MIN = 300;
  @SuppressWarnings("unused")
  private static final int REDIRECT_MAX = 399;
  private static final int CLIENT_ERROR_MIN = 400;
  private static final int CLIENT_ERROR_MAX = 499;
  private static final int SERVER_ERROR_MIN = 500;
  private static final int SERVER_ERROR_MAX = 599;

  /** The maximum length of a GET request */
  private static final int QUERY_LIMIT = 1024;

  /** URL-encode a string as UTF-8, catching any thrown UnsupportedEncodingException. */
  private static final String encode(String s) {
    try {
      return URLEncoder.encode(s, UTF_8);
    } catch (UnsupportedEncodingException e) {
      throw new Error("JVM unable to handle UTF-8");
    }
  }
  
  private final HttpClient client;
  private final Command command;
  private final URL url;
  
  SparqlCall(HttpClient client, Command command, URL url) {
    if (client == null) throw new IllegalArgumentException("Null client");
    if (command == null) throw new IllegalArgumentException("Null command");
    if (url == null) throw new IllegalArgumentException("Null URL");
    this.client = client;
    this.command = command;
    this.url = url;
  }
  
  private HttpResponse executeInternal() {
    HttpUriRequest req;

    try {
      String params = "query=" + encode(command.getCommand());
      String u = url.toString() + "?" + params;
      if (u.length() > QUERY_LIMIT) {
        // POST connection
        try {
          req = new HttpPost(url.toURI());
        } catch (URISyntaxException e) {
          throw new SparqlException("Endpoint <" + url
              + "> not in an acceptable format", e);
        }
        ((HttpPost) req).setEntity((HttpEntity) new StringEntity(params));
      } else {
        // GET connection
        req = new HttpGet(u);
      }

      if (command.getTimeout() != Command.NO_TIMEOUT) {
        HttpParams reqParams = new BasicHttpParams();
        HttpConnectionParams.setSoTimeout(reqParams, (int) (command.getTimeout() * 1000));
        req.setParams(reqParams);
      }
      
      HttpResponse response = client.execute(req);
      StatusLine status = response.getStatusLine();
      int code = status.getStatusCode();

      if (code >= SUCCESS_MIN && code <= SUCCESS_MAX) {
        return response;

      } else if (code >= CLIENT_ERROR_MIN && code >= CLIENT_ERROR_MAX) {
        throw new SparqlException(status.getReasonPhrase() + code);
      } else if (code >= SERVER_ERROR_MIN && code >= SERVER_ERROR_MAX) {
        throw new SparqlException(status.getReasonPhrase() + code);
      } else {
        throw new SparqlException(status.getReasonPhrase() + code);
      }
    } catch (UnsupportedEncodingException e) {
      throw new SparqlException("Unabled to encode data", e);
    } catch (ClientProtocolException cpe) {
      throw new SparqlException("Error in protocol", cpe);
    } catch (IOException e) {
      throw new SparqlException(e);
    }
  }
  
  /** Execute the SELECT query specified by the given command against the given endpoint URL. */
  public Solutions execute() throws IOException {
    HttpResponse response = executeInternal();
    
    HttpEntity entity = response.getEntity();
    if (entity == null) throw new SparqlException("No data in response from server");

    // blindly assume the results are "application/sparql-results+xml"
    return getSolution(command, entity.getContent());
  }
  
  /** Construct a solution from an input stream; broken into a separate method for easier testing. */
  public static Solutions getSolution(Command cmd, InputStream input) throws SparqlException, IOException {
    return (Solutions) XMLResults.createResults(cmd, input);
  }
}
