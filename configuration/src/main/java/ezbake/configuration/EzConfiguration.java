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

package ezbake.configuration;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;

import java.util.Properties;

import ezbake.common.properties.DuplicatePropertyException;
import ezbake.common.properties.EzProperties;
import ezbake.common.security.NoOpTextCryptoProvider;
import ezbake.common.security.SharedSecretTextCryptoProvider;
import ezbake.common.security.TextCryptoProvider;
import ezbake.configuration.constants.EzBakePropertyConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class takes care of loading the "configuration".  It does this by calling the resource loaders that are passed
 * to it. It also has the encryption/decryption provider which is used to decrypt "encrypted" properties.
 *
 * @version 2.0
 */
public class EzConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(EzConfiguration.class.getName());

    private final Properties configuration = new Properties();

    /**
     * Default constructor which will pass the system defaults to the constructor below.  Currently these defaults are
     * using the NoOpTextProvider {@link ezbake.common.security.NoOpTextCryptoProvider} for the cryptography provider
     * and the properties loaders that this will be used are {@link DirectoryConfigurationLoader} and
     * {@link OpenShiftConfigurationLoader}.
     */
    public EzConfiguration() throws EzConfigurationLoaderException  {
        this(new DirectoryConfigurationLoader(), new OpenShiftConfigurationLoader());
    }

     /**
     * Constructor which takes in a variable amount of configuration loaders.  The configuration loaders, if they can
     * will load and return a set of properties objects.  These properties object will be merged together override
     * eaching other, in the reverse order that they were passed in (last argument would be the most relevant etc..)
     *
     * So if you were to call:
     * EzConfiguration(new Loader1(), new Loader2(), new Loader3());
     *
     * If there were to be a conflict with properties between the last argument would be read.  So in this
     * example: conflicting properties would resolve in the following order: Loader3, then Loader2, and finally Loader1.
     *
     * @param configurationLoaders  specifies the configuration loaders to load from which will create the properties.
     *
     * @throws EzConfigurationLoaderException if there was a problem loading from the configuration loaders
     */
    public EzConfiguration(EzConfigurationLoader ... configurationLoaders) throws EzConfigurationLoaderException {
        EzProperties ezProps = new EzProperties();
        for(int i=0; i<configurationLoaders.length; ++i) {
            try  {
                if(configurationLoaders[i].isLoadable()) {
                    ezProps.mergeProperties(configurationLoaders[i].loadConfiguration(), true);
                }
            } catch(DuplicatePropertyException ignored) {
                // We should never get this exception since we want to override properties
            }
        }

        configuration.putAll(ezProps);
    }

    /**
     * Returns the properties object that is represented by the configuration.
     *
     * @return the properties object that was loaded by the configuration
     */
    public Properties getProperties() {
        return configuration;
    }
}
