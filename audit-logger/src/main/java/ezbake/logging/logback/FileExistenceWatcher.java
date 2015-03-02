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

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

/**
 * User: jhastings
 * Date: 11/14/14
 * Time: 11:55 AM
 */
public class FileExistenceWatcher implements Runnable {

    private boolean exists = false;
    private boolean stopped = false;
    private Path path;

    public FileExistenceWatcher(Path filePath) {
        this.path = filePath.toAbsolutePath();
        exists = Files.exists(filePath);
    }

    @Override
    public void run() {
        Path watchPath = path.getParent();
        if (watchPath == null) {
            stopped = true;
            return;
        }

        WatchService watchService;
        try {
            watchService = FileSystems.getDefault().newWatchService();
            watchPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE);
        } catch (IOException e) {
            stopped = true;
            return;
        }

        File watchFile = watchPath.toFile();
        while(!stopped) {
            WatchKey key;
            try {
                key = watchService.take();
            } catch (InterruptedException e) {
                stopped = true;
                return;
            }

            for (WatchEvent<?> event: key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();
                if (kind == StandardWatchEventKinds.OVERFLOW) {
                    // Do nothing
                    continue;
                }

                // Create or delete. check for existence
                if (Path.class.isAssignableFrom(event.context().getClass())) {
                    Path file = (Path) event.context();
                    Path absolutePath = new File(watchFile, file.toString()).toPath();
                    // Only process events for the file we're watching
                    if (absolutePath.equals(path)) {
                        exists = Files.exists(absolutePath);
                    }
                }
            }

            // Put the key back into the ready state
            boolean valid = key.reset();
            if (!valid) {
                break;
            }
        }

        stopped = true;
    }

    public void stop() {
        stopped = true;
    }

    public boolean isRunning() {
        return !stopped;
    }

    /**
     * This method can be used to check for existence of the file. The run method will update the value
     * based on whether or not the file has been deleted or created
     *
     * @return true if the file existed after the last event
     */
    public boolean fileExists() {
        return exists;
    }

}
