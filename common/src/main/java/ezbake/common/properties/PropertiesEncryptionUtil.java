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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;

import ezbake.common.security.TextCryptoProvider;

import java.util.Properties;

public final class PropertiesEncryptionUtil {

    @VisibleForTesting
    final static String PROPERTY_ENCRYPTION_PREFIX = "ENC~(";
    @VisibleForTesting
    final static String PROPERTY_ENCRYPTION_SUFFIX = ")~";

    public static boolean isEncryptedProperty(Properties props, String key) {
        String value = props.getProperty(key);
        return isEncryptedValue(value);
    }

    public static String decryptProperty(Properties props, String key, TextCryptoProvider provider)
        throws SecurityException{
        if(!isEncryptedProperty(props, key)) {
            return null;
        }

        return decryptPropertyValue(props.getProperty(key), provider);
    }

    public static String encryptPropertyValue(String value, TextCryptoProvider provider) throws SecurityException {
        return PROPERTY_ENCRYPTION_PREFIX + provider.encrypt(value) + PROPERTY_ENCRYPTION_SUFFIX;
    }

    static String decryptPropertyValue(String value, TextCryptoProvider provider) {
        String innerValue = getInnerValue(value);
        return provider.decrypt(innerValue);
    }

    static boolean isEncryptedValue(String value) {
        if(Strings.isNullOrEmpty(value)) {
            return false;
        }

       String trimmedValue = value.trim();
       return trimmedValue.startsWith(PROPERTY_ENCRYPTION_PREFIX) && trimmedValue.endsWith(PROPERTY_ENCRYPTION_SUFFIX);
    }

    private static String getInnerValue(String value) {
        return value.substring(PROPERTY_ENCRYPTION_PREFIX.length(),
            (value.length() - PROPERTY_ENCRYPTION_SUFFIX.length()));
    }
}
