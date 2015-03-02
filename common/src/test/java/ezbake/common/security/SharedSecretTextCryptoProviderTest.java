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

package ezbake.common.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import ezbake.common.properties.EzProperties;
import org.junit.Test;

import java.util.Properties;

public class SharedSecretTextCryptoProviderTest {
    @Test
    public void testEncryptDecrypt() throws SecurityException {
        final String plainText = "My test message";
        final String password = "swordfish";
        TextCryptoProvider provider = new SharedSecretTextCryptoProvider(password);
        String cipherText = provider.encrypt(plainText);
        assertEquals("The decrypted text does not match the plain text!", plainText, provider.decrypt(cipherText));
    }

    @Test
    public void testEncryptDecryptDollarSigns() throws SecurityException {
        final String plainText = "$mongo99$mongo99";
        final String password = "ognsVcuS3N4InhpnuiHEliPcb4JSe1FE";
        TextCryptoProvider provider = new SharedSecretTextCryptoProvider(password);
        String cipherText = provider.encrypt(plainText);
        String actualValue = provider.decrypt(cipherText);
        assertEquals("The decrypted text does not match the plain text!", plainText, actualValue);
    }

    @Test
    public void testWithProperties() throws SecurityException {
        EzProperties ezProperties = new EzProperties();
        ezProperties.setTextCryptoProvider(new SharedSecretTextCryptoProvider("secret"));
        Properties propertiesToEncrypt = new Properties();
        propertiesToEncrypt.put("database1.password", "$mongo99$mongo99");
        propertiesToEncrypt.put("database2.password", "superpassword");

        for(String key : propertiesToEncrypt.stringPropertyNames()) {
            String value = propertiesToEncrypt.getProperty(key);
            ezProperties.setProperty(key, value, true);
        }

        assertTrue(ezProperties.stringPropertyNames().size() > 0);
        for (String key : ezProperties.stringPropertyNames()) {
            String decryptedValue = ezProperties.getProperty(key);
            String originalValue = propertiesToEncrypt.getProperty(key);
            assertEquals(originalValue, decryptedValue);
        }

    }
}
