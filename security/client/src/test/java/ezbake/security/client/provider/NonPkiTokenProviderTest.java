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
import com.google.common.base.Suppliers;
import ezbake.base.thrift.EzSecurityToken;
import ezbake.base.thrift.TokenRequest;
import ezbake.base.thrift.TokenType;
import ezbake.crypto.PKeyCryptoException;
import ezbake.security.common.core.EzSecurityTokenUtils;
import ezbake.security.test.MockEzSecurityToken;
import ezbake.security.thrift.EzSecurity;
import ezbake.security.thrift.ezsecurityConstants;
import ezbake.thrift.ThriftClientPool;
import org.apache.thrift.TException;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Properties;

/**
 * User: jhastings
 * Date: 10/13/14
 * Time: 8:47 PM
 */
public class NonPkiTokenProviderTest {

    private static final String appSecurityId = "SecurityClient";


    private static Supplier<ThriftClientPool> getSupplier(final EzSecurity.Client client) {
        return Suppliers.memoize(new Supplier<ThriftClientPool>() {
            @Override
            public ThriftClientPool get() {
                ThriftClientPool pool = EasyMock.createNiceMock(ThriftClientPool.class);
                try {
                    EasyMock.expect(pool.getClient(ezsecurityConstants.SERVICE_NAME, EzSecurity.Client.class)).andReturn(client).anyTimes();
                } catch (TException e) {
                    return null;
                }
                return pool;
            }
        });
    }

    private static EzSecurityToken buildToken() throws IOException, PKeyCryptoException {
        EzSecurityToken token =  MockEzSecurityToken.getBlankToken(appSecurityId, appSecurityId, System.currentTimeMillis() + 6000);
        MockEzSecurityToken.populateUserInfo(token, "USER", "USA", "EzBake");
        return token;
    }

    Properties configuration;
    @Before
    public void setUp() {
        configuration = new Properties();
    }

    @Test
    public void testInit() {
        TokenProvider provider = new NonPkiTokenProvider(configuration, getSupplier(null));
    }

    @Test
    public void testTokenValidity() {
        TokenProvider provider = new NonPkiTokenProvider(configuration, getSupplier(null));
        Assert.assertTrue(provider.isValidToken(new EzSecurityToken()));
    }

    @Test
    public void testFetchToken() throws TException, PKeyCryptoException, IOException {
        EzSecurity.Client client = EasyMock.createNiceMock(EzSecurity.Client.class);
        EasyMock.expect(client.requestToken(
                EasyMock.anyObject(TokenRequest.class),
                EasyMock.anyString()
        )).andReturn(buildToken());
        Supplier<ThriftClientPool> supplier = getSupplier(client);
        ThriftClientPool pool = supplier.get();
        EasyMock.replay(client, pool);

        TokenProvider provider = new NonPkiTokenProvider(configuration, supplier);
        EzSecurityToken token = provider.getSecurityToken(new TokenRequest());
        Assert.assertNotNull(token);
        Assert.assertTrue(provider.isValidToken(token));
    }

    @Test
    public void testDerivedToken() throws IOException, PKeyCryptoException, TException {
        EzSecurity.Client client = EasyMock.createNiceMock(EzSecurity.Client.class);
        EasyMock.expect(client.requestToken(
                EasyMock.anyObject(TokenRequest.class),
                EasyMock.anyString()
        )).andReturn(buildToken());
        Supplier<ThriftClientPool> supplier = getSupplier(client);
        ThriftClientPool pool = supplier.get();
        EasyMock.replay(client, pool);


        EzSecurityToken token = buildToken();
        TokenRequest tokenRequest = new TokenRequest(appSecurityId, System.currentTimeMillis(), token.getType());
        tokenRequest.setTokenPrincipal(token);
        tokenRequest.setTargetSecurityId("targetApp");

        NonPkiTokenProvider provider = new NonPkiTokenProvider(configuration, supplier);
        provider.getSecurityToken(tokenRequest);
        Assert.assertNotNull(token);
        Assert.assertTrue(provider.isValidToken(token));
    }

}
