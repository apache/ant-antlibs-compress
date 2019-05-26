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
import java.io.OutputStream;
import org.apache.ant.compress.resources.ArFileSet;
import org.apache.ant.compress.util.ArStreamFactory;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ar.ArArchiveEntry;
import org.apache.commons.compress.archivers.ar.ArArchiveOutputStream;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.ArchiveFileSet;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.types.Resource;

/**
 * Creates ar archives.
 */
public class Ar extends ArchiveBase {

    private static final String NO_DIRS_MESSAGE = "ar archives cannot store"
        + " directory entries";

    private Format format = Format.AR;

    public Ar() {
        setFactory(new ArStreamFactory() {
                @Override
                public ArchiveOutputStream getArchiveStream(OutputStream stream,
                                                            String encoding)
                    throws IOException {
                    ArArchiveOutputStream o =
                        (ArArchiveOutputStream) super.getArchiveStream(stream,
                                                                       encoding);
                    if (format.equals(Format.BSD)) {
                        o.setLongFileMode(ArArchiveOutputStream.LONGFILE_BSD);
                    }
                    return o;
                }
            });
        setEntryBuilder(
              new ArchiveBase.EntryBuilder() {
                @Override
                public ArchiveEntry buildEntry(ArchiveBase.ResourceWithFlags r) {
                    boolean isDir = r.getResource().isDirectory();
                    if (isDir) {
                        throw new BuildException(NO_DIRS_MESSAGE);
                    }

                    int mode = ArchiveFileSet.DEFAULT_FILE_MODE;
                    if (r.getCollectionFlags().hasModeBeenSet()) {
                        mode = r.getCollectionFlags().getMode();
                    } else if (r.getResourceFlags().hasModeBeenSet()) {
                        mode = r.getResourceFlags().getMode();
                    }

                    int uid = 0;
                    if (r.getResourceFlags().hasUserIdBeenSet()) {
                        uid = r.getResourceFlags().getUserId();
                    } else if (r.getCollectionFlags().hasUserIdBeenSet()) {
                        uid = r.getCollectionFlags().getUserId();
                    }

                    int gid = 0;
                    if (r.getResourceFlags().hasGroupIdBeenSet()) {
                        gid = r.getResourceFlags().getGroupId();
                    } else if (r.getCollectionFlags().hasGroupIdBeenSet()) {
                        gid = r.getCollectionFlags().getGroupId();
                    }

                    return new ArArchiveEntry(r.getName(),
                                              r.getResource().getSize(),
                                              uid, gid, mode,
                                              round(r.getResource()
                                                    .getLastModified(), 1000)
                                              / 1000);
                }
            });
        setFileSetBuilder(new ArchiveBase.FileSetBuilder() {
                @Override
                public ArchiveFileSet buildFileSet(Resource dest) {
                    ArchiveFileSet afs = new ArFileSet();
                    afs.setSrcResource(dest);
                    return afs;
                }
            });
    }

    @Override
    public void setFilesOnly(boolean b) {
        if (!b) {
            throw new BuildException(NO_DIRS_MESSAGE);
        }
    }

    /**
     * The format for entries with filenames longer than 16 characters
     * or containign spaces - any other entry will always use "ar".
     *
     * @since Apache Compress Antlib 1.1
     */
    public void setFormat(Format f) {
        format = f;
    }

    /**
     * The supported tar formats for entries with long file names.
     */
    public final static class Format extends EnumeratedAttribute {
        private static final String AR_NAME = "ar";
        private static final String BSD_NAME = "bsd";

        public static final Format AR = new Format(AR_NAME);
        public static final Format BSD = new Format(BSD_NAME);

        public Format(String v) {
            setValue(v);
        }

        public Format() {
            setValue(AR_NAME);
        }

        @Override
        public String[] getValues() {
            return new String[] {AR_NAME, BSD_NAME};
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof Format
                && ((Format) other).getValue().equals(getValue());
        }
    }

}
