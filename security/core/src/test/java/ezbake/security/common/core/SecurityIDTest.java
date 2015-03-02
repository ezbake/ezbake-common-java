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

import org.junit.Assert;
import org.junit.Test;

/**
 * User: jhastings
 * Date: 7/8/14
 * Time: 2:20 PM
 */
public class SecurityIDTest {

    @Test
    public void reservedIDIncludesWords() {
        Assert.assertTrue(SecurityID.isReserved("_Ez_Security"));
    }

    @Test
    public void reservedIDIncludesRange() {
        Assert.assertTrue(SecurityID.isReserved("0"));
        Assert.assertTrue(SecurityID.isReserved("00"));
        Assert.assertTrue(SecurityID.isReserved("000"));
        Assert.assertTrue(SecurityID.isReserved("01"));
        Assert.assertTrue(SecurityID.isReserved("001"));
        Assert.assertTrue(SecurityID.isReserved("0001"));
        Assert.assertTrue(SecurityID.isReserved("00001"));
        Assert.assertTrue(SecurityID.isReserved("1"));
        Assert.assertTrue(SecurityID.isReserved("10"));
        Assert.assertTrue(SecurityID.isReserved("100"));
        Assert.assertTrue(SecurityID.isReserved("999"));
        Assert.assertTrue(!SecurityID.isReserved("1000"));
        Assert.assertFalse(SecurityID.isReserved("lkajsdflkjdsf"));
    }

    @Test
    public void testIsSecurityId() {
        for (SecurityID.ReservedSecurityId id : SecurityID.ReservedSecurityId.values()) {
            Assert.assertTrue(SecurityID.isSecurityId(id.toString()));
            Assert.assertTrue(SecurityID.isSecurityId(id.getId()));
            Assert.assertTrue(SecurityID.isSecurityId(id.getCn()));
        }
        Assert.assertTrue(SecurityID.isSecurityId("_Ez_System_App"));
        Assert.assertTrue(SecurityID.isSecurityId(Long.toString(Long.MAX_VALUE)));

        Assert.assertFalse(SecurityID.isSecurityId("not a security id"));
        Assert.assertFalse(SecurityID.isSecurityId("939393securityid"));
        Assert.assertFalse(SecurityID.isSecurityId("asdlfksjf9393992929"));
    }

}
