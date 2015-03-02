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

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Resources;
import ezbake.base.thrift.*;
import ezbake.configuration.ClasspathConfigurationLoader;
import ezbake.configuration.EzConfiguration;
import ezbake.configuration.EzConfigurationLoaderException;
import ezbake.crypto.PKeyCryptoException;
import ezbake.crypto.RSAKeyCrypto;
import ezbake.crypto.utils.CryptoUtil;
import ezbake.security.common.core.EzSecurityTokenUtils;
import ezbake.thrift.ThriftClientPool;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TSimpleJSONProtocol;
import org.easymock.EasyMock;
import org.junit.*;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

/**
 * User: jhastings
 * Date: 10/10/13
 * Time: 3:53 PM
 */
public class EzbakeSecurityClientTest {
    private static final String serverPrivatePath = "ezbakesecurity-key.pem";
    private static final String DN = "Some User";
    private static final String App = "SecurityClientTest";
    private static String serverPrivateKey;

    private static Properties properties;

    @BeforeClass
    public static void setUp() throws IOException, EzConfigurationLoaderException {
        EzConfiguration ezConfiguration = new EzConfiguration(new ClasspathConfigurationLoader());

        properties = ezConfiguration.getProperties();
        properties.setProperty("ezbake.security.app.id", App);

        URL url = Resources.getResource(serverPrivatePath);
        serverPrivateKey = Resources.toString(url, Charsets.UTF_8);
    }

    private EzbakeSecurityClient client;
    private RSAKeyCrypto crypto;

    @Before
    public void setUpTest() throws InvalidKeySpecException, NoSuchAlgorithmException {
        ThriftClientPool clientPool = EasyMock.createNiceMock(ThriftClientPool.class);
        client = new EzbakeSecurityClient(properties, clientPool);
        crypto = new RSAKeyCrypto(serverPrivateKey, true);
    }

    @Test
    public void testCacheKeys() {
        TokenType type = TokenType.USER;
        String subject = "TEST";
        String targetApp = "TARGET";
        List<String> list = Lists.newArrayList("A","B");
        Set<String> set = Sets.newHashSet(list);

        // No exclude or chain
        Assert.assertEquals(
                type+subject+targetApp,
                client.getCacheKey(type, subject, (String)null, null, targetApp) );
        Assert.assertEquals(
                type.toString()+subject+targetApp,
                client.getCacheKey(type, subject, "", "", targetApp) );
        Assert.assertEquals(
                type.toString()+subject+targetApp,
                client.getCacheKey(type, subject, "", null, targetApp) );
        Assert.assertEquals(
                type.toString()+subject+targetApp,
                client.getCacheKey(type, subject, (String)null, "", targetApp) );

        // Exclude auths work properly
        Assert.assertEquals(
                client.getCacheKey(type, subject, "A,B", null, targetApp),
                client.getCacheKey(TokenType.USER, subject, set, "", targetApp));
        Assert.assertEquals(
                client.getCacheKey(type, subject, "", null, targetApp),
                client.getCacheKey(TokenType.USER, subject, (Set<String>)null, "", targetApp));

        // chain works
        Assert.assertEquals(
                client.getCacheKey(type, subject, (String)null, "A,B", targetApp),
                client.getCacheKey(TokenType.USER, subject, null, list, targetApp));
        Assert.assertEquals(
                client.getCacheKey(type, subject, (String)null, "", targetApp),
                client.getCacheKey(TokenType.USER, subject, null, (List<String>)null, targetApp));

        // combo works
        Assert.assertEquals(
                client.getCacheKey(type, subject, "A,B", "A,B", targetApp),
                client.getCacheKey(TokenType.USER, subject, set, list, targetApp));
        Assert.assertEquals(
                client.getCacheKey(type, subject, (String)null, "", targetApp),
                client.getCacheKey(TokenType.USER, subject, null, (List<String>)null, targetApp));
    }

    @Test @Ignore("not sure this is useful anymore, it might need to be moved")
    public void verifyPassesForSignedData() throws InvalidKeySpecException, NoSuchAlgorithmException, TException, PKeyCryptoException, IOException {
        // Mock the user info
        EzSecurityToken signedUser = new EzSecurityToken();
        signedUser.setValidity(new ValidityCaveats("EzSecurity", "TestAppId", System.currentTimeMillis() + 100000, ""));
        signedUser.setTokenPrincipal(new EzSecurityPrincipal());
        signedUser.getTokenPrincipal().setName("Test User");
        signedUser.getTokenPrincipal().setPrincipal("CN=Test User");
        signedUser.setAuthorizations(new Authorizations());
        signedUser.getAuthorizations().setFormalAuthorizations(ImmutableSortedSet.of("P", "Y"));
        signedUser.setAuthorizationLevel("low");

        // Store the expiration for the signature
        long expiration = System.currentTimeMillis() + 10000;
        signedUser.setValidity(new ValidityCaveats());
        signedUser.getValidity().setIssuedTo("requester");
        signedUser.getValidity().setNotAfter(expiration);

        // Create the signed object
        signedUser.getValidity().setSignature(EzSecurityTokenUtils.tokenSignature(signedUser, crypto));

        //boolean verifies = client.verifyUserInfoResponse(signedUser);
        //Assert.assertTrue("Signed user info should verify", verifies);
    }


    @Test
    public void testVerifyReceivedTokenWithValid() throws TException, PKeyCryptoException, IOException {
        EzSecurityToken su = new EzSecurityToken();
        su.setValidity(new ValidityCaveats("EzSecurity", "TestAppId", System.currentTimeMillis() + 100000, ""));
        su.getValidity().setIssuedFor("SecurityClientTest");
        su.setTokenPrincipal(new EzSecurityPrincipal());
        su.getTokenPrincipal().setName("123 123");
        su.getTokenPrincipal().setPrincipal("1213232131");
        su.getTokenPrincipal().setValidity(new ValidityCaveats("EzSecurity", "", System.currentTimeMillis(), ""));
        su.setAuthorizations(new Authorizations());
        su.getAuthorizations().setFormalAuthorizations(ImmutableSortedSet.of("P"));
        su.setAuthorizationLevel("low");

        su.getValidity().setSignature(EzSecurityTokenUtils.tokenSignature(su, crypto));

        client.validateReceivedToken(su);
    }

    @Test @Ignore("not sure this is useful anymore, it might need to be moved")
    public void testEzSecurityDNValidate() throws PKeyCryptoException, NoSuchAlgorithmException, InvalidKeyException, SignatureException, IOException {
        EzSecurityPrincipal dn = new EzSecurityPrincipal();
        dn.setPrincipal(DN);
        dn.setValidity(new ValidityCaveats());
        dn.getValidity().setNotAfter(System.currentTimeMillis() + 1000);
        dn.getValidity().setSignature(EzSecurityTokenUtils.principalSignature(dn, crypto));

        //boolean value = client.verifyEzSecurityPrincipal(dn);
        //Assert.assertTrue(value);
    }
    @Test @Ignore("not sure this is useful anymore, it might need to be moved")
    public void testEzSecurityDNValidateInvalid() throws PKeyCryptoException, NoSuchAlgorithmException, InvalidKeyException, SignatureException, IOException {
        EzSecurityPrincipal dn = new EzSecurityPrincipal();
        dn.setPrincipal(DN);
        dn.setValidity(new ValidityCaveats());
        dn.getValidity().setNotAfter(System.currentTimeMillis() - 1);
        //verify token expires
        dn.getValidity().setSignature(EzSecurityTokenUtils.principalSignature(dn, crypto));
        //Assert.assertFalse(client.verifyEzSecurityPrincipal(dn));
        //dn.getValidity().setNotAfter(System.currentTimeMillis() + 1000);
        //verify can't update expiration without updating the signature
        //Assert.assertFalse(client.verifyEzSecurityPrincipal(dn));
        //dn.getValidity().setSignature(CryptoUtil.encode(crypto.sign("invalid string".getBytes())));
        //verify invalid signature is invalid
        //Assert.assertFalse(client.verifyEzSecurityPrincipal(dn));
    }

    @Test(expected=EzSecurityTokenException.class)
    public void testVerifyReceivedTokenWithInvalid() throws TException, PKeyCryptoException, IOException {
        EzSecurityToken su = new EzSecurityToken();
        su.setValidity(new ValidityCaveats());
        su.getValidity().setIssuedTo("TestAppId");
        su.getValidity().setIssuedFor("NotMe");
        su.getValidity().setNotAfter(System.currentTimeMillis() + 100000);
        su.setTokenPrincipal(new EzSecurityPrincipal());
        su.getTokenPrincipal().setName("123 123");
        su.getTokenPrincipal().setPrincipal("1213232131");
        su.setAuthorizations(new Authorizations());
        su.getAuthorizations().setFormalAuthorizations(ImmutableSortedSet.of("Z"));
        su.setAuthorizationLevel("low");
        su.getValidity().setSignature(EzSecurityTokenUtils.tokenSignature(su, crypto));

        client.validateReceivedToken(su);
    }

    @Test @Ignore("not sure this is useful anymore, it might need to be moved")
    public void verifyFailsForExpiredResponse() throws TException, PKeyCryptoException, IOException {
        EzSecurityToken su = new EzSecurityToken();
        su.setValidity(new ValidityCaveats());
        su.getValidity().setIssuedTo("");
        su.getValidity().setNotAfter(System.currentTimeMillis());
        su.setTokenPrincipal(new EzSecurityPrincipal());
        su.getTokenPrincipal().setName("123 123");
        su.getTokenPrincipal().setPrincipal("1213232131");
        su.setAuthorizations(new Authorizations());
        su.getAuthorizations().setFormalAuthorizations(ImmutableSortedSet.of("O"));
        su.setAuthorizationLevel("low");
        su.getValidity().setSignature(EzSecurityTokenUtils.tokenSignature(su, crypto));

        //boolean verifies = client.verifyUserInfoResponse(su);
        //Assert.assertTrue(!verifies);
    }

    private static String generateProxyToken() throws TException {
        ProxyUserToken token = new ProxyUserToken(new X509Info(DN), App, "", System.currentTimeMillis() + 1000);
        return new String(new TSerializer(new TSimpleJSONProtocol.Factory()).serialize(token),
                StandardCharsets.UTF_8);
    }

    private static String generateProxyToken(long expiration) throws TException {
        ProxyUserToken token = new ProxyUserToken(new X509Info(DN), App, "", expiration);
        return new String(new TSerializer(new TSimpleJSONProtocol.Factory()).serialize(token),
                StandardCharsets.UTF_8);
    }

    private String signString(String s) throws PKeyCryptoException {
        return CryptoUtil.encode(crypto.sign(s.getBytes()));
    }

    @Test
    public void verifyProxyUserTokenAcceptsValidToken() throws TException, PKeyCryptoException {
        String token = generateProxyToken();
        String signature = signString(token);

        client.verifyProxyUserToken(token, signature);
    }

    @Test(expected=EzSecurityTokenException.class)
    public void verifyProxyUserTokenRejectsExpiredToken() throws TException, PKeyCryptoException {
        String token = generateProxyToken(System.currentTimeMillis() - 1000);
        String signature = signString(token);

        client.verifyProxyUserToken(token, signature);
    }

    @Test(expected=EzSecurityTokenException.class)
    public void verifyProxyUserTokenRejectsInvalidSignature() throws TException, PKeyCryptoException {
        String token = generateProxyToken();
        String signature = signString("Invalid signature");

        client.verifyProxyUserToken(token, signature);
    }

    @Test
    public void requestPrincipalFromRequestGetsPrincipalFromContext() throws TException, PKeyCryptoException {
        String token = generateProxyToken();
        String signature = signString(token);
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.addHeader(EzbakeSecurityClient.EFE_USER_HEADER, token);
        servletRequest.addHeader(EzbakeSecurityClient.EFE_SIGNATURE_HEADER, signature);

        RequestAttributes requestAttributes = new ServletRequestAttributes(servletRequest);
        RequestContextHolder.setRequestAttributes(requestAttributes);

        ProxyPrincipal proxyPrincipal = client.requestPrincipalFromRequest();
        client.verifyProxyUserToken(proxyPrincipal.getProxyToken(), proxyPrincipal.getSignature());
    }

    @Test
    public void requestPrincipalFromRequestGetsPrincipalFromRequest() throws TException, PKeyCryptoException {
        String token = generateProxyToken();
        String signature = signString(token);
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.addHeader(EzbakeSecurityClient.EFE_USER_HEADER, token);
        servletRequest.addHeader(EzbakeSecurityClient.EFE_SIGNATURE_HEADER, signature);

        ProxyPrincipal proxyPrincipal = client.requestPrincipalFromRequest(servletRequest);
        client.verifyProxyUserToken(proxyPrincipal.getProxyToken(), proxyPrincipal.getSignature());
    }

    @Test
    public void requestPrincipalFromRequestGetsPrincipalFromMap() throws TException, PKeyCryptoException {
        String token = generateProxyToken();
        String signature = signString(token);

        Map<String, List<String>> headerMap = new HashMap<String, List<String>>(2);
        headerMap.put(EzbakeSecurityClient.EFE_USER_HEADER, Collections.singletonList(token));
        headerMap.put(EzbakeSecurityClient.EFE_SIGNATURE_HEADER, Collections.singletonList(signature));

        ProxyPrincipal proxyPrincipal = client.requestPrincipalFromRequest(headerMap);
        client.verifyProxyUserToken(proxyPrincipal.getProxyToken(), proxyPrincipal.getSignature());
    }

    @Test
    public void testGetTargetAppId() {
        ThriftClientPool pool = client.getThriftClientPool();
        EasyMock.expect(pool.getSecurityId("serviceName")).andReturn("12345");
        EasyMock.replay(pool);

        Assert.assertEquals("12345", client.getTargetAppSecurityId("serviceName"));
        Assert.assertEquals("12345", client.getTargetAppSecurityId("12345"));
        Assert.assertEquals("unknown security id", client.getTargetAppSecurityId("unknown security id"));
        Assert.assertEquals("SecurityClientTest", client.getTargetAppSecurityId(null));
    }

}
