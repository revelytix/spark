package spark.spi.rdf;

import java.net.URI;

import spark.api.rdf.NamedNode;

public class NamedNodeImpl implements NamedNode {

  private final URI uri;
  
  public NamedNodeImpl(URI uri) {
    if(uri == null) {
      throw new NullPointerException();
    }
    this.uri = uri;
  }
  
  @Override
  public URI getURI() {
    return this.uri;
  }

  @Override
  public int hashCode() {
    return uri.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj instanceof NamedNode) {
      return this.uri.equals(((NamedNode)obj).getURI());
    } else {
      return false;
    }
  }
  
  @Override
  public String toString() {
    return "<" + uri.toString() + ">";
  }
}

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