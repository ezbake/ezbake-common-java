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

package ezbakehelpers.ezconfigurationhelpers.application;

import ezbake.common.properties.EzProperties;
import ezbake.configuration.constants.EzBakePropertyConstants;
import java.util.Properties;

public class EzBakeApplicationConfigurationHelper {
    EzProperties props;

    final String applicationName;
    final String serviceName;

    private final static char APPLICATION_SERVICE_SEPERATOR = '_';

    public EzBakeApplicationConfigurationHelper(Properties props) {
        this.props = new EzProperties(props, true);
        this.applicationName = this.props.getProperty(EzBakePropertyConstants.EZBAKE_APPLICATION_NAME);
        this.serviceName = this.props.getProperty(EzBakePropertyConstants.EZBAKE_SERVICE_NAME);
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getApplicationVersion() {
        return props.getProperty(EzBakePropertyConstants.EZBAKE_APPLICATION_VERSION);
    }

    public int getApplicationInstanceNumber() {
        return props.getInteger(EzBakePropertyConstants.EZBAKE_APPLICATION_INSTANCE_NUMBER, 1);
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getSecurityID() {
        return props.getProperty(EzBakePropertyConstants.EZBAKE_SECURITY_ID);
    }

    public String getCertificatesDir() {
        return props.getPath(EzBakePropertyConstants.EZBAKE_CERTIFICATES_DIRECTORY, "");
    }

    public String getApplicationServiceName() {
        return applicationName + APPLICATION_SERVICE_SEPERATOR + serviceName;
    }

    public static String getApplicationServiceName(String appName, String service) {
        return appName + APPLICATION_SERVICE_SEPERATOR + service;
    }
}
