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

package ezbake.util;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.boolex.OnMarkerEvaluator;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.filter.EvaluatorFilter;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.RollingPolicy;
import ch.qos.logback.core.spi.FilterReply;
import ezbake.logging.logback.ExternalRotateRollingPolicy;
import org.slf4j.LoggerFactory;

public class AuditLoggerConfigurator {

    private static final Logger logbackLogger = ((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger("AUDIT_LOGGER");
    private static final String ROTATE_APPENDER_NAME = "ROTATE_APPENDER";
    private static boolean hasInitialized = false;


    public static Logger getAuditLogBackLogger() {
        return logbackLogger;
    }


    //This changes the location new log files will be saved. The previous location will still have the old logs.
    public static void setFilePath(String filePath){
        if (!hasInitialized) {
            auditLoggerInitializer(filePath);
        } else {
            RollingFileAppender appender = (RollingFileAppender) logbackLogger.getAppender(ROTATE_APPENDER_NAME);
            configureAppender(filePath, appender);
        }
    }

    public static void setAdditivity(boolean additivity){
        logbackLogger.setAdditive(additivity);
    }

    @SuppressWarnings("unchecked")
    private static void auditLoggerInitializer(String filePath) {
        // This an instance of the LoggerFactory bound internally at runtime time.
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();


        // Check to see if using default configuration, if so don't output to console.
        if (loggerContext.getLoggerList().size() == 1) {
            Logger root = loggerContext.getLogger("ROOT");
            root.detachAppender("console");
        }

        // Just a simple pattern for outputting the messages, includes the ISO8601 formatted timestamp and marker.
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(loggerContext);
        encoder.setPattern("[%d{ISO8601}] %msg%n");
        encoder.start();


        // This is the appender, the object that actually outputs to the file. It gets attached to the Logger.
        RollingFileAppender appender = new RollingFileAppender();
        appender.setName(ROTATE_APPENDER_NAME);
        appender.setContext(loggerContext);
        appender.setEncoder(encoder);
        configureAppender(filePath, appender);

        // Use the external rotate policy
        ExternalRotateRollingPolicy policy = new ExternalRotateRollingPolicy(appender);
        policy.setContext(loggerContext);
        policy.start();
        appender.setRollingPolicy(policy);

        // This filter checks each log for the AUDIT marker. It will only accept those logs with the AUDIT marker.
        EvaluatorFilter evalFilter= new EvaluatorFilter();
        OnMarkerEvaluator markerEval= new OnMarkerEvaluator();
        markerEval.addMarker("AUDIT");
        markerEval.start();
        evalFilter.setEvaluator(markerEval);
        evalFilter.setOnMatch(FilterReply.ACCEPT);
        evalFilter.setOnMismatch(FilterReply.DENY);
        evalFilter.start();
        appender.addFilter(evalFilter);

        // Get a new logger from the loggerContext then make sure there are no other appenders and attach rfAppender.
        logbackLogger.detachAndStopAllAppenders();
        appender.start();
        logbackLogger.addAppender(appender);
        logbackLogger.setLevel(Level.INFO);
        logbackLogger.setAdditive(false);
        hasInitialized = true;
    }

    private static void configureAppender(String filePath, FileAppender appender) {
        if (appender.isStarted()) {
            appender.stop();
        }
        appender.setFile(filePath.replace(".log", ".auditLogFile.log"));
        appender.start();
    }


}
