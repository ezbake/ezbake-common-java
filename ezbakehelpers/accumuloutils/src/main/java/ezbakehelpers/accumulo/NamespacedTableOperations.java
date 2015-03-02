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

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.accumulo.core.client.*;
import org.apache.accumulo.core.client.admin.DiskUsage;
import org.apache.accumulo.core.client.admin.TableOperations;
import org.apache.accumulo.core.client.admin.TimeType;
import org.apache.accumulo.core.data.Range;
import org.apache.accumulo.core.iterators.IteratorUtil;
import org.apache.accumulo.core.security.Authorizations;
import org.apache.hadoop.io.Text;

import java.io.IOException;
import java.util.*;

public class NamespacedTableOperations implements TableOperations {
    private TableOperations operations;
    private String namespace;

    public NamespacedTableOperations(TableOperations operations, String namespace) {
        this.operations = operations;
        this.namespace = namespace;
    }

    @Override
    public SortedSet<String> list() {
        return operations.list();
    }

    @Override
    public boolean exists(String tableName) {
        return operations.exists(NamespaceUtil.getFullTableName(namespace, tableName));
    }

    @Override
    public void create(String tableName) throws AccumuloException, AccumuloSecurityException, TableExistsException {
        operations.create(NamespaceUtil.getFullTableName(namespace, tableName));
    }

    @Override
    public void create(String tableName, boolean limitVersion) throws AccumuloException, AccumuloSecurityException, TableExistsException {
        operations.create(NamespaceUtil.getFullTableName(namespace, tableName), limitVersion);
    }

    @Override
    public void create(String tableName, boolean versioningIter, TimeType timeType) throws AccumuloException, AccumuloSecurityException, TableExistsException {
        operations.create(NamespaceUtil.getFullTableName(namespace, tableName), versioningIter, timeType);
    }

    @Override
    public void importTable(String tableName, String importDir) throws TableExistsException, AccumuloException, AccumuloSecurityException {
        operations.importTable(NamespaceUtil.getFullTableName(namespace, tableName), importDir);
    }

    @Override
    public void exportTable(String tableName, String exportDir) throws TableNotFoundException, AccumuloException, AccumuloSecurityException {
        operations.exportTable(NamespaceUtil.getFullTableName(namespace, tableName), exportDir);
    }

    @Override
    public void addSplits(String tableName, SortedSet<Text> partitionKeys) throws TableNotFoundException, AccumuloException, AccumuloSecurityException {
        operations.addSplits(NamespaceUtil.getFullTableName(namespace, tableName), partitionKeys);
    }

    @Override
    public Collection<Text> getSplits(String tableName) throws TableNotFoundException {
        return operations.getSplits(NamespaceUtil.getFullTableName(namespace, tableName));
    }

    @Override
    public Collection<Text> listSplits(String tableName) throws TableNotFoundException, AccumuloSecurityException, AccumuloException {
        return operations.listSplits(NamespaceUtil.getFullTableName(namespace, tableName));
    }

    @Override
    public Collection<Text> getSplits(String tableName, int maxSplits) throws TableNotFoundException {
        return operations.getSplits(NamespaceUtil.getFullTableName(namespace, tableName), maxSplits);
    }

    @Override
    public Collection<Text> listSplits(String tableName, int maxSplits) throws TableNotFoundException, AccumuloSecurityException, AccumuloException {
        return operations.listSplits(NamespaceUtil.getFullTableName(namespace, tableName), maxSplits);
    }

    @Override
    public Text getMaxRow(String tableName, Authorizations auths, Text startRow, boolean startInclusive, Text endRow, boolean endInclusive) throws TableNotFoundException, AccumuloException, AccumuloSecurityException {
        return operations.getMaxRow(NamespaceUtil.getFullTableName(namespace, tableName), auths, startRow, startInclusive, endRow, endInclusive);
    }

    @Override
    public void merge(String tableName, Text start, Text end) throws AccumuloException, AccumuloSecurityException, TableNotFoundException {
        operations.merge(NamespaceUtil.getFullTableName(namespace, tableName), start, end);
    }

    @Override
    public void deleteRows(String tableName, Text start, Text end) throws AccumuloException, AccumuloSecurityException, TableNotFoundException {
        operations.deleteRows(NamespaceUtil.getFullTableName(namespace, tableName), start, end);
    }

    @Override
    public void compact(String tableName, Text start, Text end, boolean flush, boolean wait) throws AccumuloSecurityException, TableNotFoundException, AccumuloException {
        operations.compact(NamespaceUtil.getFullTableName(namespace, tableName), start, end, flush, wait);
    }

    @Override
    public void compact(String tableName, Text start, Text end, List<IteratorSetting> iterators, boolean flush, boolean wait) throws AccumuloSecurityException, TableNotFoundException, AccumuloException {
        operations.compact(NamespaceUtil.getFullTableName(namespace, tableName), start, end, iterators, flush, wait);
    }

    @Override
    public void cancelCompaction(String tableName) throws AccumuloSecurityException, TableNotFoundException, AccumuloException {
        operations.cancelCompaction(NamespaceUtil.getFullTableName(namespace, tableName));
    }

    @Override
    public void delete(String tableName) throws AccumuloException, AccumuloSecurityException, TableNotFoundException {
        operations.delete(NamespaceUtil.getFullTableName(namespace, tableName));
    }

    @Override
    public void clone(String srcTableName, String newTableName, boolean flush, Map<String, String> propertiesToSet, Set<String> propertiesToExclude) throws AccumuloException, AccumuloSecurityException, TableNotFoundException, TableExistsException {
        operations.clone(NamespaceUtil.getFullTableName(namespace, srcTableName), NamespaceUtil.getFullTableName(namespace, newTableName), flush, propertiesToSet, propertiesToExclude);
    }

    @Override
    public void rename(String oldTableName, String newTableName) throws AccumuloSecurityException, TableNotFoundException, AccumuloException, TableExistsException {
        operations.rename(NamespaceUtil.getFullTableName(namespace, oldTableName), NamespaceUtil.getFullTableName(namespace, newTableName));
    }

    @Override
    public void flush(String tableName) throws AccumuloException, AccumuloSecurityException {
        operations.flush(NamespaceUtil.getFullTableName(namespace, tableName));
    }

    @Override
    public void flush(String tableName, Text start, Text end, boolean wait) throws AccumuloException, AccumuloSecurityException, TableNotFoundException {
        operations.flush(NamespaceUtil.getFullTableName(namespace, tableName), start, end, wait);
    }

    @Override
    public void setProperty(String tableName, String property, String value) throws AccumuloException, AccumuloSecurityException {
        operations.setProperty(NamespaceUtil.getFullTableName(namespace, tableName), property, value);
    }

    @Override
    public void removeProperty(String tableName, String property) throws AccumuloException, AccumuloSecurityException {
        operations.removeProperty(NamespaceUtil.getFullTableName(namespace, tableName), property);
    }

    @Override
    public Iterable<Map.Entry<String, String>> getProperties(String tableName) throws AccumuloException, TableNotFoundException {
        return operations.getProperties(NamespaceUtil.getFullTableName(namespace, tableName));
    }

    @Override
    public void setLocalityGroups(String tableName, Map<String, Set<Text>> groups) throws AccumuloException, AccumuloSecurityException, TableNotFoundException {
        operations.setLocalityGroups(NamespaceUtil.getFullTableName(namespace, tableName), groups);
    }

    @Override
    public Map<String, Set<Text>> getLocalityGroups(String tableName) throws AccumuloException, TableNotFoundException {
        return operations.getLocalityGroups(NamespaceUtil.getFullTableName(namespace, tableName));
    }

    @Override
    public Set<Range> splitRangeByTablets(String tableName, Range range, int maxSplits) throws AccumuloException, AccumuloSecurityException, TableNotFoundException {
        return operations.splitRangeByTablets(NamespaceUtil.getFullTableName(namespace, tableName), range, maxSplits);
    }

    @Override
    public void importDirectory(String tableName, String dir, String failureDir, boolean setTime) throws TableNotFoundException, IOException, AccumuloException, AccumuloSecurityException {
        operations.importDirectory(NamespaceUtil.getFullTableName(namespace, tableName), dir, failureDir, setTime);
    }

    @Override
    public void offline(String tableName) throws AccumuloSecurityException, AccumuloException, TableNotFoundException {
        operations.offline(NamespaceUtil.getFullTableName(namespace, tableName));
    }

    @Override
    public void offline(String tableName, boolean wait) throws AccumuloSecurityException, AccumuloException, TableNotFoundException {
        operations.offline(NamespaceUtil.getFullTableName(namespace, tableName), wait);
    }

    @Override
    public void online(String tableName) throws AccumuloSecurityException, AccumuloException, TableNotFoundException {
        operations.online(NamespaceUtil.getFullTableName(namespace, tableName));
    }

    @Override
    public void online(String tableName, boolean wait) throws AccumuloSecurityException, AccumuloException, TableNotFoundException {
        operations.online(NamespaceUtil.getFullTableName(namespace, tableName), wait);
    }

    @Override
    public void clearLocatorCache(String tableName) throws TableNotFoundException {
        operations.clearLocatorCache(NamespaceUtil.getFullTableName(namespace, tableName));
    }

    @Override
    public Map<String, String> tableIdMap() {
        return Maps.filterEntries(operations.tableIdMap(), new Predicate<Map.Entry<String, String>>() {
            @Override
            public boolean apply(Map.Entry<String, String> input) {
                return input.getKey().startsWith(namespace);
            }
        });
    }

    @Override
    public void attachIterator(String tableName, IteratorSetting setting) throws AccumuloSecurityException, AccumuloException, TableNotFoundException {
        operations.attachIterator(NamespaceUtil.getFullTableName(namespace, tableName), setting);
    }

    @Override
    public void attachIterator(String tableName, IteratorSetting setting, EnumSet<IteratorUtil.IteratorScope> scopes) throws AccumuloSecurityException, AccumuloException, TableNotFoundException {
        operations.attachIterator(NamespaceUtil.getFullTableName(namespace, tableName), setting, scopes);
    }

    @Override
    public void removeIterator(String tableName, String name, EnumSet<IteratorUtil.IteratorScope> scopes) throws AccumuloSecurityException, AccumuloException, TableNotFoundException {
        operations.removeIterator(NamespaceUtil.getFullTableName(namespace, tableName), name, scopes);
    }

    @Override
    public IteratorSetting getIteratorSetting(String tableName, String name, IteratorUtil.IteratorScope scope) throws AccumuloSecurityException, AccumuloException, TableNotFoundException {
        return operations.getIteratorSetting(NamespaceUtil.getFullTableName(namespace, tableName), name, scope);
    }

    @Override
    public Map<String, EnumSet<IteratorUtil.IteratorScope>> listIterators(String tableName) throws AccumuloSecurityException, AccumuloException, TableNotFoundException {
        return operations.listIterators(NamespaceUtil.getFullTableName(namespace, tableName));
    }

    @Override
    public void checkIteratorConflicts(String tableName, IteratorSetting setting, EnumSet<IteratorUtil.IteratorScope> scopes) throws AccumuloException, TableNotFoundException {
        operations.checkIteratorConflicts(NamespaceUtil.getFullTableName(namespace, tableName), setting, scopes);
    }

    @Override
    public int addConstraint(String tableName, String constraintClassName) throws AccumuloException, AccumuloSecurityException, TableNotFoundException {
        return operations.addConstraint(NamespaceUtil.getFullTableName(namespace, tableName), constraintClassName);
    }

    @Override
    public void removeConstraint(String tableName, int number) throws AccumuloException, AccumuloSecurityException {
        operations.removeConstraint(NamespaceUtil.getFullTableName(namespace, tableName), number);
    }

    @Override
    public Map<String, Integer> listConstraints(String tableName) throws AccumuloException, TableNotFoundException {
        return operations.listConstraints(NamespaceUtil.getFullTableName(namespace, tableName));
    }

    @Override
    public List<DiskUsage> getDiskUsage(Set<String> tables) throws AccumuloException, AccumuloSecurityException, TableNotFoundException {
        Set<String> tablesWithNamespace = Sets.newHashSet();
        for (String table : tables) {
            tablesWithNamespace.add(NamespaceUtil.getFullTableName(namespace, table));
        }
        return operations.getDiskUsage(tablesWithNamespace);
    }

    @Override
    public boolean testClassLoad(String tableName, String className, String asTypeName) throws AccumuloException, AccumuloSecurityException, TableNotFoundException {
        return operations.testClassLoad(NamespaceUtil.getFullTableName(namespace, tableName), className, asTypeName);
    }
}
