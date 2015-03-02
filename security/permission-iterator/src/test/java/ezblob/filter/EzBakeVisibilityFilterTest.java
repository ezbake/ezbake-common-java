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

package ezblob.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import static ezbake.security.serialize.VisibilitySerialization.serializeVisibilityWithDataToValue;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import org.apache.accumulo.core.client.IteratorSetting;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Value;
import org.apache.thrift.TException;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import ezbake.base.thrift.AdvancedMarkings;
import ezbake.base.thrift.Authorizations;
import ezbake.base.thrift.Permission;
import ezbake.base.thrift.PlatformObjectVisibilities;
import ezbake.base.thrift.Visibility;
import ezbake.data.iterator.EzBakeVisibilityFilter;
import ezbake.thrift.ThriftUtils;

public class EzBakeVisibilityFilterTest {
    EzBakeVisibilityFilter ezBakeVisibilityFilter;

    @Before
    public void setUp() {
        ezBakeVisibilityFilter = new EzBakeVisibilityFilter();
    }

    @Test
    public void testDescribeOptions() {
        assertEquals("EzBake Visibility Iterator", ezBakeVisibilityFilter.describeOptions().getName());
        assertTrue(ezBakeVisibilityFilter.describeOptions().getNamedOptions().containsKey("userAuthorizationBase64"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void initThrowsIllegalArgumentsExceptionWithoutUserKey() throws IOException {
        final Map<String, String> options = Maps.newHashMap();
        ezBakeVisibilityFilter.init(ezBakeVisibilityFilter, options, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void initThrowsIllegalArgumentsExceptionWithoutRequiredPermissions() throws IOException {
        final Map<String, String> options = Maps.newHashMap();
        options.put("userAuthorizationBase64", "ABCDE");
        ezBakeVisibilityFilter.init(ezBakeVisibilityFilter, options, null);
    }

    @Test(expected = IOException.class)
    public void initThrowsIOExceptionWithoutInvalidUserKey() throws IOException {
        final Map<String, String> options = Maps.newHashMap();
        options.put("userAuthorizationBase64", "This===invalid");
        options.put("requiredPermissions", "READ,WRITE");
        ezBakeVisibilityFilter.init(ezBakeVisibilityFilter, options, null);
    }

    @Test
    public void acceptReturnsTrueForUnsetPermissions() throws TException, IOException {
        final Authorizations auths = new Authorizations();
        auths.setPlatformObjectAuthorizations(Sets.newHashSet(1L));

        final PlatformObjectVisibilities platformObjectVisibilities = new PlatformObjectVisibilities();
        platformObjectVisibilities.setPlatformObjectDiscoverVisibility(null);
        platformObjectVisibilities.setPlatformObjectReadVisibility(null);
        platformObjectVisibilities.setPlatformObjectWriteVisibility(null);
        platformObjectVisibilities.setPlatformObjectManageVisibility(null);
        final AdvancedMarkings advancedMarkings = new AdvancedMarkings();
        advancedMarkings.setPlatformObjectVisibility(platformObjectVisibilities);
        final Visibility visibility = new Visibility();
        visibility.setAdvancedMarkings(advancedMarkings);

        final Value value = serializeVisibilityWithDataToValue(visibility, "test".getBytes());
        final Key key = new Key("key");

        final Map<String, String> options = Maps.newHashMap();
        options.put("userAuthorizationBase64", ThriftUtils.serializeToBase64(auths));
        options.put("requiredPermissions", "READ");
        ezBakeVisibilityFilter.init(ezBakeVisibilityFilter, options, null);
        assertTrue(ezBakeVisibilityFilter.accept(key, value));
    }

    @Test
    public void acceptReturnsTrueForValidPermissions() throws TException, IOException {
        final Authorizations auths = new Authorizations();
        auths.setPlatformObjectAuthorizations(Sets.newHashSet(1L));

        final PlatformObjectVisibilities platformObjectVisibilities = new PlatformObjectVisibilities();
        platformObjectVisibilities.setPlatformObjectReadVisibility(Sets.newHashSet(1L));
        final AdvancedMarkings advancedMarkings = new AdvancedMarkings();
        advancedMarkings.setPlatformObjectVisibility(platformObjectVisibilities);
        final Visibility visibility = new Visibility();
        visibility.setAdvancedMarkings(advancedMarkings);

        final Value value = serializeVisibilityWithDataToValue(visibility, "test".getBytes());
        final Key key = new Key("key");

        final Map<String, String> options = Maps.newHashMap();
        options.put("userAuthorizationBase64", ThriftUtils.serializeToBase64(auths));
        options.put("requiredPermissions", "WRITE,MANAGE_VISIBILITY,DISCOVER");
        ezBakeVisibilityFilter.init(ezBakeVisibilityFilter, options, null);
        assertTrue(ezBakeVisibilityFilter.accept(key, value));
    }

    @Test
    public void acceptReturnsTrueForMultipleRequiredPermissions() throws TException, IOException {
        final Authorizations auths = new Authorizations();
        auths.setPlatformObjectAuthorizations(Sets.newHashSet(1L));

        final PlatformObjectVisibilities platformObjectVisibilities = new PlatformObjectVisibilities();
        platformObjectVisibilities.setPlatformObjectReadVisibility(Sets.newHashSet(1L));
        platformObjectVisibilities.setPlatformObjectManageVisibility(Sets.newHashSet(1L));
        final AdvancedMarkings advancedMarkings = new AdvancedMarkings();
        advancedMarkings.setPlatformObjectVisibility(platformObjectVisibilities);
        final Visibility visibility = new Visibility();
        visibility.setAdvancedMarkings(advancedMarkings);

        final Value value = serializeVisibilityWithDataToValue(visibility, "test".getBytes());
        final Key key = new Key("key");

        final Map<String, String> options = Maps.newHashMap();
        options.put("userAuthorizationBase64", ThriftUtils.serializeToBase64(auths));
        options.put("requiredPermissions", "MANAGE_VISIBILITY,READ");
        ezBakeVisibilityFilter.init(ezBakeVisibilityFilter, options, null);
        assertTrue(ezBakeVisibilityFilter.accept(key, value));
    }

    @Test
    public void acceptReturnsFalseForMissingMultipleRequiredPermissions() throws TException, IOException {
        final Authorizations auths = new Authorizations();
        auths.setPlatformObjectAuthorizations(Sets.newHashSet(1L));

        final PlatformObjectVisibilities platformObjectVisibilities = new PlatformObjectVisibilities();
        platformObjectVisibilities.setPlatformObjectReadVisibility(Sets.newHashSet(1L));
        platformObjectVisibilities.setPlatformObjectManageVisibility(Sets.newHashSet(2L));
        final AdvancedMarkings advancedMarkings = new AdvancedMarkings();
        advancedMarkings.setPlatformObjectVisibility(platformObjectVisibilities);
        final Visibility visibility = new Visibility();
        visibility.setAdvancedMarkings(advancedMarkings);

        final Value value = serializeVisibilityWithDataToValue(visibility, "test".getBytes());
        final Key key = new Key("key");

        final Map<String, String> options = Maps.newHashMap();
        options.put("userAuthorizationBase64", ThriftUtils.serializeToBase64(auths));
        options.put("requiredPermissions", "MANAGE_VISIBILITY,READ");
        ezBakeVisibilityFilter.init(ezBakeVisibilityFilter, options, null);
        assertFalse(ezBakeVisibilityFilter.accept(key, value));
    }

    @Test
    public void acceptReturnsFalseIfReadIsntSet() throws TException, IOException {
        final Authorizations auths = new Authorizations();
        auths.setPlatformObjectAuthorizations(Sets.newHashSet(1L));

        final PlatformObjectVisibilities platformObjectVisibilities = new PlatformObjectVisibilities();
        platformObjectVisibilities.setPlatformObjectReadVisibility(Sets.newHashSet(0L));
        final AdvancedMarkings advancedMarkings = new AdvancedMarkings();
        advancedMarkings.setPlatformObjectVisibility(platformObjectVisibilities);
        final Visibility visibility = new Visibility();
        visibility.setAdvancedMarkings(advancedMarkings);

        final Value value = serializeVisibilityWithDataToValue(visibility, "test".getBytes());
        final Key key = new Key("key");

        final Map<String, String> options = Maps.newHashMap();
        options.put("userAuthorizationBase64", ThriftUtils.serializeToBase64(auths));
        options.put("requiredPermissions", "READ");
        ezBakeVisibilityFilter.init(ezBakeVisibilityFilter, options, null);
        assertFalse(ezBakeVisibilityFilter.accept(key, value));
    }

    @Test
    public void acceptReturnsFalseIfWriteIsntSet() throws TException, IOException {
        final Authorizations auths = new Authorizations();
        auths.setPlatformObjectAuthorizations(Sets.newHashSet(1L));

        final PlatformObjectVisibilities platformObjectVisibilities = new PlatformObjectVisibilities();
        platformObjectVisibilities.setPlatformObjectWriteVisibility(Sets.newHashSet(0L));
        final AdvancedMarkings advancedMarkings = new AdvancedMarkings();
        advancedMarkings.setPlatformObjectVisibility(platformObjectVisibilities);
        final Visibility visibility = new Visibility();
        visibility.setAdvancedMarkings(advancedMarkings);

        final Value value = serializeVisibilityWithDataToValue(visibility, "test".getBytes());
        final Key key = new Key("key");

        final Map<String, String> options = Maps.newHashMap();
        options.put("userAuthorizationBase64", ThriftUtils.serializeToBase64(auths));
        options.put("requiredPermissions", "WRITE");
        ezBakeVisibilityFilter.init(ezBakeVisibilityFilter, options, null);
        assertFalse(ezBakeVisibilityFilter.accept(key, value));
    }

    @Test
    public void acceptReturnsFalseIfManageVisibilityIsntSet() throws TException, IOException {
        final Authorizations auths = new Authorizations();
        auths.setPlatformObjectAuthorizations(Sets.newHashSet(1L));

        final PlatformObjectVisibilities platformObjectVisibilities = new PlatformObjectVisibilities();
        platformObjectVisibilities.setPlatformObjectManageVisibility(Sets.newHashSet(0L));
        final AdvancedMarkings advancedMarkings = new AdvancedMarkings();
        advancedMarkings.setPlatformObjectVisibility(platformObjectVisibilities);
        final Visibility visibility = new Visibility();
        visibility.setAdvancedMarkings(advancedMarkings);

        final Value value = serializeVisibilityWithDataToValue(visibility, "test".getBytes());
        final Key key = new Key("key");

        final Map<String, String> options = Maps.newHashMap();
        options.put("userAuthorizationBase64", ThriftUtils.serializeToBase64(auths));
        options.put("requiredPermissions", "MANAGE_VISIBILITY");
        ezBakeVisibilityFilter.init(ezBakeVisibilityFilter, options, null);
        assertFalse(ezBakeVisibilityFilter.accept(key, value));
    }

    @Test
    public void testSetOptionsReturnsProperIteratorSetting() throws Exception {
        final Authorizations expectedAuths =
                new Authorizations().setExternalCommunityAuthorizations(Sets.newHashSet("foo", "bar"));

        final Set<Permission> expectedPermissions = EnumSet.of(Permission.READ, Permission.DISCOVER);
        final IteratorSetting iteratorSetting = new IteratorSetting(15, "test", EzBakeVisibilityFilter.class);
        EzBakeVisibilityFilter.setOptions(iteratorSetting, expectedAuths, expectedPermissions);

        assertEquals(
                expectedAuths,
                ThriftUtils.deserializeFromBase64(Authorizations.class,
                        iteratorSetting.getOptions().get("userAuthorizationBase64")));

        assertEquals("READ,DISCOVER", iteratorSetting.getOptions().get("requiredPermissions"));
    }
}
