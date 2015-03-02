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

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import org.apache.accumulo.core.security.ColumnVisibility;
import org.apache.accumulo.core.security.VisibilityEvaluator;
import org.apache.accumulo.core.security.VisibilityParseException;
import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;

import ezbake.base.thrift.AdvancedMarkings;
import ezbake.base.thrift.Authorizations;
import ezbake.base.thrift.Permission;
import ezbake.base.thrift.PlatformObjectVisibilities;
import ezbake.base.thrift.Visibility;

/**
 * Utilities to calculate available permissions based on visibility of the data and authorizations of the user.
 */
public class PermissionUtils {
    /**
     * Set containing no permissions at all. This is useful for shorter permissions checks.
     */
    public static final Set<Permission> NO_PERMS = Collections.unmodifiableSet(EnumSet.noneOf(Permission.class));

    /**
     * Set containing all possible permissions. This is useful for shorter permissions checks.
     */
    public static final Set<Permission> ALL_PERMS = Collections.unmodifiableSet(EnumSet.allOf(Permission.class));

    /**
     * Get permissions for user (based their authorizations) against the data's visibility including formal auths.
     *
     * @param auths Authorizations of the user
     * @param visibility Visibility of the data
     * @return The set of permissions the user has for the data
     */
    public static Set<Permission> getPermissions(Authorizations auths, Visibility visibility) {
        return getPermissions(auths, visibility, true, ALL_PERMS);
    }

    /**
     * Get permissions for user (based their authorizations) against the data's visibility.
     *
     * @param auths Authorizations of the user
     * @param visibility Visibility of the data
     * @param validateFormalAuths true to validate formal authorizations, false to skip
     * @return The set of permissions the user has for the data
     */
    public static Set<Permission> getPermissions(
            Authorizations auths, Visibility visibility, boolean validateFormalAuths) {
        return getPermissions(auths, visibility, validateFormalAuths, ALL_PERMS);
    }

    /**
     * Get permissions for user (based their authorizations) against the data's visibility.
     *
     * @param auths Authorizations of the user
     * @param visibility Visibility of the data
     * @param validateFormalAuths true to validate formal authorizations, false to skip
     * @param subsetToCheck The subset of permissions to check (to avoid more bitvector operations than needed)
     * @return The set of permissions the user has for the data (a subset of the subsetToCheck)
     */
    public static Set<Permission> getPermissions(
            Authorizations auths, Visibility visibility, boolean validateFormalAuths, Set<Permission> subsetToCheck) {
        if (visibility == null) {
            return ALL_PERMS; // No visibility to check
        }

        if (auths == null) {
            return NO_PERMS; // Has visibility but no auths
        }

        if (validateFormalAuths && !validateVisibilityExpression(
                auths.getFormalAuthorizations(), visibility.getFormalVisibility())) {
            return NO_PERMS; // Formals auths check failed
        }

        final AdvancedMarkings markings = visibility.getAdvancedMarkings();
        if (markings == null) {
            return ALL_PERMS; // No further visibility to check
        }

        if (!validateVisibilityExpression(
                auths.getExternalCommunityAuthorizations(), markings.getExternalCommunityVisibility())) {
            return NO_PERMS; // External community auths check failed
        }

        final PlatformObjectVisibilities pov = markings.getPlatformObjectVisibility();
        if (pov == null) {
            return ALL_PERMS; // No further visibility to check
        }

        final Set<Long> authCheck = auths.getPlatformObjectAuthorizations();
        final Set<Permission> perms = EnumSet.noneOf(Permission.class);
        for (final Permission permToCheck : subsetToCheck) {
            Set<Long> permVisibility = null;
            switch (permToCheck) {
                case READ:
                    permVisibility = pov.getPlatformObjectReadVisibility();
                    break;
                case WRITE:
                    permVisibility = pov.getPlatformObjectWriteVisibility();
                    break;
                case MANAGE_VISIBILITY:
                    permVisibility = pov.getPlatformObjectManageVisibility();
                    break;
                case DISCOVER:
                    permVisibility = pov.getPlatformObjectDiscoverVisibility();
                    break;
                default:
                    throw new IllegalArgumentException("Unknown Permission enum value" + permToCheck);
            }

            // Null/Empty visibility means world-accessible, else check intersection
            if (permVisibility == null || permVisibility.isEmpty() || authCheck != null && !Sets
                    .intersection(authCheck, permVisibility).isEmpty()) {
                perms.add(permToCheck);
            }
        }

        return perms;
    }

    /**
     * Validate Accumulo-style visibility expression against a set of authorizations.
     *
     * @param auths Formal authorizations of the user
     * @param visibilityExpression Accumulo-style visibility expression
     * @return true if validation authorizations against visibility expression, false otherwise
     */
    public static boolean validateVisibilityExpression(Set<String> auths, String visibilityExpression) {
        if (StringUtils.isBlank(visibilityExpression)) {
            return true; // No visibility to check
        }

        if (auths == null || auths.isEmpty()) {
            return false; // Has visibility but no auths
        }

        final VisibilityEvaluator visEval = new VisibilityEvaluator(
                new org.apache.accumulo.core.security.Authorizations(
                        auths.toArray(new String[] {})));

        try {
            return visEval.evaluate(new ColumnVisibility(visibilityExpression));
        } catch (final VisibilityParseException e) {
            return false;
        }
    }

    /**
     * Generate Accumulo-style visibility expression from a Visibility object.
     *
     * @param visibility the visibility object to derive the expression from
     * @return the visibility expression computed from both the formal visibility string and community visibility string
     */
    public static String getVisibilityString(Visibility visibility) {
        String fullVisibility = "";
        if (visibility.isSetFormalVisibility() && !Strings.isNullOrEmpty(visibility.getFormalVisibility())) {
            fullVisibility = visibility.getFormalVisibility();
        }
        if (visibility.isSetAdvancedMarkings() && visibility.getAdvancedMarkings().isSetExternalCommunityVisibility()
                && !Strings.isNullOrEmpty(visibility.getAdvancedMarkings().getExternalCommunityVisibility())) {
            final String externalCommunityVisibility =
                    visibility.getAdvancedMarkings().getExternalCommunityVisibility();

            // If both the formal visibility and external community visibility are set, then they need to be ANDed
            if (!fullVisibility.isEmpty()) {
                fullVisibility = "(" + fullVisibility + ")&(" + externalCommunityVisibility + ")";
            } else {
                fullVisibility = externalCommunityVisibility;
            }
        }
        return fullVisibility;
    }
}
