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

package ezbake.local.zookeeper;

import java.io.IOException;
import org.apache.curator.test.TestingServer;

public class LocalZookeeper
{
    private TestingServer server;

    public LocalZookeeper() throws Exception
    {
        server = new TestingServer();
    }

    public LocalZookeeper(int portNumber) throws Exception
    {
       server = new TestingServer(portNumber);
    }

    public String getConnectionString()
    {
        return server.getConnectString();
    }

    public void shutdown() throws IOException
    {
        server.close();
    }

    public static void main(String [] args) throws Exception
    {
        LocalZookeeper zookeeper = null;
        if (args != null && args.length >= 1) {
            try {
                int port = Integer.parseInt(args[0]);
                zookeeper = new LocalZookeeper(port);
            } catch(Exception ex) {
                System.out.println(ex.toString());
            }
        } else {
            zookeeper = new LocalZookeeper();
        }
        System.out.println(zookeeper.getConnectionString());
    }
}
