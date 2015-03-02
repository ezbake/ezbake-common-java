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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public enum AuditEventType {
    /**
     * Used when a user attempts to log on, successfully or not.
     * Log the user's credentials.
     */
    LogOn("Logon"),

    /**
     * Used when a user logs out of the system.
     * Log the user's credentials.
     */
    LogOff("Logoff"),

    /**
     * Used when either a file or object is created.
     * Log details regarding the file/object and the user's credentials.
     */
    FileObjectCreate("File/Object Create"),

    /**
     * Used when a file or object is accessed.
     * Log details regarding the file/object and the user's credentials.
     */
    FileObjectAccess("File/Object Access"),

    /**
     * Used when a user deletes a file or object.
     * Log details regarding the file/object and the user's credentials.
     */
    FileObjectDelete("File/Object Delete"),

    /**
     * Used when there are changes to a file or object.
     * Log details regarding the modifications, the file/object and the user's credentials.
     */
    FileObjectModify("File/Object Modify"),

    /**
     * Used when there are changes to the permissions on a file or object.
     * Log details regarding the modifications, the file/object and the user's credentials.
     */
    FileObjectPermissionModifications("File/Object Permission Modifications"),

    /**
     * Used when there are changes to the ownership of file or object.
     * Log details regarding the changes, the file/object and the user's credentials.
     */
    FileObjectOwnershipMod("File/Object Ownership Mod"),

    /**
     * Used when there is a write or download to an external location.
     * Log details regarding the information written/downloaded, the external location, and the user's credentials.
     */
    WritesDownloadsToExternal("Writes/downloads to external"),

    /**
     * Used when there is an upload from an external device or media.
     * Log details regarding the information being uploaded, the external device/media and the user's credentials.
     */
    UploadsFromExternalDevicesMedia("Uploads from external Devices/Media"),

    /**
     * Used when either a user or group is added.
     * Log details regarding the new user/group and the user's credentials that made the change.
     */
    UserGroupMgmtAdd("User& Group Mgmt add"),

    /**
     * Used when either a user or group is deleted.
     * Log details regarding the deleted user/group and the user's credentials that made the change.
     */
    UserGroupMgmtDelete("User& Group Mgmt delete"),

    /**
     * Used when either a user or group is modified.
     * Log details regarding the modifications, the modified user/group and the user's credentials that made the change.
     */
    UserGroupMgmtModify("User& Group Mgmt modify"),

    /**
     * Used when either a user or group is suspended.
     * Log details regarding the suspension, the suspended user/group and the user's credentials that made the change.
     */
    UserGroupMgmtSuspend("User& Group Mgmt Suspend"),

    /**
     * Used when either a user or group is locked.
     * Log details regarding the locked user/group and the user's credentials that made the change.
     */
    UserGroupMgmtLock("User& Group Mgmt lock"),

    /**
     * Used when a group/role is added.
     * Log details regarding the new group/role and the user's credentials.
     */
    UserGroupMgmtGroupRoleAdd("User& Group Mgmt Group/Role add"),

    /**
     * Used when a group/role is deleted.
     * Log details regarding the deleted group/role and the user's credentials.
     */
    UserGroupMgmtGroupRoleDelete("User& Group Mgmt Group/Role delete"),

    /**
     * Used when a group/role is modified.
     * Log details regarding the modifications, the modified group/role and the user's credentials.
     */
    UserGroupMgmtGroupRoleModify("User& Group Mgmt Group/Role modify"),

    /**
     * Used when a user uses privileged or special rights to change a security or audit policy.
     * Log details regarding rights used, security or audit policy changes, and the user's credentials.
     */
    UseOfPrivilegedSpecialRightsEventsSecurityOrAuditPolicyChanges(
            "Use of Privileged/Special Rights Events Security or Audit Policy Changes"),

    /**
     * Used when a user uses privileged or special rights to change configurations.
     * Log details regarding rights used, the configuration changes, and the user's credentials.
     */
    UseOfPrivilegedSpecialRightsEventsConfigurationChanges(
            "Use of Privileged/Special Rights Events Configuration Changes"),

    /**
     * Used when a user accesses admin or root level.
     * Log the user's credentials.
     */
    AdminOrRootLevelAccess("Admin or Root Level Access"),

    /**
     * Used when the privileges or role of a user is escalated.
     * Log details regarding the escalation and the user's credentials.
     */
    PrivilegeRoleEscalation("Privilege/Role Escalation"),

    /**
     * Used when a user accesses the data for the audit log or other general log files.
     * Log details regarding the data accessed and the user's credentials.
     */
    AuditAndLogDataAccess("Audit and Log data Access"),

    /**
     * Used when the system is rebooted, restarted or shutdown.
     * Log details regarding the event and the user's credentials.
     */
    SystemRebootRestartShutdown("System Reboot, Restart, & Shutdown"),

    /**
     * Used when information is printed to a device.
     * Log details regarding the information that is printed, the device, and the user's credentials.
     */
    PrintToADevice("Print to a device"),

    /**
     * Used when information is printed to a file.
     * Log details regarding the information that is printed, the file, and the user's credentials.
     */
    PrintToAFile("Print to a File"),

    /**
     * Used when an application is initialized.
     * Log details regarding the application and the user's credentials.
     */
    ApplicationInitialization("Application Initialization"),

    /**
     * A more general audit-able event used for when information is exported out of the system.
     * Log details regarding the information that is exported, the location the information is exported to and the user's credentials.
     */
    ExportOfInformation("Export of Information"),

    /**
     * A more general audit-able event used for when information is imported into the system.
     * Log details regarding the information that is imported, the location the information is imported from and the user's credentials.
     */
    ImportOfInformation("Import of Information");

    /**
     * The String name of the event
     */
    private String name;

    private AuditEventType(String name) {
        this.name = name;
    }

    /**
     * @return the string name of the event to be printed to the log
     */
    @Nonnull
    public String getName() {
        return name;
    }

    /**
     * Parse out the name, returning the AuditEventType for the name,
     * if the name isn't found, then returns null
     *
     * @param name - the name to parse
     * @return AuditEventType matching, or null
     */
    public static @Nullable AuditEventType fromName(String name) {
        for ( AuditEventType v : values()) {
            if ( v.getName().equals(name) )
                return v;
        }
        return null;
    }
}
