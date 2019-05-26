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
import org.apache.ant.compress.util.TarStreamFactory;
import org.apache.ant.compress.resources.TarFileSet;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarConstants;
import org.apache.tools.ant.types.ArchiveFileSet;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.types.Resource;

/**
 * Creates tar archives.
 */
public class Tar extends ArchiveBase {
    private Format format = Format.USTAR;

    public Tar() {
        setFactory(new TarStreamFactory(){
                @Override
                public ArchiveOutputStream getArchiveStream(OutputStream stream,
                                                            String encoding)
                    throws IOException {
                    TarArchiveOutputStream o =
                        (TarArchiveOutputStream) super.getArchiveStream(stream,
                                                                        encoding);
                    if (format.equals(Format.OLDGNU)) {
                        o.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
                    }
                    else if (format.equals(Format.GNU)) {
                        o.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
                        o.setBigNumberMode(TarArchiveOutputStream
                                           .BIGNUMBER_STAR);
                    }
                    else if (format.equals(Format.STAR)) {
                        o.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
                        o.setBigNumberMode(TarArchiveOutputStream
                                           .BIGNUMBER_STAR);
                    }
                    else if (format.equals(Format.PAX)) {
                        o.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
                        o.setBigNumberMode(TarArchiveOutputStream
                                           .BIGNUMBER_POSIX);
                        o.setAddPaxHeadersForNonAsciiNames(true);
                    }
                    return o;
                }
            });
        setEntryBuilder(
              new ArchiveBase.EntryBuilder() {
                @Override
                public ArchiveEntry buildEntry(ArchiveBase.ResourceWithFlags r) {
                    boolean isDir = r.getResource().isDirectory();
                    String name = r.getName();
                    if (isDir && !name.endsWith("/")) {
                        name += "/";
                    } else if (!isDir && name.endsWith("/")) {
                        name = name.substring(0, name.length() - 1);
                    }
                    TarArchiveEntry ent =
                        new TarArchiveEntry(name, getPreserveLeadingSlashes());
                    ent.setModTime(round(r.getResource().getLastModified(),
                                         1000));
                    ent.setSize(isDir ? 0 : r.getResource().getSize());

                    if (!isDir && r.getCollectionFlags().hasModeBeenSet()) {
                        ent.setMode(r.getCollectionFlags().getMode());
                    } else if (isDir
                               && r.getCollectionFlags().hasDirModeBeenSet()) {
                        ent.setMode(r.getCollectionFlags().getDirMode());
                    } else if (r.getResourceFlags().hasModeBeenSet()) {
                        ent.setMode(r.getResourceFlags().getMode());
                    } else {
                        ent.setMode(isDir
                                    ? ArchiveFileSet.DEFAULT_DIR_MODE
                                    : ArchiveFileSet.DEFAULT_FILE_MODE);
                    }

                    if (r.getResourceFlags().hasUserIdBeenSet()) {
                        ent.setUserId(r.getResourceFlags().getUserId());
                    } else if (r.getCollectionFlags().hasUserIdBeenSet()) {
                        ent.setUserId(r.getCollectionFlags().getUserId());
                    }

                    if (r.getResourceFlags().hasGroupIdBeenSet()) {
                        ent.setGroupId(r.getResourceFlags().getGroupId());
                    } else if (r.getCollectionFlags().hasGroupIdBeenSet()) {
                        ent.setGroupId(r.getCollectionFlags().getGroupId());
                    }
 
                    if (r.getResourceFlags().hasUserNameBeenSet()) {
                        ent.setUserName(r.getResourceFlags().getUserName());
                    } else if (r.getCollectionFlags().hasUserNameBeenSet()) {
                        ent.setUserName(r.getCollectionFlags().getUserName());
                    }

                    if (r.getResourceFlags().hasGroupNameBeenSet()) {
                        ent.setGroupName(r.getResourceFlags().getGroupName());
                    } else if (r.getCollectionFlags().hasGroupNameBeenSet()) {
                        ent.setGroupName(r.getCollectionFlags().getGroupName());
                    }
 
                    return ent;
                }
            });
        setFileSetBuilder(new ArchiveBase.FileSetBuilder() {
                @Override
                public ArchiveFileSet buildFileSet(Resource dest) {
                    ArchiveFileSet afs = new TarFileSet();
                    afs.setSrcResource(dest);
                    return afs;
                }
            });
    }

    /**
     * The format to use.
     */
    public void setFormat(Format f) {
        format = f;
    }

    /**
     * The supported tar formats for entries with long file names.
     */
    public final static class Format extends EnumeratedAttribute {
        private static final String USTAR_NAME = "ustar";
        private static final String OLDGNU_NAME = "oldgnu";
        private static final String GNU_NAME = "gnu";
        private static final String STAR_NAME = "star";
        private static final String PAX_NAME = "pax";

        public static final Format USTAR = new Format(USTAR_NAME);
        public static final Format OLDGNU = new Format(OLDGNU_NAME);
        public static final Format GNU = new Format(GNU_NAME);
        public static final Format STAR = new Format(STAR_NAME);
        public static final Format PAX = new Format(PAX_NAME);

        public Format(String v) {
            setValue(v);
        }

        public Format() {
            setValue(USTAR_NAME);
        }

        @Override
        public String[] getValues() {
            return new String[] {
                USTAR_NAME, OLDGNU_NAME, GNU_NAME,
                STAR_NAME, PAX_NAME
            };
        }

        public boolean equals(Object other) {
            return other instanceof Format
                && ((Format) other).getValue().equals(getValue());
        }
    }
}
