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

package ezbake.logging.logback;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.rolling.RollingFileAppender;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * User: jhastings
 * Date: 11/13/14
 * Time: 4:12 PM
 */
public class TestExternalRotateRollingPolicy {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    public Logger getLogger() {
        LoggerContext  context = (LoggerContext)LoggerFactory.getILoggerFactory();
        Logger rootLogger = context.getLogger("ROOT");
        rootLogger.detachAppender("console");

        // Just a simple pattern for outputting the messages, includes the ISO8601 formatted timestamp and marker.
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern("%msg");
        encoder.start();

        RollingFileAppender appender = new RollingFileAppender<>();
        appender.setContext(context);
        appender.setRollingPolicy(new ExternalRotateRollingPolicy());
        appender.getRollingPolicy().setParent(appender);
        appender.setFile(new File(folder.getRoot(), "log").getAbsolutePath());

        appender.setEncoder(encoder);
        appender.start();
        rootLogger.addAppender(appender);
        context.start();
        return rootLogger;

    }

    @Test
    public void testLogFiles() throws IOException {
        Logger logger = getLogger();

        // Log some stuff
        logger.info("HELLO");
        logger.info("HELLO");

        // Rename the log file
        new File(folder.getRoot(), "log").renameTo(new File(folder.getRoot(), "log.1"));

        // Log to the new file
        logger.info("HELLO2");

        // Needed to flush the logs
        logger.detachAndStopAllAppenders();

        String fileText = Files.toString(new File(folder.getRoot(), "log"), StandardCharsets.UTF_8);
        Assert.assertEquals("HELLO2", fileText);
    }

}
