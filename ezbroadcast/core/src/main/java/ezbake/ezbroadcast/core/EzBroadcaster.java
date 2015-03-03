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

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.io.Files;

import org.apache.accumulo.core.util.BadArgumentException;

import org.apache.thrift.TException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ezbake.base.thrift.EzSecurityTokenException;
import ezbake.base.thrift.Permission;
import ezbake.base.thrift.Visibility;
import ezbake.common.openshift.OpenShiftUtil;
import ezbake.crypto.PKeyCryptoException;
import ezbake.crypto.RSAKeyCrypto;
import ezbake.ezbroadcast.core.thrift.SecureMessage;
import ezbake.security.client.EzSecurityTokenWrapper;
import ezbake.security.client.EzbakeSecurityClient;
import ezbake.security.permissions.PermissionUtils;
import ezbake.thrift.ThriftUtils;

public abstract class EzBroadcaster implements Closeable {
    private static final Logger log = LoggerFactory.getLogger(EzBroadcaster.class);
    
    public static final String BROADCASTER_CLASS = "broadcaster.class";
    public static final String PRODUCTION_MODE = "broadcaster.security.production";

    private static final String ALGO = "AES";

    protected EzbakeSecurityClient security;
    protected boolean isProduction = false;
    private ConcurrentHashMap<String, RSAKeyCrypto> topicKeys = new ConcurrentHashMap<>();
    
    public abstract void startListening(Receiver receiver);
    
    protected abstract void broadcastImpl(String topic, byte[] payload) throws IOException;

    protected abstract Optional<byte[]> receiveImpl(String topic) throws IOException;

    /**
     * This method retrieves any necessary configuration values for the operation of
     * the EzBroadcaster implementation and initializes all underlying portions of
     * the broadcaster.
     *
     * @param props  the properties object that will be used to prepare the
     *               broadcaster
     */
    protected abstract void prepare(Properties props, String groupId);

    /**
     * This method subscribes the broadcaster to the given topic, allowing receive to be called on that topic.
     *
     * @param topic topic to subscribe to for listening
     */
    protected abstract void subscribe(String topic);

    /**
     * This method unsubscribes the broadcaster from the given topic, disallowing receive to be called on that topic.
     *
     * @param topic topic to unsubscribe from
     */
    protected abstract void unsubscribe(String topic);
    
    /**
     * This method registers the given topic as a topic that is being broadcast to.
     *
     * @param topic topic to register as a broadcast topic
     */
    protected abstract void register(String topic);

    /**
     * This method unregisters the given topic as a topic that is being broadcast to.
     *
     * @param topic topic to unregister as a broadcast topic
     */
    protected abstract void unregister(String topic);

    /**
     * This method subscribes the broadcaster to the given topic, allowing receive to be called on that topic.
     *
     * @param topic topic to subscribe to for listening
     */
    public void subscribeToTopic(String topic) {
        loadTopicKey(topic, true);
        subscribe(topic);
    }
    
    /**
     * This method unsubscribes the broadcaster from the given topic, disallowing receive to be called on that topic.
     *
     * @param topic topic to unsubscribe from
     */
    public void unsubscribeFromTopic(String topic) {
        topicKeys.remove(topic);
        unsubscribe(topic);
    }
    
    /**
     * This method registers the given topic as a topic that is being broadcast to.
     *
     * @param topic topic to register as a broadcast topic
     */
    public void registerBroadcastTopic(String topic) {
        loadTopicKey(topic, false);
        register(topic);
    }
    
    /**
     * This method unregisters the given topic as a topic that is being broadcast to.
     *
     * @param topic topic to unregister as a broadcast topic
     */
    public void unregisterFromTopic(String topic) {
        topicKeys.remove(topic);
        unregister(topic);
    }
    
    /**
     * This method broadcasts a payload onto the given topic.
     *
     * @param topic the topic to broadcast onto
     * @param visibility the visibility of the message
     * @param payload the payload of the message
     */
    public void broadcast(String topic, Visibility visibility, byte[] payload) throws IOException {
        byte[] payloadToSend = payload;
        SecureMessage message = new SecureMessage();
        message.setVisibility(visibility);
        if (isProduction) {
            try {
                // Generate a new symmetric key and encrypt the message
                Cipher cipher = Cipher.getInstance(ALGO);
                KeyGenerator gen = KeyGenerator.getInstance(ALGO);
                gen.init(256);
                SecretKey key = gen.generateKey();
                cipher.init(Cipher.ENCRYPT_MODE, key);
                payloadToSend = cipher.doFinal(payload);

                // Get topic crypto object, encrypt the key and embed it into the message
                RSAKeyCrypto crypto = topicKeys.get(topic);
                message.setKey(crypto.encrypt(key.getEncoded()));
            } catch (Exception e) {
                log.error("Could not encrypt message for topic [{}].", topic, e);
                throw new RuntimeException(e);
            }
        }
        message.setContent(payloadToSend);
        byte[] serializedMessage;
        try {
            serializedMessage = ThriftUtils.serialize(message);
        } catch (TException e) {
            throw new IOException("Could not serialize message", e);
        }
        broadcastImpl(topic, serializedMessage);
    }
    
    /**
     * This method polls the given topic for a new message.
     *
     * @param topic the topic to poll
     * @return the next message from the given topic. Optional.absent is returned in the case that no message is found
     */
    public Optional<SecureMessage> receive(String topic) throws IOException {
        Optional<byte[]> encryptedPayload = receiveImpl(topic);
        Optional<SecureMessage> payloadToReturn = extractMessage(encryptedPayload);
        if (isProduction && payloadToReturn.isPresent()) {
            payloadToReturn = decrypt(topic, payloadToReturn.get());
        }
        return payloadToReturn;
    }

    protected Optional<SecureMessage> extractMessage(Optional<byte[]> serializedMessage) throws IOException {
        try {
            Optional<SecureMessage> result = Optional.absent();
            if (serializedMessage.isPresent()) {
                SecureMessage message = ThriftUtils.deserialize(SecureMessage.class, serializedMessage.get());
                result = Optional.of(message);
            }
            return result;
        } catch (TException e) {
            throw new IOException("Couldn't create Envelope from message", e);
        }
    }

    protected Optional<SecureMessage> decrypt(String topic, SecureMessage message) throws IOException {
        RSAKeyCrypto crypto = topicKeys.get(topic);
        Optional<SecureMessage> decryptedPayload = Optional.absent();
        try {
            if (message.isSetKey()) {
                if (crypto != null && crypto.hasPrivate()) {
                    Visibility visibility = message.getVisibility();
                    EzSecurityTokenWrapper token = security.fetchAppToken();
                    if (PermissionUtils.getPermissions(token.getAuthorizations(), visibility, true).contains(Permission.READ)) {
                        // Decrypt the symmetric key
                        byte[] symmetricKey = crypto.decrypt(message.getKey());

                        // Use the symmetric key to decrypt the message
                        SecretKey key = new SecretKeySpec(symmetricKey, ALGO);
                        Cipher cipher = Cipher.getInstance(ALGO);
                        cipher.init(Cipher.DECRYPT_MODE, key);
                        decryptedPayload = Optional.of(new SecureMessage(visibility, ByteBuffer.wrap(cipher.doFinal(message.getContent()))));
                    } else {
                        log.warn("Pipeline is not authorized to read message with visibility of {}, dropping message.", visibility);
                    }
                } else {
                    String error = "No private key found for broadcaster topic [" + topic + "]. Cannot decrypt messages. Please re-initialize the broadcaster with a private key to receive messages.";
                    log.error(error);
                    throw new RuntimeException(error);
                }
            } else {
                log.debug("Message was not encrypted, or no key was set");
            }
        } catch (IllegalBlockSizeException | InvalidKeyException | BadPaddingException e) {
            log.error("Encryption not set up properly, this error is fatal.", e);
            throw new RuntimeException(e);
        } catch (BadArgumentException e) {
            log.error(String.format("Visibility string %s not valid, message cannot be decrypted.", message.getVisibility().getFormalVisibility()), e);
            throw new IOException(e);
        } catch (NoSuchAlgorithmException e) {
            log.error("Incorrect algorithm used to initialize crypto", e);
            throw new RuntimeException(e);
        } catch (NoSuchPaddingException e) {
            log.error("Incorrect padding used to initialize crypto", e);
            throw new RuntimeException(e);
        } catch (PKeyCryptoException e) {
            log.error("Invalid crypto object", e);
            throw new RuntimeException(e);
        } catch (EzSecurityTokenException e) {
            log.error("Could not retrieve token from security service", e);
            throw new RuntimeException(e);
        }
        return decryptedPayload;
    }

    /**
     * This factory method is used to instantiate an EZzroadcaster implementation from the parameters in the given
     * EZConfiguration object.
     *
     * @param props configuration parameters to use when instantiating the broadcaster
     * @param groupId the group ID to be used with this broadcaster. This separates each consumer of a topic so that
     *                they both receive ALL messages from the topic. If this broadcaster is not being used to receive
     *                messages then the group ID can be any string
     * @return an instantiated and prepared EzBroadcaster implementation
     */
    public static EzBroadcaster create(Properties props, String groupId) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(groupId), "groupId cannot be null or empty");
        String broadcasterClass = props.getProperty(BROADCASTER_CLASS);
        Preconditions.checkNotNull(broadcasterClass, "The '" + BROADCASTER_CLASS + "' configuration value must be set");
        EzBroadcaster broadcaster;

        try {
            broadcaster = (EzBroadcaster)Class.forName(broadcasterClass).newInstance();
            String productionValue = props.getProperty(PRODUCTION_MODE);

            // If in an OpenShift container, always assume in production
            broadcaster.isProduction = OpenShiftUtil.inOpenShiftContainer() || (productionValue != null && Boolean.parseBoolean(productionValue));
            log.debug("In Production Mode: {}", broadcaster.isProduction);

            if (!broadcaster.isProduction) {
                log.warn("Broadcaster is running in local mode. Encryption and security checking is off");
            }
            broadcaster.prepare(props, groupId);
            broadcaster.security = new EzbakeSecurityClient(props);
        } catch (Exception e) {
            log.error("Could not instantiate broadcaster for class '" + broadcasterClass + "'", e);
            throw new RuntimeException("Could not instantiate broadcaster for class '" + broadcasterClass + "'", e);
        }
        return broadcaster;
    }

    /**
     * This factory method is used to instantiate an EzBroadcaster implementation from the parameters in the given
     * EZConfiguration object and will load a key for the specified topic. </br >
     * Note: Register / Subscribe topic as needed. 
     *<pre>
     * @param props     configuration parameters to use when instantiating the broadcaster
     * @param groupId   the group ID to be used with this broadcaster
     * @param key       the public or private key to be used with this broadcaster
     * @param topic     the topic to which key belongs
     * @param isPrivate whether provided key is private or public
     * @return an instantiated and prepared EzBroadcaster implementation
     * </pre>
     */
    public static EzBroadcaster create(Properties props, String groupId, String key, String topic, boolean isPrivate) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(key), "key cannot be null or empty");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(topic), "topic cannot be null or empty");
        
        EzBroadcaster broadcaster = create(props, groupId);
        
        try {
            broadcaster.topicKeys.put(topic, new RSAKeyCrypto(key, isPrivate));
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            log.error("Could not instantiate topic key for class '" + props.getProperty(BROADCASTER_CLASS) + "'", e);
            throw new RuntimeException("Could not instantiate broadcaster for class '" + props.getProperty(BROADCASTER_CLASS) + "'", e);
        }
        return broadcaster;
    }

    protected void loadTopicKey(String topic, boolean isListener) {
        if (isProduction) {
            // If the key has been loaded, do not load again.
            if (topicKeys.containsKey(topic)) {
                log.warn("The key for topic [{}] has already been loaded.", topic);
                return;
            }

            String privateFile = "keys/" + topic + ".priv";
            String publicFile = "keys/" + topic + ".pub";
            InputStream privateInputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(privateFile);
            InputStream publicInputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(publicFile);

            /* Confirm we have correct key for broadcasting or listening.
             * Priv needed for listening and pub only for broadcasting */

            // First check if we have at least one key.
            if (privateInputStream == null && publicInputStream == null) {
                log.error("Did not find any keys for topic [" + topic + "].");
                throw new IllegalStateException("Did not find any keys for topic [" + topic + "].");
            }

            // Listeners need private key. If no private key was found, we need to fail pipeline.
            if (isListener && privateInputStream == null) {
                throw new IllegalStateException("Did not find private key for listener topic [" + topic + "].");
            }

            // Let's add the key we have. Try private first, then public.
            if (privateInputStream != null) {
                addKey(privateFile, privateInputStream);
            } else if (publicInputStream != null) {
                addKey(publicFile, publicInputStream);
            } else {
                throw new IllegalStateException("Encountered an unexpected state for [" + topic + "].");
            }
        }
    }

    protected void addKey(String fileName, InputStream inputStream) {
        RSAKeyCrypto key = null;
        final int BUFFER = 4096;
        int count = 0, total = 0;
        byte data[] = new byte[BUFFER];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        String keyName = Files.getNameWithoutExtension(fileName);
        String extension = Files.getFileExtension(fileName);

        try {

            while (total <= BUFFER && (count = inputStream.read(data, 0, BUFFER)) != -1) { 
                baos.write(data, 0, count);
                total += count;
            }

            if (total > BUFFER) {
                throw new IllegalStateException("Topic key [" + keyName + "] exceeds size limit.");
            }

            String keyData = new String(baos.toByteArray());

            if (extension.equalsIgnoreCase("pub")) {
                key = new RSAKeyCrypto(keyData, false);
            } else {
                key = new RSAKeyCrypto(keyData, true);
            }

            topicKeys.put(keyName, key);
            log.debug("Added key [{}]. Total keys {}.", keyName, topicKeys.size());
        } catch (Exception e) {
            log.error("Could not initialize encryption.", e);
            throw new RuntimeException(e);
        } finally {
            try {
                baos.close();
            } catch (IOException e) {
                log.warn("Issue closing byte array input stream while reading key", e);
            }
        }
    }

    @Override
    public void close() throws IOException {
        if (security != null) {
            security.close();
        }
    }
}
