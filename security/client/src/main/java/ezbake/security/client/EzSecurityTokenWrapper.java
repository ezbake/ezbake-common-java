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

package ezbake.security.client;

import ezbake.base.thrift.EzSecurityToken;
import ezbake.base.thrift.TokenType;
import ezbake.security.common.core.EzSecurityTokenUtils;

@SuppressWarnings("serial")
public class EzSecurityTokenWrapper extends EzSecurityToken {
    
    /**
     * Default Constructor
     */
    public EzSecurityTokenWrapper() {
        super();
    }
    
    /**
     * Overloaded Constructor
     * @param ezToken - security token to copy from
     */
    public EzSecurityTokenWrapper(EzSecurityToken ezToken) {
        super(ezToken);
    }

    public String getSecurityId() {
        return getValidity().getIssuedTo();
    }
    
    public String getTargetSecurityId() {
        return getValidity().getIssuedFor();
    }
 
    public String getUserId() {

        if(super.getType() == TokenType.USER) {
            return getTokenPrincipal().getPrincipal();
        }
        return null;
    }
    
    public String getUsername() {
        if(super.getType() == TokenType.USER) {
            return getTokenPrincipal().getName();
        }
        return null;
    }
    
    public void setSecurityId(String securityId) {
        super.getValidity().setIssuedTo(securityId);
    }
    
   
    public String getApplicationSecurityId() {
        if(super.getType() == TokenType.APP) {
            return getTokenPrincipal().getPrincipal();
        }
        return null;
    }
    

    public void setUserId(String id) {
        getTokenPrincipal().setPrincipal(id);
    }
    
    public void setTargetSecurityId(String target) {
        getValidity().setIssuedFor(target);
    }
    
    public void setUsername(String username) {
        getTokenPrincipal().setName(username);
    }
    
    public void setApplicationSecurityId(String appId) {
        getTokenPrincipal().setPrincipal(appId);
    }

    /**
     * Used to determine whether or not a user should be given access to Ezbake Administrator functionality
     *
     * @return true if the token is an EzAdmin token
     */
    public boolean isEzAdmin() {
        return EzSecurityTokenUtils.isEzAdmin(this);
    }

}
