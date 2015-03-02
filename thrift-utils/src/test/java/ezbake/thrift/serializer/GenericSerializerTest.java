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

package ezbake.thrift.serializer;

import static org.junit.Assert.*;

import ezbake.base.thrift.AppInfo;

import org.apache.thrift.TException;
import org.junit.Test;

public class GenericSerializerTest {
    private final AppInfo testClass = new AppInfo("test");

    @Test
    public void BinarySerializationSucceedsTest() throws TException {
        final BinarySerializer serializerBinary = new BinarySerializer();
        final byte[] binarilySerialized = serializerBinary.serialize(testClass);
        final AppInfo binarilyDeserialized = serializerBinary.deserialize(AppInfo.class, binarilySerialized);
        assertEquals(testClass,binarilyDeserialized);
    }

    @Test
    public void Base64SerilizationSucceedsTest() throws TException {
        final Base64Serializer serializerBase64 = new Base64Serializer();
        final String base64Serialized = serializerBase64.serialize(testClass);
        final AppInfo base64Deserialized = serializerBase64.deserialize(AppInfo.class, base64Serialized);
        assertEquals(testClass,base64Deserialized);
    }

    @Test
    public void CachedBinarySerializationSucceedsTest() throws TException {
        final BinarySerializer subSerializer = new BinarySerializer();
        final CachingSerializer<byte[]> serializerBinaryCache = new CachingSerializer<byte[]>(subSerializer);
        final BinarySerializer serializerBinary = new BinarySerializer();
        final byte[] binarilySerialized = serializerBinary.serialize(testClass);
        final AppInfo binarilyDeserialized = serializerBinary.deserialize(AppInfo.class, binarilySerialized);
        byte[] binarilyCacheSerialized = serializerBinaryCache.serialize(testClass);
        assertArrayEquals(binarilySerialized,binarilyCacheSerialized);
        binarilyCacheSerialized = serializerBinaryCache.serialize(testClass);
        assertArrayEquals(binarilySerialized,binarilyCacheSerialized);
        assertEquals(binarilyDeserialized,serializerBinaryCache.deserialize(AppInfo.class, binarilyCacheSerialized));
    }

    @Test
    public void CachedBase64SerializationSucceedsTest() throws TException {
        final Base64Serializer subSerializer = new Base64Serializer();
        final CachingSerializer<String> serializerBase64Cache = new CachingSerializer<String>(subSerializer);
        final Base64Serializer serializerBase64 = new Base64Serializer();
        final String base64Serialized = serializerBase64.serialize(testClass);
        final AppInfo base64Deserialized = serializerBase64.deserialize(AppInfo.class, base64Serialized);
        String base64CacheSerialized = serializerBase64Cache.serialize(testClass);
        assertEquals(base64Serialized,base64CacheSerialized);
        base64CacheSerialized = serializerBase64Cache.serialize(testClass);
        assertEquals(base64Serialized,base64CacheSerialized);
        assertEquals(base64Deserialized,serializerBase64Cache.deserialize(AppInfo.class, base64CacheSerialized));
    }
}
