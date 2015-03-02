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

package ezbake.base.thrift;

import com.codahale.metrics.MetricRegistry;
import ezbake.base.thrift.metrics.MetricRegistryThrift;
import org.apache.thrift.TProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;


public abstract class EzBakeBaseThriftService implements EzBakeBaseService.Iface
{
    private Properties configurationProperties = null;

    private final MetricRegistry metricRegistry = new MetricRegistry();

    public abstract TProcessor getThriftProcessor();

    private Logger logger = LoggerFactory.getLogger(EzBakeBaseThriftService.class);

    public void setConfigurationProperties(Properties props) {
        this.configurationProperties = props;
    }

    public Properties getConfigurationProperties() {
        return configurationProperties;
    }

    public MetricRegistry getMetricRegistry() {
        return metricRegistry;
    }

    /**
     * No service implementing EzBakeBaseThriftService should call
     * @return
     */
    @Override
    public final MetricRegistryThrift getMetricRegistryThrift() {
        return EzBakeMetricRegistryBuilder.buildFrom(getMetricRegistry());
    }

    // You should override this to add your custom shutdown code
    public void shutdown() {
    }

    @Override
    public boolean ping() {
        return true;
    }
}
