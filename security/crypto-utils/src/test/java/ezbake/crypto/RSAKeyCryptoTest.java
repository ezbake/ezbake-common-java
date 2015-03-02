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

package ezbake.crypto;

import org.apache.commons.io.FileUtils;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;


import static org.junit.Assert.assertTrue;

public class RSAKeyCryptoTest {

    private static Logger log = LoggerFactory.getLogger(RSAKeyCryptoTest.class);

    private static final String serverPrivatePath = "src/test/resources/application.priv";
    private static final String rubyKeyFolder = "src/test/resources/ruby_gen_keys";

    private static String privateKey;

    @BeforeClass
    public static void init() throws IOException {
        log.info("RSAKeyCrypto JUnit Test Initialization");
        privateKey = FileUtils.readFileToString(new File(serverPrivatePath));
    }


    @Test
    public void test1() throws
            NoSuchAlgorithmException, InvalidKeyException, SignatureException, PKeyCryptoException,
            InvalidKeySpecException {
        RSAKeyCrypto crypto = new RSAKeyCrypto(privateKey, true);
        byte[] data = "Some Data".getBytes();
        byte[] cipherData = crypto.sign(data);
        assertTrue(crypto.verify(data, cipherData));
    }

    @Test
    public void testGenerateCSR() {
        RSAKeyCrypto crypto = new RSAKeyCrypto();
        assertTrue(crypto.hasPublic() && crypto.hasPrivate());
        String csr = crypto.getCSR("CN=Gary Drocella");
        Assert.assertNotNull(csr);
        Assert.assertTrue(!csr.isEmpty());
    }

    @Test
    public void testRubyGeneratedKeys() throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
        File privateKey = new File(rubyKeyFolder, "application.priv");
        File publicKey = new File(rubyKeyFolder, "ezbakesecurityservice.pub");

        String priv = FileUtils.readFileToString(privateKey);
        String pub = FileUtils.readFileToString(publicKey);

        RSAKeyCrypto crypto = new RSAKeyCrypto(priv, pub);

        // pass - just making sure no exceptions thrown
    }

    @Test
    public void test2() throws InvalidKeySpecException, NoSuchAlgorithmException, PKeyCryptoException, InvalidKeyException, SignatureException {
        String publicPEM = RSAKeyCrypto.getPublicFromPrivatePEM(privateKey);
        RSAKeyCrypto crypto = new RSAKeyCrypto(privateKey, publicPEM);

        byte[] data = "Some Data".getBytes();
        byte[] cipherData = crypto.sign(data);
        assertTrue(crypto.verify(data, cipherData));
    }

    @Test
    public void testPEMPublic() {
        RSAKeyCrypto key = new RSAKeyCrypto();
        Assert.assertTrue(key.getPublicPEM().startsWith("-----BEGIN PUBLIC KEY-----"));
        Assert.assertTrue(key.getPublicPEM().endsWith("-----END PUBLIC KEY-----\n"));
    }

}
