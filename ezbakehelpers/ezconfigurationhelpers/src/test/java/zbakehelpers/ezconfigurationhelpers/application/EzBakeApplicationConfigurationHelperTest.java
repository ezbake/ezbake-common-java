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

import ezbake.configuration.constants.EzBakePropertyConstants;

import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

public class EzBakeApplicationConfigurationHelperTest {
    @Test
    public void testWithProperties() {
        Properties props = new Properties();
        props.setProperty(EzBakePropertyConstants.EZBAKE_APPLICATION_NAME, "myapp");
        props.setProperty(EzBakePropertyConstants.EZBAKE_SERVICE_NAME, "myservice");


        EzBakeApplicationConfigurationHelper helper = new EzBakeApplicationConfigurationHelper(props);
        Assert.assertNotNull(helper.getApplicationName());
    }
}
