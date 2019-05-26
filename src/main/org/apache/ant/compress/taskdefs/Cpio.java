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
import org.apache.ant.compress.util.CpioStreamFactory;
import org.apache.ant.compress.resources.CpioFileSet;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.cpio.CpioArchiveEntry;
import org.apache.commons.compress.archivers.cpio.CpioArchiveOutputStream;
import org.apache.commons.compress.archivers.cpio.CpioConstants;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.ArchiveFileSet;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.types.Resource;

/**
 * Creates cpio archives.
 */
public class Cpio extends ArchiveBase {
    private Format format = Format.BINARY;
    private int blockSize = CpioConstants.BLOCK_SIZE;

    public Cpio() {
        setFactory(new CpioStreamFactory() {
                @Override
                public ArchiveOutputStream getArchiveStream(OutputStream stream,
                                                            String encoding)
                    throws IOException {
                    return new CpioArchiveOutputStream(stream,
                                                       format.getFormat(),
                                                       blockSize,
                                                       encoding);
                }
            });
        setEntryBuilder(
              new ArchiveBase.EntryBuilder() {
                @Override
                public ArchiveEntry buildEntry(ArchiveBase.ResourceWithFlags r) {
                    boolean isDir = r.getResource().isDirectory();
                    CpioArchiveEntry ent =
                        new CpioArchiveEntry(format.getFormat(), r.getName(),
                                             isDir
                                             ? 0 : r.getResource().getSize());
                    ent.setTime(round(r.getResource().getLastModified(), 1000)
                                / 1000);

                    int mode = isDir
                        ? ArchiveFileSet.DEFAULT_DIR_MODE
                        : ArchiveFileSet.DEFAULT_FILE_MODE;
                    if (!isDir && r.getCollectionFlags().hasModeBeenSet()) {
                        ent.setMode(r.getCollectionFlags().getMode());
                    } else if (isDir
                               && r.getCollectionFlags().hasDirModeBeenSet()) {
                        ent.setMode(r.getCollectionFlags().getDirMode());
                    } else if (r.getResourceFlags().hasModeBeenSet()) {
                        ent.setMode(r.getResourceFlags().getMode());
                    } else {
                        ent.setMode(mode);
                    }

                    if (r.getResourceFlags().hasUserIdBeenSet()) {
                        ent.setUID(r.getResourceFlags().getUserId());
                    } else if (r.getCollectionFlags().hasUserIdBeenSet()) {
                        ent.setUID(r.getCollectionFlags().getUserId());
                    }

                    if (r.getResourceFlags().hasGroupIdBeenSet()) {
                        ent.setGID(r.getResourceFlags().getGroupId());
                    } else if (r.getCollectionFlags().hasGroupIdBeenSet()) {
                        ent.setGID(r.getCollectionFlags().getGroupId());
                    }
 
                    return ent;
                }
            });
        setFileSetBuilder(new ArchiveBase.FileSetBuilder() {
                @Override
                public ArchiveFileSet buildFileSet(Resource dest) {
                    ArchiveFileSet afs = new CpioFileSet();
                    afs.setSrcResource(dest);
                    return afs;
                }
            });
    }

    /**
     * The format to use.
     * <p>Default is binary</p>
     */
    public void setFormat(Format f) {
        format = f;
    }

    /**
     * The blocksize to use.
     * <p>Default is 512 bytes.</p>
     */
    public void setBlockSize(int size) {
        if (size <= 0) {
            throw new BuildException("Block size must be a positive number");
        }
        blockSize = size;
    }

    /**
     * The supported cpio formats.
     */
    public static class Format extends EnumeratedAttribute {
        private static final String BINARY_NAME = "binary";
        private static final String OLD_ASCII_NAME = "old-ascii";
        private static final String ODC_NAME = "odc";
        private static final String NEW_ASCII_NAME = "new-ascii";
        private static final String CRC_NAME = "crc";

        public static final Format BINARY = new Format(BINARY_NAME);
        public static final Format OLD_ASCII = new Format(OLD_ASCII_NAME);
        public static final Format ODC = new Format(ODC_NAME);
        public static final Format NEW_ASCII = new Format(NEW_ASCII_NAME);
        //public static final Format CRC = new Format(CRC_NAME);

        public Format(String v) {
            setValue(v);
        }

        public Format() {
            setValue(BINARY_NAME);
        }

        @Override
        public String[] getValues() {
            return new String[] {
                BINARY_NAME, OLD_ASCII_NAME, ODC_NAME, NEW_ASCII_NAME, //CRC_NAME
            };
        }

        public short getFormat() {
            String v = getValue();
            if (v.equals(OLD_ASCII_NAME) || v.equals(ODC_NAME)) {
                return CpioConstants.FORMAT_OLD_ASCII;
            }
            if (v.equals(NEW_ASCII_NAME)) {
                return CpioConstants.FORMAT_NEW;
            }
            if (v.equals(CRC_NAME)) {
                return CpioConstants.FORMAT_NEW_CRC;
            }
            return CpioConstants.FORMAT_OLD_BINARY;
        }

        public boolean equals(Object other) {
            return other instanceof Format
                && ((Format) other).getFormat() == getFormat();
        }
    }
}
