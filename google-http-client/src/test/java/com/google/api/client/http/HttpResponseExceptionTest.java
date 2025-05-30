/*
 * Copyright (c) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.api.client.http;

import static com.google.api.client.testing.http.HttpTesting.SIMPLE_GENERIC_URL;
import static com.google.api.client.util.StringUtils.LINE_SEPARATOR;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.google.api.client.http.HttpResponseException.Builder;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;
import com.google.api.client.util.ExponentialBackOff;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests {@link HttpResponseException}.
 *
 * @author Yaniv Inbar
 */
@RunWith(JUnit4.class)
public class HttpResponseExceptionTest {

  @Test
  public void testConstructor() throws Exception {
    HttpTransport transport = new MockHttpTransport();
    HttpRequest request = transport.createRequestFactory().buildGetRequest(SIMPLE_GENERIC_URL);
    HttpResponse response = request.execute();
    HttpHeaders headers = response.getHeaders();
    HttpResponseException responseException = new HttpResponseException(response);
    assertThat(responseException).hasMessageThat().isEqualTo("200\nGET " + SIMPLE_GENERIC_URL);
    assertNull(responseException.getContent());
    assertEquals(200, responseException.getStatusCode());
    assertNull(responseException.getStatusMessage());
    assertTrue(headers == responseException.getHeaders());
  }

  @Test
  public void testBuilder() throws Exception {
    HttpHeaders headers = new HttpHeaders();
    Builder builder =
        new HttpResponseException.Builder(9, "statusMessage", headers)
            .setMessage("message")
            .setContent("content");
    assertEquals("message", builder.getMessage());
    assertEquals("content", builder.getContent());
    assertEquals(9, builder.getStatusCode());
    assertEquals("statusMessage", builder.getStatusMessage());
    assertTrue(headers == builder.getHeaders());
    HttpResponseException e = builder.build();
    assertEquals("message", e.getMessage());
    assertEquals("content", e.getContent());
    assertEquals(9, e.getStatusCode());
    assertEquals("statusMessage", e.getStatusMessage());
    assertTrue(headers == e.getHeaders());
  }

  @Test
  public void testConstructorWithStatusMessage() throws Exception {
    HttpTransport transport =
        new MockHttpTransport() {
          @Override
          public LowLevelHttpRequest buildRequest(String method, String url) throws IOException {
            return new MockLowLevelHttpRequest() {
              @Override
              public LowLevelHttpResponse execute() throws IOException {
                MockLowLevelHttpResponse result = new MockLowLevelHttpResponse();
                result.setReasonPhrase("OK");
                return result;
              }
            };
          }
        };
    HttpRequest request = transport.createRequestFactory().buildGetRequest(SIMPLE_GENERIC_URL);
    HttpResponse response = request.execute();
    HttpResponseException responseException = new HttpResponseException(response);
    assertEquals("OK", responseException.getStatusMessage());
  }

  @Test
  public void testConstructor_noStatusCode() throws Exception {
    HttpTransport transport =
        new MockHttpTransport() {
          @Override
          public LowLevelHttpRequest buildRequest(String method, String url) throws IOException {
            return new MockLowLevelHttpRequest() {
              @Override
              public LowLevelHttpResponse execute() throws IOException {
                MockLowLevelHttpResponse result = new MockLowLevelHttpResponse();
                result.setStatusCode(0);
                return result;
              }
            };
          }
        };
    final HttpRequest request =
        transport.createRequestFactory().buildGetRequest(SIMPLE_GENERIC_URL);
    HttpResponseException responseException =
        assertThrows(
            HttpResponseException.class,
            new ThrowingRunnable() {
              @Override
              public void run() throws Throwable {
                request.execute();
              }
            });
    assertThat(responseException).hasMessageThat().isEqualTo("GET " + SIMPLE_GENERIC_URL);
  }

  @Test
  public void testConstructor_messageButNoStatusCode() throws Exception {
    HttpTransport transport =
        new MockHttpTransport() {
          @Override
          public LowLevelHttpRequest buildRequest(String method, String url) throws IOException {
            return new MockLowLevelHttpRequest() {
              @Override
              public LowLevelHttpResponse execute() throws IOException {
                MockLowLevelHttpResponse result = new MockLowLevelHttpResponse();
                result.setStatusCode(0);
                result.setReasonPhrase("Foo");
                return result;
              }
            };
          }
        };
    final HttpRequest request =
        transport.createRequestFactory().buildGetRequest(SIMPLE_GENERIC_URL);
    HttpResponseException responseException =
        assertThrows(
            HttpResponseException.class,
            new ThrowingRunnable() {
              @Override
              public void run() throws Throwable {
                request.execute();
              }
            });
    assertThat(responseException).hasMessageThat().isEqualTo("Foo\nGET " + SIMPLE_GENERIC_URL);
  }

  @Test
  public void testComputeMessage() throws Exception {
    HttpTransport transport =
        new MockHttpTransport() {
          @Override
          public LowLevelHttpRequest buildRequest(String method, String url) throws IOException {
            return new MockLowLevelHttpRequest() {
              @Override
              public LowLevelHttpResponse execute() throws IOException {
                MockLowLevelHttpResponse result = new MockLowLevelHttpResponse();
                result.setReasonPhrase("Foo");
                return result;
              }
            };
          }
        };
    HttpRequest request = transport.createRequestFactory().buildGetRequest(SIMPLE_GENERIC_URL);
    HttpResponse response = request.execute();
    assertThat(HttpResponseException.computeMessageBuffer(response).toString())
        .isEqualTo("200 Foo\nGET " + SIMPLE_GENERIC_URL);
  }

  @Test
  public void testThrown() throws Exception {
    HttpTransport transport =
        new MockHttpTransport() {
          @Override
          public LowLevelHttpRequest buildRequest(String method, String url) throws IOException {
            return new MockLowLevelHttpRequest() {
              @Override
              public LowLevelHttpResponse execute() throws IOException {
                MockLowLevelHttpResponse result = new MockLowLevelHttpResponse();
                result.setStatusCode(HttpStatusCodes.STATUS_CODE_NOT_FOUND);
                result.setReasonPhrase("Not Found");
                result.setContent("Unable to find resource");
                return result;
              }
            };
          }
        };
    final HttpRequest request =
        transport.createRequestFactory().buildGetRequest(SIMPLE_GENERIC_URL);
    HttpResponseException responseException =
        assertThrows(
            HttpResponseException.class,
            new ThrowingRunnable() {
              @Override
              public void run() throws Throwable {
                request.execute();
              }
            });

    assertThat(responseException)
        .hasMessageThat()
        .isEqualTo(
            "404 Not Found\nGET "
                + SIMPLE_GENERIC_URL
                + LINE_SEPARATOR
                + "Unable to find resource");
    // no retries expected
    assertEquals(1, responseException.getAttemptCount());
  }

  @Test
  public void testInvalidCharset() throws Exception {
    HttpTransport transport =
        new MockHttpTransport() {
          @Override
          public LowLevelHttpRequest buildRequest(String method, String url) throws IOException {
            return new MockLowLevelHttpRequest() {
              @Override
              public LowLevelHttpResponse execute() throws IOException {
                MockLowLevelHttpResponse result = new MockLowLevelHttpResponse();
                result.setStatusCode(HttpStatusCodes.STATUS_CODE_NOT_FOUND);
                result.setReasonPhrase("Not Found");
                result.setContentType("text/plain; charset=");
                result.setContent("Unable to find resource");
                return result;
              }
            };
          }
        };
    final HttpRequest request =
        transport.createRequestFactory().buildGetRequest(SIMPLE_GENERIC_URL);
    HttpResponseException responseException =
        assertThrows(
            HttpResponseException.class,
            new ThrowingRunnable() {
              @Override
              public void run() throws Throwable {
                request.execute();
              }
            });

    assertThat(responseException)
        .hasMessageThat()
        .isEqualTo("404 Not Found\nGET " + SIMPLE_GENERIC_URL);
  }

  @Test
  public void testAttemptCountWithBackOff() throws Exception {
    HttpTransport fakeTransport =
        new MockHttpTransport() {
          @Override
          public LowLevelHttpRequest buildRequest(String method, String url) throws IOException {
            return new MockLowLevelHttpRequest() {
              @Override
              public LowLevelHttpResponse execute() throws IOException {
                MockLowLevelHttpResponse result = new MockLowLevelHttpResponse();
                result.setStatusCode(HttpStatusCodes.STATUS_CODE_SERVER_ERROR);
                result.setReasonPhrase("Error");
                result.setContent("Unknown Error");
                return result;
              }
            };
          }
        };
    ExponentialBackOff backoff = new ExponentialBackOff.Builder().build();
    final HttpRequest request =
        fakeTransport.createRequestFactory().buildGetRequest(new GenericUrl("http://not/used"));
    request.setUnsuccessfulResponseHandler(
        new HttpBackOffUnsuccessfulResponseHandler(backoff)
            .setBackOffRequired(
                new HttpBackOffUnsuccessfulResponseHandler.BackOffRequired() {
                  public boolean isRequired(HttpResponse response) {
                    return true;
                  }
                }));
    request.setNumberOfRetries(1);
    HttpResponseException responseException =
        assertThrows(
            HttpResponseException.class,
            new ThrowingRunnable() {
              @Override
              public void run() throws Throwable {
                request.execute();
              }
            });

    assertEquals(500, responseException.getStatusCode());
    // original request and 1 retry - total 2
    assertEquals(2, responseException.getAttemptCount());
  }

  @Test
  public void testUnsupportedCharset() throws Exception {
    HttpTransport transport =
        new MockHttpTransport() {
          @Override
          public LowLevelHttpRequest buildRequest(String method, String url) throws IOException {
            return new MockLowLevelHttpRequest() {
              @Override
              public LowLevelHttpResponse execute() throws IOException {
                MockLowLevelHttpResponse result = new MockLowLevelHttpResponse();
                result.setStatusCode(HttpStatusCodes.STATUS_CODE_NOT_FOUND);
                result.setReasonPhrase("Not Found");
                result.setContentType("text/plain; charset=invalid-charset");
                result.setContent("Unable to find resource");
                return result;
              }
            };
          }
        };
    final HttpRequest request =
        transport.createRequestFactory().buildGetRequest(SIMPLE_GENERIC_URL);
    HttpResponseException responseException =
        assertThrows(
            HttpResponseException.class,
            new ThrowingRunnable() {
              @Override
              public void run() throws Throwable {
                request.execute();
              }
            });
    assertThat(responseException)
        .hasMessageThat()
        .isEqualTo("404 Not Found\nGET " + SIMPLE_GENERIC_URL);
  }

  @Test
  public void testSerialization() throws Exception {
    HttpTransport transport = new MockHttpTransport();
    HttpRequest request = transport.createRequestFactory().buildGetRequest(SIMPLE_GENERIC_URL);
    HttpResponse response = request.execute();
    HttpResponseException responseException = new HttpResponseException(response);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ObjectOutput s = new ObjectOutputStream(out);
    s.writeObject(responseException);
    ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
    ObjectInputStream objectInput = new ObjectInputStream(in);
    HttpResponseException e2 = (HttpResponseException) objectInput.readObject();
    assertEquals(responseException.getMessage(), e2.getMessage());
    assertEquals(responseException.getStatusCode(), e2.getStatusCode());
    assertNull(e2.getHeaders());
  }
}
