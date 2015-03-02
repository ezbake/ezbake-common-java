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

/**
 * User: jhastings
 * Date: 7/8/14
 * Time: 11:16 AM
 */
public class SecurityID {
    public static final long reservedRangeStart = 0;
    public static final long reservedRangeEnd = 1000;
    public static final String RESERVED_ID_PREFIX = "_Ez_";

    public static enum ReservedSecurityId {
        CA("00", "ezbakeca"),
        EzSecurity("01", "_Ez_Security"),
        EFE("02", "_Ez_EFE"),
        Registration("03", "_Ez_Registration"),
        Deployer("04", "_Ez_Deployer"),
        INS_REG("_Ez_INS_REG", "_Ez_INS_REG");

        private String id;
        private String cn;
        ReservedSecurityId(String id, String CN) {
            this.id = id;
            this.cn = CN;
        }
        public String getId() {
            return id;
        }
        public String getCn() {
            return cn;
        }
        public static ReservedSecurityId fromId(String id) {
            if (id != null) {
                for (ReservedSecurityId sid : values()) {
                    if (sid.getId().equalsIgnoreCase(id)) {
                        return sid;
                    }
                }
            }
            throw new IllegalArgumentException("Unknown " + ReservedSecurityId.class.getSimpleName() + " value: " + id);
        }
        public static ReservedSecurityId fromCn(String cn) {
            if (cn != null) {
                for (ReservedSecurityId sid : values()) {
                    if (sid.getCn().equalsIgnoreCase(cn)) {
                        return sid;
                    }
                }
            }
            throw new IllegalArgumentException("Unknown " + ReservedSecurityId.class.getSimpleName() + " value: " + cn);
        }
        public static ReservedSecurityId fromEither(String cnOrId) {
            if (cnOrId != null) {
                for (ReservedSecurityId sid : values()) {
                    if (sid.toString().equalsIgnoreCase(cnOrId) || sid.getId().equalsIgnoreCase(cnOrId) || sid.getCn().equalsIgnoreCase(cnOrId)) {
                        return sid;
                    }
                }
            }
            throw new IllegalArgumentException("Unknown " + ReservedSecurityId.class.getSimpleName() + " value: " +
                    cnOrId);
        }
        public static boolean isReserved(String id) {
            if (id != null) {
                for (ReservedSecurityId sid : values()) {
                    if (sid.toString().equalsIgnoreCase(id) || sid.getId().equalsIgnoreCase(id) || sid.getCn().equalsIgnoreCase(id)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    public static boolean isReserved(String securityID) {
        // Is it one of the specially reserved IDs
        boolean reserved = false;

        if (securityID != null) {
            // Is it in the reserved range?
            if (ReservedSecurityId.isReserved(securityID) || securityID.startsWith(RESERVED_ID_PREFIX)) {
                reserved = true;
            } else {
                try {
                    long numberValue = Long.parseLong(securityID, 10);
                    if (numberValue >= reservedRangeStart && numberValue < reservedRangeEnd) {
                        reserved = true;
                    }
                } catch (NumberFormatException e) {
                    // Security ID wasn't a valid
                }
            }
        }

        return reserved;
    }

    public static boolean isSecurityId(String securityId) {
        boolean isSecurityId = false;
        if (securityId != null) {
            // Check for reserved security IDs
            if (isReserved(securityId)) {
                isSecurityId = true;
            } else {
                // if it isn't a reserved id then it must be a number
                try {
                    long value = Long.parseLong(securityId, 10);
                    isSecurityId = true;
                } catch (NumberFormatException e) {
                    // not a valid security id
                }
            }
        }
        return isSecurityId;
    }

}
