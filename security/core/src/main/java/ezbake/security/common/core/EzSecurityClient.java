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

import ezbake.base.thrift.EzSecurityToken;
import ezbake.base.thrift.EzSecurityTokenException;
import ezbake.base.thrift.ProxyPrincipal;
import ezbake.security.thrift.EzSecurity;
import org.apache.thrift.TException;

import java.io.Closeable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: jhastings
 * Date: 8/21/14
 * Time: 9:29 AM
 */
public interface EzSecurityClient extends Closeable {
    public static final String EFE_USER_HEADER = "ezb_verified_user_info";
    public static final String EFE_SIGNATURE_HEADER = "ezb_verified_signature";


    /**
     * Retrieves an EzSecurity.Client object from the connection pool
     *
     * @return a connected EzSecurity.Client
     * @throws TException
     */
    public EzSecurity.Client getClient() throws TException;

    /**
     * Returns an EzSecurity.Client to the connection pool
     * @param client an EzSecurity.Client object returned from {@link ezbake.security.common.core.EzSecurityClient#getClient()}
     */
    public void returnClient(EzSecurity.Client client);

    /**
     * Attempt to read the proxy princiapl from the HTTP headers. This will use the RquestContextHolder to
     * get the HttpServletRequest. Requires that org.springframework.web.context.request.RequestContextListener be
     * registered in the deployment descriptor.
     *
     * @return the proxy principal that was contained in the HTTP headers
     * @throws EzSecurityTokenException
     */
    public ProxyPrincipal requestPrincipalFromRequest() throws EzSecurityTokenException;

    /**
     * Look up the headers for the proxy principal in the passed in map of headers
     *
     * @param headers map of headers
     * @return a proxy principal if one was contained in the headers
     * @throws EzSecurityTokenException
     */
    public ProxyPrincipal requestPrincipalFromRequest(Map<String, List<String>> headers) throws EzSecurityTokenException;

    /**
     * Call the ping method on the EzSecurity service
     *
     * @return the result of the ping method
     * @throws TException
     */
    public boolean ping() throws TException;

    /**
     * Verify that the header DN and signature were issued by EzSecurity
     * @throws EzSecurityTokenException if the headers are missing or invalid
     */
    public void validateCurrentRequest() throws EzSecurityTokenException;

    /**
     * Validate an EzSecurityToken received by anyone other than the security service. This should always be called
     * before trusting the information present in the received token.
     *
     * @param token received token
     * @throws EzSecurityTokenException if the token is invalid
     */
    public void validateReceivedToken(EzSecurityToken token) throws EzSecurityTokenException;

    /**
     * Fetch a token for the currently proxied user. The proxied user info will be taken from the request headers
     *
     * @return a USER token
     * @throws EzSecurityTokenException
     */
    public EzSecurityToken fetchTokenForProxiedUser() throws EzSecurityTokenException;

    /**
     * Fetch a token for the currently proxied user. The proxied user info will be taken from the request headers
     *
     * @param exclude a set of authorizations that should not be included in the returned token
     * @return a USER token
     * @throws EzSecurityTokenException
     */
    public EzSecurityToken fetchTokenForProxiedUser(Set<String> exclude) throws EzSecurityTokenException;

    /**
     * Fetch a token for the currently proxied user. The proxied user info will be taken from the request headers
     *
     * @param targetId the application the token should be issuedFor
     * @return a USER token
     * @throws EzSecurityTokenException
     */
    public EzSecurityToken fetchTokenForProxiedUser(String targetId) throws EzSecurityTokenException;

    /**
     * Fetch a token for the currently proxied user. The proxied user info will be taken from the request headers
     *
     * @param targetId the application the token should be issuedFor
     * @param exclude a set of authorizations that should not be included in the returned token
     * @return a USER token
     * @throws EzSecurityTokenException
     */
    public EzSecurityToken fetchTokenForProxiedUser(String targetId, Set<String> exclude) throws EzSecurityTokenException;

    /**
     * Fetch a token for the currently proxied user
     *
     * @param principal the principal of the proxied user
     * @param targetID the application the token should be issuedFor
     * @return a USER token
     * @throws EzSecurityTokenException
     */
    public EzSecurityToken fetchTokenForProxiedUser(ProxyPrincipal principal, String targetID) throws EzSecurityTokenException;

    /**
     * Fetch a token for the currently proxied user
     *
     * @param principal the principal of the proxied user
     * @param targetID the application the token should be issuedFor
     * @param exclude a set of authorizations that should not be included in the returned token
     * @return a USER token
     * @throws EzSecurityTokenException
     */
    public EzSecurityToken fetchTokenForProxiedUser(ProxyPrincipal principal, String targetID, Set<String> exclude) throws EzSecurityTokenException;

    /**
     * Fetch an EzSecurityToken for the app running this client. It will also be issued for the current app
     *
     * @return an APP for the token
     * @throws EzSecurityTokenException
     */
    public EzSecurityToken fetchAppToken() throws EzSecurityTokenException;

    /**
     * Fetch an EzSecurityToken for the app running this client, that will be issued for the target application.
     *
     * @param exclude a set of authorizations that should not be included in the returned token
     * @return an APP for the token
     * @throws EzSecurityTokenException
     */
    public EzSecurityToken fetchAppToken(Set<String> exclude) throws EzSecurityTokenException;

    /**
     * Fetch an EzSecurityToken for the app running this client, that will be issued for the target application.
     *
     * @param targetID security id of the app it should be issued for
     * @return an APP for the token
     * @throws EzSecurityTokenException
     */
    public EzSecurityToken fetchAppToken(String targetID) throws EzSecurityTokenException;

    /**
     * Fetch an EzSecurityToken for the app running this client, that will be issued for the target application.
     *
     * @param targetID security id of the app it should be issued for
     * @param exclude a set of authorizations that should not be included in the returned token
     * @return an APP for the token
     * @throws EzSecurityTokenException
     */
    public EzSecurityToken fetchAppToken(String targetID, Set<String> exclude) throws EzSecurityTokenException;

    /**
     * Fetch a token from EzSecurity that will be derived from a token that was received previously. This will retain
     * the information about the subject, but can be issuedFor other target applications
     *
     * @param token a token issued by EzSecurity
     * @param targetID the issuedFor application
     * @return a derived EzSecurity token
     * @throws EzSecurityTokenException
     */
    public EzSecurityToken fetchDerivedTokenForApp(EzSecurityToken token, String targetID) throws EzSecurityTokenException;

    /**
     * Fetch a token from EzSecurity that will be derived from a token that was received previously. This will retain
     * the information about the subject, but can be issuedFor other target applications
     *
     * @param token a token issued by EzSecurity
     * @param targetID the issuedFor application
     * @param exclude a set of authorizations that should not be included in the returned token
     * @return a derived EzSecurity token
     * @throws EzSecurityTokenException
     */
    public EzSecurityToken fetchDerivedTokenForApp(EzSecurityToken token, String targetID, Set<String> exclude) throws EzSecurityTokenException;
}
