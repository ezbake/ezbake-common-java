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

package ezbake.ezbroadcast.redismq;

import com.fourtwosix.redismq.consumer.RedisConsumer;
import com.fourtwosix.redismq.message.RedisMessage;
import com.fourtwosix.redismq.producer.RedisProducer;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import ezbake.ezbroadcast.core.EzBroadcaster;
import ezbake.ezbroadcast.core.Receiver;
import ezbake.ezbroadcast.core.thrift.SecureMessage;
import ezbakehelpers.ezconfigurationhelpers.redis.RedisConfigurationHelper;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RedisMQBroadcaster extends EzBroadcaster implements Serializable {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(RedisProducer.class);
    public static final String REDISMQ_TIMEOUT_MS_PROP = "broadcaster.redismq.timeout.ms";

    private RedisProducer producer;
    private RedisConsumer consumer;

    private Set<String> broadcastTopics;
    private Set<String> topicsToListenTo;
    private Properties props;
    private String groupId;
    private ExecutorService executor;

    @Override
    protected void prepare(Properties props, String groupId) {
        String timeout = props.getProperty(REDISMQ_TIMEOUT_MS_PROP, null);
        Preconditions.checkNotNull(timeout, REDISMQ_TIMEOUT_MS_PROP + " must be set");

        RedisConfigurationHelper redisConfig = new RedisConfigurationHelper(props);
        String hostname = redisConfig.getRedisHost();
        int port = redisConfig.getRedisPort();
        this.props = props;
        this.groupId = groupId;

        // Initialize the Producer
        producer = new RedisProducer(hostname, port);

        // Initialize the Consumer
        consumer = new RedisConsumer(Integer.parseInt(timeout), groupId, hostname, port);

        broadcastTopics = Sets.newHashSet();
        topicsToListenTo = Sets.newHashSet();
    }

    @Override
    protected void broadcastImpl(String topic, byte[] payload) throws IOException {
        Preconditions.checkArgument(topic != null, "topic cannot be null");
        Preconditions.checkState(broadcastTopics.contains(topic), "'" + topic + "' must be registered before broadcasting");
        logger.debug("Preparing to send message on '{}'", topic);
        RedisMessage data = new RedisMessage(topic, payload);
        producer.send(data);
    }

    @Override
    protected Optional<byte[]> receiveImpl(String topic) throws IOException {
        Preconditions.checkArgument(topic != null, "topic cannot be null");
        Preconditions.checkState(topicsToListenTo.contains(topic), "You must subscribe to '" + topic + "' before attempting to listen to it");
        logger.debug("Preparing to receive message on '{}'", topic);
        Optional<byte[]> result = consumer.poll(topic);
        return result;
    }

    @Override
    public void startListening(final Receiver receiver) {
        Preconditions.checkState(!topicsToListenTo.isEmpty(), "Not subscribed to any topics. Please subscribe to topics before attempting to listen");
        String timeout = props.getProperty(REDISMQ_TIMEOUT_MS_PROP);
        RedisConfigurationHelper redisConfig = new RedisConfigurationHelper(props);
        String hostname = redisConfig.getRedisHost();
        int port = redisConfig.getRedisPort();

        List<Runnable> runnables = Lists.newArrayList();

        for (final String topic : topicsToListenTo) {
            // Initialize the Consumer
            final RedisConsumer rconsumer = new RedisConsumer(Integer.parseInt(timeout), groupId, hostname, port);
            runnables.add(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            Optional<SecureMessage> payloadToReturn = extractMessage(rconsumer.poll(topic));
                            if (isProduction && payloadToReturn.isPresent()) {
                                payloadToReturn = decrypt(topic, payloadToReturn.get());
                            }
                            if (payloadToReturn.isPresent()) {
                                receiver.receive(topic, payloadToReturn.get());
                            }
                        } catch (IOException e) {
                            logger.error("Could not decrypt message");
                        }
                    }
                }
            });
        }
        logger.debug("Running {} threads for listening", runnables.size());
        executor = Executors.newFixedThreadPool(runnables.size());
        for (Runnable runnable : runnables) {
            executor.submit(runnable);
        }
    }

    @Override
    public void subscribe(String topic) {
        Preconditions.checkArgument(topic != null, "topic cannot be null");
        logger.debug("Subscribed to topic '{}'", topic);
        topicsToListenTo.add(topic);
    }

    @Override
    public void register(String topic) {
        Preconditions.checkArgument(topic != null, "topic cannot be null");
        logger.info("Registered broadcast topic '{}'", topic);
        broadcastTopics.add(topic);
    }

    @Override
    public void unsubscribe(String topic) {
        Preconditions.checkArgument(topic != null, "topic cannot be null");
        topicsToListenTo.remove(topic);
        logger.debug("Unsubscribed from topic '{}'", topic);
        consumer.unsubscribeFromTopic(topic);
    }

    @Override
    public void unregister(String topic) {
        Preconditions.checkArgument(topic != null, "topic cannot be null");
        broadcastTopics.remove(topic); 
    }

    public void close() throws IOException {
        super.close();
        producer.close();
        consumer.close();
        if (executor != null) {
            executor.shutdownNow();
        }
    }
}
