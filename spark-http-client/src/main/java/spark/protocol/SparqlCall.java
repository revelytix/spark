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
import spark.api.exception.SparqlException;

/**
 * This class provides a single method for executing a SPARQL HTTP query request and returning the
 * server response. It is called by ProtocolCommand, and the primary purpose for having this as a
 * separate class is to isolate the code which uses the Apache HTTP client in one place.
 */
public class SparqlCall {

  // HTTP response codes.
  private static final int SUCCESS_MIN = 200;
  private static final int SUCCESS_MAX = 299;
  @SuppressWarnings("unused")
  private static final int REDIRECT_MIN = 300;
  @SuppressWarnings("unused")
  private static final int REDIRECT_MAX = 399;

  /** The maximum length of a GET request */
  private static final int QUERY_LIMIT = 1024;

  /** Method name for HTTP POST. */
  private static final String POST = "POST";
  /** Accept header for content negotiation. */
  private static final String ACCEPT = "Accept";
  /** Content-Type header for content negotiation. */
  private static final String CONTENT_TYPE = "Content-Type";
  /** Content-Type value to use for URL-encoded query params included in the POST request body. */
  private static final String FORM_ENCODED = "application/x-www-form-urlencoded";
  
  /** URL-encode a string as UTF-8, catching any thrown UnsupportedEncodingException. */
  private static final String encode(String s) {
    try {
      return URLEncoder.encode(s, UTF_8);
    } catch (UnsupportedEncodingException e) {
      throw new Error("JVM unable to handle UTF-8");
    }
  }
  
  /**
   * Executes a SPARQL HTTP protocol request for the given command, and returns the response.
   * @param command The SPARQL protocol command.
   * @return The HTTP response.
   */
  static HttpResponse executeRequest(ProtocolCommand command, String mimeType) {
    HttpClient client = ((ProtocolConnection)command.getConnection()).getHttpClient();
    URL url = ((ProtocolDataSource)command.getConnection().getDataSource()).getUrl();
    HttpUriRequest req;

    try {
      String params = "query=" + encode(command.getCommand());
      String u = url.toString() + "?" + params;
      if (u.length() > QUERY_LIMIT) {
        // POST connection
        try {
          req = new HttpPost(url.toURI());
        } catch (URISyntaxException e) {
          throw new SparqlException("Endpoint <" + url + "> not in an acceptable format", e);
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
      
      // Add Accept and Content-Type (for POST'ed queries) headers to the request.
      addHeaders(req, mimeType);
      
      // There's a small chance the request could be aborted before it's even executed, we'll have to live with that.
      command.setRequest(req);
      
      HttpResponse response = client.execute(req);
      StatusLine status = response.getStatusLine();
      int code = status.getStatusCode();
      
      // TODO the client doesn't handle redirects for posts; should we do that here?

      if (code >= SUCCESS_MIN && code <= SUCCESS_MAX) {
        return response;
      } else {
        throw new SparqlException("Unexpected status code in server response: " +
            status.getReasonPhrase() + "(" + code + ")");
      }
    } catch (UnsupportedEncodingException e) {
      throw new SparqlException("Unabled to encode data", e);
    } catch (ClientProtocolException cpe) {
      throw new SparqlException("Error in protocol", cpe);
    } catch (IOException e) {
      throw new SparqlException(e);
    }
  }
  
  /**
   * Add headers to a request.
   * @param req The request to set the headers on.
   */
  static void addHeaders(HttpUriRequest req, String mimeType) {
    if (POST.equalsIgnoreCase(req.getMethod())) {
      req.addHeader(CONTENT_TYPE, FORM_ENCODED);
    }
    if (mimeType != null) req.setHeader(ACCEPT, mimeType);
  }
}
