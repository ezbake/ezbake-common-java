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
import org.apache.accumulo.core.client.admin.InstanceOperations;
import org.apache.accumulo.core.client.admin.NamespaceOperations;
import org.apache.accumulo.core.client.admin.SecurityOperations;
import org.apache.accumulo.core.client.admin.TableOperations;
import org.apache.accumulo.core.security.Authorizations;

public class NamespacedConnector extends Connector {
    private Connector connector;
    private String namespace;

    public NamespacedConnector(Connector connector, String namespace) {
        this.connector = connector;
        this.namespace = namespace;
    }

    @Override
    public BatchScanner createBatchScanner(String tableName, Authorizations authorizations, int numQueryThreads) throws TableNotFoundException {
        return connector.createBatchScanner(NamespaceUtil.getFullTableName(namespace, tableName), authorizations, numQueryThreads);
    }

    @Override
    public BatchDeleter createBatchDeleter(String tableName, Authorizations authorizations, int numQueryThreads, long maxMemory, long maxLatency, int maxWriteThreads) throws TableNotFoundException {
        return connector.createBatchDeleter(NamespaceUtil.getFullTableName(namespace, tableName), authorizations, numQueryThreads, maxMemory, maxLatency, maxWriteThreads);
    }

    @Override
    public BatchDeleter createBatchDeleter(String tableName, Authorizations authorizations, int numQueryThreads, BatchWriterConfig config) throws TableNotFoundException {
        return connector.createBatchDeleter(NamespaceUtil.getFullTableName(namespace, tableName), authorizations, numQueryThreads, config);
    }

    @Override
    public BatchWriter createBatchWriter(String tableName, long maxMemory, long maxLatency, int maxWriteThreads) throws TableNotFoundException {
        return connector.createBatchWriter(NamespaceUtil.getFullTableName(namespace, tableName), maxMemory, maxLatency, maxWriteThreads);
    }

    @Override
    public BatchWriter createBatchWriter(String tableName, BatchWriterConfig config) throws TableNotFoundException {
        return connector.createBatchWriter(NamespaceUtil.getFullTableName(namespace, tableName), config);
    }

    @Override
    public MultiTableBatchWriter createMultiTableBatchWriter(long maxMemory, long maxLatency, int maxWriteThreads) {
        return new NamespacedMultiTableBatchWriter(connector.createMultiTableBatchWriter(maxMemory, maxLatency, maxWriteThreads), namespace);
    }

    @Override
    public MultiTableBatchWriter createMultiTableBatchWriter(BatchWriterConfig config) {
        return new NamespacedMultiTableBatchWriter(connector.createMultiTableBatchWriter(config), namespace);
    }

    @Override
    public Scanner createScanner(String tableName, Authorizations authorizations) throws TableNotFoundException {
        return connector.createScanner(NamespaceUtil.getFullTableName(namespace, tableName), authorizations);
    }

    @Override
    public ConditionalWriter createConditionalWriter(String tableName, ConditionalWriterConfig config) throws TableNotFoundException {
        return connector.createConditionalWriter(NamespaceUtil.getFullTableName(namespace, tableName), config);
    }

    @Override
    public Instance getInstance() {
        return connector.getInstance();
    }

    @Override
    public String whoami() {
        return connector.whoami();
    }

    @Override
    public TableOperations tableOperations() {
        return new NamespacedTableOperations(connector.tableOperations(), namespace);
    }

    @Override
    public NamespaceOperations namespaceOperations() {
        return connector.namespaceOperations();
    }

    @Override
    public SecurityOperations securityOperations() {
        return new NamespacedSecurityOperations(connector.securityOperations(), namespace);
    }

    @Override
    public InstanceOperations instanceOperations() {
        return connector.instanceOperations();
    }
}
