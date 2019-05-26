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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.apache.ant.compress.util.ArchiveStreamFactory;
import org.apache.ant.compress.util.EntryHelper;
import org.apache.ant.compress.util.Messages;
import org.apache.ant.compress.util.StreamHelper;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Expand;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.resources.FileResource;
import org.apache.tools.ant.util.FileNameMapper;
import org.apache.tools.ant.util.FileUtils;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;

/**
 * Expand an archive.
 * <p>PatternSets are used to select files to extract
 * <I>from</I> the archive.  If no patternset is used, all files are extracted.
 * </p>
 * <p>FileSets may be used to select archived files
 * to perform unarchival upon.
 * </p>
 * <p>File permissions will not be restored on extracted files.</p>
 */
public abstract class ExpandBase extends Expand {
    private final ArchiveStreamFactory factory;

    protected ExpandBase(ArchiveStreamFactory factory) {
        this.factory = factory;
    }

    private boolean skipUnreadable = false;

    /**
     * No encoding support in general.
     * @param encoding not used
     */
    public void setEncoding(String encoding) {
        throw new BuildException("The " + getTaskName()
                                 + " task doesn't support the encoding"
                                 + " attribute", getLocation());
    }

    /**
     * No unicode extra fields in general.
     */
    public void setScanForUnicodeExtraFields(boolean b) {
        throw new BuildException("The " + getTaskName()
                                 + " task doesn't support the scanForUnicodeExtraFields"
                                 + " attribute", getLocation());
    }

    /**
     * Whether to skip entries that Commons Compress signals it cannot read.
     *
     * @since Compress Antlib 1.1
     */
    public void setSkipUnreadableEntries(boolean b) {
        skipUnreadable = b;
    }

    /**
     * Whether to skip entries that Commons Compress signals it cannot read.
     *
     * @since Compress Antlib 1.1
     */
    public boolean getSkipUnreadableEntries() {
        return skipUnreadable;
    }

    /** {@inheritDoc} */
    @Override
    protected void expandFile(FileUtils fileUtils, File srcF, File dir) {
        if (!srcF.exists()) {
            throw new BuildException("Unable to expand " + srcF
                                     + " as the file does not exist",
                                     getLocation());
        }
        InputStream is = null;
        try {
            is = StreamHelper.getInputStream(factory, new FileResource(srcF),
                                             getEncoding());
            if (is != null) {
                expandArchiveStream(srcF.getPath(), (ArchiveInputStream) is,
                                    dir);
            } else {
                is = new FileInputStream(srcF);
                expandStream(srcF.getPath(), is, dir);
            }
        } catch (IOException ioe) {
            throw new BuildException("Error while expanding " + srcF.getPath()
                                     + "\n" + ioe.toString(),
                                     ioe, getLocation());
        } finally {
            FileUtils.close(is);
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void expandResource(Resource srcR, File dir) {
        if (!srcR.isExists()) {
            throw new BuildException("Unable to expand " + srcR.getName()
                                     + " as the it does not exist",
                                     getLocation());
        }

        try (InputStream i = srcR.getInputStream()) {
            expandStream(srcR.getName(), i, dir);
        } catch (IOException ioe) {
            throw new BuildException("Error while expanding " + srcR.getName(),
                                     ioe, getLocation());
        }
    }

    private void expandStream(String name, InputStream stream, File dir)
        throws IOException {
        try (ArchiveInputStream is =
                 factory.getArchiveStream(new BufferedInputStream(stream),
                                          getEncoding())) {
            expandArchiveStream(name, is, dir);
        }
    }

    private void expandArchiveStream(String name, ArchiveInputStream is,
                                     File dir)
        throws IOException {
        FileNameMapper mapper = getMapper();
        log("Expanding: " + name + " into " + dir, Project.MSG_INFO);
        boolean empty = true;
        ArchiveEntry ent = null;
        while ((ent = is.getNextEntry()) != null) {
            if (skipUnreadable && !is.canReadEntryData(ent)) {
                log(Messages.skippedIsUnreadable(ent));
                continue;
            }
            empty = false;
            log("extracting " + ent.getName(), Project.MSG_DEBUG);
            extractFile(FileUtils.getFileUtils(), null, dir, is,
                        ent.getName(), ent.getLastModifiedDate(),
                        ent.isDirectory(), mapper);
        }
        if (empty && getFailOnEmptyArchive()) {
            throw new BuildException("archive '" + name + "' is empty");
        }
        log("expand complete", Project.MSG_VERBOSE);
    }
}
