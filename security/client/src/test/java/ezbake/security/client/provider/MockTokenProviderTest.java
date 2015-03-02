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

import com.google.common.base.Supplier;
import com.google.common.collect.Sets;

import ezbake.base.thrift.*;
import ezbake.crypto.PKeyCryptoException;
import ezbake.security.common.core.EzSecurityTokenUtils;
import ezbake.security.test.MockEzSecurityToken;
import ezbake.security.thrift.EzSecurity;
import ezbake.thrift.ThriftClientPool;
import org.apache.thrift.TException;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Properties;

/**
 * User: jhastings
 * Date: 10/13/14
 * Time: 4:18 PM
 */
public class MockTokenProviderTest {

    private static String getProxyPrincipal() throws TException {
        return EzSecurityTokenUtils.serializeProxyUserTokenToJSON(new ProxyUserToken(new X509Info("TEST"), "TEST", "TEST", 0l));
    }
    private static EzSecurityToken buildToken() throws IOException, PKeyCryptoException {
        EzSecurityToken token =  MockEzSecurityToken.getBlankToken("TEST", "TEST", System.currentTimeMillis() + 6000);
        MockEzSecurityToken.populateUserInfo(token, "USER", "USA", "EzBake");
        return token;
    }

    @Test
    public void testNoSetupRequired() throws TException {
        MockTokenProvider provider = new MockTokenProvider(new Properties());

        TokenRequest request = new TokenRequest("12312", 0l, null);
        request.setProxyPrincipal(new ProxyPrincipal(getProxyPrincipal(), ""));

        EzSecurityToken token = provider.getSecurityToken(request);
        Assert.assertNotNull(token);
    }

    @Test
    public void testNullTokenRequest() {
        MockTokenProvider provider = new MockTokenProvider(new Properties());
        EzSecurityToken token = provider.getSecurityToken(null);
        Assert.assertNotNull(token);
    }

    @Test
    public void testFetchAppToken() {
        TokenRequest request = new TokenRequest("TEST", System.currentTimeMillis(), TokenType.APP);
        request.setTargetSecurityId("TEST");
        MockTokenProvider provider = new MockTokenProvider(new Properties());
        EzSecurityToken token = provider.getSecurityToken(request);
        Assert.assertNotNull(token);
        Assert.assertEquals(request.getType(), token.getType());
        Assert.assertEquals(Sets.newHashSet("U"), token.getAuthorizations().getFormalAuthorizations());

    }

    @Test
    public void testFetchAppTokenCustomAuths() {
        TokenRequest request = new TokenRequest("TEST", System.currentTimeMillis(), TokenType.APP);
        request.setTargetSecurityId("TEST");
        Properties props = new Properties();

        props.put(MockTokenProvider.MOCK_TOKEN_AUTHS_KEY, "A,B,D");
        MockTokenProvider provider = new MockTokenProvider(props);
        EzSecurityToken token = provider.getSecurityToken(request);

        Assert.assertEquals(Sets.newHashSet("A","B","D"), token.getAuthorizations().getFormalAuthorizations());
    }


    @Test
    public void testDerivedToken() throws IOException, PKeyCryptoException, TException {
        EzSecurityToken token = buildToken();
        TokenRequest tokenRequest = new TokenRequest("TEST", System.currentTimeMillis(), token.getType());
        tokenRequest.setTokenPrincipal(token);
        tokenRequest.setTargetSecurityId("targetApp");

        MockTokenProvider provider = new MockTokenProvider(new Properties());
        provider.getSecurityToken(tokenRequest);
        Assert.assertNotNull(token);
        Assert.assertTrue(provider.isValidToken(token));
    }
}
