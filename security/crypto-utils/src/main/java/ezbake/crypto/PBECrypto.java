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

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PBECrypto {
    
    private Logger log = LoggerFactory.getLogger(PBECrypto.class);
    
    private static Random r = new SecureRandom();
    private byte[] salt;
    private String passcode;
    private PBEParameterSpec spec;
    
    public static final int SZ = 8;
    
    public PBECrypto(String passcode) {
        this.passcode = passcode;
        salt = new byte[SZ];
        
        // Generate a random salt
        r.nextBytes(salt);
        
        spec = new PBEParameterSpec(salt,SZ);
    }
    
    public SecretKey generateKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        log.debug("Generate PBE Key with passcode {}", passcode);
        PBEKeySpec pbeKeySpec = new PBEKeySpec(this.passcode.toCharArray());
        SecretKeyFactory skFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
        
        return skFactory.generateSecret(pbeKeySpec);
    }
    
    public byte[] encrypt(SecretKey key, byte[] data) throws InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, ShortBufferException {
        log.info("PBE Encrypt {}", data);
        log.info("With Key {}", key.getEncoded());
        byte[] encrypted = new byte[data.length + 8];
        Cipher encryptCipher = Cipher.getInstance("PBEWithMD5AndDES");
        encryptCipher.init(Cipher.ENCRYPT_MODE, key, spec);
        
     
        encrypted = encryptCipher.doFinal(data);
        
        return encrypted;
    }
    
    public byte[] decrypt(SecretKey key, byte[] data) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, ShortBufferException {
        log.info("PBE Decrypt {}", data);
        log.info("With Key {}", key.getEncoded());
        Cipher decryptCipher = Cipher.getInstance("PBEWithMD5AndDES");
        decryptCipher.init(Cipher.DECRYPT_MODE, key, spec);
        byte[] unciphered = null;
        
        unciphered = decryptCipher.doFinal(data);

        log.info("Unciphered {}", unciphered);
        
        return unciphered;
    }
    
    public void setSalt(byte[] salt) {
        this.salt = salt;
        spec = new PBEParameterSpec(salt,SZ);
    }
    
    public byte[] getSalt() {
        return this.salt;
    }
    
    private byte[] truncate(byte[] data) {
        int count = 0;
        for(int i = data.length-1; i>= 0; i--) {
            if(data[i] != 0x0) {
                break;
            }
            count++;
        }
        
        byte[] truncated = new byte[data.length-count];
        
        for(int i = 0; i < truncated.length; i++) {
            truncated[i] = data[i];
        }
        
        return truncated;
    }
    
 
}
