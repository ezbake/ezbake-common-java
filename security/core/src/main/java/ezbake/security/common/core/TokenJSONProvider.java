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

import ezbake.base.thrift.*;
import ezbake.crypto.PKeyCrypto;

/**
 * User: jhastings
 * Date: 7/11/14
 * Time: 8:40 AM
 */
public interface TokenJSONProvider {
    public EzSecurityTokenJson getTokenJSON(EzSecurityToken token, PKeyCrypto signer) throws Exception;
}


