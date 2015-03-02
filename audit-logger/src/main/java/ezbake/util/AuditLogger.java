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

import ch.qos.logback.classic.Logger;
import ezbake.base.thrift.EzSecurityToken;
import ezbake.configuration.EzConfiguration;
import ezbake.configuration.EzConfigurationLoaderException;
import ezbakehelpers.ezconfigurationhelpers.application.EzBakeApplicationConfigurationHelper;
import ezbakehelpers.ezconfigurationhelpers.system.SystemConfigurationHelper;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import javax.annotation.Nonnull;
import java.util.Properties;


/**
 *
 * This class will be used to facilitate AUDIT logging in ezbake apps.<br/><br/>
 *
 * <b>Setup</b>: the main program or app initialization will need to first initialize the AuditLogger.<br/><br/>
 *
 *     AuditLoggerConfigurator.setFilePath("/path/to/write/logs/to/");<br/><br/>
 *
 * <b>Usage</b>:<br/><br/>
 *
 *   Create instance for your class:<br/><br/>
 *
 *       <pre>private final static AuditLogger auditLogger = AuditLogger.getAuditLogger(MyClass.class);</pre><br/><br/>
 *
 *    There are a few convenient methods of actually logging the audit event.<br/><br/>
 *
 *    <b>First</b>:<br/><br/>
 *
 *    <pre>
 *        public Result createFooBar(String name, String something, EzSecurityToken token) {
 *                   AuditEvent evt = event(AuditEventType.FileObject_Create, token).arg("start", start).arg("count", count);
 *                   try {
 *                       securityClient.validateReceivedToken(token);
 *                       return service.createSomething(name, something, token);
 *                   } catch (Exception e) {
 *                       evt.failed();
 *                       throw e;
 *                   } finally {
 *                       auditLogger.logEvent(evt);
 *                   }
 *        }
 *    </pre><br/><br/>
 *
 *    <b>Second</b>:<br/><br/>
 *
 *    <pre>
 *        public Result createFooBar(String name, String something, EzSecurityToken token) {
 *                   boolean success = true;
 *                   try {
 *                       securityClient.validateReceivedToken(token);
 *                       return service.createSomething(name, something, securityToken);
 *                   } catch (Exception e) {
 *                       success = false;
 *                       throw e;
 *                   } finally {
 *                       auditLogger.logEvent(AuditEventType.FileObject_Create, success, token,
 *                       String.format("name=%s, something=%s", name, something));
 *                   }
 *        }
 *    </pre><br/><br/>
 *
 *    <b>Third</b><br/><br/>
 *    Is the same as the second, but you can just use a string instead of the Enum as the first argument to the AuditEvent
 */
public class AuditLogger {

    public static AuditLogger getAuditLogger(Class clz) {
        return new AuditLogger(clz);
    }

    /**
     * Gets a logger using the default configuration to load the logger path
     * @param clz The class for the underlying logger
     * @return A new AuditLogger
     * @throws EzConfigurationLoaderException If it fails to load EzConfiguration
     */
    public static AuditLogger getDefaultAuditLogger(Class clz) throws EzConfigurationLoaderException {
        return getDefaultAuditLogger(clz, new EzConfiguration().getProperties());
    }

    /**
     * Gets a logger using configuration properties based on the passed in properties
     * @param clz The class for the underlying logger
     * @param properties The properties to use to get the log path
     * @return A new AuditLogger
     */
    public static AuditLogger getDefaultAuditLogger(Class clz, Properties properties) {
        EzBakeApplicationConfigurationHelper appConfig = new EzBakeApplicationConfigurationHelper(properties);
        String applicationName = appConfig.getApplicationName();
        String serviceName = appConfig.getServiceName();

        SystemConfigurationHelper sysConf = new SystemConfigurationHelper(properties);
        String logFilePath = sysConf.getLogFilePath(applicationName, serviceName);
        AuditLoggerConfigurator.setFilePath(logFilePath);
        return new AuditLogger(clz);
    }

    // The one logger that will be shared by any class that uses auditLogger.AuditLogger
    private static Logger logbackLogger = AuditLoggerConfigurator.getAuditLogBackLogger();
    // A marker for each of the types of Audit-able events.
    private final static Marker AUDIT = MarkerFactory.getMarker("AUDIT");

    private static final String AUDIT_EVENT_FMT = "[{}] {} [app={}] [user={}] [message={}]";

    private Class clz;

    public AuditLogger() {
        this(AuditLogger.class);
    }

    public AuditLogger(Class clz) {
        this.clz = clz;
    }


    // Collection of very general wrappers for the debug log level. Should generally not be used.
    public void log(String msg) {
        logbackLogger.info(AUDIT, msg);
    }
    public void log(String format, Object arg) {
        logbackLogger.info(AUDIT, format,arg);
    }
    public void log(String format, Object [] argArray) {
        logbackLogger.info(AUDIT, format, argArray);
    }
    public void log(String format, Object arg1, Object arg2) {
        logbackLogger.info(AUDIT,  format,  arg1,  arg2);
    }

    public void logEvent(@Nonnull String auditEvent, boolean success, @Nonnull EzSecurityToken securityToken, @Nonnull String msg) {
        //noinspection ConstantConditions
        if(securityToken == null){
            throw new IllegalStateException("No user token given.");
        }

        final String userName = securityToken.tokenPrincipal.principal;
        final String appName = securityToken.validity.issuedTo;
        final String successStr =  success ? "SUCCESS" : "FAILED";

        logbackLogger.info(AUDIT, AUDIT_EVENT_FMT, new Object[]{auditEvent, successStr, appName, userName, msg});
    }

    public void logEvent(AuditEventType auditEvent, boolean success, EzSecurityToken securityToken, String msg) {
        logEvent(auditEvent.getName(), success, securityToken, msg);
    }

    /**
     * Log an auditEvent to the audit log.
     *
     * @param auditEvent the audit event, see class document
     */
    public void logEvent(AuditEvent auditEvent) {
        logEvent(auditEvent.getEventName(), auditEvent.isSuccess(), auditEvent.getSecurityToken(), auditEvent.msg());
    }
}