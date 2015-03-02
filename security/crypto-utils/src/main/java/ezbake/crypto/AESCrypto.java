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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;

public class AESCrypto {
    Logger log = LoggerFactory.getLogger(AESCrypto.class);

    public AESCrypto() {

    }

    public SecretKey generateAESKey() {
        SecretKey key = null;
        try {
            KeyGenerator keygenerator = KeyGenerator.getInstance("AES");
            keygenerator.init(256);
            key = keygenerator.generateKey();
        } catch (NoSuchAlgorithmException e) {
            log.error("Error generating an AES 256 bit key {}", e);
            throw new RuntimeException("Unable to generate AES Keys with invalid Java cryptography configuration", e);
        }

        return key;
    }

    public byte[] encrypt(SecretKey key, byte[] data) {
        byte[] cipherData = null;

        try {

            Cipher cipher = Cipher.getInstance("AES");

            // initialise cipher to with secret key
            cipher.init(Cipher.ENCRYPT_MODE, key);

            cipherData = cipher.doFinal(data);
        }
        catch(Exception e) {
            log.error("Error: {}", e);
        }

        return cipherData;
    }

    public byte[] decrypt(SecretKey key, byte[] cipherData) {
        byte[] data = null;

        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            data = cipher.doFinal(cipherData);
        }
        catch(Exception e) {
            log.error("Error: {}",e);
        }

        return data;
    }
}
