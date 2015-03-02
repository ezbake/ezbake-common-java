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

package ezbake.security.client.provider;

import com.google.common.base.Supplier;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import ezbake.base.thrift.EzSecurityTokenException;
import ezbake.thrift.ThriftClientPool;
import ezbake.base.thrift.EzSecurityToken;
import ezbake.base.thrift.TokenRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;


/**
 * @author gdrocella
 * @date 07/08/14
 */
public interface TokenProvider {
    public static final Logger LOGGER = LoggerFactory.getLogger(TokenProvider.class);
    public static final String CLIENT_MODE = "ezbake.security.client.mode";

    public EzSecurityToken getSecurityToken(TokenRequest tokenRequest) throws EzSecurityTokenException;
    public EzSecurityToken refreshSecurityToken(EzSecurityToken token) throws EzSecurityTokenException;
    public boolean isValidToken(EzSecurityToken token);

    /**
     * Mock - Fully Mocked -> No call to ez security service or validation.
     * Local Server - Starts local instance of EzSecurity and calls it directly
     * Dev - Mocked, No validation, but call is still made to ez security service.
     * Real - Default, Full validation, call is made to ez security service.
     */
    public enum ClientMode {
        MOCK(MockTokenProvider.class.getCanonicalName()),
        LOCAL_SERVER("ezbake.security.client.provider.EzbakeFullyMockedTokenProvider"),
        Dev(NonPkiTokenProvider.class.getCanonicalName()),
        REAL(EzbakeRealTokenProvider.class.getCanonicalName());
        
        private String impl;
        
        ClientMode(String impl) {
            this.impl = impl;
        }
        
        public String getValue() {
            return toString().toLowerCase();
        }

        public String getImpl() {
            return impl;
        }
        
        public static ClientMode getEnum(String value) {
            if (value != null) {
                String v = value.toLowerCase();

                for (ClientMode cm : values()) {
                    if (cm.getValue().equals(v)) {
                        return cm;
                    }
                }
            }
            // Default to real
            return REAL;
        }
    }

    public static class Module extends AbstractModule {
        private Properties p;
        private Supplier<ThriftClientPool> poolSupplier;
        public Module(Properties p, Supplier<ThriftClientPool> poolSupplier) {
            this.p = p;
            this.poolSupplier = poolSupplier;
        }

        @Override
        protected void configure() {
            ClientMode mode = ClientMode.getEnum(p.getProperty(CLIENT_MODE));
            bind(Properties.class).toInstance(p);
            if (poolSupplier != null) {
                bind(new TypeLiteral<Supplier<ThriftClientPool>>() {
                }).toInstance(poolSupplier);
            }

            try {
                Class<? extends TokenProvider> providerClass = Class.forName(mode.getImpl())
                        .asSubclass(TokenProvider.class);
                bind(TokenProvider.class).to(providerClass);
            } catch (ClassNotFoundException e) {
                LOGGER.error("Failed to use configured client mode {}", mode);
            }

        }
    }
}
