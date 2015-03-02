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

package ezbake.security.common.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;

/**
 * User: jhastings
 * Date: 4/29/14
 * Time: 9:02 AM
 */
public class FileWatcher implements Runnable {
    private static Logger log = LoggerFactory.getLogger(FileWatcher.class);

    private FileWatchUpdater updater;
    private Path path;
    protected boolean done = false;

    public FileWatcher(Path pathToWatch, FileWatchUpdater updater) {
        this.updater = updater;
        this.path = pathToWatch;
    }

    public void stopWatching() {
        done = true;
    }

    public boolean isRunning() {
        return !done;
    }

    public boolean hasFile() {
        return path.toFile().exists();
    }

    @Override
    public void run() {
        Path watchPath = path.getParent();
        if(watchPath == null){
            watchPath =  Paths.get(".").toAbsolutePath().getParent();
            log.info("No parent directory supplied for {}, using {} as parent directory instead.", path,String.valueOf(watchPath));
        }
        WatchService watchService;
        try {
            watchService = FileSystems.getDefault().newWatchService();
            log.info("Starting {} for directory {} and file '{}'", FileWatcher.class.getSimpleName(), watchPath,path.getFileName());
            watchPath.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE);
        } catch (IOException e) {
            log.error("Failed to start watcher", e);
            done = true;
            return;
        }

        WATCHLOOP:
        while(!done) {
            WatchKey key;
            try {
                key = watchService.take();
            } catch (InterruptedException e) {
                return;
            }

            for (WatchEvent<?> event: key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();
                if (kind == StandardWatchEventKinds.OVERFLOW) {
                    continue;
                }

                if (Path.class.isAssignableFrom(Path.class)) {
                    Path filename = ((WatchEvent<Path>) event).context();
                    Path child = watchPath.resolve(filename);
                    if (!child.equals(path)) {
                        continue;
                    }

                    // Sleep a few milliseconds. Some editors seem to truncate the file and rename a copy in it's place
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        // ignore
                    }

                    try {
                        log.info("Detected event {} on file {}", kind, child);
                        if (!updater.loadUpdate(new FileInputStream(child.toFile()))) {
                            log.info("Updater doesn't wish to continue watching");
                            break WATCHLOOP;
                        }
                    } catch (FileNotFoundException e) {
                        log.info("Unable to re-load watched file {} - {}", child, e.getMessage());
                    }
                }
            }

            boolean valid = key.reset();
            if (!valid) {
                break;
            }
        }
        done = true;
        log.info("Finished watching {} for updates", path);
    }

    public static interface FileWatchUpdater extends Closeable {
        /**
         *
         * @param is
         * @return true to continue watching
         */
        public boolean loadUpdate(InputStream is);
    }
}
