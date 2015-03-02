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

package ezbake.security.client.validation;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Resources;
import ezbake.base.thrift.EzSecurityToken;
import ezbake.base.thrift.EzSecurityTokenException;
import ezbake.configuration.ClasspathConfigurationLoader;
import ezbake.configuration.EzConfiguration;
import ezbake.configuration.EzConfigurationLoaderException;
import ezbake.configuration.constants.EzBakePropertyConstants;
import ezbake.crypto.PKeyCrypto;
import ezbake.crypto.PKeyCryptoException;
import ezbake.crypto.RSAKeyCrypto;
import ezbake.security.common.core.EzSecurityTokenUtils;
import ezbake.security.common.core.TokenExpiredException;
import ezbake.security.test.MockEzSecurityToken;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.Properties;

/**
 * User: jhastings
 * Date: 10/13/14
 * Time: 3:54 PM
 */
public class EzSecurityTokenValidatorTest {

    private static final String serverPrivatePath = "ezbakesecurity-key.pem";
    static PKeyCrypto serverCrypt;

    @BeforeClass
    public static void setUp() throws InvalidKeySpecException, NoSuchAlgorithmException, IOException {
        serverCrypt = new RSAKeyCrypto(Resources.toString(Resources.getResource(serverPrivatePath), StandardCharsets.UTF_8), true);
    }

    Properties configuration;
    @Before
    public void setUpTest() throws EzConfigurationLoaderException {
        configuration = new EzConfiguration(new ClasspathConfigurationLoader()).getProperties();
    }

    private static void signEzSecurityToken(EzSecurityToken token) throws IOException, PKeyCryptoException {
        token.getValidity().setSignature(EzSecurityTokenUtils.tokenSignature(token, serverCrypt));
    }

    @Test
    public void testValidateReceivedToken() throws IOException, PKeyCryptoException, EzSecurityTokenException, TokenExpiredException {
        EzSecurityToken token = MockEzSecurityToken.getMockUserToken("TEST", "low", Sets.newHashSet("A"),
                Maps.<String, List<String>>newHashMap(), false);
        token.getValidity().setIssuedFor(configuration.getProperty(EzBakePropertyConstants.EZBAKE_SECURITY_ID));
        signEzSecurityToken(token);

        EzSecurityTokenValidator validator = new EzSecurityTokenValidator(configuration);
        // would throw if this test failed
        validator.validateToken(token);
    }

    @Test(expected=EzSecurityTokenException.class)
    public void testValidateIssuedForOther() throws IOException, PKeyCryptoException, EzSecurityTokenException, TokenExpiredException {
        EzSecurityToken token = MockEzSecurityToken.getMockUserToken("TEST", "low", Sets.newHashSet("A"),
                Maps.<String, List<String>>newHashMap(), false);
        token.getValidity().setIssuedFor("NotIssuedForMe");
        signEzSecurityToken(token);

        EzSecurityTokenValidator validator = new EzSecurityTokenValidator(configuration);
        // would throw if this test failed
        validator.validateToken(token);
    }

    @Test(expected=TokenExpiredException.class)
    public void testValidateTokenExpired() throws IOException, PKeyCryptoException, EzSecurityTokenException, TokenExpiredException {
        EzSecurityToken token = MockEzSecurityToken.getMockUserToken("TEST", "low", Sets.newHashSet("A"),
                Maps.<String, List<String>>newHashMap(), false);
        token.getValidity().setIssuedFor(configuration.getProperty(EzBakePropertyConstants.EZBAKE_SECURITY_ID));
        token.getValidity().setNotAfter(System.currentTimeMillis());
        signEzSecurityToken(token);

        EzSecurityTokenValidator validator = new EzSecurityTokenValidator(configuration);
        // would throw if this test failed
        validator.validateToken(token);
    }
}
