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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.apache.accumulo.core.data.Value;
import org.apache.thrift.TException;

import ezbake.base.thrift.Visibility;
import ezbake.security.serialize.thrift.VisibilityWrapper;
import ezbake.thrift.ThriftUtils;

/**
 * BVSerialization contains a number of static functions that help with serializing data with it's visibility markings
 */
public class VisibilitySerialization {
    /**
     * This helper will serialize a visibility object and some binary data into a single binary array that can be used
     * to write the object to a database, specifically Accumulo.
     *
     * @param visibility visibility markings to be serialized with the data
     * @param rawData the raw binary data wrapped in an Accumulo Value
     * @return the serialized data
     * @throws java.io.IOException
     * @throws TException
     */
    public static Value serializeVisibilityWithDataToValue(Visibility visibility, byte[] rawData) throws IOException,
            TException {
        return new Value(serializeVisibilityWithData(visibility, rawData));
    }

    /**
     * This helper will serialize a visibility object and some binary data into a single binary array that can be used
     * to write the object to a database, specifically Accumulo.
     *
     * @param visibility visibility markings to be serialized with the data
     * @param rawData the raw binary data
     * @return the serialized data
     * @throws java.io.IOException
     * @throws TException
     */
    public static byte[] serializeVisibilityWithData(Visibility visibility, byte[] rawData) throws IOException,
            TException {
        // Ensure sizes are written out as a big endian int
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                DataOutputStream outputStream = new DataOutputStream(byteArrayOutputStream)) {
            final byte[] visibilityBytes = ThriftUtils.serialize(visibility);

            // write visibility size
            outputStream.writeInt(visibilityBytes.length);

            // write visibility bytes
            outputStream.write(visibilityBytes);

            // write rawData size
            outputStream.writeInt(rawData.length);

            // write rawData bytes
            outputStream.write(rawData);
            outputStream.flush();

            return byteArrayOutputStream.toByteArray();
        }
    }

    /**
     * This helper takes a Value, Accumulo's native data type for values, and deserializes it into a wrapped
     * visibility object. It will throw a IOException if it cannot deserialize the value.
     *
     * @param object a value that has been serialized with BVSerialization
     * @return a wrapper object with Visibility and a binary value
     * @throws java.io.IOException, ClassNotFoundException
     * @throws TException
     */
    public static VisibilityWrapper deserializeVisibilityWrappedValue(Value object) throws IOException, TException {
        return deserializeVisibilityWrappedBytes(object.get());
    }

    /**
     * This helper takes a byte array and deserializes it into a wrapped visibility object. It will throw a
     * IOException if it cannot deserialize the value.
     *
     * @param object a value that has been serialized with BVSerialization
     * @return a wrapper object with visibility and a binary value
     * @throws java.io.IOException, TException
     */
    public static VisibilityWrapper deserializeVisibilityWrappedBytes(byte[] object) throws IOException, TException {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(object);
                DataInputStream inputStream = new DataInputStream(byteArrayInputStream)) {
            // read visibility size, read rawData
            final int visibilityLength = inputStream.readInt();
            final byte[] serializedVisibility = new byte[visibilityLength];
            inputStream.readFully(serializedVisibility, 0, visibilityLength);

            // read rawData size, read rawData
            final int rawLength = inputStream.readInt();
            final byte[] returnedRawData = new byte[rawLength];
            inputStream.readFully(returnedRawData, 0, rawLength);

            final VisibilityWrapper vw = new VisibilityWrapper();
            vw.setVisibilityMarkings(ThriftUtils.deserialize(Visibility.class, serializedVisibility));
            vw.setValue(returnedRawData);

            return vw;
        }
    }
}
