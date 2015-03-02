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

package ezbakehelpers.ezconfigurationhelpers.azkaban;

import java.util.Properties;

import ezbake.common.properties.EzProperties;
import ezbake.common.security.TextCryptoProvider;
import ezbake.configuration.constants.EzBakePropertyConstants;
import ezbakehelpers.ezconfigurationhelpers.system.SystemConfigurationHelper;

public class AzkabanConfigurationHelper {
    private final EzProperties ezProperties;

    public AzkabanConfigurationHelper(Properties properties) {
        this(properties, new SystemConfigurationHelper(properties).getTextCryptoProvider());
    }

    public AzkabanConfigurationHelper(Properties properties, TextCryptoProvider provider) {
        this.ezProperties = new EzProperties(properties, true);
        this.ezProperties.setTextCryptoProvider(provider);
    }

    public String getAzkabanUrl() {
        return ezProperties.getProperty(EzBakePropertyConstants.AZKABAN_URL);
    }

    public String getUsername() {
        return ezProperties.getProperty(EzBakePropertyConstants.AZKABAN_USERNAME);
    }

    public String getPassword() {
        return ezProperties.getProperty(EzBakePropertyConstants.AZKABAN_PASSWORD);
    }

}
