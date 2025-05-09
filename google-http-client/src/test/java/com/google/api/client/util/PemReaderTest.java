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

package com.google.api.client.util;

import java.io.InputStream;
import java.io.InputStreamReader;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests {@link PemReader}.
 *
 * @author Yaniv Inbar
 */
@RunWith(JUnit4.class)
public class PemReaderTest {

  private static final byte[] EXPECTED_BYTES = {
    48, -126, 2, 117, 2, 1, 0, 48, 13, 6, 9, 42, -122, 72, -122, -9, 13, 1, 1, 1, 5, 0, 4, -126, 2,
    95, 48, -126, 2, 91, 2, 1, 0, 2, -127, -127, 0, -67, 82, 117, -113, 35, 77, 69, 84, -20, -18,
    63, 94, -74, 75, 97, 60, 52, 56, -35, 101, 87, -93, 16, 68, 102, -93, 20, 49, 66, -88, 61, -123,
    119, -37, 80, 24, -75, -68, 33, -5, 113, -9, 7, -41, 123, 92, -18, 105, -24, 73, 74, 2, -36,
    -56, 44, 90, -51, -4, -119, -96, -35, -47, -120, -29, 65, -118, 16, 106, 89, -114, -99, -34, 84,
    10, -86, 72, 44, 69, 51, 89, -111, -47, 73, 89, -36, -27, -1, -96, -79, -124, -81, 108, -94, 56,
    118, -89, 74, -9, -73, 39, 109, -65, -40, -80, -85, -105, -18, -71, 98, 47, -76, -32, -19, 79,
    -33, 4, -126, -104, 97, -99, -60, 0, -86, 66, -88, 23, 124, -43, 2, 3, 1, 0, 1, 2, -127, -128,
    121, -44, 31, 92, 93, -18, 50, -120, 100, -13, 39, -118, 78, 42, -111, -58, -55, 32, 50, -80,
    45, 69, -4, -120, -41, -73, 103, -98, 15, 115, -18, 42, -2, 38, -2, 18, -8, -105, -71, 18, 114,
    -110, -15, -45, -29, 73, -71, 14, 35, -15, 77, -108, 43, -7, 16, 57, -38, -58, 0, -42, -87, 7,
    86, 91, 49, -112, 10, -2, -83, 49, -104, -123, 51, 116, -46, 3, -120, 100, -88, 77, 113, -115,
    -38, 97, 31, 118, -63, 41, 67, -11, -30, -65, 73, 114, -123, -128, 65, 60, 47, -54, -30, -58, 8,
    -92, 119, 28, -98, 20, -111, 65, 70, 101, 88, -78, -40, -108, 78, 92, 20, -46, -126, -11, -66,
    -10, -37, -87, -115, -67, 2, 65, 0, -5, 116, -123, -16, -115, 32, -35, 36, 81, -125, -128, -10,
    55, 75, -73, 30, -62, 19, -116, -110, 24, -61, -33, -28, -93, -63, -69, 51, -35, -14, -36, -75,
    127, 22, -123, -101, -45, -63, -20, 30, -6, 85, -108, 24, -104, 119, 22, -53, -54, -45, -27,
    120, -24, 44, -111, -21, -104, 101, -75, 102, -13, -2, -120, 59, 2, 65, 0, -64, -66, 114, -88,
    106, -77, -50, 25, -66, 98, 37, -7, 42, 70, -80, 56, 126, 87, -11, 76, -23, 95, -5, 95, -82,
    -88, -125, -119, -9, -113, 121, -10, 57, 36, 37, -26, -12, -126, -1, -45, -78, 16, -25, -101,
    -81, -37, 90, 127, -75, -112, -100, -91, 65, -94, -78, -128, 40, -77, -64, -4, 1, 103, -50, 47,
    2, 64, 52, 14, -21, -85, -31, -117, -20, 60, -104, -93, -95, 15, 88, 99, 84, -122, 9, -88, 2,
    114, 60, -82, 80, -84, 5, 59, 22, -122, -90, 108, -95, 68, -14, 10, -73, -98, -117, 56, -102,
    -87, -49, 41, -24, 127, 47, 17, 120, -90, -72, 87, 38, 42, -31, -26, 88, 79, 110, 61, -96, 80,
    -80, 51, 2, 1, 2, 64, 17, 56, -13, 69, -39, 66, -9, -57, -107, 27, 112, 9, 51, -99, -35, 97, 46,
    -24, -19, 34, 82, 56, 33, 94, 11, 93, 67, 99, -80, -101, 65, 106, -98, -16, 123, -14, -121, 38,
    -83, 117, 93, 19, -27, -98, 35, -72, -107, -3, -109, 91, -72, -93, -117, -103, -34, 25, 85,
    -119, -70, 84, -54, 75, 92, 65, 2, 64, 30, -82, 75, 100, -32, 123, 48, 18, -75, 26, 80, 109,
    108, -3, -33, -110, -127, -49, 30, -4, -93, 30, 4, 73, 85, -8, -36, 70, -123, -59, -124, 121,
    -101, 95, 28, -55, -1, -23, 83, -91, 111, 54, 60, -40, -100, -81, 11, -45, -118, 11, -51, 0,
    -28, 32, 4, 85, 69, 122, 111, 110, 100, -86, -73, 46
  };

  @Test
  public void testReadFirstSectionAndClose() throws Exception {
    InputStream stream =
        getClass().getClassLoader().getResourceAsStream("com/google/api/client/util/secret.pem");
    PemReader.Section section =
        PemReader.readFirstSectionAndClose(new InputStreamReader(stream), "PRIVATE KEY");
    Assert.assertArrayEquals(EXPECTED_BYTES, section.getBase64DecodedBytes());
  }
}
