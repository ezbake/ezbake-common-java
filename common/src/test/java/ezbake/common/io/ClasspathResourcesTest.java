/*   Copyright (C) 2013-2014 Computer Sciences Corporation
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
 * limitations under the License. */

package ezbake.common.io;

import com.google.common.io.Resources;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for {@link ezbake.common.io.ClasspathResources}
 */
public class ClasspathResourcesTest {

   @Test
   public void testGetResource() throws IOException {
        Assert.assertNull(ClasspathResources.getResource("/ResourceDoesNotExist"));    
        String expected = "abcdefghijklmnopqrstuvwxyz";
        String x = Resources.toString(ClasspathResources.getResource("/abcs.txt"), Charset.defaultCharset()).trim();
        Assert.assertEquals(expected, x);
   }
    
  @Test
 public void testGetResourceWithNullThreadContextLoader() throws IOException {
    ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
    try {
        Thread.currentThread().setContextClassLoader(null);
        String expected = "abcdefghijklmnopqrstuvwxyz";
        String x = Resources.toString(ClasspathResources.getResource("/abcs.txt"), Charset.defaultCharset()).trim();
        Assert.assertEquals(expected, x);
    } finally {
        Thread.currentThread().setContextClassLoader(oldLoader);
    }
 }
}
