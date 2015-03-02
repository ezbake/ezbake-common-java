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

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.DESedeEngine;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performs Triple DES Symmetric key cryptography.
 * @author gdrocella
 * @date 07/28/14
 */
public class TDESCrypto {
    Logger log = LoggerFactory.getLogger(TDESCrypto.class);
    
    private PaddedBufferedBlockCipher encryptCipher; 
    private PaddedBufferedBlockCipher decryptCipher;
    
    private byte[] key;
    
    public TDESCrypto(byte[] key) {
        encryptCipher = new PaddedBufferedBlockCipher(new DESedeEngine());
        decryptCipher = new PaddedBufferedBlockCipher(new DESedeEngine());
        
        encryptCipher.init(true, new KeyParameter(key));
        decryptCipher.init(false, new KeyParameter(key));
        
        this.key = key;
    }
    
    public String encrypt(byte[] data) throws DataLengthException, IllegalStateException, InvalidCipherTextException {
        log.debug("TDESCrypto Encrypt {}", data);
        
        byte[] encrypted = new byte[data.length + (data.length % 8)];
        
        int p = encryptCipher.processBytes(data, 0, data.length, encrypted, 0);
        p = encryptCipher.doFinal(encrypted, p);
        
        log.debug("Encrypted Data {}", encrypted);
        
        String b64Encoded = new String(Base64.encode(encrypted));
        return b64Encoded;
    }
    
    public byte[] decrypt(String data) throws DataLengthException, IllegalStateException, InvalidCipherTextException {
        log.debug("TDESDecrypt {}",data);
        
        byte[] decoded = Base64.decode(data.getBytes());
        byte[] unciphered = new byte[decoded.length];
        
        int p = decryptCipher.processBytes(decoded, 0, unciphered.length, unciphered, 0);
        p = decryptCipher.doFinal(unciphered, p);

        log.debug("Decrypted Data {}", unciphered);
        
        return unciphered;
        
    }
}
