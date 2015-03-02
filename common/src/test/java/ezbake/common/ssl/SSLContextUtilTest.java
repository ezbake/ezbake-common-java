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

package ezbake.common.ssl;

import com.google.common.io.CharStreams;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * User: jhastings
 * Date: 10/1/14
 * Time: 9:34 AM
 */
public class SSLContextUtilTest {

    // For this to work properly this test must be run from the module directory
    String filePath = "src/test/resources/abcs.txt";
    String classPath = "/abcs.txt";

    @Test
    public void testGetInputStream() throws IOException {
        String contents = CharStreams.toString(Files.newBufferedReader(Paths.get(filePath), StandardCharsets.UTF_8));
        String fileContents = CharStreams.toString(new InputStreamReader(SSLContextUtil.getInputStream(filePath)));
        Assert.assertEquals(contents, fileContents);

        String classpathContents = CharStreams.toString(new InputStreamReader(SSLContextUtil.getInputStream(classPath)));
        Assert.assertEquals(contents, classpathContents);
    }
}
