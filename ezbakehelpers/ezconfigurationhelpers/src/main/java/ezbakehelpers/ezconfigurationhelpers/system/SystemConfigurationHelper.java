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

package ezbakehelpers.ezconfigurationhelpers.system;

import com.google.common.base.Strings;
import com.google.common.base.Preconditions;

import java.io.File;

import ezbake.common.openshift.OpenShiftUtil;
import ezbake.common.properties.EzProperties;
import ezbake.common.security.TextCryptoProvider;
import ezbake.common.security.NoOpTextCryptoProvider;
import ezbake.common.security.SharedSecretTextCryptoProvider;
import ezbake.configuration.constants.EzBakePropertyConstants;

import ezbakehelpers.ezconfigurationhelpers.application.EzBakeApplicationConfigurationHelper;

import java.util.Properties;

public class SystemConfigurationHelper {
    EzProperties props;

    public SystemConfigurationHelper(Properties props) {
        this.props = new EzProperties(props, true);
    }

    public String getLogDirectory() {
        String retVal = props.getProperty(EzBakePropertyConstants.EZBAKE_LOG_DIRECTORY);
        if(retVal != null)
            return retVal;

        // Check for openshift
        if(OpenShiftUtil.inOpenShiftContainer()) {
            retVal = System.getenv("OPENSHIFT_LOG_DIR");
        }

        // if we are still null then push it to /tmp to maintain backwards compatiablity
        if(retVal == null) {
            retVal = "/tmp";
        }

        return retVal;
    }

    public boolean shouldLogToStdOut() {
        return props.getBoolean(EzBakePropertyConstants.EZBAKE_LOG_TO_STDOUT, false);
    }

    /**
     * Based on configuration file return true/false
     * on whether or not one is permission to deploy/undeploy/delete
     * Application
     *
     * @return boolean on whether or not admin is required to deploy
     */
    public boolean isAdminApplicationDeployment() {
        return props.getBoolean(EzBakePropertyConstants.EZBAKE_ADMIN_APPLICATION_DEPLOYMENT, true);
    }

    public TextCryptoProvider getTextCryptoProvider() {
        String envVar = props.getProperty(EzBakePropertyConstants.EZBAKE_SHARED_SECRET_ENVIRONMENT_VARIABLE);
        if(Strings.isNullOrEmpty(envVar)) {
           return new NoOpTextCryptoProvider();
        }
        String secret = System.getenv(envVar);
        if(Strings.isNullOrEmpty(secret)) {
           return new NoOpTextCryptoProvider();
        } else {
            return new SharedSecretTextCryptoProvider(secret);
        }
    }

     public String getLogFilePath(String applicationName, String serviceName) {
        serviceName = Preconditions.checkNotNull(serviceName, "Service can not be null!");
        String fileName;
        StringBuilder path = new StringBuilder(getLogDirectory());

        // we are no in an openshift container then lets add the folders
        if(!OpenShiftUtil.inOpenShiftContainer()) {
            if(applicationName != null) {
                path.append(File.separator).append(applicationName).append(File.separator);
            } else {
                path.append(File.separator).append(serviceName).append(File.separator);
            }
        }

        if(applicationName != null) {
            fileName = EzBakeApplicationConfigurationHelper.getApplicationServiceName(applicationName, serviceName) + ".log";
        } else {
            fileName = serviceName + ".log";
        }

        path.append(fileName);
        return path.toString();
    }
}
