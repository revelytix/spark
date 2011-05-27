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
package spark.spi.rdf;

import spark.api.rdf.BlankNode;

/**
 * Basic implementation of {@link BlankNode}.
 * 
 * toString as:  _:label
 */
public class BlankNodeImpl implements BlankNode {

  private final String label;
  
  /**
   * Construct a new BlankNode with a label
   * @param label
   */
  public BlankNodeImpl(String label) {
    this.label = label;
  }
  
  @Override
  public String getLabel() {
    return this.label;
  }

  
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((label == null) ? 0 : label.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (!(obj instanceof BlankNode))
      return false;
    BlankNode other = (BlankNode) obj;
    if (label == null) {
      if (other.getLabel() != null)
        return false;
    } else if (!label.equals(other.getLabel()))
      return false;
    return true;
  }

  public String toString() {
    return "_:" + label;
  }
}
