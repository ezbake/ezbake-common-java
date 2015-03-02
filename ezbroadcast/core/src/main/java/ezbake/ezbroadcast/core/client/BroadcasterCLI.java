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

package ezbake.ezbroadcast.core.client;

import com.google.common.base.Optional;
import ezbake.base.thrift.Visibility;
import ezbake.ezbroadcast.core.EzBroadcaster;
import ezbake.ezbroadcast.core.thrift.SecureMessage;
import ezbake.thrift.ThriftUtils;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;

/**
 * This class is a REPL that can be used to broadcast data onto topics from any one of the different broadcaster
 * systems. A configuration file must be provided that will be used to create a broadcaster.
 */
public class BroadcasterCLI {

    @Option(name="-f", aliases="--configFile", usage="The configuration file for the broadcaster to be instantiated", required=true)
    private String configFile;

    @Option(name="-c", aliases="--thriftClass", usage="The thrift object type that will be used to receive messages.")
    private String thriftClass;

    public void run() throws InterruptedException, IOException, TException, ClassNotFoundException {
        Properties props = new Properties();
        props.load(new FileInputStream(new File(configFile)));

        EzBroadcaster broadcaster = EzBroadcaster.create(props, "BroadcasterCLI");

        String input = "";
        Scanner scan = new Scanner(System.in);

        while (!input.equals("q")) {
            System.out.print("Broadcast(b) or Receive(r)? ");
            input = scan.nextLine();

            if (input.equals("b")) {
                System.out.print("Enter a topic: ");
                String topic = scan.nextLine();
                broadcaster.registerBroadcastTopic(topic);
                System.out.print("Enter a string to broadcast: ");
                String message = scan.nextLine();

                broadcaster.broadcast(topic, new Visibility().setFormalVisibility("U"), message.getBytes());
            } else if (input.equals("r")) {
                System.out.print("Enter a topic: ");
                String topic = scan.nextLine();
                broadcaster.subscribeToTopic(topic);

                Optional<SecureMessage> message = broadcaster.receive(topic);
                if (message.isPresent()) {
                    if (thriftClass != null) {
                        Class clazz = Class.forName(thriftClass);
                        TBase object = ThriftUtils.deserialize(clazz, message.get().getContent());
                        System.out.println(object.toString());
                    } else {
                        System.out.println(new String(message.get().getContent(), "UTF-8"));
                    }
                } else {
                    System.out.println("no message");
                }
            }
        }

        System.out.println("done");
        broadcaster.close();
    }

    public static void main(String[] args) {
        BroadcasterCLI broadcasterCLI = new BroadcasterCLI();
        CmdLineParser parser = new CmdLineParser(broadcasterCLI);
        try {
            parser.parseArgument(args);
            broadcasterCLI.run();
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            parser.printUsage(System.err);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}
