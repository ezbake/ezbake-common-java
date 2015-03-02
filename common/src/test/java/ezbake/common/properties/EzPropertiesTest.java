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

package ezbake.common.properties;

import org.junit.Test;

import java.util.Properties;
import static org.junit.Assert.*;

public class EzPropertiesTest {

    @Test
    public void testMergeNoCollisions() throws DuplicatePropertyException {
        Properties p1 = new Properties();
        Properties p2 = new Properties();

        p1.setProperty("some.prop", "true");
        p2.setProperty("another.prop", "cool");

        EzProperties props = new EzProperties(p1, true);
        props.mergeProperties(p2, false);

        assertEquals("First prop is correct", "true", props.getProperty("some.prop"));
        assertEquals("Second prop is correct", "cool", props.getProperty("another.prop"));
    }

    @Test(expected = DuplicatePropertyException.class)
    public void testMerge_Collisions() throws DuplicatePropertyException {
        Properties p1 = new Properties();
        Properties p2 = new Properties();

        p1.setProperty("some.prop", "true");
        p2.setProperty("some.prop", "cool");

        EzProperties props = new EzProperties(p1, true);
        props.mergeProperties(p2, false);
    }
}
