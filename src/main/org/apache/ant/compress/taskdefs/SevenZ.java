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

import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.ant.compress.resources.SevenZFileSet;
import org.apache.ant.compress.util.SevenZStreamFactory;
import org.apache.ant.compress.util.SevenZStreamFactory.SevenZArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZMethod;
import org.apache.commons.compress.archivers.sevenz.SevenZMethodConfiguration;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.ArchiveFileSet;
import org.apache.tools.ant.types.Resource;

/**
 * Creates 7z archives.
 */
public class SevenZ extends ArchiveBase {

    private boolean keepCompression = false;
    private String contentCompression;
    private List<SevenZMethodConfiguration> contentMethods;

    public SevenZ() {
        setFactory(new SevenZStreamFactory() {
                @Override
                public ArchiveOutputStream getArchiveOutputStream(File f,
                                                                  String encoding)
                    throws IOException {
                    SevenZArchiveOutputStream o = (SevenZArchiveOutputStream)
                        super.getArchiveOutputStream(f, encoding);
                    if (contentCompression != null) {
                        o.setContentCompression(asMethod(contentCompression));
                    }
                    if (contentMethods != null) {
                        o.setContentMethods(contentMethods);
                    }
                    return o;
                }
            });
        setEntryBuilder(
              new ArchiveBase.EntryBuilder() {
                @Override
                public ArchiveEntry buildEntry(ArchiveBase.ResourceWithFlags r) {
                    SevenZArchiveEntry entry = new SevenZArchiveEntry();
                    entry.setName(r.getName());
                    entry.setDirectory(r.getResource().isDirectory());
                    entry.setLastModifiedDate(new Date(r.getResource()
                                                       .getLastModified()));
                    entry.setSize(r.getResource().getSize());
                    if (keepCompression
                        && r.getResourceFlags().hasContentMethods()) {
                        entry.setContentMethods(r.getResourceFlags()
                                                .getContentMethods());
                    }
                    return entry;
                }
            });
        setFileSetBuilder(new ArchiveBase.FileSetBuilder() {
                @Override
                public ArchiveFileSet buildFileSet(Resource dest) {
                    ArchiveFileSet afs = new SevenZFileSet();
                    afs.setSrcResource(dest);
                    return afs;
                }
            });
    }

    /**
     * Sets the compression method to use for entry contents - the
     * default is LZMA2 with no additional options.
     *
     * <p>As of Commons Compress 1.8 only COPY (which means no
     * compression), LZMA2, BZIP2 and DEFLATE are supported.</p>
     */
    public void setContentCompression(String method) {
        if (contentMethods != null && method != null) {
            throw new BuildException("you must not specify contentCompression and nested contentMethod elements at the same time");
        }
        this.contentCompression = method;
    }

    /**
     * Adds a compression method to use for entry contents - the
     * default is LZMA2 with no additional options.
     * @since 1.5
     */
    public void addConfiguredContentMethod(ContentMethod cm) {
        if (contentCompression != null) {
            throw new BuildException("you must not specify contentCompression and nested contentMethod elements at the same time");
        }
        if (contentMethods == null) {
            contentMethods = new ArrayList<>();
        }
        contentMethods.add(asMethodConfiguration(cm));
    }

    /**
     * Whether the original compression of entries coming from a 7z
     * archive should be kept (for example when updating an archive).
     * Default is false.
     * @param keep if true, keep the original compression
     * @since 1.5
     */
    public void setKeepCompression(boolean keep) {
        keepCompression = keep;
    }

    private static SevenZMethod asMethod(String method) {
        return (SevenZMethod) Enum.valueOf(SevenZMethod.class,
                                           method.toUpperCase(Locale.US));
    }

    private static SevenZMethodConfiguration
        asMethodConfiguration(ContentMethod cm) {
        return new SevenZMethodConfiguration(asMethod(cm.method), cm.option);
    }

    /**
     * Container for a supported 7z method and its configuration.
     * @since 1.5
     */
    public static class ContentMethod {
        private String method;
        private Integer option;

        /**
         * The "method" which can be a compression method, an
         * encryption method or a filter.
         */
        public void setMethod(String m) {
            method = m;
        }
        /**
         * Option for the method, must be understood by the method.
         */
        public void setOption(int o) {
            option = Integer.valueOf(o);
        }
    }
}
