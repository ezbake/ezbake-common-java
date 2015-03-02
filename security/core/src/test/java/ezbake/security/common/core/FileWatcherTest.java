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

package ezbake.security.common.core;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * User: jhastings
 * Date: 8/14/14
 * Time: 9:56 PM
 */
public class FileWatcherTest {
    private static final Logger logger = LoggerFactory.getLogger(FileWatcherTest.class);
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @After
    public void cleanFileSystem() throws IOException {
        FileUtils.cleanDirectory(folder.getRoot());
    }

    @Test
    public void testEvents() throws IOException, InterruptedException {
        final String contents = "TESTSTSETSTSTETSTSETST";

        FileWatcher w = new FileWatcher(Paths.get(folder.getRoot()+"/testFile"), new FileWatcher.FileWatchUpdater() {
            @Override
            public boolean loadUpdate(InputStream is) {
                try {
                    byte[] bytes = new byte[contents.length()];
                    is.read(bytes, 0, contents.length());
                    logger.debug("Read bytes {}", bytes);
                    Assert.assertEquals(contents, new String(bytes));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return false;
            }

            @Override
            public void close() throws IOException {

            }
        });
        Thread watchThread = new Thread(w);
        watchThread.start();

        Thread.sleep(50);

        File f2 = folder.newFile("otherFile");
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(f2.toPath(), Charset.defaultCharset()))){
            writer.println("HTESTS");
        }
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(f2.toPath(), Charset.defaultCharset()))){
            writer.println("HTElkajsldkfjSTS");
        }

        File watchPath = folder.newFile("testFile");
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(watchPath.toPath(), Charset.defaultCharset()))){
            writer.println(contents);
        }

        watchThread.join(10*1000);
    }

}
