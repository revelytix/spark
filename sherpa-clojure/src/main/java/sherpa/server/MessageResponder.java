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
package sherpa.server;

import org.apache.avro.Protocol.Message;

/**
 * Simpler interface that avoids overloaded methods for reification in Clojure.
 */
public interface MessageResponder {

  /**
   * Server should respond to a generic Avro request with a generic Avro response. 
   * @param message The incoming protocol message
   * @param request The request object payload
   * @return The response object payload
   * @throws Exception
   */
  Object respond(Message message, Object request) throws Exception;

}
