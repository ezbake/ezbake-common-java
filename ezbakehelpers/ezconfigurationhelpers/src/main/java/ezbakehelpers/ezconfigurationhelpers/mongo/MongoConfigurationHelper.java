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

package ezbakehelpers.ezconfigurationhelpers.mongo;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import ezbake.configuration.constants.EzBakePropertyConstants;
import ezbake.common.properties.EzProperties;
import ezbake.common.security.TextCryptoProvider;
import ezbakehelpers.ezconfigurationhelpers.system.SystemConfigurationHelper;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class MongoConfigurationHelper {

    private EzProperties ezProperties;

    public MongoConfigurationHelper(Properties properties) {
        this(properties, new SystemConfigurationHelper(properties).getTextCryptoProvider());
    }

    public MongoConfigurationHelper(Properties properties, TextCryptoProvider provider) {
        this.ezProperties = new EzProperties(properties, true);
        this.ezProperties.setTextCryptoProvider(provider);
    }

    /**
     * Returns the value for mongo host names from the configuration. This will be the raw value, and if multiple
     * mongo host names are returned, they will be in the form of a comma separated string
     *
     * @return comma delimited string of mongo host names
     */
    public String getMongoDBHostName() {
        return ezProperties.getProperty(EzBakePropertyConstants.MONGODB_HOST_NAME);
    }

    /**
     * Parses the mongo host name string, splitting on commas, and returning individual host names in a list. If no
     * mongo host names are configured, this will return an empty list
     *
     * @return List of mongo host names
     */
    public List<String> getMongoDBHostNames() {
        String hostString = ezProperties.getProperty(EzBakePropertyConstants.MONGODB_HOST_NAME);
        List<String> hosts;
        if (hostString != null) {
            hosts = Splitter.on(",")
                    .omitEmptyStrings()
                    .trimResults()
                    .splitToList(hostString);
        } else {
            hosts = Collections.emptyList();
        }
        return hosts;
    }

    public String getMongoDBDatabaseName() {
        return ezProperties.getProperty(EzBakePropertyConstants.MONGODB_DB_NAME);
    }

    public String getMongoDBUserName() {
        return ezProperties.getProperty(EzBakePropertyConstants.MONGODB_USER_NAME);
    }

    public String getMongoDBPassword() {
        return ezProperties.getProperty(EzBakePropertyConstants.MONGODB_PASSWORD);
    }

    public boolean useMongoDBSSL() {
        return ezProperties.getBoolean(EzBakePropertyConstants.MONGODB_USE_SSL, true);
    }

    public int getMongoDBPort() {
        return ezProperties.getInteger(EzBakePropertyConstants.MONGODB_PORT, 27017);
    }

    public String getMongoConnectionString() {
        StringBuilder builder = new StringBuilder("mongodb://");

        // User credentials
        builder.append(getMongoDBUserName())
                .append(':')
                .append(getMongoDBPassword())
                .append('@');

        // Host names
        Map<String, Integer> hostPortMap = Maps.newTreeMap();
        int mongoPort = getMongoDBPort();
        for (String hostName : getMongoDBHostNames()) {
            hostPortMap.put(hostName, mongoPort);
        }
        builder.append(Joiner.on(",").withKeyValueSeparator(":").join(hostPortMap));

        // DatabaseName
        builder.append('/').append(getMongoDBDatabaseName());

        // SSL
        builder.append("?ssl=").append(useMongoDBSSL());
        return builder.toString();
    }
}
