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

package ezbake.security.permissions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import static ezbake.security.permissions.PermissionUtils.ALL_PERMS;
import static ezbake.security.permissions.PermissionUtils.NO_PERMS;
import static ezbake.security.test.PermTestHelpers.createAuths;
import static ezbake.security.test.PermTestHelpers.createVisibility;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.google.common.collect.Sets;

import ezbake.base.thrift.AdvancedMarkings;
import ezbake.base.thrift.Authorizations;
import ezbake.base.thrift.Permission;
import ezbake.base.thrift.PlatformObjectVisibilities;
import ezbake.base.thrift.Visibility;

// NOTE: This set bit positions in these tests are based off the visibilities generated in createVisibility()
public class PermissionUtilsTest {
    @Test
    public void testGetPermissionsAll() {
        final Visibility visibility = createVisibility();

        final Authorizations auths = createAuths(
                Sets.newHashSet("U", "C", "S", "TS", "USA"), Sets.newHashSet("Foo", "Bar"),
                Sets.newHashSet(3L, 56L, 2785L, 123876592237L));

        assertEquals(ALL_PERMS, PermissionUtils.getPermissions(auths, visibility, false));
        assertEquals(ALL_PERMS, PermissionUtils.getPermissions(auths, visibility, true));
    }

    @Test
    public void testGetPermissionsReadDiscover() {
        final Visibility visibility = createVisibility();

        final Authorizations auths = createAuths(
                Sets.newHashSet("U", "C", "S", "TS", "USA"), Sets.newHashSet("Foo", "Bar"), Sets.newHashSet(56L));

        assertEquals(
                EnumSet.of(Permission.READ, Permission.DISCOVER),
                PermissionUtils.getPermissions(auths, visibility, false));

        assertEquals(
                EnumSet.of(Permission.READ, Permission.DISCOVER),
                PermissionUtils.getPermissions(auths, visibility, true));
    }

    @Test
    public void testGetPermissionsNoPerms() {
        final Visibility visibility = createVisibility();

        final Authorizations auths = createAuths(
                Sets.newHashSet("U", "C", "S", "TS", "USA"), Sets.newHashSet("Foo", "Bar"),
                Sets.newHashSet(4L, 57L, 2786L, 12387659238L)); // Add 1 to all positions

        assertEquals(NO_PERMS, PermissionUtils.getPermissions(auths, visibility, false));
        assertEquals(NO_PERMS, PermissionUtils.getPermissions(auths, visibility, true));
    }

    @Test
    public void testGetPermissionsExternalCommunityMismatch() {
        final Visibility visibility = createVisibility();

        // Remove required auth
        final Authorizations auths = createAuths(
                Sets.newHashSet("U", "C", "S", "TS", "USA"), Sets.newHashSet("Foo"),
                Sets.newHashSet(3L, 56L, 2785L, 123876592237L));

        assertEquals(NO_PERMS, PermissionUtils.getPermissions(auths, visibility, false));
        assertEquals(NO_PERMS, PermissionUtils.getPermissions(auths, visibility, true));
    }

    @Test
    public void testGetPermissionsFormalAuthsMismatch() {
        final Visibility visibility = createVisibility();

        final Authorizations auths = createAuths(
                Sets.newHashSet("U"), Sets.newHashSet("Foo", "Bar"), Sets.newHashSet(3L, 56L, 2785L, 123876592237L));

        // Formal auths validation turned off in this call so everything should pass
        assertEquals(ALL_PERMS, PermissionUtils.getPermissions(auths, visibility, false));
        assertEquals(NO_PERMS, PermissionUtils.getPermissions(auths, visibility, true));
    }

    @Test
    public void testGetPermissionsSubset() {
        final Visibility visibility = createVisibility();

        final Authorizations auths = createAuths(
                Sets.newHashSet("U", "C", "S", "TS", "USA"), Sets.newHashSet("Foo", "Bar"),
                Sets.newHashSet(3L, 56L, 2785L, 123876592237L));

        final Set<Permission> permsSubset = EnumSet.of(Permission.READ, Permission.WRITE);

        assertEquals(permsSubset, PermissionUtils.getPermissions(auths, visibility, false, permsSubset));
        assertEquals(permsSubset, PermissionUtils.getPermissions(auths, visibility, true, permsSubset));
    }

    @Test
    public void testNullVisibility() {
        final Authorizations auths = createAuths(
                Sets.newHashSet("U", "C", "S", "TS", "USA"), Sets.newHashSet("Foo", "Bar"),
                Sets.newHashSet(3L, 56L, 2785L, 123876592237L));

        assertEquals(ALL_PERMS, PermissionUtils.getPermissions(auths, null, true));
        assertEquals(ALL_PERMS, PermissionUtils.getPermissions(null, null, true));
    }

    @Test
    public void testNullAuths() {
        final Visibility visibility = createVisibility();
        assertEquals(NO_PERMS, PermissionUtils.getPermissions(null, visibility, true));
    }

    @Test
    public void testEmptyFormalVisibility() {
        final Visibility visibility = createVisibility();
        visibility.unsetFormalVisibility();

        final Authorizations auths = createAuths(
                Sets.newHashSet("U", "C", "S", "TS", "USA"), Sets.newHashSet("Foo", "Bar"), Sets.newHashSet(56L));

        assertEquals(
                EnumSet.of(Permission.READ, Permission.DISCOVER),
                PermissionUtils.getPermissions(auths, visibility, false));

        assertEquals(
                EnumSet.of(Permission.READ, Permission.DISCOVER),
                PermissionUtils.getPermissions(auths, visibility, true));
    }

    @Test
    public void testEmptyFormalAuths() {
        final Visibility visibility = createVisibility();
        final Authorizations auths = createAuths(null, Sets.newHashSet("Foo", "Bar"), Sets.newHashSet(56L));

        assertEquals(
                EnumSet.of(Permission.READ, Permission.DISCOVER),
                PermissionUtils.getPermissions(auths, visibility, false));

        assertEquals(NO_PERMS, PermissionUtils.getPermissions(auths, visibility, true));
    }

    @Test
    public void testNullMarkings() {
        final Visibility visibility = createVisibility();
        visibility.unsetAdvancedMarkings();

        final Authorizations auths = createAuths(
                Sets.newHashSet("U", "C", "S", "TS", "USA"), Sets.newHashSet("Foo", "Bar"),
                Sets.newHashSet(3L, 56L, 2785L, 123876592237L));

        assertEquals(ALL_PERMS, PermissionUtils.getPermissions(auths, visibility, true));
    }

    @Test
    public void testNullPlatformObjectVisibilities() {
        final Visibility visibility = createVisibility();
        visibility.getAdvancedMarkings().unsetPlatformObjectVisibility();

        final Authorizations auths = createAuths(
                Sets.newHashSet("U", "C", "S", "TS", "USA"), Sets.newHashSet("Foo", "Bar"),
                Sets.newHashSet(3L, 56L, 2785L, 123876592237L));

        assertEquals(ALL_PERMS, PermissionUtils.getPermissions(auths, visibility, true));
    }

    @Test
    public void testNullPlatformObjectAuths() {
        final Visibility visibility = createVisibility();

        final Authorizations auths = createAuths(
                Sets.newHashSet("U", "C", "S", "TS", "USA"), Sets.newHashSet("Foo", "Bar"), new HashSet<Long>());

        assertEquals(NO_PERMS, PermissionUtils.getPermissions(auths, visibility, true));
    }

    @Test
    public void testEmptyPlatformObjectAuths() {
        final Visibility visibility = createVisibility();
        visibility.getAdvancedMarkings().setPlatformObjectVisibility(
                new PlatformObjectVisibilities().setPlatformObjectDiscoverVisibility(Collections.<Long>emptySet())
                        .setPlatformObjectManageVisibility(Collections.<Long>emptySet())
                        .setPlatformObjectReadVisibility(Collections.<Long>emptySet())
                        .setPlatformObjectWriteVisibility(Collections.<Long>emptySet()));

        final Authorizations auths = createAuths(
                Sets.newHashSet("U", "C", "S", "TS", "USA"), Sets.newHashSet("Foo", "Bar"), new HashSet<Long>());

        assertEquals(ALL_PERMS, PermissionUtils.getPermissions(auths, visibility, true));
    }

    @Test
    public void testNullPlatformObjectAuthsAndNullLeafVisibilities() {
        final Visibility visibility = createVisibility();
        visibility.getAdvancedMarkings().setPlatformObjectVisibility(new PlatformObjectVisibilities());

        final Authorizations auths = createAuths(
                Sets.newHashSet("U", "C", "S", "TS", "USA"), Sets.newHashSet("Foo", "Bar"), new HashSet<Long>());

        assertEquals(ALL_PERMS, PermissionUtils.getPermissions(auths, visibility, true));
    }

    @Test
    public void testValidateFormalAuths() {
        assertTrue(PermissionUtils.validateVisibilityExpression(null, null));
        assertTrue(PermissionUtils.validateVisibilityExpression(Sets.newHashSet("U", "C", "UK"), null));
        assertTrue(PermissionUtils.validateVisibilityExpression(Sets.newHashSet("U", "C", "UK"), ""));
        assertFalse(PermissionUtils.validateVisibilityExpression(null, "TS&USA"));
        assertFalse(PermissionUtils.validateVisibilityExpression(new HashSet<String>(), "TS&USA"));
        assertFalse(PermissionUtils.validateVisibilityExpression(Sets.newHashSet("U", "C", "UK"), "TS&USA"));
        assertTrue(PermissionUtils.validateVisibilityExpression(Sets.newHashSet("U", "C", "UK"), "U"));
        assertTrue(PermissionUtils.validateVisibilityExpression(Sets.newHashSet("U", "C", "S", "TS", "USA"), "U"));

        assertTrue(
                PermissionUtils.validateVisibilityExpression(Sets.newHashSet("U", "C", "S", "TS", "USA"), "TS&USA"));

        assertFalse(PermissionUtils.validateVisibilityExpression(Sets.newHashSet("U", "C", "S", "TS", "USA"), "FOO"));
        assertTrue(PermissionUtils.validateVisibilityExpression(Sets.newHashSet("U", "C", "S", "TS", "USA"), "U|USA"));
        assertTrue(PermissionUtils.validateVisibilityExpression(Sets.newHashSet("U", "C", "S", "TS"), "U|USA"));
        assertFalse(PermissionUtils.validateVisibilityExpression(Sets.newHashSet("C", "S", "TS"), "U|USA"));
    }

    @Test
    public void testGenerateExpression_OnlyFormal() {
        final Visibility visibility = new Visibility().setFormalVisibility("TS&AB&CD");
        assertEquals("TS&AB&CD", PermissionUtils.getVisibilityString(visibility));
    }

    @Test
    public void testGenerateExpression_OnlyExternal() {
        final Visibility visibility = new Visibility().setAdvancedMarkings(
                new AdvancedMarkings().setExternalCommunityVisibility("COM1&COM2"));

        assertEquals("COM1&COM2", PermissionUtils.getVisibilityString(visibility));
    }

    @Test
    public void testGenerateExpression_BothStrings() {
        final Visibility visibility = new Visibility().setFormalVisibility("TS&AB&CD").setAdvancedMarkings(
                new AdvancedMarkings().setExternalCommunityVisibility("COM1&COM2"));

        assertEquals("(TS&AB&CD)&(COM1&COM2)", PermissionUtils.getVisibilityString(visibility));
    }

    @Test
    public void testGenerateExpression_FormalIsEmpty() {
        final Visibility visibility = new Visibility().setFormalVisibility("").setAdvancedMarkings(
                new AdvancedMarkings().setExternalCommunityVisibility("COM1&COM2"));

        assertEquals("COM1&COM2", PermissionUtils.getVisibilityString(visibility));
    }

    @Test
    public void testGenerateExpression_ExternalIsEmpty() {
        final Visibility visibility = new Visibility().setFormalVisibility("TS&AB&CD").setAdvancedMarkings(
                new AdvancedMarkings().setExternalCommunityVisibility(""));

        assertEquals("TS&AB&CD", PermissionUtils.getVisibilityString(visibility));
    }
}
