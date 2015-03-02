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
import com.google.common.collect.Sets;
import com.google.common.io.Resources;
import ezbake.base.thrift.*;
import ezbake.configuration.constants.EzBakePropertyConstants;
import ezbake.crypto.PKeyCrypto;
import ezbake.crypto.PKeyCryptoException;
import ezbake.crypto.RSAKeyCrypto;
import ezbake.security.common.core.EzSecurityTokenUtils;
import ezbake.security.test.MockEzSecurityToken;
import ezbake.security.thrift.EzSecurity;
import ezbake.security.thrift.ezsecurityConstants;
import ezbake.thrift.ThriftClientPool;
import org.apache.thrift.TException;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Properties;

/**
 * User: jhastings
 * Date: 10/13/14
 * Time: 4:44 PM
 */
public class RealTokenProviderTest {
    private static final String appSecurityId = "SecurityClientTest";
    private static final String sslDir = "/pki";
    private static final String serverPrivatePath = "/ezbakesecurity-key.pem";
    private static PKeyCrypto serverCrypt;

    @BeforeClass
    public static void setUpClass() throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
        String privateKey = Resources.toString(RealTokenProviderTest.class.getResource(serverPrivatePath), StandardCharsets.UTF_8);
        serverCrypt = new RSAKeyCrypto(privateKey, true);
    }

    Properties configuration;
    @Before
    public void setUp() {
        configuration = new Properties();
        configuration.setProperty(EzBakePropertyConstants.EZBAKE_SECURITY_ID, appSecurityId);
        configuration.setProperty(EzBakePropertyConstants.EZBAKE_CERTIFICATES_DIRECTORY,
                RealTokenProviderTest.class.getResource(sslDir).getFile());
    }

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

    private static TokenRequest buildTokenRequest(String id, String tid, TokenType type, String dn) throws TException, PKeyCryptoException {
        ProxyUserToken userToken = new ProxyUserToken(new X509Info(dn), "TEST", "TEST", 0l);
        String proxyToken = EzSecurityTokenUtils.serializeProxyUserTokenToJSON(userToken);
        String proxyTokenSignature = EzSecurityTokenUtils.proxyUserTokenSignature(userToken, serverCrypt);

        TokenRequest request = new TokenRequest(id, System.currentTimeMillis(), type);
        request.setTargetSecurityId(tid);
        request.setProxyPrincipal(new ProxyPrincipal(proxyToken, proxyTokenSignature));
        return request;
    }

    private static EzSecurityToken buildToken() throws IOException, PKeyCryptoException {
        EzSecurityToken token =  MockEzSecurityToken.getBlankToken(appSecurityId, appSecurityId, System.currentTimeMillis()+6000);
        MockEzSecurityToken.populateUserInfo(token, "USER", "USA", "EzBake");
        token.getValidity().setSignature(EzSecurityTokenUtils.tokenSignature(token, serverCrypt));
        return token;
    }

    @Test
    public void testInit() {
        EzbakeRealTokenProvider provider = new EzbakeRealTokenProvider(configuration, getSupplier(null));
    }

    @Test(expected=RuntimeException.class)
    public void testBadCertificatesDir() {
        EzbakeRealTokenProvider provider = new EzbakeRealTokenProvider(new Properties(), getSupplier(null));
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

        EzbakeRealTokenProvider provider = new EzbakeRealTokenProvider(configuration, supplier);
        TokenRequest request = buildTokenRequest(appSecurityId, appSecurityId, TokenType.USER, "USER");
        EzSecurityToken token = provider.getSecurityToken(request);
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

        EzbakeRealTokenProvider provider = new EzbakeRealTokenProvider(configuration, supplier);
        provider.getSecurityToken(tokenRequest);
        Assert.assertNotNull(token);
        Assert.assertTrue(provider.isValidToken(token));
    }

}
