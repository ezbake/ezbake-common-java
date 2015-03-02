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

package ezbake.security.client.provider;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import ezbake.base.thrift.*;
import ezbake.security.client.EzBakeSecurityClientConfigurationHelper;
import ezbake.security.common.core.EzSecurityTokenUtils;
import ezbake.security.common.core.SecurityID;
import ezbakehelpers.ezconfigurationhelpers.application.EzBakeApplicationConfigurationHelper;
import org.apache.thrift.TException;

import javax.inject.Inject;
import java.util.Properties;
import java.util.Set;

/**
 * User: jhastings
 * Date: 10/13/14
 * Time: 3:09 PM
 */
public class MockTokenProvider implements TokenProvider {

    public static final String MOCK_TOKEN_AUTHS_KEY = "ezbake.security.mock.token.auths";

    private Properties configuration;
    private EzBakeApplicationConfigurationHelper appConfig;
    private EzBakeSecurityClientConfigurationHelper securityClientConfig;
    private Set<String> tokenAuths;

    @Inject
    public MockTokenProvider(Properties configuration) {
        this.configuration = configuration;
        appConfig = new EzBakeApplicationConfigurationHelper(configuration);
        securityClientConfig = new EzBakeSecurityClientConfigurationHelper(configuration);

        String authsString = configuration.getProperty(MOCK_TOKEN_AUTHS_KEY, "U");
        tokenAuths = Sets.newHashSet(authsString.split(","));
    }

    @Override
    public EzSecurityToken getSecurityToken(TokenRequest tokenRequest) {
        tokenRequest = (tokenRequest == null) ? mockUpTokenRequest() : tokenRequest;

        // base token
        EzSecurityToken token = new EzSecurityToken(
                getValidityCaveats(),
                tokenRequest.getType(),
                getTokenPrincipal(tokenRequest));

        // auths
        Authorizations userAuths = new Authorizations();
        userAuths.setFormalAuthorizations(tokenAuths);
        token.setAuthorizations(userAuths);

        return token;
    }

    @Override
    public EzSecurityToken refreshSecurityToken(EzSecurityToken token) {
        // just update the expiration timestamp
        token.getValidity().setNotAfter(token.getValidity().getNotAfter()+10*1000);
        return token;
    }

    @Override
    public boolean isValidToken(EzSecurityToken token) {
        return true;
    }

    private EzSecurityPrincipal getTokenPrincipal(TokenRequest request) {
        EzSecurityPrincipal principal = null;
        if (request.isSetProxyPrincipal()) {
            ProxyUserToken proxyToken = EzSecurityTokenUtils.deserializeProxyUserToken(
                    request.getProxyPrincipal().getProxyToken());
            principal = new EzSecurityPrincipal(
                    proxyToken.getX509().getSubject(),
                    new ValidityCaveats(
                            proxyToken.getIssuedBy(),
                            proxyToken.getIssuedTo(),
                            proxyToken.getNotAfter(),
                            ""));
            principal.setIssuer(proxyToken.getX509().getIssuer());
        } else if (request.isSetTokenPrincipal()) {
            principal = request.getTokenPrincipal().getTokenPrincipal();
        } else if (request.isSetPrincipal()) {
            principal = request.getPrincipal();
        } else if (request.getType() == TokenType.APP) {
            principal = new EzSecurityPrincipal(
                    request.getSecurityId(),
                    new ValidityCaveats(
                            "_Ez_Security",
                            request.getSecurityId(),
                            System.currentTimeMillis()+1000,
                            ""));
        }

        return principal;
    }

    private String getAppSecurityId() {
        String securityId = appConfig.getSecurityID();
        if (Strings.isNullOrEmpty(securityId)) {
            securityId = "PLEASE_SET_ezbake.security.app.id";
        }
        return securityId;
    }

    private ValidityCaveats getValidityCaveats() {
        return new ValidityCaveats(
                SecurityID.ReservedSecurityId.EzSecurity.getCn(),
                getAppSecurityId(),
                System.currentTimeMillis()+10*1000,
                "");
    }

    private TokenRequest mockUpTokenRequest() {
        TokenRequest tokenRequest = new TokenRequest(getAppSecurityId(), System.currentTimeMillis(), TokenType.USER);

        String put;
        try {
            put = EzSecurityTokenUtils.serializeProxyUserTokenToJSON(new ProxyUserToken(
                        new X509Info(securityClientConfig.getMockUser()),
                        SecurityID.ReservedSecurityId.EzSecurity.getCn(),
                        SecurityID.ReservedSecurityId.EFE.getCn(),
                        System.currentTimeMillis()+12000
                ));
        } catch (TException e) {
            put = "";
        }
        tokenRequest.setProxyPrincipal(new ProxyPrincipal(put, ""));
        return tokenRequest;
    }

}
