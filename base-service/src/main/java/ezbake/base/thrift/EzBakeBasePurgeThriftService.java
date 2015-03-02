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

package ezbake.base.thrift;

import org.apache.thrift.TException;

import java.util.Set;

public abstract class EzBakeBasePurgeThriftService extends EzBakeBaseThriftService implements
        EzBakeBasePurgeService.Iface {

    @Override
    public abstract PurgeState beginPurge(String purgeCallbackService, long purgeId, Set<Long> idsToPurge, EzSecurityToken initiatorToken) throws PurgeException, EzSecurityTokenException, TException;

    @Override
    public abstract PurgeState beginVirusPurge(String purgeCallbackService, long purgeId, Set<Long> idsToPurge, EzSecurityToken initiatorToken) throws PurgeException, EzSecurityTokenException, TException;

    @Override
    public abstract PurgeState purgeStatus(EzSecurityToken token, long purgeId) throws EzSecurityTokenException, TException;

    @Override
    public abstract PurgeState cancelPurge(EzSecurityToken token, long purgeId) throws EzSecurityTokenException, TException;
}
