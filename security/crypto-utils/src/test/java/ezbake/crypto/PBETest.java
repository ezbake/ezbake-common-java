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

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PBETest {
    
    private Logger log = LoggerFactory.getLogger(PBETest.class);
    
    
    @Test
    public void test() throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, ShortBufferException {
        PBECrypto pbeCrypto = new PBECrypto("Pass Phrase");
        SecretKey key = pbeCrypto.generateKey();
        
        String testStr = "A";
        
        for(int i = 0; i <= 26; i++) {
            testStr = testStr + "A";
            log.debug("{}",testStr);
            byte[] cipher = pbeCrypto.encrypt(key, testStr.getBytes());
            byte[] uncipher = pbeCrypto.decrypt(key, cipher);
            assertTrue(testStr.equals(new String(uncipher)));
        }
    }
}
