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

package ezbakehelpers.mongoutils;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.mongodb.*;
import ezbake.common.properties.EzProperties;
import ezbake.common.security.TextCryptoProvider;
import ezbake.common.ssl.SSLContextException;
import ezbakehelpers.ezconfigurationhelpers.mongo.MongoConfigurationHelper;
import ezbakehelpers.ezconfigurationhelpers.ssl.SslConfigurationHelper;
import ezbakehelpers.ezconfigurationhelpers.system.SystemConfigurationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * User: jhastings
 * Date: 10/1/14
 * Time: 8:54 AM
 */
public class MongoHelper {
    private static final Logger logger = LoggerFactory.getLogger(MongoHelper.class);

    private MongoConfigurationHelper mongoConfigurationHelper;
    private SslConfigurationHelper sslConfigurationHelper;
    private EzProperties ezProperties;

    public MongoHelper(Properties configuration) {
        this(configuration, new SystemConfigurationHelper(configuration).getTextCryptoProvider());
    }

    public MongoHelper(Properties configuration, TextCryptoProvider provider) {
        ezProperties = new EzProperties(configuration, true);
        ezProperties.setTextCryptoProvider(provider);
        mongoConfigurationHelper = new MongoConfigurationHelper(configuration, provider);
        sslConfigurationHelper = new SslConfigurationHelper(configuration, provider);
    }

    public MongoConfigurationHelper getMongoConfigurationHelper() {
        return mongoConfigurationHelper;
    }

    public MongoClientOptions getMongoClientOptions() {
        MongoClientOptions.Builder builder = MongoClientOptions.builder();
        if (mongoConfigurationHelper.useMongoDBSSL()) {
            try {
                builder.socketFactory(sslConfigurationHelper.getSystemSSLContext().getSocketFactory());
            } catch (IOException | SSLContextException e) {
                logger.warn("Mongo configured with SSL, but failed to load the system SSL context", e);
            }
        }
        return builder.build();
    }

    public MongoCredential getMongoCredential() {
        checkArgument(!Strings.isNullOrEmpty(mongoConfigurationHelper.getMongoDBUserName()),
                "Mongo Username must not be null");
        checkArgument(!Strings.isNullOrEmpty(mongoConfigurationHelper.getMongoDBPassword()),
                "Mongo Password must not be null");
        checkArgument(!Strings.isNullOrEmpty(mongoConfigurationHelper.getMongoDBDatabaseName()),
                "Mongo Database must not be null");
        return MongoCredential.createMongoCRCredential(
                mongoConfigurationHelper.getMongoDBUserName(),
                mongoConfigurationHelper.getMongoDBDatabaseName(),
                mongoConfigurationHelper.getMongoDBPassword().toCharArray());
    }

    public List<ServerAddress> getMongoServerAddress() throws UnknownHostException {
        List<ServerAddress> serverAddresses = Lists.newArrayList();
        for (String hostname : mongoConfigurationHelper.getMongoDBHostNames()) {
            serverAddresses.add(new ServerAddress(hostname, mongoConfigurationHelper.getMongoDBPort()));
        }
        return serverAddresses;
    }


    public Mongo getMongo() throws UnknownHostException {
        Mongo mongo;

        if (Strings.isNullOrEmpty(mongoConfigurationHelper.getMongoDBPassword())) {
            mongo = new MongoClient(getMongoServerAddress(), getMongoClientOptions());
        } else {
            mongo = new MongoClient(getMongoServerAddress(), Lists.newArrayList(getMongoCredential()),
                    getMongoClientOptions());
        }
        return mongo;
    }
}
