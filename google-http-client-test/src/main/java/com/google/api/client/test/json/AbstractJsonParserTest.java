/**
 * Copyright 2019 Google LLC
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>https://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.api.client.test.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public abstract class AbstractJsonParserTest {

  protected abstract JsonFactory newJsonFactory();

  private static final String TEST_JSON;
  private static final String TEST_JSON_BIG_DECIMAL;

  static {
    TEST_JSON = "{\"strValue\": \"bar\", \"intValue\": 123, \"boolValue\": false}";
    TEST_JSON_BIG_DECIMAL = "{\"bigDecimalValue\": 1559341956102}";
  }

  @Test
  public void testParse_basic() throws IOException {
    JsonObjectParser parser = new JsonObjectParser(newJsonFactory());
    InputStream inputStream = new ByteArrayInputStream(TEST_JSON.getBytes(StandardCharsets.UTF_8));
    GenericJson json = parser.parseAndClose(inputStream, StandardCharsets.UTF_8, GenericJson.class);

    assertTrue(json.get("strValue") instanceof String);
    assertEquals("bar", json.get("strValue"));
    assertTrue(json.get("intValue") instanceof BigDecimal);
    assertEquals(new BigDecimal(123), json.get("intValue"));
    assertTrue(json.get("boolValue") instanceof Boolean);
    assertEquals(Boolean.FALSE, json.get("boolValue"));
  }

  @Test
  public void testGetWrongType() throws IOException {
    JsonObjectParser parser = new JsonObjectParser(newJsonFactory());
    InputStream inputStream = new ByteArrayInputStream(TEST_JSON.getBytes(StandardCharsets.UTF_8));
    GenericJson json = parser.parseAndClose(inputStream, StandardCharsets.UTF_8, GenericJson.class);
    assertTrue(json.get("strValue") instanceof String);
    assertEquals("bar", json.get("strValue"));
    assertTrue(json.get("intValue") instanceof BigDecimal);
    assertEquals(new BigDecimal(123), json.get("intValue"));
    assertTrue(json.get("boolValue") instanceof Boolean);
    assertEquals(Boolean.FALSE, json.get("boolValue"));
  }

  @Test
  public void testParse_badJson() throws IOException {
    JsonObjectParser parser = new JsonObjectParser(newJsonFactory());
    InputStream inputStream = new ByteArrayInputStream("not json".getBytes(StandardCharsets.UTF_8));
    try {
      parser.parseAndClose(inputStream, StandardCharsets.UTF_8, GenericJson.class);
      fail("Malformed JSON not detected");
    } catch (IOException ex) {
      Assert.assertNotNull(ex.getMessage());
    }
  }

  @Test
  public void testParse_bigDecimal() throws IOException {
    JsonObjectParser parser = new JsonObjectParser(newJsonFactory());
    InputStream inputStream =
        new ByteArrayInputStream(TEST_JSON_BIG_DECIMAL.getBytes(StandardCharsets.UTF_8));
    GenericJson json = parser.parseAndClose(inputStream, StandardCharsets.UTF_8, GenericJson.class);

    assertTrue(json.get("bigDecimalValue") instanceof BigDecimal);
    assertEquals(new BigDecimal("1559341956102"), json.get("bigDecimalValue"));
  }
}
