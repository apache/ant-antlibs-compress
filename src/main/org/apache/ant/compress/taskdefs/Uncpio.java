/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Expand;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.util.FileNameMapper;
import org.apache.tools.ant.util.FileUtils;

import org.apache.commons.compress.archivers.cpio.CpioArchiveEntry;
import org.apache.commons.compress.archivers.cpio.CpioArchiveInputStream;

/**
 * Uncpio a file.
 * <p>PatternSets are used to select files to extract
 * <I>from</I> the archive.  If no patternset is used, all files are extracted.
 * </p>
 * <p>FileSet's may be used to select archived files
 * to perform unarchival upon.
 * </p>
 * <p>File permissions will not be restored on extracted files.</p>
 */
public class Uncpio extends Expand {
    /**
     * No encoding support in Uncpio.
     * @param encoding not used
     * @throws BuildException always
     * @since Ant 1.6
     */
    public void setEncoding(String encoding) {
        throw new BuildException("The " + getTaskName()
                                 + " task doesn't support the encoding"
                                 + " attribute", getLocation());
    }

    /** {@inheritDoc} */
    protected void expandFile(FileUtils fileUtils, File srcF, File dir) {
        if (!srcF.exists()) {
            throw new BuildException("Unable to uncpio "
                    + srcF
                    + " as the file does not exist",
                    getLocation());
        }
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(srcF);
            expandStream(srcF.getPath(), fis, dir);
        } catch (IOException ioe) {
            throw new BuildException("Error while expanding " + srcF.getPath()
                                     + "\n" + ioe.toString(),
                                     ioe, getLocation());
        } finally {
            FileUtils.close(fis);
        }
    }

    /** {@inheritDoc} */
    protected void expandResource(Resource srcR, File dir) {
        if (!srcR.isExists()) {
            throw new BuildException("Unable to uncpio "
                                     + srcR.getName()
                                     + " as the it does not exist",
                                     getLocation());
        }

        InputStream i = null;
        try {
            i = srcR.getInputStream();
            expandStream(srcR.getName(), i, dir);
        } catch (IOException ioe) {
            throw new BuildException("Error while expanding " + srcR.getName(),
                                     ioe, getLocation());
        } finally {
            FileUtils.close(i);
        }
    }

    private void expandStream(String name, InputStream stream, File dir)
        throws IOException {
        CpioArchiveInputStream is = null;
        try {
            is = new CpioArchiveInputStream(new BufferedInputStream(stream));
            log("Expanding: " + name + " into " + dir, Project.MSG_INFO);
            CpioArchiveEntry ent = null;
            boolean empty = true;
            FileNameMapper mapper = getMapper();
            while ((ent = is.getNextCPIOEntry()) != null) {
                empty = false;
                extractFile(FileUtils.getFileUtils(), null, dir, is,
                            ent.getName(),
                            /* TODO revisit */ new Date(ent.getTime()),
                            ent.isDirectory(), mapper);
            }
            if (empty && getFailOnEmptyArchive()) {
                throw new BuildException("archive '" + name + "' is empty");
            }
            log("expand complete", Project.MSG_VERBOSE);
        } finally {
            FileUtils.close(is);
        }
    }
}
