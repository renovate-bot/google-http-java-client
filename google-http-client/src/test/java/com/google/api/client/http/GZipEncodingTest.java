/*
 * Copyright (c) 2013 Google Inc.
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

import static org.junit.Assert.assertFalse;

import com.google.api.client.testing.util.TestableByteArrayOutputStream;
import com.google.api.client.util.ByteArrayStreamingContent;
import com.google.api.client.util.StringUtils;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests {@link GZipEncoding}.
 *
 * @author Yaniv Inbar
 */
@RunWith(JUnit4.class)
public class GZipEncodingTest {

  private static final byte[] EXPECED_ZIPPED =
      new byte[] {
        31, -117, 8, 0, 0, 0, 0, 0, 0, -1, -53, -49, -57, 13, 0, -30, -66, -14, 54, 28, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0
      };

  // TODO: remove when no longer using Java < 16: https://bugs.openjdk.java.net/browse/JDK-8244706
  @Deprecated
  private static final byte[] EXPECED_ZIPPED_BELOW_JAVA_16 =
      new byte[] {
        31, -117, 8, 0, 0, 0, 0, 0, 0, 0, -53, -49, -57, 13, 0, -30, -66, -14, 54, 28, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0
      };

  @Test
  public void test() throws IOException {
    // TODO: remove when no longer using Java < 16.
    byte[] expected =
        System.getProperty("java.version").compareTo("16") >= 0
            ? EXPECED_ZIPPED
            : EXPECED_ZIPPED_BELOW_JAVA_16;

    GZipEncoding encoding = new GZipEncoding();
    ByteArrayStreamingContent content =
        new ByteArrayStreamingContent(StringUtils.getBytesUtf8("oooooooooooooooooooooooooooo"));
    TestableByteArrayOutputStream out = new TestableByteArrayOutputStream();
    encoding.encode(content, out);
    assertFalse(out.isClosed());
    Assert.assertArrayEquals(expected, out.getBuffer());
  }
}
