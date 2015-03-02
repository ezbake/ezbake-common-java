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

/**
 * A common interface for doing text encryption.
 */
public interface TextCryptoProvider {
    /**
     * Encrypts a message and return a "encrypted" message.
     *
     * @param message the message to be encrypted
     *
     * @throws SecurityException if an error occurs while encrypting the message
     */
    public String encrypt(String message) throws SecurityException;
    
    /**
     * Decrypt a message and return a plain text message.
     *
     * @param encryptedMessage the message to be decrypted
     *
     * @throws SecurityException if an error occurs while trying to decrypt the message.
     */
    public String decrypt(String encryptedMessage) throws SecurityException;
}
