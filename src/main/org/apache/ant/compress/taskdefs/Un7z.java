/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.apache.ant.compress.taskdefs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.ant.compress.util.Messages;
import org.apache.ant.compress.util.SevenZStreamFactory;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.util.FileNameMapper;
import org.apache.tools.ant.util.FileUtils;

/**
 * Un7z a file.
 * @since Apache Compress Antlib 1.3
 */
public class Un7z extends ExpandBase {

    public Un7z() {
        super(new SevenZStreamFactory());
    }

    // overridden in order to take advantage of SevenzFile
    @Override
    protected void expandFile(FileUtils fileUtils, File srcF, File dir) {
        if (!srcF.exists()) {
            throw new BuildException("Unable to expand " + srcF
                                     + " as the file does not exist",
                                     getLocation());
        }
        log("Expanding: " + srcF + " into " + dir, Project.MSG_INFO);
        FileNameMapper mapper = getMapper();
        SevenZFile outer = null;
        try {
            final SevenZFile zf = outer = new SevenZFile(srcF);
            boolean empty = true;
            SevenZArchiveEntry ze = zf.getNextEntry();
            while (ze != null) {
                empty = false;
                /* TODO implement canReadEntryData in CC
                if (getSkipUnreadableEntries() && !zf.canReadEntryData(ze)) {
                    log(Messages.skippedIsUnreadable(ze));
                    continue;
                }
                */
                log("extracting " + ze.getName(), Project.MSG_DEBUG);
                try (InputStream is = new InputStream() {
                        @Override
                        public int read() throws IOException {
                            return zf.read();
                        }
                        @Override
                        public int read(byte[] b) throws IOException {
                            return zf.read(b);
                        }
                    }) {
                    extractFile(fileUtils, srcF, dir, is,
                                ze.getName(), ze.getLastModifiedDate(),
                                ze.isDirectory(), mapper);
                }
                ze = zf.getNextEntry();
            }
            if (empty && getFailOnEmptyArchive()) {
                throw new BuildException("archive '" + srcF + "' is empty");
            }
            log("expand complete", Project.MSG_VERBOSE);
        } catch (IOException ioe) {
            throw new BuildException(
                "Error while expanding " + srcF.getPath()
                + "\n" + ioe.toString(),
                ioe);
        } finally {
            if (outer != null) {
                try {
                    outer.close();
                } catch (IOException ex) {
                    // swallow
                }
            }
        }
    }

}
