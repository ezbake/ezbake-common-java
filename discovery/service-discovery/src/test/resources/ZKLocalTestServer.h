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

/*
 * ZKLocalTestServer.h
 *
 *  Created on: Mar 4, 2014
 *      Author: oarowojolu
 */

#ifndef ZKLOCALTESTSERVER_H_
#define ZKLOCALTESTSERVER_H_

#include <cassert>
#include <cstdlib>
#include <string>
#include <sstream>

namespace ezbake {
namespace local {

class ZKLocalTestServer {
public:
    static const unsigned int DEFAULT_PORT = 55405;

public:
    ZKLocalTestServer() {}
    virtual ~ZKLocalTestServer() {}

    static const std::string& ResourceDir() {
        static std::string path("../../../src/test/resources");
        return path;
    }

    static const std::string& TargetDir() {
        static std::string path("../../../target");
        return path;
    }

    static void start(bool clean=true) {
        std::ostringstream ss;
        if (clean) {
            ss << ResourceDir() << "/zkServer.sh startClean " << DEFAULT_PORT << " " << TargetDir() << "/local-zookeeper.jar";
        } else {
            ss << ResourceDir() << "/zkServer.sh start " << DEFAULT_PORT << " " << TargetDir() << "/local-zookeeper.jar";
        }
        assert(system(ss.str().c_str()) == 0);
    }

    static void stop() {
        std::ostringstream ss;
        ss << ResourceDir() << "/zkServer.sh stop";
        assert(system(ss.str().c_str()) == 0);
    }
};

} /* namespace local */
} /* namespace ezbake */

#endif /* ZKLOCALTESTSERVER_H_ */
