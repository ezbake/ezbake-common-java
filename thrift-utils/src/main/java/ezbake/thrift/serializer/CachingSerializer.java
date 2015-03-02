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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.Arrays;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;

public class CachingSerializer<T> implements ThriftSerializer<T> {
    private static final short cacheSize = 500;
    private final ThriftSerializer<T> subSerializer;

    private LoadingCache<ClassSerialPair<T>,TBase<?,?>> deserializerCache = CacheBuilder.newBuilder().maximumSize(cacheSize).build(
        new CacheLoader<ClassSerialPair<T>,TBase<?,?>>() {
            @Override
            public TBase<?,?> load(ClassSerialPair<T> classSerialPair) throws TException {
                return subSerializer.deserialize(classSerialPair.getThriftClass(),classSerialPair.getSerializedObject());
            }
        });

    public CachingSerializer(ThriftSerializer<T> serializer) {
        subSerializer = serializer;
    }

    @Override
    public T serialize(TBase<?,?> thriftObject) throws TException {
        T serializedObject = subSerializer.serialize(thriftObject);
        deserializerCache.put(new ClassSerialPair<T>(thriftObject.getClass(),serializedObject), thriftObject);
        return serializedObject;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <U extends TBase<?,?>> U deserialize(Class<U> thriftClass, T serializedObject) throws TException {
        return (U)deserializerCache.getUnchecked(new ClassSerialPair<T>(thriftClass,serializedObject));
    }

    private static class ClassSerialPair<T> {
        private final T serializedObject;
        private final Class<? extends TBase<?,?>> thriftClass;
        private final int hashCode;
        private final ArrayType arrayType;

        public <U extends TBase<?,?>> ClassSerialPair(Class<U> thriftClass, T serializedObject) throws TException {
            this.thriftClass = thriftClass;
            this.serializedObject = serializedObject;
            HashCodeBuilder builder = new HashCodeBuilder(7,99).append(thriftClass.hashCode());
            /* We don't care if the subobjects are the same, we care if their value is. But when your generic can be an array, funny things happen */
            if (serializedObject instanceof boolean[]) {
                arrayType = ArrayType.BOOLEAN;
                builder.append(Arrays.hashCode((boolean[])serializedObject));
            } else if (serializedObject instanceof byte[]) {
                arrayType = ArrayType.BYTE;
                builder.append(Arrays.hashCode((byte[])serializedObject));
            } else if (serializedObject instanceof char[]) {
                arrayType = ArrayType.CHAR;
                builder.append(Arrays.hashCode((char[])serializedObject));
            } else if (serializedObject instanceof double[]) {
                arrayType = ArrayType.DOUBLE;
                builder.append(Arrays.hashCode((double[])serializedObject));
            } else if (serializedObject instanceof int[]) {
                arrayType = ArrayType.INT;
                builder.append(Arrays.hashCode((int[])serializedObject));
            } else if (serializedObject instanceof float[]) {
                arrayType = ArrayType.FLOAT;
                builder.append(Arrays.hashCode((float[])serializedObject));
            } else if (serializedObject instanceof long[]) {
                arrayType = ArrayType.LONG;
                builder.append(Arrays.hashCode((long[])serializedObject));
            } else if (serializedObject instanceof short[]) {
                arrayType = ArrayType.SHORT;
                builder.append(Arrays.hashCode((short[])serializedObject));
            } else if (serializedObject instanceof Object[]) {
                arrayType = ArrayType.OBJECT;
                builder.append(Arrays.hashCode((Object[])serializedObject));
            } else {
                arrayType = ArrayType.NONE;
                builder.append(serializedObject.hashCode());
            }
            hashCode = builder.build();
        }

        public T getSerializedObject() {
            return serializedObject;
        }

        @SuppressWarnings("unchecked")
        public <U extends TBase<?,?>> Class<U> getThriftClass() {
            return (Class<U>)thriftClass;
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean equals(Object other) {
            if (!(other instanceof CachingSerializer.ClassSerialPair)) {
                return false;
            }
            ClassSerialPair<T> cspair = (ClassSerialPair<T>)other;
            if (!this.thriftClass.equals(cspair.getThriftClass())) { // class type mismatch, not equal
                return false;
            }
            /* We don't care if the subobjects are the same, we care if their value is. But when your generic can be an array, funny things happen */
            switch (arrayType) {
            case BOOLEAN:
                if (cspair.getSerializedObject() instanceof boolean[]) {
                    return Arrays.equals((boolean[])this.getSerializedObject(),(boolean[])cspair.getSerializedObject());
                }
                break;
            case BYTE:
                if (cspair.getSerializedObject() instanceof byte[]) {
                    return Arrays.equals((byte[])this.getSerializedObject(),(byte[])cspair.getSerializedObject());
                }
                break;
            case CHAR:
                if (cspair.getSerializedObject() instanceof char[]) {
                    return Arrays.equals((char[])this.getSerializedObject(),(char[])cspair.getSerializedObject());
                }
                break;
            case DOUBLE:
                if (cspair.getSerializedObject() instanceof double[]) {
                    return Arrays.equals((double[])this.getSerializedObject(),(double[])cspair.getSerializedObject());
                }
                break;
            case INT:
                if (cspair.getSerializedObject() instanceof int[]) {
                    return Arrays.equals((int[])this.getSerializedObject(),(int[])cspair.getSerializedObject());
                }
                break;
            case FLOAT:
                if (cspair.getSerializedObject() instanceof float[]) {
                    return Arrays.equals((float[])this.getSerializedObject(),(float[])cspair.getSerializedObject());
                }
                break;
            case LONG:
                if (cspair.getSerializedObject() instanceof long[]) {
                    return Arrays.equals((long[])this.getSerializedObject(),(long[])cspair.getSerializedObject());
                }
                break;
            case SHORT:
                if (cspair.getSerializedObject() instanceof short[]) {
                    return Arrays.equals((short[])this.getSerializedObject(),(short[])cspair.getSerializedObject());
                }
                break;
            case OBJECT:
                if (cspair.getSerializedObject() instanceof Object[]) {
                    return Arrays.equals((Object[])this.getSerializedObject(),(Object[])cspair.getSerializedObject());
                }
                break;
            case NONE:
                return this.serializedObject.equals(cspair.getSerializedObject()); // all other types use standard class equivalency
            }
            return false;
        }

        private enum ArrayType {
            BOOLEAN, BYTE, CHAR, DOUBLE, INT, FLOAT, LONG, SHORT, OBJECT, NONE
        }
    }
}
