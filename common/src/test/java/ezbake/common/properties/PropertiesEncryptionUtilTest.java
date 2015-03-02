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

package ezbake.common.properties;

import ezbake.common.security.TextCryptoProvider;
import ezbake.common.security.NoOpTextCryptoProvider;

import java.util.Properties;

import org.junit.Before;
import org.junit.BeforeClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class PropertiesEncryptionUtilTest {

    private static TextCryptoProvider provider;
    private Properties props;

    @BeforeClass
    public static void setupBeforeClass() {
        provider = new NoOpTextCryptoProvider();
    }

    @Before
    public void setup() {
        this.props = new Properties();
    }

    @Test
    public void testIsEncryptedProperty() {
        assertFalse(PropertiesEncryptionUtil.isEncryptedProperty(props, "NonExitentKey"));
        final String testKey = "testKey";
        props.setProperty(testKey, PropertiesEncryptionUtil.PROPERTY_ENCRYPTION_PREFIX + "text"
            + PropertiesEncryptionUtil.PROPERTY_ENCRYPTION_SUFFIX);
        assertTrue(PropertiesEncryptionUtil.isEncryptedProperty(props, testKey));
    }

    @Test
    public void testDecryptProperty() throws SecurityException {
        final String testKey = "testKey";
        final String testValue = "test";
        props.setProperty(testKey, PropertiesEncryptionUtil.PROPERTY_ENCRYPTION_PREFIX + testValue
            + PropertiesEncryptionUtil.PROPERTY_ENCRYPTION_SUFFIX);

        assertEquals(testValue, PropertiesEncryptionUtil.decryptProperty(props, testKey, provider));
    }

    @Test
    public void testEncryptPropertyValue() throws SecurityException {
        final String testValue = "test";
        final String expected = PropertiesEncryptionUtil.PROPERTY_ENCRYPTION_PREFIX + testValue
            + PropertiesEncryptionUtil.PROPERTY_ENCRYPTION_SUFFIX;

        assertEquals(expected, PropertiesEncryptionUtil.encryptPropertyValue(testValue, provider));
    }
}
