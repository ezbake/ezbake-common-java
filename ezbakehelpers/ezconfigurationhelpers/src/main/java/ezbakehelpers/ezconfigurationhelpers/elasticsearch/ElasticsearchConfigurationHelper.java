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

package ezbakehelpers.ezconfigurationhelpers.elasticsearch;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;

import ezbake.common.properties.EzProperties;
import ezbake.configuration.constants.EzBakePropertyConstants;

public class ElasticsearchConfigurationHelper {
    private EzProperties props;

    public ElasticsearchConfigurationHelper(Properties props) {
        this.props = new EzProperties(props, true);
    }

    /**
     * This helper will return a list containing all of the elasticsearch hostnames.
     * @return a list of elasticsearch hostnames
     */
    public List<String> getElasticsearchHosts() {
        List<String> hostNames;
        String hosts = getElasticsearchHost();
        if (hosts != null) {
            hostNames = Splitter.on(",").omitEmptyStrings().trimResults().splitToList(hosts);
        } else {
            hostNames = Collections.emptyList();
        }
        return hostNames;
    }

    /**
     * This helper will return a comma separated string of elasticsearch hosts names with port
     *    host1:port,host2:port....
     * @return a comma separated list or elasticsearch hosts, or null if no host present
     */
    public String getElasticsearchHostWithPort() {
        String hostsWithPort = null;
        int port = getElasticsearchPort();
        String hostString = getElasticsearchHost();

        Map<String, Integer> hostPortMap = Maps.newTreeMap();
        if (hostString != null) {
            for (String hostName : Splitter.on(",").omitEmptyStrings().trimResults().split(hostString)) {
                hostPortMap.put(hostName, port);
            }
            hostsWithPort = Joiner.on(",").withKeyValueSeparator(":").join(hostPortMap);
        }
        return hostsWithPort;
    }

    /**
     * This helper returns the value of the elasticsearch host configuration property. Note that this may be a
     * comma separated list of hosts. The port will not likly
     * @return
     */
    public String getElasticsearchHost() {
        String host = props.getProperty(EzBakePropertyConstants.ELASTICSEARCH_HOST);
        if (host != null) {
            host = Joiner.on(",").join(Splitter.on(",").omitEmptyStrings().trimResults().split(host));
        }
        return host;
    }

    public int getElasticsearchPort() {
        return props.getInteger(EzBakePropertyConstants.ELASTICSEARCH_PORT, 9300);
    }

    public int getElasticsearchThriftPort() {
        return props.getInteger(EzBakePropertyConstants.ELASTICSEARCH_THRIFT_PORT, 9500);
    }

    public String getElasticsearchClusterName() {
        return props.getProperty(EzBakePropertyConstants.ELASTICSEARCH_CLUSTER_NAME);
    }

    public boolean getForceRefresh() {
        return props.getBoolean(EzBakePropertyConstants.ELASTICSEARCH_FORCE_REFRESH_ON_PUT, false);
    }
}