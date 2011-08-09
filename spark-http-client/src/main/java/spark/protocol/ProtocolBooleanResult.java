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

import java.util.Collections;
import java.util.List;

import spark.api.Command;
import spark.spi.BooleanResultImpl;

/**
 * Extension of the default {@link BooleanResult} implementation with SPARQL protocol metadata fields.
 * No need to create format-specific implementations since they all carry the same information.
 * 
 * @author Alex Hall
 * @created Aug 9, 2011
 */
public class ProtocolBooleanResult extends BooleanResultImpl implements ProtocolResult {

  private final List<String> metadata;
  
  /**
   * Instantiates the protocol boolean result.
   * @param command The command which originated the request.
   * @param result The boolean result value.
   * @param metadata The metadata associated with the result.
   */
  public ProtocolBooleanResult(Command command, boolean result, List<String> metadata) {
    super(command, result);
    this.metadata = (metadata != null) ? 
        Collections.unmodifiableList(metadata) : Collections.<String>emptyList();
  }

  /* (non-Javadoc)
   * @see spark.protocol.ProtocolResult#getMetadata()
   */
  @Override
  public List<String> getMetadata() {
    return metadata;
  }

}
