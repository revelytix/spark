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

import java.net.MalformedURLException;
import java.net.URL;

import spark.api.Credentials;
import spark.api.DataSource;

public class ProtocolDataSource implements DataSource {

  private final URL url;
  
  public ProtocolDataSource(String url) throws MalformedURLException {
    this(new URL(url));
  }
  
  public ProtocolDataSource(URL url) {
    this.url = url;
  }
  
  public URL getUrl() {
    return this.url;
  }
  
  @Override
  public ProtocolConnection getConnection(Credentials creds) {
    return new ProtocolConnection(this, creds);
  }

}
