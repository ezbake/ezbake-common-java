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

package ezbakehelpers.ezconfigurationhelpers.postgres;

import com.google.common.base.Charsets;
import ezbake.base.thrift.EzSecurityToken;
import ezbake.common.properties.EzProperties;
import ezbake.common.security.TextCryptoProvider;
import ezbake.configuration.constants.EzBakePropertyConstants;
import ezbakehelpers.ezconfigurationhelpers.system.SystemConfigurationHelper;
import org.apache.commons.codec.binary.Base64;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Configuration Helper for PostgreSQL
 */
public class PostgresConfigurationHelper {


    private EzProperties ezProperties;

    public PostgresConfigurationHelper(Properties properties) {
        this(properties, new SystemConfigurationHelper(properties).getTextCryptoProvider());
    }

    public PostgresConfigurationHelper(Properties properties, TextCryptoProvider provider) {
        this.ezProperties = new EzProperties(properties, true);
        this.ezProperties.setTextCryptoProvider(provider);
    }

    public String getHost() {
        return ezProperties.getProperty(EzBakePropertyConstants.POSTGRES_HOST);
    }

    public String getPort() {
        return ezProperties.getProperty(EzBakePropertyConstants.POSTGRES_PORT);
    }

    public String getUsername() {
        return ezProperties.getProperty(EzBakePropertyConstants.POSTGRES_USERNAME);
    }

    public String getPassword() {
        return ezProperties.getProperty(EzBakePropertyConstants.POSTGRES_PASSWORD);
    }

    public String getDatabase() {
        return ezProperties.getProperty(EzBakePropertyConstants.POSTGRES_DB);
    }

    public Connection getEzPostgresConnection(EzSecurityToken token) throws SQLException, TException {
        String appName = ezProperties.getProperty(EzBakePropertyConstants.EZBAKE_APPLICATION_NAME);
        Properties dbProperties = new Properties();
        dbProperties.putAll(ezProperties);
        dbProperties.put("user", ezProperties.getProperty(EzBakePropertyConstants.POSTGRES_USERNAME, appName));
        dbProperties.put("password", ezProperties.getProperty(EzBakePropertyConstants.POSTGRES_PASSWORD, appName));
        if (useSSL()) {
            dbProperties.put("ssl", "true");
            dbProperties.put("sslfactory", "org.postgresql.ssl.NonValidatingFactory");
        }

        if (token != null) {
            dbProperties.put("ezbakeTokenProvider", "ezbake.data.postgres.ExplicitTokenProvider");
            dbProperties.put("ezbakeToken", new String(Base64.encodeBase64(new TSerializer().serialize(token)),
                    Charsets.US_ASCII));
        }

        return DriverManager.getConnection(String.format("jdbc:ezbake:postgresql://%s:%s/%s",
                        ezProperties.getProperty(EzBakePropertyConstants.POSTGRES_HOST, "localhost"),
                        ezProperties.getProperty(EzBakePropertyConstants.POSTGRES_PORT, "5432"),
                        ezProperties.getProperty(EzBakePropertyConstants.POSTGRES_DB, appName)),
                dbProperties);
    }

    public Boolean useSSL() {
        return ezProperties.getBoolean(EzBakePropertyConstants.POSTGRES_USE_SSL, true);
    }

}
