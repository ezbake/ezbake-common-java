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

package ezbakehelpers.ezconfigurationhelpers.ssl;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import ezbake.common.properties.EzProperties;
import ezbake.common.security.TextCryptoProvider;
import ezbake.common.ssl.SSLContextException;
import ezbake.common.ssl.SSLContextUtil;
import ezbake.configuration.constants.EzBakePropertyConstants;
import ezbakehelpers.ezconfigurationhelpers.application.EzBakeApplicationConfigurationHelper;
import ezbakehelpers.ezconfigurationhelpers.system.SystemConfigurationHelper;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * User: jhastings
 * Date: 8/20/14
 * Time: 10:30 PM
 */
public class SslConfigurationHelper {

    /* Defaults when properties are not set */
    public static final String SSL_PROTOCOL = "TLSv1";
    public static final String PRIVATE_KEY_FILE = "application.priv";
    public static final String EZ_SECURITY_PUBLIC = "ezbakesecurityservice.pub";
    public static final String KEYSTORE_FILENAME = "application.p12";
    public static final String KEYSTORE_TYPE = "PKCS12";
    public static final String KEYSTORE_PASS = "password";
    public static final String TRUSTSTORE_FILENAME = "ezbakeca.jks";
    public static final String TRUSTSTORE_TYPE = "JKS";
    public static final String TRUSTSTORE_PASS = "password";

    private EzProperties ezProperties;
    private EzBakeApplicationConfigurationHelper appHelper;

    public SslConfigurationHelper(Properties properties) {
        this(properties, new SystemConfigurationHelper(properties).getTextCryptoProvider());
    }

    public SslConfigurationHelper(Properties properties, TextCryptoProvider provider) {
        ezProperties = new EzProperties(properties, true);
        ezProperties.setTextCryptoProvider(provider);
        appHelper = new EzBakeApplicationConfigurationHelper(ezProperties);
    }

    public String getSslProtocol() {
        String protocol = ezProperties.getProperty(EzBakePropertyConstants.EZBAKE_SSL_PROTOCOL_KEY);
        if (protocol == null || protocol.isEmpty()) {
            protocol = SSL_PROTOCOL;
        }
        return protocol;
    }

    /**
     * Get the configured SSL Ciphers, if set. Will return null if the key has no value.
     *
     * @return an array of ciphers, or null if no value was set
     */
    public String[] getSslCiphers() {
        String ciphers = ezProperties.getProperty(EzBakePropertyConstants.EZBAKE_SSL_CIPHERS_KEY);
        if (ciphers != null) {
            return Iterables.toArray(Splitter.on(',').trimResults().omitEmptyStrings().split(ciphers), String.class);
        }
        return null;
    }

    public boolean isPeerAuthRequired() {
        return ezProperties.getBoolean(EzBakePropertyConstants.EZBAKE_SSL_PEER_AUTH_REQUIRED, true);
    }

    public String getKeystoreFile() {
        return getKeystoreFile(null);
    }

    public String getKeystoreFile(String serviceName) {
        String propertyKey = serviceKey(EzBakePropertyConstants.EZBAKE_APPLICATION_KEYSTORE_FILE, serviceName);
        return ezProperties.getProperty(propertyKey, KEYSTORE_FILENAME);
    }

    public String getKeystoreType() {
        return getKeystoreType(null);
    }

    public String getKeystoreType(String serviceName) {
        String propertyKey = serviceKey(EzBakePropertyConstants.EZBAKE_APPLICATION_KEYSTORE_TYPE, serviceName);
        return ezProperties.getProperty(propertyKey, KEYSTORE_TYPE);
    }

    public String getKeystorePass() {
        return getKeystorePass(null);
    }

    public String getKeystorePass(String serviceName) {
        String propertyKey = serviceKey(EzBakePropertyConstants.EZBAKE_APPLICATION_KEYSTORE_PASS, serviceName);
        return ezProperties.getProperty(propertyKey, KEYSTORE_PASS);
    }

    public String getTruststoreFile() {
        return getTruststoreFile(null);
    }

    public String getTruststoreFile(String serviceName) {
        String propertyKey = serviceKey(EzBakePropertyConstants.EZBAKE_APPLICATION_TRUSTSTORE_FILE, serviceName);
        return ezProperties.getProperty(propertyKey, TRUSTSTORE_FILENAME);
    }

    public String getTruststoreType() {
        return getTruststoreType(null);
    }

    public String getTruststoreType(String serviceName) {
        String propertyKey = serviceKey(EzBakePropertyConstants.EZBAKE_APPLICATION_TRUSTSTORE_TYPE, serviceName);
        return ezProperties.getProperty(propertyKey, TRUSTSTORE_TYPE);
    }

    public String getTruststorePass() {
        return getTruststorePass(null);
    }

    public String getTruststorePass(String serviceName) {
        String propertyKey = serviceKey(EzBakePropertyConstants.EZBAKE_APPLICATION_TRUSTSTORE_PASS, serviceName);
        return ezProperties.getProperty(propertyKey, TRUSTSTORE_PASS);
    }

    public String getPrivateKeyFile() {
        return getPrivateKeyFile(null);
    }

    public String getPrivateKeyFile(String serviceName) {
        String propertyKey = serviceKey(EzBakePropertyConstants.EZBAKE_APPLICATION_PRIVATE_KEY_FILE, serviceName);
        return ezProperties.getProperty(propertyKey, PRIVATE_KEY_FILE);
    }

    public String getEzSecurityPublic() {
        return ezProperties.getProperty(EzBakePropertyConstants.EZBAKE_APPLICATION_PUBLIC_KEY_FILE, EZ_SECURITY_PUBLIC);
    }


    private static String serviceKey(String key, String serviceName) {
        return !(serviceName == null || serviceName.isEmpty()) ? String.format("%s.%s", key, serviceName) : key;
    }


    /**
     * This helper will return the absolute path to the EzBake application certificate
     *
     * @return absolute path to EzBake application certificate
     */
    public String getEzBakeKeyStorePath() {
        return new File(appHelper.getCertificatesDir(), getKeystoreFile()).getAbsolutePath();
    }

    /**
     * This helper will return the absolute path to the EzBake truststore
     *
     * @return absolute path to EzBake truststore
     */
    public String getEzBakeTrustStorePath() {
        return new File(appHelper.getCertificatesDir(), getTruststoreFile()).getAbsolutePath();
    }

    /**
     * This helper will return the absolute path to the configured service keystore
     *
     * Ex:
     *     ezbake.ssl.keystore.file.serviceName=pki/server.p12
     *
     *     Calling getServiceKeyStorePath("serviceName") would return the absolute path to the file "pki/server.p12".
     *     This could be relative to the directory the service is running from (when outside a container) or relative
     *     to the repo directory if in a container.
     *
     * @param serviceName suffix on the keystore property
     * @return absolute path to the serivce keystore
     */
    public String getServiceKeyStorePath(String serviceName) {
        return ezProperties.getPath(serviceKey(EzBakePropertyConstants.EZBAKE_APPLICATION_KEYSTORE_FILE, serviceName),
                null);
    }

    /**
     * This helper will return the absolute path to the configured service truststore
     *
     * Ex:
     *     ezbake.ssl.truststore.file.serviceName=pki/truststore.p12
     *
     *     Calling getServiceTrustStorePath("serviceName") would return the absolute path to the file
     *     "pki/truststore.p12". This could be relative to the directory the service is running from (when outside a
     *     container) or relative to the repo directory if in a container.
     *
     * @param serviceName suffix on the truststore property
     * @return absolute path to the serivce truststore
     */
    public String getServiceTrustStorePath(String serviceName) {
        return ezProperties.getPath(serviceKey(EzBakePropertyConstants.EZBAKE_APPLICATION_TRUSTSTORE_FILE, serviceName),
                null);
    }

    /**
     * This helper will return the absolute path to the configured system keystore. This is the server certificate. If
     * no keystore is configured, this will return null
     *
     * @return absolute path to the system keystore
     */
    public String getSystemKeyStorePath() {
        return ezProperties.getPath(EzBakePropertyConstants.SYSTEM_KEYSTORE_PATH, null);
    }

    /**
     * This helper will return the type of the configured system keystore. This is the server certificate
     *
     * @return type of the keystore
     */
    public String getSystemKeyStoreType() {
        return ezProperties.getProperty(EzBakePropertyConstants.SYSTEM_KEYSTORE_TYPE);
    }

    /**
     * This helper will return the password for the configured system keystore. This is the server certificate
     *
     * @return password for the keystore
     */
    public String getSystemKeyStorePassword() {
        return ezProperties.getProperty(EzBakePropertyConstants.SYSTEM_KEYSTORE_PASSWORD);
    }

    public String getSystemTruststorePath() {
        return ezProperties.getPath(EzBakePropertyConstants.SYSTEM_TRUSTSTORE_PATH, "/etc/pki/jks/truststore.jks");
    }

    public String getSystemTruststoreType() {
        return ezProperties.getProperty(EzBakePropertyConstants.SYSTEM_TRUSTSTORE_TYPE);
    }

    public String getSystemTruststorePassword() {
        return ezProperties.getProperty(EzBakePropertyConstants.SYSTEM_TRUSTSTORE_PASSWORD);
    }

    /**
     * This helper initializes and returns an SSL context for EzBake internal certs
     *
     * @return an SSL context with EzBake certs
     * @throws IOException
     * @throws SSLContextException
     */
    public SSLContext getEzBakeSSLContext() throws IOException, SSLContextException {
        return SSLContextUtil.getSSLContext(
                getEzBakeKeyStorePath(),
                getKeystorePass(),
                getKeystoreType(),
                getEzBakeTrustStorePath(),
                getTruststorePass(),
                getTruststoreType());
    }

    /**
     * This helper initializes and returns an SSL context for the server, using the system keystore and truststore
     * properties
     *
     * @return an SSL context for the system/server
     * @throws IOException
     * @throws SSLContextException
     */
    public SSLContext getSystemSSLContext() throws IOException, SSLContextException {
        return SSLContextUtil.getSSLContext(
                getEzBakeKeyStorePath(),
                getKeystorePass(),
                getKeystoreType(),
                getSystemTruststorePath(),
                getSystemTruststorePassword(),
                getSystemTruststoreType());
    }

    /**
     * This helper returns an SSL context for the named service. The keystores must be configured with
     * configuration properties in the format:
     *     ezbake.ssl.keystore.file.serviceName
     *
     *
     * @param serviceName suffix on the keystore/truststore property keys
     * @return an SSL context using the configured keystores
     * @throws IOException
     * @throws SSLContextException
     */
    public SSLContext getServiceSSLContext(String serviceName) throws IOException, SSLContextException {
        return SSLContextUtil.getSSLContext(
                getServiceKeyStorePath(serviceName),
                getKeystorePass(serviceName),
                getKeystoreType(serviceName),
                getServiceTrustStorePath(serviceName),
                getTruststorePass(serviceName),
                getTruststoreType(serviceName));
    }
}
