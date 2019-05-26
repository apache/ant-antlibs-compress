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
import java.util.Enumeration;
import java.util.Map;
import java.util.zip.ZipException;

import org.apache.ant.compress.util.ZipStreamFactory;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.ant.compress.util.Messages;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.resources.FileProvider;

/**
 * Scans zip archives for resources.
 */
public class ZipScanner extends CommonsCompressArchiveScanner {

    public ZipScanner() {
        this(false, null);
    }

    public ZipScanner(boolean skipUnreadable, Project project) {
        super(new ZipStreamFactory(),
              new CommonsCompressArchiveScanner.ResourceBuilder() {
                @Override
                public Resource buildResource(Resource archive, String encoding,
                                            ArchiveEntry entry) {
                    return new ZipResource(archive, encoding,
                                           (ZipArchiveEntry) entry);
                }
            }, skipUnreadable, project);
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

        FileProvider fp = (FileProvider) src.as(FileProvider.class);
        if (fp == null) {
            super.fillMapsFromArchive(src, encoding, fileEntries,
                                      matchFileEntries, dirEntries,
                                      matchDirEntries);
            return;
        }

        File srcFile = fp.getFile();
        ZipArchiveEntry entry = null;
        ZipFile zf = null;

        try {
            try {
                zf = new ZipFile(srcFile, encoding);
            } catch (ZipException ex) {
                throw new BuildException("Problem reading " + srcFile, ex);
            } catch (IOException ex) {
                throw new BuildException("Problem opening " + srcFile, ex);
            }
            Enumeration e = zf.getEntries();
            while (e.hasMoreElements()) {
                entry = (ZipArchiveEntry) e.nextElement();
                if (getSkipUnreadableEntries() && !zf.canReadEntryData(entry)) {
                    log(Messages.skippedIsUnreadable(entry));
                    continue;
                }
                Resource r = new ZipResource(srcFile, encoding, entry);
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
        } finally {
            ZipFile.closeQuietly(zf);
        }
    }
}
