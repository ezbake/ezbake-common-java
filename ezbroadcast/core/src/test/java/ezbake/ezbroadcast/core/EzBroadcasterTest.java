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

package ezbake.ezbroadcast.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.util.Properties;

import com.google.common.io.Resources;
import com.google.common.collect.Sets;
import ezbake.base.thrift.AdvancedMarkings;
import ezbake.base.thrift.EzSecurityTokenException;
import ezbake.base.thrift.Visibility;
import ezbake.configuration.constants.EzBakePropertyConstants;
import ezbake.security.client.EzSecurityTokenWrapper;
import ezbake.security.client.EzbakeSecurityClient;
import ezbake.thrift.ThriftTestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;

import com.google.common.io.Files;
import com.google.common.base.Optional;

import ezbake.ezbroadcast.core.thrift.SecureMessage;

import static org.mockito.Mockito.*;

public class EzBroadcasterTest {
    private EzBroadcaster broadcaster;
    private static final Logger log = LoggerFactory.getLogger(EzBroadcasterTest.class);

    @Test(expected = NullPointerException.class)
    public void testCreate_MissingClass() {
        log.info("STARTING TEST: [MISSING_CLASS]");
        broadcaster = EzBroadcaster.create(new Properties(), "test");
    }

    @Test(expected = RuntimeException.class)
    public void testCreate_BadClass() { 
        log.info("STARTING TEST: [BAD_CLASS]");
        Properties props = new Properties();
        props.setProperty(EzBroadcaster.BROADCASTER_CLASS, "whatever");
        broadcaster = EzBroadcaster.create(props, "test");
    }

    @Test
    public void testBroadcast_NotProduction() throws IOException, EzSecurityTokenException {
        log.info("STARTING TEST: [NOT_PRODUCTION]");
        String messageToSend = "hello";
        setupBroadcaster(false, false);
        broadcaster.broadcast("topic", new Visibility().setFormalVisibility("U"), messageToSend.getBytes());
        String message = new String(((InMemoryBroadcaster)broadcaster).getMessageFromTopic("topic"));

        assertTrue("Message is correct and unencrypted", message.contains(messageToSend));
        assertEquals("Message is received", messageToSend, new String(broadcaster.receive("topic").get().getContent()));
    }

    @Test
    public void testBroadcast_EncryptedCorrectAuths() throws Exception { 
        log.info("STARTING TEST: [ENCRYPTED_CORRECT_AUTHS]");
        String messageToSend = "something";
        setupBroadcaster(true, true);

        broadcaster.broadcast("encrypted", new Visibility().setFormalVisibility("U"), messageToSend.getBytes("UTF-8"));

        byte[] payload = ((InMemoryBroadcaster)broadcaster).getMessageFromTopic("encrypted");
        assertFalse("Encrypted string should not equal original", new String(payload).contains(messageToSend));
        log.info("Encrypted messages not equal: [" + messageToSend + "] - [" + new String(payload).replaceAll("\\s", "") + "]");

        byte[] decryptedPayload = broadcaster.receive("encrypted").get().getContent();
        assertEquals("Message is received and decrypted properly", messageToSend, new String(decryptedPayload));
        log.info("Decrypted messages are equal: [" + messageToSend + "] - [" + new String(decryptedPayload) + "]");
    }

    @Test
    public void testBroadcast_EncryptedCorrectAuthsExpression() throws Exception { 
        log.info("STARTING TEST: [ENCRYPTED_CORRECT_AUTHS_EXPRESSION]");
        String messageToSend = "something";
        setupBroadcaster(true, true);

        broadcaster.broadcast("encrypted", new Visibility().setFormalVisibility("S&(USA|CAN)"), messageToSend.getBytes("UTF-8"));

        byte[] payload = ((InMemoryBroadcaster)broadcaster).getMessageFromTopic("encrypted");
        assertFalse("Encrypted string should not equal original", new String(payload).contains(messageToSend));

        byte[] decryptedPayload = broadcaster.receive("encrypted").get().getContent();
        assertEquals("Message is received and decrypted properly", messageToSend, new String(decryptedPayload));
    }

    @Test
    public void testBroadcast_EncryptedCorrectAuthsExpressionWithCommunities() throws Exception {
        log.info("STARTING TEST: [ENCRYPTED_CORRECT_AUTHS_EXPRESSION_WITH_COMMUNITIES]");
        String messageToSend = "something";
        setupBroadcaster(true, true);

        broadcaster.broadcast("encrypted", new Visibility().setFormalVisibility("S&(USA|CAN)").setAdvancedMarkings(new AdvancedMarkings().setExternalCommunityVisibility("TEST")), messageToSend.getBytes("UTF-8"));

        byte[] payload = ((InMemoryBroadcaster)broadcaster).getMessageFromTopic("encrypted");
        assertFalse("Encrypted string should not equal original", new String(payload).contains(messageToSend));

        byte[] decryptedPayload = broadcaster.receive("encrypted").get().getContent();
        assertEquals("Message is received and decrypted properly", messageToSend, new String(decryptedPayload));
    }

    @Test
    public void testBroadcast_EncryptedIncorrectAuthsExpressionWithCommunities() throws Exception {
        log.info("STARTING TEST: [ENCRYPTED_INCORRECT_AUTHS_EXPRESSION_WITH_COMMUNITIES]");
        String messageToSend = "something";
        setupBroadcaster(true, true);

        broadcaster.broadcast("encrypted", new Visibility().setFormalVisibility("S&(USA|CAN)").setAdvancedMarkings(new AdvancedMarkings().setExternalCommunityVisibility("TEST&OTHER")), messageToSend.getBytes("UTF-8"));

        byte[] payload = ((InMemoryBroadcaster)broadcaster).getMessageFromTopic("encrypted");
        assertFalse("Encrypted string should not equal original", new String(payload).contains(messageToSend));

        Optional<SecureMessage> received = broadcaster.receive("encrypted");
        assertFalse("Message is received and decrypted properly", received.isPresent());
    }

    @Test
    public void testBroadcast_EncryptedIncorrectAuths() throws Exception {
        log.info("STARTING TEST: [ENCRYPTED_INCORRECT_AUTHS]");
        String messageToSend = "shouldn't get this message back";
        setupBroadcaster(true, true);

        broadcaster.broadcast("encrypted", new Visibility().setFormalVisibility("AB&G"), messageToSend.getBytes("UTF-8"));

        byte[] payload = ((InMemoryBroadcaster)broadcaster).getMessageFromTopic("encrypted");
        assertFalse("Encrypted string should not equal original", new String(payload).contains(messageToSend));

        Optional<SecureMessage> received = broadcaster.receive("encrypted");
        assertFalse("Message is received and decrypted properly", received.isPresent());
    }
    
    @Test(expected = RuntimeException.class)
    public void test_unregisteredTopic() throws Exception { 
        log.info("STARTING TEST: [UNREGISTERED_TOPIC]");
        String messageToSend = "hello there!";
        setupBroadcaster(true, true);

        broadcaster.broadcast("encrypted", new Visibility().setFormalVisibility("U"), messageToSend.getBytes("UTF-8"));

        byte[] payload = ((InMemoryBroadcaster)broadcaster).getMessageFromTopic("encrypted");
        assertFalse("Encrypted string should not equal original", new String(payload).contains(messageToSend));
        
        broadcaster.unregisterFromTopic("encrypted");

        byte[] decryptedPayload = broadcaster.receive("encrypted").get().getContent();
        log.info("We should not see this log: [" + new String(decryptedPayload) + "]");
    }

    @Test(expected = IOException.class)
    public void testBroadcast_EncryptedBadVisString() throws Exception {
        log.info("STARTING TEST: [ENCRYPTED_BAD_VIS_STRING]");
        String messageToSend = "shouldn't get this message back";
        setupBroadcaster(true, true);

        broadcaster.broadcast("encrypted", new Visibility().setFormalVisibility("AB&G@#$"), messageToSend.getBytes("UTF-8"));

        byte[] payload = ((InMemoryBroadcaster)broadcaster).getMessageFromTopic("encrypted");
        assertFalse("Encrypted string should not equal original", new String(payload).contains(messageToSend));

        Optional<SecureMessage> received = broadcaster.receive("encrypted");
        assertFalse("Message is received and decrypted properly", received.isPresent());
    }

    @Test(expected = RuntimeException.class)
    public void testInitializeEncryption_NoKey() throws IOException, EzSecurityTokenException {
        log.info("STARTING TEST: [INITIALIZE_ENCRYPTION_NO_KEY]");
        String messageToSend = "hello";
        setupBroadcaster(true, true);
        broadcaster.broadcast("topic", new Visibility().setFormalVisibility("U"), messageToSend.getBytes());
    }

    @Test
    public void testBroadcast_NoPrivateKey() throws IOException, EzSecurityTokenException {
        log.info("STARTING TEST: [BROADCAST_NO_PRIVATE_KEY]");
        String messageToSend = "Shouldn't get this message back";
        setupBroadcaster(true, true);

        broadcaster.broadcast("pubkey", new Visibility().setFormalVisibility("AB&G"), messageToSend.getBytes("UTF-8"));

        byte[] payload = ((InMemoryBroadcaster) broadcaster).getMessageFromTopic("pubkey");
        assertFalse("Encrypted string should not equal original", new String(payload).contains(messageToSend));

        try {
            Optional<SecureMessage> received = broadcaster.receive("pubkey");
            assertTrue("The broadcaster successfully received a message, which should not be the case", false);
        } catch (RuntimeException e) {
            assertTrue("A runtime exception was thrown because the broadcaster is not equipped to receive messages", true);
        }
    }

    @Test(expected = RuntimeException.class)
    public void setBadEncryptionFile() throws IOException {
        log.info("STARTING TEST: [BAD_ENCRYPTION_KEY]");
        Properties props = new Properties();
        props.setProperty(EzBroadcaster.BROADCASTER_CLASS, "ezbake.ezbroadcast.core.InMemoryBroadcaster");
        props.setProperty(EzBroadcaster.PRODUCTION_MODE, Boolean.toString(true));
        props.setProperty(EzBakePropertyConstants.EZBAKE_SECURITY_ID, "client");
        props.put(EzBakePropertyConstants.EZBAKE_SECURITY_SERVICE_MOCK_SERVER, Boolean.toString(true));
        props.setProperty(EzBakePropertyConstants.EZBAKE_SSL_CIPHERS_KEY, "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA,TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,TLS_RSA_WITH_AES_128_CBC_SHA");
        props.setProperty(EzBakePropertyConstants.EZBAKE_CERTIFICATES_DIRECTORY, System.getProperty("user.dir") + File.separator + "src/test/resources/ssl/client");
        broadcaster = EzBroadcaster.create(props, "test", "123456781234567812345678123456", "badkey", true);
        broadcaster.registerBroadcastTopic("badkey");
    }
    
    @Test
    public void testLoadKeyTwice() throws IOException {
        log.info("STARTING TEST: [LOAD_KEY_TWICE]");
        Properties props = new Properties();
        props.setProperty(EzBroadcaster.BROADCASTER_CLASS, "ezbake.ezbroadcast.core.InMemoryBroadcaster");
        props.setProperty(EzBroadcaster.PRODUCTION_MODE, Boolean.toString(true));
        props.setProperty(EzBakePropertyConstants.EZBAKE_SECURITY_ID, "client");
        props.put(EzBakePropertyConstants.EZBAKE_SECURITY_SERVICE_MOCK_SERVER, Boolean.toString(true));
        props.setProperty(EzBakePropertyConstants.EZBAKE_SSL_CIPHERS_KEY, "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA,TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,TLS_RSA_WITH_AES_128_CBC_SHA");
        props.setProperty(EzBakePropertyConstants.EZBAKE_CERTIFICATES_DIRECTORY, System.getProperty("user.dir") + File.separator + "src/test/resources/ssl/client");
            
        
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("keys/pubkey.pub");
        broadcaster = EzBroadcaster.create(props, "test", getKeyData("pubkey.pub", inputStream), "pubkey", false);
        broadcaster.registerBroadcastTopic("pubkey");
    }

    @Test(expected = RuntimeException.class)
    public void testRegisterBroadcaster_NoKey() throws IOException, EzSecurityTokenException {
        log.info("STARTING TEST: [REGISTER_BROADCASTER_NO_KEY]");
        setupBroadcaster(true, true);
        broadcaster.registerBroadcastTopic("nokey");
    }

    @Test(expected = RuntimeException.class)
    public void testSubscribeTopic_NoKey() throws IOException, EzSecurityTokenException {
        log.info("STARTING TEST: [SUBCRIBE_TOPIC_NO_KEY]");
        setupBroadcaster(true, true);
        broadcaster.subscribeToTopic("nokey");
    }

    @Test(expected = RuntimeException.class)
    public void testIncorrectKeySize() throws IOException {
        log.info("STARTING TEST: [INCORRECT_KEY_SIZE]");
        Properties props = new Properties();
        props.setProperty(EzBroadcaster.BROADCASTER_CLASS, "ezbake.ezbroadcast.core.InMemoryBroadcaster");
        props.setProperty(EzBroadcaster.PRODUCTION_MODE, Boolean.toString(true));
        props.setProperty(EzBakePropertyConstants.EZBAKE_SECURITY_ID, "client");
        props.put(EzBakePropertyConstants.EZBAKE_SECURITY_SERVICE_MOCK_SERVER, Boolean.toString(true));
        props.setProperty(EzBakePropertyConstants.EZBAKE_SSL_CIPHERS_KEY, "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA,TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,TLS_RSA_WITH_AES_128_CBC_SHA");
        props.setProperty(EzBakePropertyConstants.EZBAKE_CERTIFICATES_DIRECTORY, System.getProperty("user.dir") + File.separator + "src/test/resources/ssl/client");
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("keys/largekey.priv");
        broadcaster = EzBroadcaster.create(props, "test", getKeyData("largekey.priv", inputStream), "largekey", true);
        broadcaster.registerBroadcastTopic("largekey");
    }

    @Test
    public void setupBroadcasterWithKey() throws Exception {
        log.info("STARTING TEST: [SETUP_BROADCASTER_WITH_KEY]");
        Properties props = new Properties();
        props.setProperty(EzBroadcaster.BROADCASTER_CLASS, "ezbake.ezbroadcast.core.InMemoryBroadcaster");
        props.setProperty(EzBroadcaster.PRODUCTION_MODE, Boolean.toString(true));
        props.setProperty(EzBakePropertyConstants.EZBAKE_SECURITY_ID, "client");
        props.put(EzBakePropertyConstants.EZBAKE_SECURITY_SERVICE_MOCK_SERVER, Boolean.toString(true));
        props.setProperty(EzBakePropertyConstants.EZBAKE_SSL_CIPHERS_KEY, "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA,TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,TLS_RSA_WITH_AES_128_CBC_SHA");
        props.setProperty(EzBakePropertyConstants.EZBAKE_CERTIFICATES_DIRECTORY, System.getProperty("user.dir") + File.separator + "src/test/resources/ssl/client");
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("keys/encrypted.priv");
        String strKey = getKeyData("keys/encrypted.priv", inputStream);
        broadcaster = EzBroadcaster.create(props, "test", strKey, "encrypted", true);
        setupMock();

        String messageToSend = "something";

        broadcaster.broadcast("encrypted", new Visibility().setFormalVisibility("S&(USA|CAN)"), messageToSend.getBytes("UTF-8"));

        byte[] payload = ((InMemoryBroadcaster)broadcaster).getMessageFromTopic("encrypted");
        assertFalse("Encrypted string should not equal original", new String(payload).contains(messageToSend));

        byte[] decryptedPayload = broadcaster.receive("encrypted").get().getContent();
        assertEquals("Message is received and decrypted properly", messageToSend, new String(decryptedPayload));
    }

    private void setupBroadcaster(boolean isProduction, boolean loadKeys) throws EzSecurityTokenException {
        Properties props = new Properties();
        props.setProperty(EzBroadcaster.BROADCASTER_CLASS, "ezbake.ezbroadcast.core.InMemoryBroadcaster");
        props.setProperty(EzBroadcaster.PRODUCTION_MODE, Boolean.toString(isProduction));
        props.setProperty(EzBakePropertyConstants.EZBAKE_SECURITY_ID, "client");
        props.put(EzBakePropertyConstants.EZBAKE_SECURITY_SERVICE_MOCK_SERVER, Boolean.toString(true));
        props.setProperty(EzBakePropertyConstants.EZBAKE_SSL_CIPHERS_KEY, "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA,TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,TLS_RSA_WITH_AES_128_CBC_SHA");
        props.setProperty(EzBakePropertyConstants.EZBAKE_CERTIFICATES_DIRECTORY, System.getProperty("user.dir") + File.separator + "src/test/resources/ssl/client");
        broadcaster = EzBroadcaster.create(props, "test");

        if (loadKeys) {
            broadcaster.registerBroadcastTopic("encrypted");
            broadcaster.registerBroadcastTopic("pubkey");
        }
        setupMock();
    }

    private void setupMock() throws EzSecurityTokenException {
        EzSecurityTokenWrapper mockToken = new EzSecurityTokenWrapper(ThriftTestUtils.generateTestSecurityToken("U", "AB", "B", "CD", "EF", "USA", "CAN"));
        mockToken.getAuthorizations().setExternalCommunityAuthorizations(Sets.newHashSet("TEST"));
        broadcaster.security = when(mock(EzbakeSecurityClient.class).fetchAppToken()).thenReturn(mockToken).getMock();
    }

    private String getKeyData(String fileName, InputStream inputStream) {
        String keyData = "";
        final int BUFFER = 4096;
        int count = 0, total = 0;
        byte data[] = new byte[BUFFER];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        String keyName = Files.getNameWithoutExtension(fileName).toLowerCase();
        String extension = Files.getFileExtension(fileName).toLowerCase();

        try {

            // We only want pub or priv key files. 
            if (!extension.equalsIgnoreCase("pub") && !extension.equalsIgnoreCase("priv")) { 
                log.error("Extension [{}] is not valid for topic [{}].", extension, keyName);
                return keyData;
            }

            while (total <= BUFFER && (count = inputStream.read(data, 0, BUFFER)) != -1) { 
                baos.write(data, 0, count);
                total += count;
            }

            if (total > BUFFER) {
                throw new IllegalStateException("Topic key [" + keyName + "] exceeds size limit.");
            }

            keyData = new String(baos.toByteArray());
        } catch (Exception e) {
            log.error("Could not initialize encryption.", e);
            throw new RuntimeException(e);
        } finally {
            try {
                baos.close();
            } catch (IOException e) { log.error("Key error: ", e);}
        }
        return keyData;
    }
}
