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

package org.apache.ant.compress.resources;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Map;

import org.apache.ant.compress.util.StreamHelper;
import org.apache.ant.compress.util.ArchiveStreamFactory;
import org.apache.ant.compress.util.Messages;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.ArchiveScanner;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.util.FileUtils;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;

/**
 * Scans tar archives for resources.
 */
public class CommonsCompressArchiveScanner extends ArchiveScanner {

    private final ArchiveStreamFactory factory;
    private final ResourceBuilder builder;
    private final boolean skipUnreadable;
    private final Project project;

    public CommonsCompressArchiveScanner(ArchiveStreamFactory factory,
                                         ResourceBuilder builder) {
        this(factory, builder, false, null);
    }

    public CommonsCompressArchiveScanner(ArchiveStreamFactory factory,
                                         ResourceBuilder builder,
                                         boolean skipUnreadableEntries,
                                         Project project) {
        this.factory = factory;
        this.builder = builder;
        skipUnreadable = skipUnreadableEntries;
        this.project = project;
    }

    /**
     * Whether to skip entries that Commons Compress signals it cannot read.
     *
     * @since Compress Antlib 1.1
     */
    public boolean getSkipUnreadableEntries() {
        return skipUnreadable;
    }

    /**
     * Fills the file and directory maps with resources read from the
     * archive.
     *
     * @param src the archive to scan.
     * @param encoding encoding used to encode file names inside the archive.
     * @param fileEntries Map (name to resource) of non-directory
     * resources found inside the archive.
     * @param matchFileEntries Map (name to resource) of non-directory
     * resources found inside the archive that matched all include
     * patterns and didn't match any exclude patterns.
     * @param dirEntries Map (name to resource) of directory
     * resources found inside the archive.
     * @param matchDirEntries Map (name to resource) of directory
     * resources found inside the archive that matched all include
     * patterns and didn't match any exclude patterns.
     */
    @Override
    protected void fillMapsFromArchive(Resource src, String encoding,
                                       Map fileEntries, Map matchFileEntries,
                                       Map dirEntries, Map matchDirEntries) {
        ArchiveEntry entry = null;
        ArchiveInputStream ai = null;

        try {
            try {
                ai = StreamHelper.getInputStream(factory, src, encoding);
                if (ai == null) {
                    ai =
                        factory.getArchiveStream(new BufferedInputStream(src
                                                                         .getInputStream()),
                                                 encoding);
                }
            } catch (IOException ex) {
                throw new BuildException("problem opening " + src, ex);
            }
            while ((entry = ai.getNextEntry()) != null) {
                if (skipUnreadable && !ai.canReadEntryData(entry)) {
                    log(Messages.skippedIsUnreadable(entry));
                    continue;
                }
                Resource r = builder.buildResource(src, encoding, entry);
                String name = entry.getName();
                if (entry.isDirectory()) {
                    name = trimSeparator(name);
                    dirEntries.put(name, r);
                    if (match(name)) {
                        matchDirEntries.put(name, r);
                    }
                } else {
                    fileEntries.put(name, r);
                    if (match(name)) {
                        matchFileEntries.put(name, r);
                    }
                }
            }
        } catch (IOException ex) {
            throw new BuildException("problem reading " + src, ex);
        } finally {
            FileUtils.close(ai);
        }
    }

    /**
     * @since Compress Antlib 1.1
     */
    protected final void log(String msg) {
        if (project != null) {
            project.log(msg);
        } else {
            // rely on Ant's output redirection
            System.out.println(msg);
        }
    }

    public static interface ResourceBuilder {
        /**
         * Creates the matching archive entry resource.
         */
        Resource buildResource(Resource archive, String encoding,
                               ArchiveEntry entry);
    }
}
