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

package ezbake.security.test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Set;

import ezbake.base.thrift.Authorizations;
import ezbake.base.thrift.EzSecurityPrincipal;
import ezbake.base.thrift.EzSecurityToken;
import ezbake.base.thrift.TokenType;
import ezbake.base.thrift.ValidityCaveats;

public class MockEzSecurityToken {

    public static final long defaultTokenExpiration = 60 /* seconds */ * 1000 /* milliseconds */;
    public static final String defaultSecurityId = "client";
    public static final String defaultUserPrincipal = "CN=EzbakeClient, OU=42six, O=CSC, C=US";
    public static final String defaultUserCitizenship = "USA";
    public static final String defaultUserOrganization = "ORG";
    public static final String defaultAuthorizationLevel = "U";
    public static final String EZ_INTERNAL_PROJECT = "_Ez_internal_project_";
    public static final String EZ_INTERNAL_ADMIN_GROUP = "_Ez_administrator";
    public static final Set<String> defaultAuthorizations = Collections.singleton("U");
    public static final Map<String, List<String>> mockProjectGroups;
    public static final Map<String, List<String>> mockAdminProjectGroups;

    static {
        mockProjectGroups = new HashMap<>();
        mockProjectGroups.put("project 1", Collections.singletonList("group 1"));
        mockAdminProjectGroups = new HashMap<>();
        mockAdminProjectGroups.put(EZ_INTERNAL_PROJECT, Collections.singletonList(EZ_INTERNAL_ADMIN_GROUP));
    }

    /* -- Just the defaults -- */

    /**
     * Helper function that will return a mocked out EzSecurityToken
     *
     * @return a valid, but unsigned, EzSecurityToken. Can be used along with the mock mode of EzbakeSecurityClient
     *         which doesn't validate token signatures
     */
    public static EzSecurityToken getMockEzSecurityToken() {
        return getMockEzSecurityToken(null, null, null, null, null, null, null, null, null, null, 0, false, false);
    }

    /* -- User Tokens -- */
    public static EzSecurityToken getMockUserToken(String principal) {
        return getMockUserToken(principal, null, null, null, false);
    }

    public static EzSecurityToken getMockUserToken(boolean admin) {
        return getMockUserToken(null, null, null, null, admin);
    }

    public static EzSecurityToken getMockUserToken(String principal, String authorizationLevel, Set<String> auths, Map<String, List<String>> projectGroups, boolean admin) {
        EzSecurityToken token = getBlankToken(null, null, 0);
        token.setType(TokenType.USER);

        populateUserInfo(token, principal, null, null);
        populateAuthorizations(token, authorizationLevel, auths);
        populateExternalProjectGroups(token, projectGroups, admin);

        return token;
    }

    /* -- App Tokens -- */
    public static EzSecurityToken getMockAppToken(String appId) {
        return getMockAppToken(appId, null, null, null, null);
    }

    public static EzSecurityToken getMockAppToken(String appId, String appPrincipal) {
        return getMockAppToken(appId, appPrincipal, null, null, null);
    }

    public static EzSecurityToken getMockAppToken(String appId, String authorizationLevel, Set<String> authorizations) {
        return getMockAppToken(appId, null, authorizationLevel, authorizations, null);
    }

    public static EzSecurityToken getMockAppToken(String appId, Map<String, List<String>> projectGroups) {
        return getMockAppToken(appId, null, null, null, projectGroups);
    }

    public static EzSecurityToken getMockAppToken(String appId,
                                                  String appPrincipal,
                                                  String authorizationLevel,
                                                  Set<String> authorizations,
                                                  Map<String, List<String>> projectGroups) {
        EzSecurityToken ezToken = getBlankToken(null, null, 0);
        ezToken.setType(TokenType.APP);

        populateAppInfo(ezToken, appId, appPrincipal);
        populateAuthorizations(ezToken, authorizationLevel, authorizations);
        populateExternalProjectGroups(ezToken, projectGroups, false);

        return ezToken;
    }

    public static EzSecurityToken getMockEzSecurityToken(String applicationSecurityId,
                                                         String targetApplicationSecurityId,
                                                         String principal,
                                                         String appPrincipal,
                                                         String citizenship,
                                                         String organization,
                                                         String authorizationLevel,
                                                         Set<String> authorizations,
                                                         Map<String, List<String>> projectGroups,
                                                         TokenType type,
                                                         long tokenExpiration,
                                                         boolean admin,
                                                         boolean validForExternalRequests
    ) {
        EzSecurityToken ezToken = new EzSecurityToken();
        ezToken.setValidity(new ValidityCaveats("EzSecurity", applicationSecurityId, System.currentTimeMillis() + tokenExpiration, ""));
        ezToken.getValidity().setIssuedFor(targetApplicationSecurityId);
        ezToken.getAuthorizations().setFormalAuthorizations(authorizations);
        ezToken.setAuthorizationLevel(authorizationLevel);

        ezToken.setType(type);
        switch (type) {
            case USER:
                populateUserInfo(ezToken, principal, citizenship, organization);
                break;
            case APP:
                populateAppInfo(ezToken, applicationSecurityId, appPrincipal);
                break;
        }

        populateExternalProjectGroups(ezToken, projectGroups, admin);

        return ezToken;
    }

    /* -- Token Generating and Populating functions -- */

    /**
     * Generate an EzSecurityToken with just the basics set on it
     * @param securityId the apps security ID, if null, defaultSecurityId will be used
     * @param targetSecurityId the target security ID, if null, securityId will be used
     * @param expiration how long the token should live before expiring
     * @return the initialized EzSecurityToken
     */
    public static EzSecurityToken getBlankToken(String securityId, String targetSecurityId, long expiration) {
        EzSecurityToken token = new EzSecurityToken();
        token.setValidity(new ValidityCaveats("EzSecurity",
                securityId == null ? defaultSecurityId : securityId,
                getExpires(expiration), ""));
        token.getValidity().setIssuedFor((targetSecurityId == null) ? token.getValidity().getIssuedTo() : targetSecurityId);
        token.setTokenPrincipal(new EzSecurityPrincipal("", token.getValidity()));
        token.setAuthorizations(new Authorizations());
        return token;
    }

    /**
     * Determine the expiration timestamp for the token
     * @param expiration how long the token should live before expiring, if null, defaultTokenExpiration will be used
     * @return the expiration timestamp
     */
    public static long getExpires(long expiration) {
        if (expiration == 0) {
            expiration = defaultTokenExpiration;
        }
        return System.currentTimeMillis() + expiration;
    }

    /**
     * Set the appropriate fields on the EzSecurityToken for the passed in fields
     * @param token an EzSecurityToken to populate with UserInfo. This object will only have UserInfo updated on it
     * @param principal user principal, defaultUserPrincipal will be used if null
     * @param citizenship user citizenship, defaultUserCitizenship will be used if null
     * @param organization user organization, defaultUserOrganization will be used if null
     */
    public static void populateUserInfo(final EzSecurityToken token, String principal, String citizenship, String organization) {
        token.getTokenPrincipal().setPrincipal((principal == null) ? defaultUserPrincipal : principal);
        token.setCitizenship((citizenship == null) ? defaultUserCitizenship : citizenship);
        token.setOrganization((organization == null) ? defaultUserOrganization : organization);
    }

    /**
     * Sets the appropriate fields on the EzSecurityToken for the passed in application information
     * @param token an EzSecurityToken to populate with AppInfo. This object will only have AppInfo updated on it
     * @param appId the application security id, defaultSecurityId will be used if null
     * @param appPrincipal the application's principal, defaultSecurityId will be used if null
     */
    public static void populateAppInfo(final EzSecurityToken token, String appId, String appPrincipal) {
        token.getValidity().setIssuedTo((appId == null) ? defaultSecurityId : appId);
        token.getTokenPrincipal().setPrincipal((appId == null) ? defaultSecurityId : appId);
        token.getTokenPrincipal().setExternalID((appPrincipal == null) ? defaultSecurityId : appPrincipal);
    }

    /**
     * Sets the authorization levels on the token
     * @param token an EzSecurityToken to populate with authorizations
     * @param level the authorization level. if null, defaultAuthorizationLevel will be used
     * @param auths the auths. if null, defaultAuthorizations will be used
     */
    public static void populateAuthorizations(final EzSecurityToken token, String level, Set<String> auths) {
        token.getAuthorizations().setFormalAuthorizations((auths == null) ? defaultAuthorizations : auths);
        token.setAuthorizationLevel((level == null) ? defaultAuthorizationLevel : level);
    }

    /**
     * Sets the external project groups no an EzSecurityToken
     * @param token an EzSecurityToken to populate. This object will only have externalProjectGroups updated on it
     * @param projectGroups optional project groups to add to the token, mockProjectGroups will be applied if null
     * @param admin if true, admin project groups will be added to the token
     */
    public static void populateExternalProjectGroups(final EzSecurityToken token, Map<String, List<String>> projectGroups, boolean admin) {
        Map<String, List<String>> pgs = new HashMap<>();
        if (projectGroups != null) {
            pgs.putAll(projectGroups);
        } else {
            pgs.putAll(mockProjectGroups);
        }
        if (admin) {
            pgs.putAll(mockAdminProjectGroups);
        }

        token.setExternalProjectGroups(pgs);
    }
}
