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

import static org.junit.Assert.*;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.DESedeEngine;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TDESCryptoTest {
    
    private static Logger log = LoggerFactory.getLogger(TDESCryptoTest.class);
    
    private static TDESCrypto tdesCrypto = null;
    private static String key = "TestTestTestTest";
    
    @BeforeClass
    public static void init() {
        tdesCrypto = new TDESCrypto(key.getBytes());
    }
    
    @Test
    public void test() throws DataLengthException, IllegalStateException, InvalidCipherTextException {
        PaddedBufferedBlockCipher encryptCipher = new PaddedBufferedBlockCipher(new DESedeEngine());
        PaddedBufferedBlockCipher decryptCipher = new PaddedBufferedBlockCipher(new DESedeEngine());
        
        byte inBuff[] = "Hello Wd".getBytes();
        byte[] outBuff = new byte[512];
        byte[] keyBytes = "TestTestTestTest".getBytes();
        byte[] uncipherData = new byte[8];
        
        encryptCipher.init(true, new KeyParameter(keyBytes));
        decryptCipher.init(false, new KeyParameter(keyBytes));
        
        encryptCipher.processBytes(inBuff, 0, inBuff.length, outBuff, 0);
        encryptCipher.doFinal(outBuff, 0);
        
        decryptCipher.processBytes(outBuff, 0, 2*inBuff.length, uncipherData, 0);
        decryptCipher.doFinal(uncipherData, 0);
        
        log.debug("Uncipher Data: {}", uncipherData);
        
        assertTrue("Hello Wd".equals(new String(uncipherData)));
    }
    
    @Test
    public void testTDesEncryption() throws DataLengthException, IllegalStateException, InvalidCipherTextException {
        String cipher = tdesCrypto.encrypt("Hello World!".getBytes());
        
        log.debug("Base 64 Encoded {}", cipher);
        
        assertTrue(cipher != null);
        
        byte[] uncipher = tdesCrypto.decrypt(cipher);
        log.debug("Uncipher {} {}", uncipher, uncipher.length);
        
        assertTrue("Hello World!".compareTo(new String(uncipher).trim()) == 0);
    }
}
