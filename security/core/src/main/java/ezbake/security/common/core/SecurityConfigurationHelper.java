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

package ezbake.security.common.core;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

import ezbakehelpers.ezconfigurationhelpers.application.EzBakeApplicationConfigurationHelper;
import ezbake.common.properties.EzProperties;
import ezbake.common.security.TextCryptoProvider;
import ezbake.configuration.constants.EzBakePropertyConstants;

import java.nio.file.Paths;
import java.nio.file.Files;
import java.io.File;
import java.util.Properties;


public class SecurityConfigurationHelper {
    /** EzSecurity Cache properties **/
    public enum USER_CACHE_TYPE {
        MEMORY,
        REDIS
    };

    private static final String DefaultSSLProtocol = "TLSv1";
    private static final String DefaultKeyStore = "application.p12";
    private static final String DefaultKeyPass = "password";
    private static final String DefaultKeyType = "PKCS12";
    private static final String DefaultTrustStore = "ezbakeca.jks";
    private static final String DefaultTrustPass = "password";
    private static final String DefaultTrustType = "JKS";
    // Classpath keystore and truststore default locations
    private static final String DefaultKeyStoreClasspath = "/ssl/" + DefaultKeyStore;
    private static final String DefaultTrustStoreClasspath = "/ssl/" + DefaultTrustStore;
    // EZConfiguration keys
    public static final String UseDefaultSslKey = "ezbake.security.default.ssl";

    protected String confPrefix;

    // SSL Context variables
    protected String keyStore;
    protected String keyStoreType;
    protected String keyStorePass;
    private boolean keyStoreIsSet;

    protected String trustStore;
    protected String trustStoreType;
    protected String trustStorePass;
    private boolean trustStoreIsSet;

    // Don't use Thrift TSSLTransportParams
    private boolean defaultSSLIsSet = false;

    private String securityId;
    private String certificateDir;

    private EzProperties ezProps;

    public SecurityConfigurationHelper(Properties props, TextCryptoProvider provider) {
        this.ezProps = new EzProperties(props, true);
        this.ezProps.setTextCryptoProvider(provider);
        EzBakeApplicationConfigurationHelper appHelper = new EzBakeApplicationConfigurationHelper(ezProps);
        this.securityId = appHelper.getSecurityID();
        this.certificateDir = appHelper.getCertificatesDir();
        this.confPrefix = this.certificateDir;

        if(securityId != null && !Files.exists(Paths.get(joinPath(this.confPrefix, DefaultKeyStore)))) {
            String localDir = joinPath(this.confPrefix, securityId);
            if(!Files.exists(Paths.get(localDir))) {
                this.confPrefix = localDir;
            }
        }
    }

    private static String joinPath(String prefix, String file) {
        File joined = new File(prefix, file);
        return joined.getPath();
    }

    /***************************************
     *
     * EzSecurity Caching accessors
     *
     ****************************************/

    public USER_CACHE_TYPE getCacheType() {
        return USER_CACHE_TYPE.valueOf(ezProps.getProperty(EzBakePropertyConstants.EZBAKE_USER_CACHE_TYPE, "MEMORY"));
    }

    public long getUserCacheTTL() {
        return ezProps.getLong(EzBakePropertyConstants.EZBAKE_USER_CACHE_TTL, 43200);
    }

    public int getUserCacheSize() {
        return ezProps.getInteger(EzBakePropertyConstants.EZBAKE_USER_CACHE_SIZE, 1000);
    }

    /***************************************
     *
     * EzSecurity Token accessors
     *
     ****************************************/

    public int getRequestExpiration() {
        return ezProps.getInteger(EzBakePropertyConstants.EZBAKE_REQUEST_EXPIRATION, 60);
    }
    public int getTokenExpiration() {
        return ezProps.getInteger(EzBakePropertyConstants.EZBAKE_TOKEN_EXPIRATION, 7200);
    }


    /***************************************
     *
     * EzSecurity Registration Service accessors
     *
     ****************************************/

    /**
     *
     * @return Registration service implementation (fully qualified class name)
     */
    public String getAppRegistrationImpl() {
        return ezProps.getProperty(EzBakePropertyConstants.EZBAKE_APP_REGISTRATION_IMPL);
    }

    /***************************************
     *
     * EzSecurity User Service accessors
     *
     ****************************************/

    /**
     *
     * @return path to the ezsecurity admins file
     */
    public String getEzAdminsFile() {
        /* TODO(soup)
        String defaultFile = new File(EZConfigurationConstants.DEFAULT_EZCONFIGURATION_DIR, "admins").getAbsolutePath();
        return ezConfiguration.getPathString(parseKey(EZ_ADMINS_FILE), defaultFile);
        */
        return null;
    }

    /**
     *
     * @return User service implementation (fully qualified class name)
     */
    public String getUserServiceImpl() {
        return ezProps.getProperty(EzBakePropertyConstants.EZBAKE_USER_SERVICE_IMPL);
    }

    /***************************************
     *
     * EzSecurity Mock accessors
     *
     ****************************************/

    public boolean useMockServer() {
        return ezProps.getBoolean(EzBakePropertyConstants.EZBAKE_SECURITY_SERVICE_MOCK_SERVER, false);
    }


    /***************************************
     *
     * EzFrontend Proxy accessors
     *
     ****************************************/

    public boolean useForwardProxy() {
        return ezProps.getBoolean(EzBakePropertyConstants.EZBAKE_USE_FORWARD_PROXY, false);
    }

    /***************************************
     *
     * SSL accessors
     *
     ****************************************/

    public String getCertDir() {
        return certificateDir;
    }
    public String getSecurityId() {
        return securityId;
    }

}
