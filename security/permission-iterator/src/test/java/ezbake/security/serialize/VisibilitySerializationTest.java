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

package ezbake.security.serialize;

import com.google.common.collect.Sets;
import ezbake.base.thrift.AdvancedMarkings;
import ezbake.base.thrift.PlatformObjectVisibilities;
import ezbake.base.thrift.Visibility;
import ezbake.security.serialize.thrift.VisibilityWrapper;
import ezbake.thrift.ThriftUtils;
import org.apache.accumulo.core.data.Value;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.Arrays;
import java.util.Random;

public class VisibilitySerializationTest {
    public static PlatformObjectVisibilities createPlatformObjectVisibilities() throws Exception {
        final PlatformObjectVisibilities pov = new PlatformObjectVisibilities();
        pov.setPlatformObjectReadVisibility(Sets.newHashSet(56L, 2785L, 123876592237L));
        pov.setPlatformObjectWriteVisibility(Sets.newHashSet(2785L, 123876592237L));
        pov.setPlatformObjectDiscoverVisibility(Sets.newHashSet(3L, 56L, 2785L, 123876592237L));
        pov.setPlatformObjectManageVisibility(Sets.newHashSet(123876592237L));

        return pov;
    }

    public static AdvancedMarkings createAdvancedMarkings() throws Exception {
        final AdvancedMarkings markings = new AdvancedMarkings();
        markings.setComposite(true);
        markings.setExternalCommunityVisibility("Foo&(Bar|Baz)");
        markings.setId(18657294732947L);
        markings.setPlatformObjectVisibility(createPlatformObjectVisibilities());
        markings.setPurgeIds(Sets.newHashSet(87L, 9723957L));

        return markings;
    }

    public static Visibility createVisibility() throws Exception {
        final Visibility visibility = new Visibility();
        visibility.setFormalVisibility("TS&USA");
        visibility.setAdvancedMarkings(createAdvancedMarkings());

        return visibility;
    }

    @Test
    public void testSerialize() throws Exception {
        final Visibility visibility = new Visibility();
        final byte[] data = "Test Data".getBytes();

        final byte[] serialized = VisibilitySerialization.serializeVisibilityWithData(visibility, data);
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(serialized);
                DataInputStream inputStream = new DataInputStream(byteArrayInputStream)) {
            // Read Visibility Length, Read Visibility object and check against expected
            final int length = inputStream.readInt();
            final byte[] serializedVisibility = new byte[length];
            inputStream.readFully(serializedVisibility, 0, length);
            assertEquals(visibility, ThriftUtils.deserialize(Visibility.class, serializedVisibility));

            // Read raw length and check against expected
            final int rawLength = inputStream.readInt();
            assertEquals(data.length, rawLength);

            // Read raw and check against expected
            final byte[] returnedRawData = new byte[data.length];
            inputStream.read(returnedRawData, 0, rawLength);
            assertTrue(Arrays.equals(data, returnedRawData));
        }
    }

    @Test
    public void testSerializeValue() throws Exception {
        final Visibility visibility = createVisibility();
        final byte[] data = "Test Data".getBytes();

        final Value value = VisibilitySerialization.serializeVisibilityWithDataToValue(visibility, data);
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(value.get());
                DataInputStream inputStream = new DataInputStream(byteArrayInputStream)) {
            // Read Visibility Length, Read Visibility object and check against expected
            final int length = inputStream.readInt();
            final byte[] serializedVisibility = new byte[length];
            inputStream.readFully(serializedVisibility, 0, length);
            assertEquals(visibility, ThriftUtils.deserialize(Visibility.class, serializedVisibility));

            // Read raw length and check against expected
            final int rawLength = inputStream.readInt();
            assertEquals(data.length, rawLength);

            // Read raw and check against expected
            final byte[] returnedRawData = new byte[data.length];
            inputStream.read(returnedRawData, 0, rawLength);
            assertTrue(Arrays.equals(data, returnedRawData));
        }
    }

    public void testDeserialize() throws Exception {
        final Visibility visibility = createVisibility();
        final byte[] data = "Test Data".getBytes();

        // Serialize everything
        final byte[] serializedData = VisibilitySerialization.serializeVisibilityWithData(visibility, data);

        // Check deserializations
        assertTrue(Arrays.equals(data, VisibilitySerialization.deserializeVisibilityWrappedBytes(serializedData)
                .getValue()));

        assertEquals(visibility, VisibilitySerialization.deserializeVisibilityWrappedBytes(serializedData)
                .getVisibilityMarkings());
    }

    @Test
    public void testDeserializeValue() throws Exception {
        final Visibility visibility = createVisibility();
        final byte[] data = "Test Data".getBytes();

        // Serialize everything
        final Value value = VisibilitySerialization.serializeVisibilityWithDataToValue(visibility, data);

        // Check deserializations
        assertTrue(Arrays.equals(data, VisibilitySerialization.deserializeVisibilityWrappedValue(value).getValue()));

        assertEquals(visibility, VisibilitySerialization.deserializeVisibilityWrappedValue(value)
                .getVisibilityMarkings());
    }

    @Test
    public void testLargeSerialize() throws Exception {
        final int size = 127000000;
        final byte[] vc = new byte[size];
        new Random().nextBytes(vc);

        final Visibility visibility = new Visibility();
        final Value val = VisibilitySerialization.serializeVisibilityWithDataToValue(visibility, vc);
        final VisibilityWrapper wrapper = VisibilitySerialization.deserializeVisibilityWrappedBytes(val.get());

        assertEquals(vc.length, wrapper.getValue().length);
        assertArrayEquals(vc, wrapper.getValue());
    }
}
