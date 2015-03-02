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

import java.util.ArrayList;

import java.util.List;
import java.util.Random;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.carrotsearch.junitbenchmarks.AbstractBenchmark;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import ezbake.base.thrift.AdvancedMarkings;
import ezbake.base.thrift.Authorizations;
import ezbake.base.thrift.PlatformObjectVisibilities;
import ezbake.base.thrift.Visibility;
import ezbake.security.test.PermTestHelpers;

/**
 * Tests performance between the {@link ezbake.security.permissions.PermissionEvaluator} and directly using {@link
 * ezbake.security.permissions.PermissionUtils}.  Also verifies that the output is the same between the two.
 * <p/>
 * {@link ezbake.base.thrift.Visibility} objects used in tests are assigned one of ten formal visibilities at random,
 * against which the {@link ezbake.base.thrift.Authorizations} used will pass a formal visibility check (from
 * PermissionUtils) half of the time (leading to another check against {@link ezbake.base.thrift.AdvancedMarkings}). A
 * varying number of different AdvancedMarkings are used in the tests, also assigned at random, which may affect the
 * impact of caching in PermissionEvaluator. Visibilities are effectively evaluated in a random order.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PermissionEvaluatorTest extends AbstractBenchmark {
    /**
     * Number of visibility objects to test.
     */
    private static final int NUM_VISIBIILITY_OBJECTS = 250000;

    /**
     * A high number of different AdvancedMarkings, relative to what might be seen in a 'typical' dataset/query.
     */
    private static final int HIGH_NUM_ADV_VARIATIONS = 1000;

    /**
     * A medium number of different AdvancedMarkings, relative to what might be seen in a 'typical' dataset/query.
     */
    private static final int MEDIUM_NUM_ADV_VARIATIONS = 250;

    /**
     * A small number of different AdvancedMarkings, relative to what might be seen in a 'typical' dataset/query.
     */
    private static final int SMALL_NUM_ADV_VARIATIONS = 50;

    /**
     * A list of visibility objects with a high number of different AdvancedMarkings.
     */
    private static final List<Visibility> highAdvVariations = new ArrayList<>();

    /**
     * A list of visibility objects with a medium number of different AdvancedMarkings.
     */
    private static final List<Visibility> mediumAdvVariations = new ArrayList<>();

    /**
     * A list of visibility objects with a low number of different AdvancedMarkings.
     */
    private static final List<Visibility> smallAdvVariations = new ArrayList<>();

    /**
     * A list of visibility objects with no AdvancedMarkings.
     */
    private static final List<Visibility> noAdvVariations = new ArrayList<>();

    /**
     * Common formal visibilities, with about the number that might be seen in a typical query.
     */
    private static final List<String> stringVisibilities = Lists.newArrayList(
            "A&B&(AUS|CAN|GBR|NZL|USA)", "A&B&USA", "C&(AUS|CAN|GBR|NZL|USA)", "C&USA", "C&D&(AUS|CAN|GBR|NZL|USA)",
            "C&D&USA", "C&D&(AUS|CAN|GBR|NZL|USA)", "E&D&USA", "E&D&F&(AUS|CAN|GBR|NZL|USA)", "E&D&F&USA");

    /**
     * Authorizations to use when testing PermissionsEvaluator.
     */
    private static Authorizations auths;

    /**
     * Build different lists of visibilities for use in metrics.
     */
    @BeforeClass
    public static void setUp() {
        setupVisibility(HIGH_NUM_ADV_VARIATIONS, highAdvVariations);
        setupVisibility(MEDIUM_NUM_ADV_VARIATIONS, mediumAdvVariations);
        setupVisibility(SMALL_NUM_ADV_VARIATIONS, smallAdvVariations);
        setupVisibility(0, noAdvVariations);
        auths = getAuths();
    }

    /**
     * Create an instance of PlatformObjectVisibilities.
     *
     * @param setLongs Numbers to set for a particular PlatformObjectVisibiility.
     * @return A newly constructed PlatformObjectVisibility.
     */
    private static PlatformObjectVisibilities createPlatformObjectVisibilities(Long... setLongs) {
        final PlatformObjectVisibilities pov = new PlatformObjectVisibilities();
        pov.setPlatformObjectReadVisibility(Sets.newHashSet(setLongs));
        pov.setPlatformObjectWriteVisibility(Sets.newHashSet(setLongs));
        pov.setPlatformObjectDiscoverVisibility(Sets.newHashSet(setLongs));
        pov.setPlatformObjectManageVisibility(Sets.newHashSet(setLongs));

        return pov;
    }

    /**
     * Prepares a randomly ordered list of Visibility objects based on the passed in values.
     * <p/>
     * If numDifferentPovs is zero, then no advanced markings will be added.
     *
     * @param numDifferentPovs The number of different PlatformObjectVisibilities to be included in this list.
     * @param visibilities The list of Visibility objects to add to.
     */
    private static void setupVisibility(int numDifferentPovs, List<Visibility> visibilities) {
        final Random random = new Random();
        for (int i = 0; i < NUM_VISIBIILITY_OBJECTS; i++) {
            final Visibility vis = new Visibility();
            vis.setFormalVisibility(stringVisibilities.get(random.nextInt(stringVisibilities.size() - 1)));

            if (numDifferentPovs > 0) {
                vis.setAdvancedMarkings(new AdvancedMarkings());
                vis.getAdvancedMarkings().setExternalCommunityVisibility(
                        stringVisibilities.get(
                                random.nextInt(stringVisibilities.size() - 1)));
                vis.getAdvancedMarkings().setPlatformObjectVisibility(
                        createPlatformObjectVisibilities(
                                (long) random.nextInt(
                                        numDifferentPovs / stringVisibilities.size())));
            }

            visibilities.add(vis);
        }
    }

    /**
     * Construct the Authorizations object to use for tests. These auths should satisfy half of the possible formal
     * visibilities it runs into.
     *
     * @return An Authorizations object that can be used for tests.
     */
    private static Authorizations getAuths() {
        final Set<String> auths = Sets.newHashSet("USA", "D", "C");
        final Set<String> externalCommunityAuths = Sets.newHashSet("USA", "D", "C");
        final Set<Long> advancedMarkingsAuths = Sets.newHashSet(1L, 2L, 3L);
        return PermTestHelpers.createAuths(auths, externalCommunityAuths, advancedMarkingsAuths);
    }

    /**
     * Test evaluation {@code auths} against {@code highAdvVariations} with the evaluator.
     */
    @Test
    public void testHighVariationEvaluatorMetrics() {
        final PermissionEvaluator evaluator = new PermissionEvaluator(auths);
        for (final Visibility v : highAdvVariations) {
            evaluator.getPermissions(v);
        }
    }

    /**
     * Test evaluation {@code auths} against {@code highAdvVariations} with PermissionUtils directly.
     */
    @Test
    public void testHighVariationStandardMetrics() {
        for (final Visibility v : highAdvVariations) {
            PermissionUtils.getPermissions(auths, v);
        }
    }

    /**
     * Test evaluation {@code auths} against {@code mediumAdvVariations} with the evaluator.
     */
    @Test
    public void testMediumVariationEvaluatorMetrics() {
        final PermissionEvaluator evaluator = new PermissionEvaluator(auths);
        for (final Visibility v : mediumAdvVariations) {
            evaluator.getPermissions(v);
        }
    }

    /**
     * Test evaluation {@code auths} against {@code mediumAdvVariations} with PermissionUtils directly.
     */
    @Test
    public void testMediumVariationStandardMetrics() {
        for (final Visibility v : mediumAdvVariations) {
            PermissionUtils.getPermissions(auths, v);
        }
    }

    /**
     * Test evaluation {@code auths} against {@code smallAdvVariations} with the evaluator.
     */
    @Test
    public void testSmallVariationEvaluatorMetrics() {
        final PermissionEvaluator evaluator = new PermissionEvaluator(auths);
        for (final Visibility v : smallAdvVariations) {
            evaluator.getPermissions(v);
        }
    }

    /**
     * Test evaluation {@code auths} against {@code smallAdvVariations} with PermissionUtils directly.
     */
    @Test
    public void testSmallVariationStandardMetrics() {
        for (final Visibility v : smallAdvVariations) {
            PermissionUtils.getPermissions(auths, v);
        }
    }

    /**
     * Test evaluation with no advanced markings.
     */
    @Test
    public void testNoAdvancedMarkingsEvaluatorMetrics() {
        final PermissionEvaluator evaluator = new PermissionEvaluator(auths);
        for (final Visibility v : noAdvVariations) {
            evaluator.getPermissions(v);
        }
    }

    /**
     * Test evaluation with no advanced markings with PermissionUtils directly.
     */
    @Test
    public void testNoAdvancedMarkingsStandardMetrics() {
        for (final Visibility v : noAdvVariations) {
            PermissionUtils.getPermissions(auths, v);
        }
    }

    /**
     * Ensure that output from the evaluator is the same as output from PermissionUtils.
     */
    @Test
    public void testValuesFromEvaluatorExpected() {
        final PermissionEvaluator evaluator = new PermissionEvaluator(auths);
        for (final Visibility v : smallAdvVariations) {
            assertEquals(PermissionUtils.getPermissions(auths, v), evaluator.getPermissions(v));
        }
    }

    /**
     * Null/blank formal visibility and null advanced markings should be supported in the evaluator.
     */
    @Test
    public void testEmptyVisibilitiesEvaluatorMetrics() {
        final PermissionEvaluator evaluator = new PermissionEvaluator(auths);
        for (int i = 0; i < NUM_VISIBIILITY_OBJECTS; i++) {
            evaluator.getPermissions(new Visibility());
        }
    }

    @Test
    public void testEmptyVisibilitiesStandardMetrics() {
        for (int i = 0; i < NUM_VISIBIILITY_OBJECTS; i++) {
            PermissionUtils.getPermissions(auths, new Visibility());
        }
    }
}
