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

import com.google.common.base.Joiner;
import ezbake.base.thrift.EzSecurityToken;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Example use:
 *
 *     AuditEvent evt = event("list_event", token).arg("start", start).arg("count", count);
 *     try {
 *
 *     } catch (Exception e) {
 *         evt.failed();
 *     }
 *
 */
public class AuditEvent {
    public static AuditEvent event(String eventName, EzSecurityToken securityToken) {
        return new AuditEvent(eventName, securityToken);
    }


    private String eventName;
    private EzSecurityToken securityToken;
    private boolean success = true;
    private Map<String, Object> arguments = new HashMap<>();

    public AuditEvent(AuditEventType eventName, EzSecurityToken securityToken) {
        this.eventName = eventName.getName();
        this.securityToken = securityToken;
    }

    public AuditEvent(String eventName, EzSecurityToken securityToken) {
        this.eventName = eventName;
        this.securityToken = securityToken;
    }

    public AuditEvent arg(String name, Object value) {
        arguments.put(name, value);
        return this;
    }

    public String getEventName() {
        return eventName;
    }

    public AuditEvent success() {
        success = true;
        return this;
    }

    public AuditEvent failed() {
        success = false;
        return this;
    }

    public boolean isSuccess() {
        return success;
    }

    public EzSecurityToken getSecurityToken() {
        return securityToken;
    }

    @SuppressWarnings("unchecked")
    public String msg() {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, Object> ele : arguments.entrySet()) {
            if (!first) { builder.append(", "); } else {first = false;}
            builder.append('"').append(ele.getKey()).append("\": ");
            if ( ele.getValue() instanceof String ) {
                builder.append('"').append(ele.getValue()).append("\"");
            } else if (ele.getValue() instanceof Collection) {
                Collection col = (Collection)ele.getValue();
                if ( col.isEmpty() ) {
                    builder.append("[ ]");
                    continue;
                }
                if ( col.iterator().next() instanceof String ) {
                    builder.append("[").append(wrappedJoinString(col)).append("]");
                } else {
                    builder.append("[").append(wrappedJoin(col, "{", "}")).append("]");
                }
            } else if (ele.getValue() instanceof Number ) {
                builder.append(ele.getValue());
            } else if(ele.getValue() instanceof Exception){
                Exception except = (Exception) ele.getValue();
                builder.append('"').append(except.getMessage()).append("\"");
            } else if (ele.getValue() == null) {
                builder.append("null");
            }
            else {
                builder.append(ele.getValue());
            }
        }
        return builder.toString();
    }

    private static String wrappedJoinString(Collection<String> col) {
        return wrappedJoin(col, "\"", "\"");
    }

    private static String wrappedJoin(Collection<?> col, String wrapLeft, String wrapRight) {
        if (col.isEmpty() ) return "";
        return wrapLeft + Joiner.on(wrapRight + "," + wrapLeft).join(col) + wrapRight;

    }
}