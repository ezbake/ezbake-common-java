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

import java.util.Map;
import java.util.HashMap;
import java.io.IOException;
import java.util.Properties;

import com.google.common.base.Optional;
import org.apache.accumulo.core.security.Authorizations;
import org.apache.accumulo.core.security.VisibilityEvaluator;

/**
 * This class is for testing purposes ONLY. It can be used in situations where a broadcaster
 * is required but there is no message bus running (unit testing for example).
 */
public class InMemoryBroadcaster extends EzBroadcaster { 
    private Map<String, byte[]> broadcasted;

    @Override
    protected void broadcastImpl(String topic, byte[] payload) throws IOException {
        broadcasted.put(topic, payload);
    }

    @Override
    protected Optional<byte[]> receiveImpl(String topic) throws IOException {
        return Optional.of(broadcasted.remove(topic));
    }

    @Override
    public void startListening(Receiver receiver) {
    }

    public byte[] getMessageFromTopic(String topic) {
        return broadcasted.get(topic);
    }

    @Override
    protected void prepare(Properties props, String groupId) {
        broadcasted = new HashMap<>();
    }

    @Override
    public void subscribe(String topic) {
    }

    @Override
    public void register(String topic) {
    }

    @Override
    public void unsubscribe(String topic) {
    }

    @Override
    public void unregister(String topic) {
    }

    @Override
    public void close() throws IOException {
    }
}
