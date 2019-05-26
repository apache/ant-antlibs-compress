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
import java.util.Date;
import java.util.Enumeration;

import org.apache.ant.compress.util.Messages;
import org.apache.ant.compress.util.ZipStreamFactory;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.util.FileNameMapper;
import org.apache.tools.ant.util.FileUtils;

/**
 * Unzip a file.
 */
public class Unzip extends ExpandBase {

    public Unzip() {
        super(new ZipStreamFactory());
    }

    @Override
    public void setEncoding(String encoding) {
        internalSetEncoding(encoding);
    }

    // overridden in order to take advantage of ZipFile
    protected void expandFile(FileUtils fileUtils, File srcF, File dir) {
        log("Expanding: " + srcF + " into " + dir, Project.MSG_INFO);
        FileNameMapper mapper = getMapper();
        if (!srcF.exists()) {
            throw new BuildException("Unable to expand " + srcF
                                     + " as the file does not exist",
                                     getLocation());
        }
        try (ZipFile zf = new ZipFile(srcF, getEncoding(), true)) {
            boolean empty = true;
            Enumeration e = zf.getEntries();
            while (e.hasMoreElements()) {
                empty = false;
                ZipArchiveEntry ze = (ZipArchiveEntry) e.nextElement();
                if (getSkipUnreadableEntries() && !zf.canReadEntryData(ze)) {
                    log(Messages.skippedIsUnreadable(ze));
                    continue;
                }
                log("extracting " + ze.getName(), Project.MSG_DEBUG);
                InputStream is = null;
                try {
                    extractFile(fileUtils, srcF, dir,
                                is = zf.getInputStream(ze),
                                ze.getName(), new Date(ze.getTime()),
                                ze.isDirectory(), mapper);
                } finally {
                    FileUtils.close(is);
                }
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
        }
    }

}
