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

package ezbakehelpers.ezconfigurationhelpers.kafka;

import ezbake.configuration.constants.EzBakePropertyConstants;
import java.util.Properties;

public class KafkaConfigurationHelper {
    private Properties props;

    public KafkaConfigurationHelper(Properties props) {
        this.props = props;
    }

    public String getKafkaZookeeper() {
        return props.getProperty(EzBakePropertyConstants.KAFKA_ZOOKEEPER);
    }

    public String getKafkaBrokerList() {
        return props.getProperty(EzBakePropertyConstants.KAFKA_BROKER_LIST);
    }

    public String getKafkaProducerType() {
        return props.getProperty(EzBakePropertyConstants.KAFKA_PRODUCER_TYPE);
    }

    public String getKafkaQueueSize() {
        return props.getProperty(EzBakePropertyConstants.KAFKA_QUEUE_SIZE);
    }

    public String getKafkaQueueTime() {
        return props.getProperty(EzBakePropertyConstants.KAFKA_QUEUE_TIME);
    }

    public String getKafkaZookeeperSessionTimeout() {
        return props.getProperty(EzBakePropertyConstants.KAFKA_ZOOKEEPER_SESSION_TIMEOUT);
    }
}
