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

package ezbakehelpers.accumulo;

import org.apache.accumulo.core.client.*;

public class NamespacedMultiTableBatchWriter implements MultiTableBatchWriter {
    private MultiTableBatchWriter writer;
    private String namespace;

    public NamespacedMultiTableBatchWriter(MultiTableBatchWriter writer, String namespace) {
        this.writer = writer;
        this.namespace = namespace;
    }

    @Override
    public BatchWriter getBatchWriter(String table) throws AccumuloException, AccumuloSecurityException, TableNotFoundException {
        return writer.getBatchWriter(NamespaceUtil.getFullTableName(namespace, table));
    }

    @Override
    public void flush() throws MutationsRejectedException {
        writer.flush();
    }

    @Override
    public void close() throws MutationsRejectedException {
        writer.close();
    }

    @Override
    public boolean isClosed() {
        return writer.isClosed();
    }
}
