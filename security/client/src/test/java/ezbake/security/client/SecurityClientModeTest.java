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

package ezbake.security.client;

import ezbake.security.client.provider.TokenProvider;
import org.junit.Test;

import java.util.Properties;

/**
 * User: jhastings
 * Date: 10/20/14
 * Time: 8:57 AM
 */
public class SecurityClientModeTest {

    @Test
    public void testMockMode() {
        Properties properties = new Properties();
        properties.setProperty(TokenProvider.CLIENT_MODE, TokenProvider.ClientMode.MOCK.getValue());
        EzbakeSecurityClient client = new EzbakeSecurityClient(properties);
    }
}
