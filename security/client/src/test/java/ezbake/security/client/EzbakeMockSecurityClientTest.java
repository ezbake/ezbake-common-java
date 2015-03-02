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

import com.google.common.collect.ImmutableSet;

import ezbake.base.thrift.*;
import ezbake.crypto.PKeyCryptoException;
import ezbake.crypto.RSAKeyCrypto;
import ezbake.security.client.provider.TokenProvider;
import ezbake.security.common.core.EzSecurityClient;
import ezbake.security.common.core.EzSecurityTokenUtils;

import ezbake.thrift.ThriftUtils;
import org.apache.commons.io.FileUtils;
import org.apache.thrift.TException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Properties;
import java.util.Set;

/**
 * User: jhastings
 * Date: 10/10/13
 * Time: 3:53 PM
 */
public class EzbakeMockSecurityClientTest {
    private static final String serverPrivatePath = EzbakeMockSecurityClientTest.class.getResource("/ezbakesecurity-key.pem").getFile();
    private static final String DN = "Some User";
    private static String serverPrivateKey;

    private static Properties properties;

    @BeforeClass
    public static void setUp() throws Exception {
        properties = new Properties();
        properties.setProperty(EzBakeSecurityClientConfigurationHelper.USE_MOCK_KEY, String.valueOf(true));
        properties.setProperty(EzBakeSecurityClientConfigurationHelper.MOCK_USER_KEY, DN);
        properties.setProperty(TokenProvider.CLIENT_MODE, TokenProvider.ClientMode.Dev.getValue());
        serverPrivateKey = FileUtils.readFileToString(new File(serverPrivatePath));
    }

    private EzbakeSecurityClient client;
    private RSAKeyCrypto crypto;

    @Before
    public void setUpTest() throws InvalidKeySpecException, NoSuchAlgorithmException {
        client = new EzbakeSecurityClient(properties);
        crypto = new RSAKeyCrypto(serverPrivateKey, true);
    }

    protected EzSecurityToken populateToken(String sid, String tid, Long exp, String principal, String name, String level, Set<String> authList) throws IOException, PKeyCryptoException {
        EzSecurityToken su = new EzSecurityToken();
        su.setTokenPrincipal(new EzSecurityPrincipal(principal, null));
        su.getTokenPrincipal().setName(name);
        su.setValidity(new ValidityCaveats());
        su.getValidity().setIssuer("EzSecurity");
        su.getValidity().setIssuedTo(sid);
        su.getValidity().setIssuedFor(tid);
        su.getValidity().setNotAfter(System.currentTimeMillis() + exp);
        su.setAuthorizationLevel(level);
        su.setAuthorizations(new Authorizations());
        su.getAuthorizations().setFormalAuthorizations(authList);

        su.getValidity().setSignature(EzSecurityTokenUtils.tokenSignature(su, crypto));
        return su;
    }

    @Test
    public void testMockVerifyReceivedTokenWithValid() throws TException, PKeyCryptoException, IOException {
        //Mock test should pass with good token
        EzSecurityToken su = populateToken("TestAppId", "SecurityClientTest", 100000l, "1213232131", "123 123", "low",
                ImmutableSet.of("read"));
        client.validateReceivedToken(su);
      }

    @Test
    public void testMockVerifyReceivedTokenWithInvalid() throws TException, PKeyCryptoException, IOException {
        //with .use.mock test set, Invalid should pass too
        EzSecurityToken su = populateToken("TestAppId", "SecurityClientTest", 100000l, "1213232131", "123 123", "low",
                ImmutableSet.of("read"));
        su.getValidity().setIssuedFor("NotTheOriginal");

        client.validateReceivedToken(su);
    }

    @Test
    public void verifyReceivedToken() throws EzSecurityTokenException, EzSecurityTokenException {
        client.validateReceivedToken(new EzSecurityToken());
    }

    @Test
    public void testFetchAppToken() throws TException {
        // Normall read these using EzConfiguration
        Properties properties = new Properties();
        properties.setProperty("ezbake.security.app.id", "mockSecurityId");
        properties.setProperty("ezbake.security.client.use.mock", "true");
        properties.setProperty("ezbake.security.client.mode", "MOCK");
        properties.setProperty("ezbake.security.fake.token", "true");
        properties.setProperty("application.name", "testapp");
        properties.setProperty("service.name", "testservice");

        EzSecurityClient client = new EzbakeSecurityClient(properties);
        EzSecurityToken token = client.fetchAppToken();

        // This shouldn't validate if the token principal is absent
        client.validateReceivedToken(token);

        // This shouldn't fail if I get a token from the security client
        ThriftUtils.serialize(token);
    }
}
