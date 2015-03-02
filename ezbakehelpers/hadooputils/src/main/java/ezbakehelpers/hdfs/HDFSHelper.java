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

package ezbakehelpers.hdfs;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import ezbake.configuration.constants.EzBakePropertyConstants;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;

import java.io.IOException;
import java.util.Properties;
import java.util.Set;

/**
 * HDFS Helpers to help get a HDFS Filesystem object
 */
public class HDFSHelper {
    /**
     * dfs.nameservices - the logical name for this new nameservice
     * <br/><br/>See: <a href="http://hadoop.apache.org/docs/r2.3.0/hadoop-yarn/hadoop-yarn-site/HDFSHighAvailabilityWithNFS.html">http://hadoop.apache.org/docs/r2.3.0/hadoop-yarn/hadoop-yarn-site/HDFSHighAvailabilityWithNFS.html</a>
     */
    public static final String HDFS_PROP_NAMESERVICES = "dfs.nameservices";
    /**
     * dfs.ha.namenodes.[nameservice ID] -  unique identifiers for each NameNode in the nameservice
     *
     * <br/><br/><b>Note:</b> Currently, only a maximum of two NameNodes may be configured per nameservice.
     *
     * <br/><br/>Example value:<pre>nn1,nn2</pre>
     *
     * <br/><br/>See: <a href="http://hadoop.apache.org/docs/r2.3.0/hadoop-yarn/hadoop-yarn-site/HDFSHighAvailabilityWithNFS.html">http://hadoop.apache.org/docs/r2.3.0/hadoop-yarn/hadoop-yarn-site/HDFSHighAvailabilityWithNFS.html</a>
     */
    public static final String HDFS_PROP_NAMENODES = "dfs.ha.namenodes.";


    /**
     * dfs.namenode.rpc-address.[nameservice ID].[name node ID] - the fully-qualified RPC address for each NameNode to listen on
     * <br/><br/>Example key-value:
     * <pre>
     * dfs.namenode.rpc-address.mycluster.nn1=nn1.hadoop.example.com:8020
     * dfs.namenode.rpc-address.mycluster.nn2=nn2.hadoop.example.com:8020
     * </pre>
     *
     * <br/><br/><b>Note:</b> You may similarly configure the "servicerpc-address" setting if you so desire.
     * <br/><br/>See: <a href="http://hadoop.apache.org/docs/r2.3.0/hadoop-yarn/hadoop-yarn-site/HDFSHighAvailabilityWithNFS.html">http://hadoop.apache.org/docs/r2.3.0/hadoop-yarn/hadoop-yarn-site/HDFSHighAvailabilityWithNFS.html</a>
     */
    public static final String HDFS_PROP_NN_RPC_ADDRESSES = "dfs.namenode.rpc-address.";

    /**
     * dfs.client.failover.proxy.provider.[nameservice ID] - the Java class that HDFS clients use to contact the Active NameNode
     * TL;DR - Set this to {@link org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider}<br/>
     * Configure the name of the Java class which will be used by the DFS Client to determine which NameNode is the
     * current Active, and therefore which NameNode is currently serving client requests.
     * The only implementation which currently ships with Hadoop is the ConfiguredFailoverProxyProvider,
     * so use this unless you are using a custom one.
     * <br/><br/>See: <a href="http://hadoop.apache.org/docs/r2.3.0/hadoop-yarn/hadoop-yarn-site/HDFSHighAvailabilityWithNFS.html">http://hadoop.apache.org/docs/r2.3.0/hadoop-yarn/hadoop-yarn-site/HDFSHighAvailabilityWithNFS.html</a>
     */
    public static final String HDFS_PROP_PROXY_PROVIDERS = "dfs.client.failover.proxy.provider.";
    /**
     * fs.defaultFS - the default path prefix used by the Hadoop FS client when none is given<br/><br/>
     *
     * Optionally, you may now configure the default path for Hadoop clients to use the new HA-enabled logical URI.
     * If you used "mycluster" as the nameservice ID earlier, this will be the value of the authority portion of all of
     * your HDFS paths.<br/><br/>
     *
     * Example: <pre>hdfs://mycluster</pre>
     * <br/><br/>See: <a href="http://hadoop.apache.org/docs/r2.3.0/hadoop-yarn/hadoop-yarn-site/HDFSHighAvailabilityWithNFS.html">http://hadoop.apache.org/docs/r2.3.0/hadoop-yarn/hadoop-yarn-site/HDFSHighAvailabilityWithNFS.html</a>
     */
    public static final String HDFS_PROP_DEFAULT_FS = "fs.defaultFS";
    /**
     * The configuration of automatic failover requires this to be set to true.
     * <br/><br/>See: <a href="http://hadoop.apache.org/docs/r2.3.0/hadoop-yarn/hadoop-yarn-site/HDFSHighAvailabilityWithNFS.html">http://hadoop.apache.org/docs/r2.3.0/hadoop-yarn/hadoop-yarn-site/HDFSHighAvailabilityWithNFS.html</a>
     */
    public static final String HDFS_PROP_HA_FAILOVER_ENABLED = "dfs.ha.automatic-failover.enabled";
    /**
     * This specifies that the cluster should be set up for automatic failover using this zookeeper quorum
     * <br/><br/>See: <a href="http://hadoop.apache.org/docs/r2.3.0/hadoop-yarn/hadoop-yarn-site/HDFSHighAvailabilityWithNFS.html">http://hadoop.apache.org/docs/r2.3.0/hadoop-yarn/hadoop-yarn-site/HDFSHighAvailabilityWithNFS.html</a>
     */
    public static final String HDFS_PROP_HA_ZK_QUORUM = "ha.zookeeper.quorum";
    /**
     * If you are running a secure cluster, you will likely want to ensure that the information stored in ZooKeeper is
     * also secured. This prevents malicious clients from modifying the metadata in ZooKeeper or potentially triggering
     * a false failover. In order to secure the information in ZooKeeper, you need the 'auth' property set
     * and the acl property set.<br/><br/>
     *
     * The first configured file specifies a list of ZooKeeper authentications, in the same format as used by the ZK CLI.
     * For example, you may specify something like:
     * <pre>digest:hdfs-zkfcs:mypassword</pre>
     * <br/><br/>See: <a href="http://hadoop.apache.org/docs/r2.3.0/hadoop-yarn/hadoop-yarn-site/HDFSHighAvailabilityWithNFS.html">http://hadoop.apache.org/docs/r2.3.0/hadoop-yarn/hadoop-yarn-site/HDFSHighAvailabilityWithNFS.html</a>
     */
    public static final String HDFS_PROP_HA_ZK_AUTH = "ha.zookeeper.auth";
    /**
     * Next, generate a ZooKeeper ACL that corresponds to this authentication, using a command like the following:
     * <pre>
     * $ java -cp $ZK_HOME/lib/*:$ZK_HOME/zookeeper-3.4.2.jar org.apache.zookeeper.server.auth.DigestAuthenticationProvider hdfs-zkfcs:mypassword
     * </pre><br/>
     * <pre>
     * output: hdfs-zkfcs:mypassword->hdfs-zkfcs:P/OQvnYyU/nF/mGYvB/xurX8dYs=
     * </pre>
     *
     * Copy and paste the section of this output after the '->' string into the property, prefixed by the string "digest:". For example:
     * <pre>
     * digest:hdfs-zkfcs:vlUvLnd8MlacsE80rDuu6ONESbM=:rwcda
     * </pre>
     * In order for these ACLs to take effect, you should then rerun the zkfc -formatZK command as described above.<br/><br/>
     *
     * After doing so, you may verify the ACLs from the ZK CLI as follows:<br/><br/>
     * <pre>
     * [zk: localhost:2181(CONNECTED) 1] getAcl /hadoop-ha
     * 'digest,'hdfs-zkfcs:vlUvLnd8MlacsE80rDuu6ONESbM=
     * : cdrwa
     * </pre>
     * <br/><br/>See: <a href="http://hadoop.apache.org/docs/r2.3.0/hadoop-yarn/hadoop-yarn-site/HDFSHighAvailabilityWithNFS.html">http://hadoop.apache.org/docs/r2.3.0/hadoop-yarn/hadoop-yarn-site/HDFSHighAvailabilityWithNFS.html</a>
     */
    public static final String HDFS_PROP_HA_ZK_ACL = "ha.zookeeper.acl";
    public static final Set<String> ALL_PROPS = ImmutableSet.of(HDFS_PROP_NAMESERVICES, HDFS_PROP_NAMENODES,
            HDFS_PROP_NN_RPC_ADDRESSES, HDFS_PROP_PROXY_PROVIDERS, HDFS_PROP_DEFAULT_FS, HDFS_PROP_HA_FAILOVER_ENABLED,
            HDFS_PROP_HA_ZK_QUORUM, HDFS_PROP_HA_ZK_AUTH, HDFS_PROP_HA_ZK_ACL);


    /**
     * Creates a new FileSystem based on the properties from {@link ezbake.common.properties.EzProperties}
     * @param properties - The properties to read from to construct the FileSystem
     * @return A new Filesystem
     * @throws IOException - on IOException from constructing the FileSystem
     */
    public static FileSystem getFileSystemFromProperties(Properties properties) throws IOException {
        boolean useLocal = Boolean.parseBoolean(properties.getProperty(EzBakePropertyConstants.HADOOP_FILESYSTEM_USE_LOCAL));
        boolean useHANameNodes = Boolean.parseBoolean(properties.getProperty(HDFS_PROP_HA_FAILOVER_ENABLED, "false"));
        if ( ! useHANameNodes || useLocal ) {
            return getNonHAFileSystemFromProperties(properties);
        } else {
            return getHAFileSystemFromProperties(properties);
        }
    }

    /**
     * Always returns an non HA FileSystem from the EzProperties
     * @param properties - The properties to read from to construct the FileSystem
     * @return A new Filesystem
     * @throws IOException - on IOException from constructing the FileSystem
     */
    public static FileSystem getNonHAFileSystemFromProperties(Properties properties) throws IOException {
        Configuration hadoopConf = new Configuration();
        String fileSystemName = properties.getProperty(EzBakePropertyConstants.HADOOP_FILESYSTEM_NAME);
        boolean useLocal = Boolean.parseBoolean(properties.getProperty(EzBakePropertyConstants.HADOOP_FILESYSTEM_USE_LOCAL));
        if (!Strings.isNullOrEmpty(fileSystemName) && !useLocal) {
            hadoopConf.set(EzBakePropertyConstants.HADOOP_FILESYSTEM_NAME, fileSystemName);
            hadoopConf.set(EzBakePropertyConstants.HADOOP_FILESYSTEM_IMPL, properties.getProperty(EzBakePropertyConstants.HADOOP_FILESYSTEM_IMPL));
        }
        return FileSystem.get(hadoopConf);
    }

    /**
     * Returns a High Availability File System from the EzProperties.
     *
     * See <a href="http://hadoop.apache.org/docs/r2.3.0/hadoop-yarn/hadoop-yarn-site/HDFSHighAvailabilityWithNFS.html">http://hadoop.apache.org/docs/r2.3.0/hadoop-yarn/hadoop-yarn-site/HDFSHighAvailabilityWithNFS.html</a>
     * for all of the properties that are required to be set.
     *
     * @param properties - EzProperties to get the HDFS Configuration from
     * @return A new FileSystem
     * @throws IOException - on IOException from constructing the FileSystem
     */
    private static FileSystem getHAFileSystemFromProperties(Properties properties) throws IOException {
        final Configuration hadoopConf = convertToHDFSConfiguration(properties);
        return FileSystem.get(hadoopConf);
    }

    /**
     * @param properties - EzProperties to get the HDFS Configuration from
     * @return {@link org.apache.hadoop.conf.Configuration} object with HDFS configuration stored in it
     */
    private static Configuration convertToHDFSConfiguration(Properties properties) {
        final Configuration hadoopConf = new Configuration();
        @SuppressWarnings("unchecked")
        FluentIterable<String> PROP_KEYS = FluentIterable.from((Set) properties.keySet());
        for (final String prop : ALL_PROPS) {
            final Set<String> properties_to_load = Sets.newHashSet();
            // if ends with a '.' we want to add all properties from EzProperties that starts with this property name
            if (prop.endsWith(".")) {
                Iterables.addAll(properties_to_load, PROP_KEYS.filter(new Predicate<String>() {
                    @Override
                    public boolean apply(String s) {
                        return s.startsWith(prop);
                    }
                }));
            } else {
                properties_to_load.add(prop);
            }
            // for each property found, if it exist in the ezProperty load it
            for (String propToLoad : properties_to_load) {
                String value = properties.getProperty(propToLoad);
                if (!Strings.isNullOrEmpty(value))
                    hadoopConf.set(propToLoad, value);
            }
        }
        //Maven including different jars causes this to get overwritten.
        //see http://stackoverflow.com/a/21118824
        hadoopConf.set(EzBakePropertyConstants.HADOOP_FILESYSTEM_IMPL,
                properties.getProperty(EzBakePropertyConstants.HADOOP_FILESYSTEM_IMPL,
                org.apache.hadoop.hdfs.DistributedFileSystem.class.getName()));
        return hadoopConf;
    }



}
