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

package ezbake.security.client.validation;

import ezbake.security.client.EzBakeSecurityClientConfigurationHelper;
import ezbakehelpers.ezconfigurationhelpers.application.EzBakeApplicationConfigurationHelper;

import java.util.Properties;

/**
 * User: jhastings
 * Date: 10/13/14
 * Time: 3:39 PM
 */
public abstract class BaseValidator {

    protected EzBakeApplicationConfigurationHelper applicationConfigurationHelper;
    protected EzBakeSecurityClientConfigurationHelper securityConfigurationHelper;

    public BaseValidator(Properties configuration) {
        this.applicationConfigurationHelper = new EzBakeApplicationConfigurationHelper(configuration);
        this.securityConfigurationHelper = new EzBakeSecurityClientConfigurationHelper(configuration);
    }
}
