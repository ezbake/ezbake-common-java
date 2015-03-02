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

package ezbake.ezbroadcast.kafka;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import ezbake.ezbroadcast.core.EzBroadcaster;
import ezbake.ezbroadcast.core.Receiver;
import ezbake.ezbroadcast.core.thrift.SecureMessage;
import ezbakehelpers.ezconfigurationhelpers.kafka.KafkaConfigurationHelper;
import kafka.consumer.*;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.javaapi.producer.Producer;
import kafka.message.MessageAndMetadata;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class KafkaBroadcaster extends EzBroadcaster implements Serializable {
    private static Logger logger = LoggerFactory.getLogger(KafkaBroadcaster.class);

    private static final String GROUP_ID_PROP = "group.id";
    private static final String METADATA_BROKER_LIST = "metadata.broker.list";
    private static final String PRODUCER_TYPE = "producer.type";
    private static final String QUEUE_SIZE = "queue.buffering.max.messages";
    private static final String QUEUE_TIME = "queue.buffering.max.ms";
    private static final String ZOOKEEPERS = "zookeeper.connect";
    private static final String ZOOKEEPER_SESSION_TIMEOUT = "zookeeper.connection.timeout.ms";
    private static final String AUTO_OFFSET_RESET = "auto.offset.reset";

    private Properties localProps;

    private Producer<byte[], byte[]> producer;

    private Set<String> topicsToListenTo;
    private Set<String> broadcastTopics;

    // Cache the last topic that was polled
    private boolean singleTopicConnector = false;
    private String topicCache;
    private ConsumerConnector connector;
    private ExecutorService executor;

    @Override
    protected void prepare(Properties props, String groupId) {
        KafkaConfigurationHelper kafkaHelper = new KafkaConfigurationHelper(props);
        localProps = new Properties();
        localProps.put(GROUP_ID_PROP, groupId);
        localProps.put(METADATA_BROKER_LIST, kafkaHelper.getKafkaBrokerList());
        localProps.put(PRODUCER_TYPE, kafkaHelper.getKafkaProducerType());
        localProps.put(QUEUE_SIZE, kafkaHelper.getKafkaQueueSize());
        localProps.put(QUEUE_TIME, kafkaHelper.getKafkaQueueTime());
        localProps.put(ZOOKEEPERS, kafkaHelper.getKafkaZookeeper());
        localProps.put(ZOOKEEPER_SESSION_TIMEOUT, kafkaHelper.getKafkaZookeeperSessionTimeout());

        // This sets the consumer to start consuming messages with the smallest offset in Kafka if no offset
        // exists for this consumer
        localProps.put(AUTO_OFFSET_RESET, "smallest");

        // Initialize the Producer
        ProducerConfig producerConfig = new ProducerConfig(localProps);
        producer = new Producer<>(producerConfig);

        topicCache = "";
        broadcastTopics = Sets.newHashSet();
        topicsToListenTo = Sets.newHashSet();
    }

    @Override
    protected void broadcastImpl(String topic, byte[] payload) throws IOException {
        Preconditions.checkArgument(topic != null, "topic cannot be null");
        Preconditions.checkState(broadcastTopics.contains(topic), "'" + topic + "' must be registered before broadcasting");

        KeyedMessage<byte[], byte[]> data = new KeyedMessage<>(topic, payload);
        producer.send(data);
    }

    @Override
    protected Optional<byte[]> receiveImpl(String topic) throws IOException {
        Preconditions.checkArgument(topic != null, "topic cannot be null");
        Preconditions.checkState(topicsToListenTo.contains(topic),
                "You must subscribe to '" + topic + "' before attempting to listen to it");
        Optional<byte[]> result = Optional.absent();

        // Initialize a new connector if the topic has changed. Zookeeper becomes unhappy if one connector attempts
        // to poll multiple topics. The overhead associated with opening a new connector is pretty minimal.
        if (!topicCache.equals(topic) || !singleTopicConnector) {
            logger.info("Re-initializing consumer connector for new topic");
            if (connector != null) {
                connector.shutdown();
            }
            // Initialize the Consumer
            ConsumerConfig consumerConfig = new ConsumerConfig(localProps);
            connector = Consumer.createJavaConsumerConnector(consumerConfig);
            topicCache = topic;
            singleTopicConnector = true;
        }

        try {
            // Create stream from the given topic, using only one thread since we are only consuming one message.

            Map<String, List<KafkaStream<byte[], byte[]>>> topicMap = connector.createMessageStreams(ImmutableMap.of(topic, 1));
            List<KafkaStream<byte[], byte[]>> streams = topicMap.get(topic);

            if (streams.size() > 0) {
                KafkaStream<byte[], byte[]> stream = streams.get(0);
                ConsumerIterator<byte[], byte[]> it = stream.iterator();
                if (it.hasNext()) {
                    MessageAndMetadata<byte[], byte[]> message = it.next();

                    byte[] bytes = message.message();
                    result = Optional.of(bytes);
                }
            }
        } catch (ConsumerTimeoutException e) {
            // There was no message on the queue, just return the absent object
            logger.info(String.format("Could not retrieve message from topic %s in given timeout.", topic));
        }

        return result;
    }

    @Override
    public void startListening(final Receiver receiver) {
        Preconditions.checkState(!topicsToListenTo.isEmpty(), "Not subscribed to any topics. Please subscribe to topics before attempting to listen");
        List<Runnable> runnables = Lists.newArrayList();
        // Initialize the Consumer
        ConsumerConfig consumerConfig = new ConsumerConfig(localProps);
        ConsumerConnector consumerConnector = Consumer.createJavaConsumerConnector(consumerConfig);

        for (final String topic : topicsToListenTo) {
            Map<String, Integer> topicCountMap = new HashMap<>();
            topicCountMap.put(topic, new Integer(1));
            Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumerConnector.createMessageStreams(topicCountMap);
            final KafkaStream<byte[], byte[]> stream =  consumerMap.get(topic).get(0);
            runnables.add(new Runnable() {
                @Override
                public void run() {
                    for(MessageAndMetadata<byte[], byte[]> msgAndMetadata : stream) {
                        try {
                            Optional<SecureMessage> payloadToReturn = extractMessage(Optional.of(msgAndMetadata.message()));
                            if (isProduction && payloadToReturn.isPresent()) {
                                payloadToReturn = decrypt(topic, payloadToReturn.get());
                            }
                            if (payloadToReturn.isPresent()) {
                                receiver.receive(topic, payloadToReturn.get());
                            }
                        } catch (IOException e) {
                            logger.error("Could not decrypt message", e);
                        }
                    }
                }
            });
        }
        executor = Executors.newFixedThreadPool(runnables.size());
        for (Runnable runnable : runnables) {
            executor.submit(runnable);
        }
    }

    @Override
    public void subscribe(String topic) {
        Preconditions.checkArgument(topic != null, "topic cannot be null");
        topicsToListenTo.add(topic);
    }

    @Override
    public void register(String topic) {
        Preconditions.checkArgument(topic != null, "topic cannot be null");
        broadcastTopics.add(topic);
    }

    @Override
    public void unsubscribe(String topic) {
        Preconditions.checkArgument(topic != null, "topic cannot be null");
        topicsToListenTo.remove(topic);
    }

    @Override
    public void unregister(String topic) {
        Preconditions.checkArgument(topic != null, "topic cannot be null");
        broadcastTopics.remove(topic);
    }

    public void close() throws IOException {
        super.close();
        if (executor != null) {
            executor.shutdownNow();
        }
        if (producer != null) {
            producer.close();
        }
        if (connector != null) {
            connector.shutdown();
        }
    }
}
