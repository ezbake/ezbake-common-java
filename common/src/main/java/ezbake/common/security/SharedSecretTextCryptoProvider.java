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

import java.nio.charset.Charset;
import org.jasypt.exceptions.EncryptionInitializationException;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.salt.FixedStringSaltGenerator;

public class SharedSecretTextCryptoProvider implements TextCryptoProvider {
    private StandardPBEStringEncryptor encryptor;
    private final String SALT = "bouncycastle";
    //private BasicTextEncryptor encryptor;

    public SharedSecretTextCryptoProvider(String secret) {
        this.encryptor = new StandardPBEStringEncryptor();
        this.encryptor.setAlgorithm("PBEWithMD5AndDES");
        FixedStringSaltGenerator saltGenerator = new FixedStringSaltGenerator();
        saltGenerator.setCharset(Charset.defaultCharset().name());
        saltGenerator.setSalt(SALT);
        this.encryptor.setSaltGenerator(saltGenerator);
        this.encryptor.setPassword(secret);
    }

    @Override
    public String encrypt(String message) throws SecurityException {
        try {
            return encryptor.encrypt(message);
        } catch(EncryptionInitializationException e) {
            throw new SecurityException(e);
        } catch(EncryptionOperationNotPossibleException e) {
            throw new SecurityException(e);
        }
    }

    @Override
    public String decrypt(String message) throws SecurityException {
        try {
            return encryptor.decrypt(message);
        } catch(EncryptionInitializationException e) {
            throw new SecurityException(e);
        } catch(EncryptionOperationNotPossibleException e) {
            throw new SecurityException(e);
        }
    }
}
