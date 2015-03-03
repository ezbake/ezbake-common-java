package ezbake.common.ins;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class INSUtilityTest {
    @Test
    public void testSplitURIPrefix() throws Exception {
        String prefix = INSUtility.getUriPrefix("NEWS://myFeed/myUniqueId/can_have/slashes.xml");
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

        assertEquals("NEWS://myFeed/", prefix);
    }

    @Test
    public void testSplitURIPrefixDash() throws Exception {
        String prefix = INSUtility.getUriPrefix("\n" +
                "SOCIAL://dev-twitter/tweet/398872121918902272");
        assertEquals("SOCIAL://dev-twitter/", prefix);
    }

    @Test
    public void testSplitURIPrefixDot() throws Exception {
        String prefix = INSUtility.getUriPrefix("\n" +
                "SOCIAL://dev.twitter/tweet/398872121918902272");
        assertEquals("SOCIAL://dev.twitter/", prefix);
    }

    @Test
    public void testSplitURIPrefixOfPrefix() throws Exception {
        String prefix = INSUtility.getUriPrefix("\n" +
                "SOCIAL://dev-twitter");
        assertEquals("SOCIAL://dev-twitter/", prefix);
    }

    @Test
    public void testSplitURIPrefixRandom() throws Exception {
        String prefix = INSUtility.getUriPrefix("\n" +
                "SOCIAL://some-THING.1234/test/123-23.test");
        assertEquals("SOCIAL://some-THING.1234/", prefix);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSplitURIPrefixInvalid() throws Exception {
        INSUtility.getUriPrefix("myId/test");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSplitURIPrefixInvalid2() throws Exception {
        INSUtility.getUriPrefix("CATEGORY://myIdtests+");
    }

    @Test
    public void testBuildUriPrefix() {
        assertEquals("SOCIAL://twitter/", INSUtility.buildUriPrefix("SOCIAL", "twitter"));
    }

    @Test
    public void testSpaceInUri() {
        assertEquals("TRACKS://TRACK_FEED/", INSUtility.getUriPrefix("TRACKS://TRACK_FEED/TEST RECORD-123456789"));
    }
}
