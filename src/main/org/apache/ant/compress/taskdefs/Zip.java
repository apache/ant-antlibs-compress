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
import java.io.OutputStream;
import java.util.zip.Deflater;

import org.apache.ant.compress.util.ZipStreamFactory;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.tools.ant.types.ArchiveFileSet;

/**
 * Creates zip archives.
 */
public class Zip extends ArchiveBase {
    private int level = Deflater.DEFAULT_COMPRESSION;
    private String comment = "";
    private boolean keepCompression = false;
    private boolean fallBackToUTF8 = false;

    public Zip() {
        setFactory(new ZipStreamFactory() {
                public ArchiveOutputStream getArchiveStream(OutputStream stream,
                                                            String encoding)
        throws IOException {
                    ZipArchiveOutputStream o =
                        (ZipArchiveOutputStream) super.getArchiveStream(stream,
                                                                        encoding);
                    o.setLevel(level);
                    o.setComment(comment);
                    o.setFallbackToUTF8(fallBackToUTF8);
                    return o;
                }
            });
        setBuilder(
              new ArchiveBase.EntryBuilder() {
                public ArchiveEntry buildEntry(ArchiveBase.ResourceWithFlags r) {
                    boolean isDir = r.getResource().isDirectory();
                    ZipArchiveEntry ent = new ZipArchiveEntry(r.getName());
                    ent.setTime(round(r.getResource().getLastModified(), 2000));
                    ent.setSize(isDir ? 0 : r.getResource().getSize());

                    if (!isDir && r.getCollectionFlags().hasModeBeenSet()) {
                        ent.setUnixMode(r.getCollectionFlags().getMode());
                    } else if (isDir
                               && r.getCollectionFlags().hasDirModeBeenSet()) {
                        ent.setUnixMode(r.getCollectionFlags().getDirMode());
                    } else if (r.getResourceFlags().hasModeBeenSet()) {
                        ent.setUnixMode(r.getResourceFlags().getMode());
                    } else {
                        ent.setUnixMode(isDir
                                        ? ArchiveFileSet.DEFAULT_DIR_MODE
                                        : ArchiveFileSet.DEFAULT_FILE_MODE);
                    }

                    if (r.getResourceFlags().getZipExtraFields() != null) {
                        ent.setExtraFields(r.getResourceFlags()
                                           .getZipExtraFields());
                    }
 
                    if (keepCompression
                        && r.getResourceFlags().hasCompressionMethod()) {
                        ent.setMethod(r.getResourceFlags()
                                      .getCompressionMethod());
                    }

                    return ent;
                }
            });
    }

    /**
     * Set the compression level to use.  Default is
     * Deflater.DEFAULT_COMPRESSION.
     * @param level compression level.
     */
    public void setLevel(int level) {
        this.level = level;
    }

    /**
     * Comment to use for archive.
     *
     * @param comment The content of the comment.
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Whether the original compression of entries coming from a ZIP
     * archive should be kept (for example when updating an archive).
     * Default is false.
     * @param keep if true, keep the original compression
     */
    public void setKeepCompression(boolean keep) {
        keepCompression = keep;
    }

    /**
     * Whether to fall back to UTF-8 if a name cannot be enoded using
     * the specified encoding.
     *
     * <p>Defaults to false.</p>
     */
    public void setFallBackToUTF8(boolean b) {
        fallBackToUTF8 = b;
    }
}