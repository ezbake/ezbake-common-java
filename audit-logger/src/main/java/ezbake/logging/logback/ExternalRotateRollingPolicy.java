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

package ezbake.logging.logback;

import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.RollingPolicyBase;
import ch.qos.logback.core.rolling.RolloverFailure;
import ch.qos.logback.core.rolling.TriggeringPolicy;

import java.io.File;
import java.nio.file.Paths;

/**
 * User: jhastings
 * Date: 11/13/14
 * Time: 4:05 PM
 */
public class ExternalRotateRollingPolicy extends RollingPolicyBase implements TriggeringPolicy {
    public static final String FILE_NAME_NOT_SET = "The File option must be set before using"+ExternalRotateRollingPolicy.class.getSimpleName();

    private FileExistenceWatcher existenceWatcher;

    public ExternalRotateRollingPolicy() { }

    public ExternalRotateRollingPolicy(RollingFileAppender parent) {
        setParent(parent);
    }

    @Override
    public void start() {
        // Need to start watching the file
        if (getActiveFileName() == null) {
            addWarn(FILE_NAME_NOT_SET);
            throw new IllegalStateException(FILE_NAME_NOT_SET);
        }

        // Start the file watcher
        existenceWatcher = new FileExistenceWatcher(Paths.get(getActiveFileName()));
        new Thread(existenceWatcher).start();
        addInfo("Watching " + getActiveFileName() + "for delete/create events");

        super.start();
    }

    @Override
    public void stop() {
        if (!isStarted()) {
            return;
        }
        if (existenceWatcher != null) {
            existenceWatcher.stop();
            addInfo("Stopped watching " + getActiveFileName() + "for delete/create events");
        }
        super.stop();
    }

    @Override
    public void rollover() throws RolloverFailure {
        // noop
    }

    @Override
    public String getActiveFileName() {
        return getParentsRawFileProperty();
    }

    @Override
    public boolean isTriggeringEvent(File file, Object o) {
        return !checkActiveFileExists();
    }

    private boolean checkActiveFileExists() {
        boolean exists = false;
        if (existenceWatcher != null) {
            exists = existenceWatcher.fileExists();
        }
        return exists;
    }
}
