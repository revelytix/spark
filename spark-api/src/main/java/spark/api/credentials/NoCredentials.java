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
package spark.api.credentials;

import spark.api.Credentials;

/**
 * A Credentials implementation when no credentials are necessary.  One common 
 * use for this is when accessing a public SPARQL endpoint.
 */
public final class NoCredentials implements Credentials {

  /**
   * The shared stateless instance.
   */
  public static NoCredentials INSTANCE = new NoCredentials();
  
  private NoCredentials() {}
  
  @Override
  public int hashCode() { 
    return 0;
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof NoCredentials;
  }  
}
