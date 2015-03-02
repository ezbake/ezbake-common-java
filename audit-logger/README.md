# Audit Logger

This gives an overview of the AuditLogger library.

## Usage

In order to use the logger just make the class like any other object:

    private static final AuditLogger auditLogger = new AuditLogger(MyClass.class);

## Logging

### Log File Names
When logging, a new log file will be created every day. The name of the log that it is currently being logged to is `auditLogFile.currentDay.log`. Once the day rolls over and a new log file is created the old log file will be renamed to the pattern `auditLogFile.%d{yyyy-MM-dd}.log`.

### Logging an Event
Ultimately the job of the AuditLogger class is to log audit-able events.<br/>
The main method for logging is

    public void AuditLogger::logEvent(AuditEvent auditEvent)

Example use:

    import ezbake.util.AuditLogger;
    import ezbake.util.AuditEvent;
    import ezbake.util.AuditEventType;
    import ezbake.base.thrift.EzSecurityToken;
    import static ezbake.util.AuditEvent.event;
    
    class YourClass {
        private final static AuditLogger auditLogger = AuditLogger.getAuditLogger(YourClass.class); 
        //...
        public void yourFunction(String yourArgument, EzSecurityToken token) {
            AuditEvent evt = event(AuditEventType.FileObject_Create.name(), token).arg("yourArgument", yourArgument);
            try {
                doSomething();
            } catch (Exception e) {
                evt.failed();
            } finally {
                auditLogger.logEvent(evt)
            }
        }

Suggestions:

  - Log each argument thats important to audit for your events.  But nothing more.
  - Use the try/finally model to ensure that you edit every event.
  - Make your audit logger a final static member, to prevent multiple copies of it running around.
  - Don't call `AuditLoggerConfigurator.setFilePath` anywhere other than an initialization of an application. 
    - Examples: `main`, `war file initialization`


## Initialization

In order to use the audit logger you must first tell it where to place the logs

For example, this could be done in the ThriftRunner, or on your web application initialization method


    AuditLoggerConfigurator.setFilePath("/path/to/write/logs/to/");

This will create a static logger and set the file path as `/path/to/write/logs/to/AUDIT_LOGS/`.

If you call the `setFilePath` method multiple times, it won't clear or move the old logs, but will start writing to the
new location.


## Dependencies

AuditLogger.java has a few dependencies. They are listed below with the versions used during testing:

* Logback
  * logback-classic-1.1.2 ^[1]
  * logback-core-1.1.2
  * slf4j-api-1.7.6
* EzConfiguration
  * ezConfiguration-1.3.2

### Interactions with Existing Loggers
Traditionally with Logback it is defined with a Logback.xml file. AuditLogger is fully able to be deployed with no other files.
AuditLogger uses a combination of the default logback configuration ^[3] as well as programmatically defined aspects.
The default settings for AuditLogger is to have it's additivity set to false.
This means that it should not interact or interfere with existing logging.

That being said, if you would like to have the logs created by AuditLogger to be see within your general loggers you can
use the method `setAdditivity(boolean additivity).`<br/>
The logs created using AuditLogger will show up under the "AUDIT" marker at the INFO level but will be forwarded to
appenders regardless of the level you have set for the root logger.<br/>
This is due to the nature of logback, since the "AUDIT_LOGGER" logger is accepting them any appender will output the
results if not explicitly told not to. If you would like to deny AuditLogger messages from specific appenders but log it
to others just add a marker filter to your appenders.<br/>
An example of an appender with a marker filter that would deny all messages with the marker AUDIT is given below:

    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
      <filter class="ch.qos.logback.core.filter.EvaluatorFilter">
          <evaluator class="ch.qos.logback.classic.boolex.OnMarkerEvaluator">
          <marker>AUDIT</marker>
          </evaluator>
          <onMismatch>NEUTRAL</onMismatch>
          <onMatch>DENY</onMatch>
      </filter>
    <encoder>
        <pattern>1 %-5marker %-5level %logger{0} - %msg%n</pattern>
    </encoder>
    </appender>

AuditLogger is not guaranteed to work when your general logging is done using log4j.
One issue has been identified in testing when using the original logger was using the "slf4j-log4j12" library.
In testing, removing this library allowed the logging to work correctly.

### Interaction with JBoss
For now, if you want to use the Audit Logger in a JBoss application, you need to disable the log4j module in JBoss.
Add a jboss-deployment-structure.xml to the WEB-INF folder that looks like this:

    <jboss-deployment-structure>
        <deployment>
            <exclusions>
                <module name="org.slf4j" />
                <module name="org.slf4j.impl" />
            </exclusions>
        </deployment>
    </jboss-deployment-structure>

### List of Audit-able Events
The follow audit-able events are defined in the AuditLogger class as string constants to encourage consistency.

  * LogOn = "Logon"
    * Used when a user attempts to log on, successfully or not.
    * Log the user's credentials.
  * LogOff ="Logoff"
    * Used when a user logs out of the system.
    * Log the user's credentials.
  * FileObjectCreate = "File/Object Create"
    * Used when either a file or object is created.
    * Log details regarding the file/object and the user's credentials.
  * FileObjectAccess = "File/Object Access"
    * Used when a file or object is accessed.
    * Log details regarding the file/object and the user's credentials.
  * FileObjectDelete = "File/Object Delete"
    * Used when a user deletes a file or object.
    * Log details regarding the file/object and the user's credentials.
  * FileObjectModify = "File/Object Modify"
    * Used when there are changes to a file or object.
    * Log details regarding the modifications, the file/object and the user's credentials.
  * FileObjectPermissionModifications = "File/Object Permission Modifications"
    * Used when there are changes to the permissions on a file or object.
    * Log details regarding the modifications, the file/object and the user's credentials.
  * FileObjectOwnershipMod ="File/Object Ownership Mod"
    * Used when there are changes to the ownership of file or object.
    * Log details regarding the changes, the file/object and the user's credentials.
  * WritesDownloadsToExternal ="Writes/downloads to external"
    * Used when there is a write or download to an external location.
    * Log details regarding the information written/downloaded, the external location, and the user's credentials.
  * UploadsFromExternalDevicesMedia = "Uploads from external Devices/Media"
    * Used when there is an upload from an external device or media.
    * Log details regarding the information being uploaded, the external device/media and the user's credentials.
  * UserGroupMgmtAdd = "User& Group Mgmt add"
    * Used when either a user or group is added.
    * Log details regarding the new user/group and the user's credentials that made the change.
  * UserGroupMgmtDelete = "User& Group Mgmt delete"
    * Used when either a user or group is deleted.
    * Log details regarding the deleted user/group and the user's credentials that made the change.
  * UserGroupMgmtModify = "User& Group Mgmt modify"
    * Used when either a user or group is modified.
    * Log details regarding the modifications, the modified user/group and the user's credentials that made the change.
  * UserGroupMgmtSuspend = "User& Group Mgmt Suspend"
    * Used when either a user or group is suspended.
    * Log details regarding the suspension, the suspended user/group and the user's credentials that made the change.
  * UserGroupMgmtLock = "User& Group Mgmt lock"
    * Used when either a user or group is locked.
    * Log details regarding the locked user/group and the user's credentials that made the change.
  * UserGroupMgmtGroupRoleAdd = "User& Group Mgmt Group/Role add"
    * Used when a group/role is added.
    * Log details regarding the new group/role and the user's credentials.
  * UserGroupMgmtGroupRoleDelete = "User& Group Mgmt Group/Role delete"
    * Used when a group/role is deleted.
    * Log details regarding the deleted group/role and the user's credentials.
  * UserGroupMgmtGroupRoleModify = "User& Group Mgmt Group/Role modify"
    * Used when a group/role is modified.
    * Log details regarding the modifications, the modified group/role and the user's credentials.
  * UseOfPrivilegedSpecialRightsEventsSecurityOrAuditPolicyChanges = "Use of Privileged/Special Rights Events Security or Audit Policy Changes"
    * Used when a user uses privileged or special rights to change a security or audit policy.
    * Log details regarding rights used, security or audit policy changes, and the user's credentials.
  * UseOfPrivilegedSpecialRightsEventsConfigurationChanges = "Use of Privileged/Special Rights Events Configuration Changes"
    * Used when a user uses privileged or special rights to change configurations.
    * Log details regarding rights used, the configuration changes, and the user's credentials.
  * AdminOrRootLevelAccess = "Admin or Root Level Access"
    * Used when a user accesses admin or root level.
    * Log the user's credentials.
  * PrivilegeRoleEscalation = "Privilege/Role Escalation"
    * Used when the privileges or role of a user is escalated.
    * Log details regarding the escalation and the user's credentials.
  * AuditAndLogDataAccess = "Audit and Log data Access"
    * Used when a user accesses the data for the audit log or other general log files.
    * Log details regarding the data accessed and the user's credentials.
  * SystemRebootRestartShutdown = "System Reboot, Restart, & Shutdown"
    * Used when the system is rebooted, restarted or shutdown.
    * Log details regarding the event and the user's credentials.
  * PrintToADevice = "Print to a device"
    * Used when information is printed to a device.
    * Log details regarding the information that is printed, the device, and the user's credentials.
  * PrintToAFile = "Print to a File"
    * Used when information is printed to a file.
    * Log details regarding the information that is printed, the file, and the user's credentials.
  * ApplicationInitialization = "Application Initialization"
    * Used when an application is initialized.
    * Log details regarding the application and the user's credentials.
  * ExportOfInformation = "Export of Information"
    * A more general audit-able event used for when information is exported out of the system.
    * Log details regarding the information that is exported, the location the information is exported to and the user's credentials.
  * ImportOfInformation = "Import of Information"
    * A more general audit-able event used for when information is imported into the system.
    * Log details regarding the information that is imported, the location the information is imported from and the user's credentials.

 ---
 Footnotes:

  1. The maven library ch.qos.logback:logback-classic:1.1.2 contains all three logback dependencies (classic, core and slf4j).
  3. I highly advise not to use the default logging configuration without any logback.xml or other defined methods for your general logging (AuditLogger will work fine with the default configuration).
     The only appender that is present with the default configuration is a console output.
     If you plan on using the default logging configuration and the console output, make sure to create a Logger before
     initializing an AuditLogger instance. AuditLogger deletes the console appender if it is the only logger when it is
     first created.
  4. Warning: if you use a turbo filter to deny the AUDIT marker then nothing will be logged to the audit log file.
