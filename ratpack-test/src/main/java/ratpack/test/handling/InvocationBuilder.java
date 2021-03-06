/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ratpack.test.handling;

import ratpack.handling.Handler;

/**
 * Builds the context fot the invocation of a {@link Handler} (for unit testing handler implementations).
 *
 * @see ratpack.test.UnitTest#invoke(ratpack.handling.Handler, ratpack.func.Action)
 */
public interface InvocationBuilder {

  Invocation invoke(Handler handler) throws InvocationTimeoutException;

  InvocationBuilder header(String name, String value);

  InvocationBuilder body(byte[] bytes, String contentType);

  InvocationBuilder body(String text, String contentType);

  InvocationBuilder responseHeader(String name, String value);

  InvocationBuilder method(String method);

  InvocationBuilder uri(String uri);

  InvocationBuilder timeout(int timeout);

  InvocationBuilder register(Object object);
}
