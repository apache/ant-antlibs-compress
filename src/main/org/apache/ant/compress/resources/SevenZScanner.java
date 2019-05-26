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

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.ant.compress.util.SevenZStreamFactory;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.ant.compress.util.Messages;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.resources.FileProvider;

/**
 * Scans 7z archives for resources.
 *
 * @since Apache Compress Antlib 1.3
 */
public class SevenZScanner extends CommonsCompressArchiveScanner {

    public SevenZScanner() {
        this(false, null);
    }

    public SevenZScanner(boolean skipUnreadable, Project project) {
        super(new SevenZStreamFactory(),
              new CommonsCompressArchiveScanner.ResourceBuilder() {
                @Override
                public Resource buildResource(Resource archive, String encoding,
                                              ArchiveEntry entry) {
                    return new SevenZResource(archive, encoding,
                                              (SevenZArchiveEntry) entry);
                }
            }, skipUnreadable, project);
    }

    /**
     * Fills the file and directory maps with resources read from the
     * archive.
     *
     * @param src the archive to scan.
     * @param encoding encoding used to encode file names inside the
     * archive - ignored.
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

        FileProvider fp = (FileProvider) src.as(FileProvider.class);
        if (fp == null) {
            super.fillMapsFromArchive(src, encoding, fileEntries,
                                      matchFileEntries, dirEntries,
                                      matchDirEntries);
            return;
        }

        File srcFile = fp.getFile();
        SevenZArchiveEntry entry = null;

        try (SevenZFile zf = new SevenZFile(srcFile)) {
            entry = zf.getNextEntry();
            while (entry != null) {
                /* TODO implement canReadEntryData in CC
                if (getSkipUnreadableEntries() && !zf.canReadEntryData(entry)) {
                   log(Messages.skippedIsUnreadable(entry));
                   continue;
                }
                */
                Resource r = new SevenZResource(srcFile, encoding, entry);
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
                entry = zf.getNextEntry();
            }
        } catch (IOException ex) {
            throw new BuildException("Problem opening " + srcFile, ex);
        }
    }
}
