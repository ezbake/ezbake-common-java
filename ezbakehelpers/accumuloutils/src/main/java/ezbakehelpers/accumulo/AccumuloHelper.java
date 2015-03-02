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

package ezbakehelpers.accumulo;

import ezbake.common.properties.EzProperties;
import ezbake.common.security.TextCryptoProvider;
import ezbake.configuration.constants.EzBakePropertyConstants;
import ezbakehelpers.ezconfigurationhelpers.system.SystemConfigurationHelper;

import java.util.Properties;

import java.io.IOException;

import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.ClientConfiguration;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.Instance;
import org.apache.accumulo.core.client.ZooKeeperInstance;
import org.apache.accumulo.core.client.mock.MockInstance;
import org.apache.accumulo.core.client.security.tokens.PasswordToken;

public class AccumuloHelper {

    private EzProperties ezconfiguration;

    public AccumuloHelper(Properties configuration) {
        this(configuration, new SystemConfigurationHelper(configuration).getTextCryptoProvider());
    }

    public AccumuloHelper(Properties configuration, TextCryptoProvider provider) {
        this.ezconfiguration = new EzProperties(configuration, true);
        this.ezconfiguration.setTextCryptoProvider(provider);
    }

    public boolean useMock() {
        return ezconfiguration.getBoolean(EzBakePropertyConstants.ACCUMULO_USE_MOCK, false);
    }

    public String getAccumuloInstance() {
        return ezconfiguration.getProperty(EzBakePropertyConstants.ACCUMULO_INSTANCE_NAME);
    }

    public String getAccumuloNamespace() {
        return ezconfiguration.getProperty(EzBakePropertyConstants.ACCUMULO_NAMESPACE);
    }

    public String getAccumuloPassword() {
        return ezconfiguration.getProperty(EzBakePropertyConstants.ACCUMULO_PASSWORD);
    }

    public String getAccumuloUsername() {
        return ezconfiguration.getProperty(EzBakePropertyConstants.ACCUMULO_USERNAME);
    }

    public String getAccumuloZookeepers() {
        return ezconfiguration.getProperty(EzBakePropertyConstants.ACCUMULO_ZOOKEEPERS);
    }

    public boolean getAccumuloUseSsl() {
        return ezconfiguration.getBoolean(EzBakePropertyConstants.ACCUMULO_USE_SSL, false);
    }

    public String getAccumuloSslTruststore() {
        return ezconfiguration.getProperty(EzBakePropertyConstants.ACCUMULO_SSL_TRUSTSTORE_PATH);
    }

    public String getAccumuloSslTruststorePassword() {
        return ezconfiguration.getProperty(EzBakePropertyConstants.ACCUMULO_SSL_TRUSTSTORE_PASSWORD);
    }

    public String getAccumuloSslTruststoreType() {
        return ezconfiguration.getProperty(EzBakePropertyConstants.ACCUMULO_SSL_TRUSTSTORE_TYPE, "JKS");
    }


    @Deprecated
    public Connector getConnector() throws IOException {
        return getConnector(false);
    }

    public Connector getConnector(boolean namespaced) throws IOException {
        Instance instance;
        if(useMock()) {
            instance = new MockInstance(getAccumuloInstance());
        } else {
            ClientConfiguration clientConfiguration = ClientConfiguration.loadDefault()
                    .withZkHosts(getAccumuloZookeepers())
                    .withInstance(getAccumuloInstance());
            if (getAccumuloUseSsl()) {
                clientConfiguration = clientConfiguration.withSsl(getAccumuloUseSsl())
                        .withTruststore(getAccumuloSslTruststore(), getAccumuloSslTruststorePassword(),
                                getAccumuloSslTruststoreType())
                        .with(ClientConfiguration.ClientProperty.INSTANCE_RPC_SSL_CLIENT_AUTH,
                                Boolean.FALSE.toString());
            }
            instance = new ZooKeeperInstance(clientConfiguration);
        }

        try {
            Connector connector = instance.getConnector(getAccumuloUsername(), new PasswordToken(getAccumuloPassword()));
            if (namespaced) {
                connector = new NamespacedConnector(connector, getAccumuloNamespace());
            }
            return connector;
        } catch(AccumuloException | AccumuloSecurityException e) {
            throw new IOException(e);
        }
    }
}
