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

import java.io.IOException;
import java.io.File;
import java.util.Date;
import java.util.Locale;

import org.apache.ant.compress.resources.SevenZFileSet;
import org.apache.ant.compress.util.SevenZStreamFactory;
import org.apache.ant.compress.util.SevenZStreamFactory.SevenZArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZMethod;
import org.apache.tools.ant.types.ArchiveFileSet;
import org.apache.tools.ant.types.Resource;

/**
 * Creates 7z archives.
 */
public class SevenZ extends ArchiveBase {

    private String contentCompression;

    public SevenZ() {
        setFactory(new SevenZStreamFactory() {
                public ArchiveOutputStream getArchiveOutputStream(File f,
                                                                  String encoding)
                    throws IOException {
                    SevenZArchiveOutputStream o = (SevenZArchiveOutputStream)
                        super.getArchiveOutputStream(f, encoding);
                    if (contentCompression != null) {
                        SevenZMethod m = (SevenZMethod)
                            Enum.valueOf(SevenZMethod.class,
                                         contentCompression.toUpperCase(Locale
                                                                        .US));
                        o.setContentCompression(m);
                    }
                    return o;
                }
            });
        setEntryBuilder(
              new ArchiveBase.EntryBuilder() {
                public ArchiveEntry buildEntry(ArchiveBase.ResourceWithFlags r) {
                    SevenZArchiveEntry entry = new SevenZArchiveEntry();
                    entry.setName(r.getName());
                    entry.setDirectory(r.getResource().isDirectory());
                    entry.setLastModifiedDate(new Date(r.getResource()
                                                       .getLastModified()));
                    entry.setSize(r.getResource().getSize());
                    return entry;
                }
            });
        setFileSetBuilder(new ArchiveBase.FileSetBuilder() {
                public ArchiveFileSet buildFileSet(Resource dest) {
                    ArchiveFileSet afs = new SevenZFileSet();
                    afs.setSrcResource(dest);
                    return afs;
                }
            });
    }

    /**
     * Sets the compression method to use for entry contents - the
     * default is LZMA2.
     *
     * <p>As of Commons Compress 1.6 only COPY (which means no
     * compression), LZMA2, BZIP2 and DEFLATE are supported.</p>
     */
    public void setContentCompression(String method) {
        this.contentCompression = method;
    }
}
