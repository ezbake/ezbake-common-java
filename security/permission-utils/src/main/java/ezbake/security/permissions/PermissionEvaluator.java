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

import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import ezbake.base.thrift.AdvancedMarkings;
import ezbake.base.thrift.Authorizations;
import ezbake.base.thrift.Permission;
import ezbake.base.thrift.PlatformObjectVisibilities;
import ezbake.base.thrift.Visibility;

/**
 * Used for evaluating bulk {@link ezbake.base.thrift.Visibility} objects against a particular {@link
 * ezbake.base.thrift.Authorizations} object. PermissionEvaluator caches the results of each comparison to allow faster
 * evaluation. All evaluation operations are performed with {@link ezbake.security.permissions.PermissionUtils}.
 */
public class PermissionEvaluator {
    /**
     * Default size for caches in this class.
     */
    private static final int DEFAULT_CACHE_SIZE = 500;

    /**
     * The Authorizations object to compare with Visibility objects.
     */
    private final Authorizations auths;

    /**
     * Keeps track of the result of the comparison of {@code auths} with the important parts of a {@link
     * ezbake.base.thrift.AdvancedMarkings} object.
     */
    private LoadingCache<AdvMarkingComponents, Set<Permission>> advancedMarkingsEvaluations;

    /**
     * Keeps track of the result of the comparison of {@code authsFormal} with a particular formal visibility.
     */
    private LoadingCache<String, Boolean> formalVisibilityEvaluations;

    /**
     * Construct a new PermissionEvaluator.
     *
     * @param auths The Authorizations object against which to evaluate Visibility objects. Methods on this class are
     * thread safe.
     * @param cacheSize The number of visibilities to cache while performing evaluations.
     */
    public PermissionEvaluator(Authorizations auths, int cacheSize) {
        this.auths = auths;
        constructCaches(cacheSize);
    }

    /**
     * Construct a new PermissionEvaluator, this constructor uses the default cache size. Cache size can be configured
     * via an overloaded constructor.
     *
     * @param auths The Authorizations object against which to evaluate Visibility objects. Methods on this class are
     * thread safe.
     */
    public PermissionEvaluator(Authorizations auths) {
        this(auths, DEFAULT_CACHE_SIZE);
    }

    /**
     * Evaluate a Visibility object against this object's authorizations and cache the result. This method is thread
     * safe.
     *
     * @param visibility The Visibility to evaluate against this objects Authorizations.
     * @return The permissions this evaluator's Authorizations have against the passed in Visibility.
     */
    public Set<Permission> getPermissions(Visibility visibility) {
        String formalVisibility = visibility.getFormalVisibility();

        if (StringUtils.isBlank(formalVisibility) || formalVisibilityEvaluations.getUnchecked(formalVisibility)) {
            AdvancedMarkings advancedMarkings = visibility.getAdvancedMarkings();
            if (advancedMarkings == null) {
                return PermissionUtils.ALL_PERMS;
            }

            return advancedMarkingsEvaluations.getUnchecked(
                    new AdvMarkingComponents(
                            visibility.getAdvancedMarkings().externalCommunityVisibility,
                            visibility.getAdvancedMarkings().getPlatformObjectVisibility()));
        } else {
            return PermissionUtils.NO_PERMS;
        }
    }

    /**
     * Builds the caches this class uses while performing evaluations.
     *
     * @param cacheSize The maximum size of these caches. Evictions are based on frequency with least used being removed
     * first.
     */
    private void constructCaches(int cacheSize) {
        advancedMarkingsEvaluations = CacheBuilder.newBuilder().maximumSize(cacheSize).build(
                new CacheLoader<AdvMarkingComponents, Set<Permission>>() {
                    private final Visibility vis = new Visibility().setAdvancedMarkings(new AdvancedMarkings());

                    @Override
                    public Set<Permission> load(AdvMarkingComponents amc) {
                        vis.getAdvancedMarkings().setPlatformObjectVisibility(amc.getPov());
                        vis.getAdvancedMarkings().setExternalCommunityVisibility(amc.getEcv());
                        return PermissionUtils.getPermissions(auths, vis, false);
                    }
                });

        formalVisibilityEvaluations = CacheBuilder.newBuilder().maximumSize(cacheSize).build(
                new CacheLoader<String, Boolean>() {
                    @Override
                    public Boolean load(String formalVisibility) {
                        return PermissionUtils
                                .validateVisibilityExpression(auths.getFormalAuthorizations(), formalVisibility);
                    }
                });
    }

    /**
     * This contains all the necessary components for comparing and Authorizations object to a Visibility object's
     * AdvancedMarkings. Can be used as a key in a Guava cache. Basically a POJO.
     */
    private static class AdvMarkingComponents {
        /**
         * External community visibility, should be from the same AdvancedMarkings object as {@code pov}.
         */
        private final String ecv;

        /**
         * PlatformObjectVisibilities, should be from the same AdvancedMarkings object as {@code ecv}.
         */
        private final PlatformObjectVisibilities pov;

        /**
         * Constructor to initialize this POJO's members.
         *
         * @param ecv Represents an AdvancedMarkings' 'External community visibility'.
         * @param pov A PlatformObjectVisibilities from an AdvancedMarking.
         */
        private AdvMarkingComponents(String ecv, PlatformObjectVisibilities pov) {
            this.ecv = ecv;
            this.pov = pov;
        }

        @Override
        public int hashCode() {
            int result = ecv != null ? ecv.hashCode() : 0;
            result = 31 * result + (pov != null ? pov.hashCode() : 0);
            return result;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final AdvMarkingComponents that = (AdvMarkingComponents) o;

            if (ecv != null ? !ecv.equals(that.ecv) : that.ecv != null) {
                return false;
            }
            if (pov != null ? !pov.equals(that.pov) : that.pov != null) {
                return false;
            }

            return true;
        }

        /**
         * Gets a PlatformObjectVisibilities.
         *
         * @return The PlatformObjectVisibilities contained by this object.
         */
        public PlatformObjectVisibilities getPov() {
            return pov;
        }

        /**
         * Gets a String that represents an external community visibility.
         *
         * @return The external community visibility String contained by this object.
         */
        public String getEcv() {
            return ecv;
        }
    }
}

