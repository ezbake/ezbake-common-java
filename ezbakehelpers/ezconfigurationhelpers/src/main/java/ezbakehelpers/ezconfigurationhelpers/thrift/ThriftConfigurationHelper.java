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

package ezbakehelpers.ezconfigurationhelpers.thrift;

import ezbake.common.properties.EzProperties;
import ezbake.configuration.constants.EzBakePropertyConstants;

import java.util.Properties;

/**
 *  * Helper class to get thrift configuration
 *   */
public class ThriftConfigurationHelper {
    private EzProperties ezConfiguration;

    public ThriftConfigurationHelper(Properties properties) {
        ezConfiguration = new EzProperties(properties,  true);
    }

    public boolean useSSL() {
        return ezConfiguration.getBoolean(EzBakePropertyConstants.THRIFT_USE_SSL, true);
    }

    public ThriftServerMode getServerMode() {
        String mode = ezConfiguration.getProperty(EzBakePropertyConstants.THRIFT_SERVER_MODE, "Simple");
        return ThriftServerMode.valueOf(mode);
    }

    public int getMaxIdleClients() {
        return ezConfiguration.getInteger(EzBakePropertyConstants.THRIFT_MAX_IDLE_CLIENTS, 10);
    }


    public long getMillisBetweenClientEvictionChecks() {
        return ezConfiguration.getLong(EzBakePropertyConstants.THRIFT_MILLIS_BETWEEN_CLIENT_EVICTION_CHECKS, 30*1000);
    }

    public long getMillisIdleBeforeEviction() {
        return ezConfiguration.getLong(EzBakePropertyConstants.THRIFT_MILLIS_IDLE_BEFORE_EVICTION, 2*60*1000);
    }

    public int getMaxPoolClients() {
        return ezConfiguration.getInteger(EzBakePropertyConstants.THRIFT_MAX_POOL_CLIENTS, 10);
    }

    public boolean getTestOnBorrow() {
        return ezConfiguration.getBoolean(EzBakePropertyConstants.THRIFT_TEST_ON_BORROW, false);
    }

    public boolean getTestWhileIdle() {
        return ezConfiguration.getBoolean(EzBakePropertyConstants.THRIFT_TEST_WHILE_IDLE, true);
    }

    public boolean getBlockWhenExhausted() {
        return ezConfiguration.getBoolean(EzBakePropertyConstants.THRIFT_BLOCK_WHEN_EXHAUSTED, true);
    }

    public boolean actuallyPoolClients() {
        return ezConfiguration.getBoolean(EzBakePropertyConstants.THRIFT_ACTUALLY_POOL_CLIENTS, true);
    }

    /**
     * Flag to log stack traces for application code which abandoned an object.
     */
    public boolean getLogAbandoned() {
        return ezConfiguration.getBoolean(EzBakePropertyConstants.THRIFT_LOG_ABANDONED, true);
    }

    /**
     * Flag to remove abandoned objects if they exceed the removeAbandonedTimeout when borrowObject is invoked.
     */
    public boolean getRemoveAbandonedOnBorrow() {
        return ezConfiguration.getBoolean(EzBakePropertyConstants.THRIFT_ABANDON_ON_BORROW, true);
    }

    /**
     * Flag to remove abandoned objects if they exceed the removeAbandonedTimeout when pool maintenance (the "evictor") runs.
     */
    public boolean getRemoveAbandonedOnMaintenance() {
        return ezConfiguration.getBoolean(EzBakePropertyConstants.THRIFT_ABANDON_ON_MAINTENANCE, true);
    }

    /**
     * Flag to remove abandoned objects if they exceed the removeAbandonedTimeout when borrowObject is invoked.
     */
    public int getRemoveAbandonedTimeout() {
        return ezConfiguration.getInteger(EzBakePropertyConstants.THRIFT_ABANDON_TIMEOUT, 60);
    }

    public enum ThriftServerMode {
        Simple(true),
        HsHa(false),
        ThreadedPool(false);

        boolean isBlocking;
        ThriftServerMode(boolean isBlocking) {
            this.isBlocking = isBlocking;
        }

        public boolean isBlocking() {
            return this.isBlocking;
        }
    }
}
