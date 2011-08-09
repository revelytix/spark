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

import java.util.List;

import spark.api.Result;

/**
 * Many query result formats used by SPARQL protocol services provide fields for a result to
 * specify additional metadata in their headers, such as links to additional information. Result
 * objects originating from such formats should implement this interface in order to provide access
 * to those metadata fields.
 * 
 * @see <a href="http://www.w3.org/TR/rdf-sparql-XMLres/#head">SPARQL XML Results</a>
 * @see <a href="http://www.w3.org/2009/sparql/docs/json-results/json-results-lc.html#select-link">SPARQL JSON Results</a>
 * 
 * @author Alex Hall
 * @created Aug 9, 2011
 */
public interface ProtocolResult extends Result {

  /**
   * Gets any metadata supplied by the service in addition to the query result data.
   * @return A list of metadata, which are typically URIs of documents with additional information.
   */
  List<String> getMetadata();
}
