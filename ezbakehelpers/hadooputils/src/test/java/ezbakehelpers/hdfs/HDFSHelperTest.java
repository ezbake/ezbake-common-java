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

import ezbake.configuration.ClasspathConfigurationLoader;
import ezbake.configuration.EzConfiguration;

import org.apache.hadoop.fs.FileSystem;

import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import java.util.Properties;

public class HDFSHelperTest {

    @Test
    public void testLocalFileSystem() throws Exception {
        EzConfiguration configuration = new EzConfiguration(new ClasspathConfigurationLoader());
        FileSystem fs = HDFSHelper.getFileSystemFromProperties(configuration.getProperties());
        assertTrue(fs.getWorkingDirectory().toString().startsWith("file"));
    }

    private Properties getHAProps() {
        Properties props = new Properties();
        props.setProperty(HDFSHelper.HDFS_PROP_HA_FAILOVER_ENABLED, "true");
        props.setProperty(HDFSHelper.HDFS_PROP_NAMESERVICES, "mycluster");
        props.setProperty(HDFSHelper.HDFS_PROP_NAMENODES + "mycluster", "nn1,nn2");
        props.setProperty(HDFSHelper.HDFS_PROP_PROXY_PROVIDERS + "mycluster", "org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider");
        props.setProperty(HDFSHelper.HDFS_PROP_DEFAULT_FS, "hdfs://mycluster");
        props.setProperty(HDFSHelper.HDFS_PROP_NN_RPC_ADDRESSES + "mycluster" + "." + "nn1", "localhost:8020");
        props.setProperty(HDFSHelper.HDFS_PROP_NN_RPC_ADDRESSES + "mycluster" + "." + "nn2", "localhost:8020");
        props.setProperty(HDFSHelper.HDFS_PROP_HA_ZK_QUORUM, "zk01,zk02,zk03");
        props.setProperty(HDFSHelper.HDFS_PROP_HA_ZK_AUTH, "digest:hdfs-zkfcs:mypassword");
        props.setProperty(HDFSHelper.HDFS_PROP_HA_ZK_ACL, "digest:hdfs-zkfcs:vlUvLnd8MlacsE80rDuu6ONESbM=:rwcda");
        return props;
    }

    @Test
    public void testHAHdfsFileSystem() throws Exception {
        FileSystem fs = HDFSHelper.getFileSystemFromProperties(getHAProps());
        assertThat(fs.getWorkingDirectory().toString(), startsWith("hdfs://mycluster"));
    }
}
