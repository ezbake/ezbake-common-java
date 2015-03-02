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

import org.apache.commons.codec.binary.Base64;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;

public class Base64Serializer implements ThriftSerializer<String> {
    private final ThriftSerializer<byte[]> serializer;
    
    public Base64Serializer() {
        this(new BinarySerializer());
    }
    
    public Base64Serializer(ThriftSerializer<byte[]> serializer) {
        this.serializer = serializer;
    }

    @Override
    public String serialize(TBase<?,?> thriftObject) throws TException {
        return Base64.encodeBase64String(serializer.serialize(thriftObject));
    }

    @Override
    public <U extends TBase<?,?>> U deserialize(Class<U> thriftClass, String serializedObject) throws TException {
        return serializer.deserialize(thriftClass, Base64.decodeBase64(serializedObject));
    }
}