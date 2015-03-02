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

package ezbake.security.test;

import java.io.IOException;
import java.util.Set;

import com.google.common.collect.Sets;

import ezbake.base.thrift.AdvancedMarkings;
import ezbake.base.thrift.Authorizations;
import ezbake.base.thrift.PlatformObjectVisibilities;
import ezbake.base.thrift.Visibility;

public class PermTestHelpers {
    public static PlatformObjectVisibilities createPlatformObjectVisibilities() {
        final PlatformObjectVisibilities pov = new PlatformObjectVisibilities();
        pov.setPlatformObjectReadVisibility(Sets.newHashSet(56L, 2785L, 123876592237L));
        pov.setPlatformObjectWriteVisibility(Sets.newHashSet(2785L, 123876592237L));
        pov.setPlatformObjectDiscoverVisibility(Sets.newHashSet(3L, 56L, 2785L, 123876592237L));
        pov.setPlatformObjectManageVisibility(Sets.newHashSet(123876592237L));

        return pov;
    }

    public static AdvancedMarkings createAdvancedMarkings() {
        final AdvancedMarkings markings = new AdvancedMarkings();
        markings.setComposite(true);
        markings.setExternalCommunityVisibility("Foo&(Bar|Baz)");
        markings.setId(18657294732947L);
        markings.setPlatformObjectVisibility(createPlatformObjectVisibilities());
        markings.setPurgeIds(Sets.newHashSet(87L, 9723957L));

        return markings;
    }

    public static Visibility createVisibility() {
        final Visibility visibility = new Visibility();
        visibility.setFormalVisibility("TS&USA");
        visibility.setAdvancedMarkings(createAdvancedMarkings());

        return visibility;
    }

    public static Authorizations createAuths(
            Set<String> formalAuths, Set<String> externalCommunityAuths, Set<Long> platformObjectAuths) {
        final Authorizations thrift = new Authorizations();
        thrift.setFormalAuthorizations(formalAuths);
        thrift.setExternalCommunityAuthorizations(externalCommunityAuths);
        thrift.setPlatformObjectAuthorizations(platformObjectAuths);

        return thrift;
    }
}
