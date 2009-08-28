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
import java.util.HashMap;
import java.util.Map;
import java.util.zip.Deflater;

import org.apache.ant.compress.util.ZipStreamFactory;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.tools.ant.types.ArchiveFileSet;
import org.apache.tools.ant.types.EnumeratedAttribute;

/**
 * Creates zip archives.
 */
public class Zip extends ArchiveBase {
    private int level = Deflater.DEFAULT_COMPRESSION;
    private String comment = "";
    private boolean keepCompression = false;
    private boolean fallBackToUTF8 = false;
    private boolean useLanguageEncodingFlag = true;
    private UnicodeExtraField createUnicodeExtraFields = UnicodeExtraField.NEVER;

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
                    o.setUseLanguageEncodingFlag(useLanguageEncodingFlag);
                    o.setCreateUnicodeExtraFields(createUnicodeExtraFields
                                                  .getPolicy());
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

    /**
     * Whether to set the language encoding flag.
     */
    public void setUseLanguageEncodingFlag(boolean b) {
        useLanguageEncodingFlag = b;
    }

    /**
     * Whether Unicode extra fields will be created.
     */
    public void setCreateUnicodeExtraFields(UnicodeExtraField b) {
        createUnicodeExtraFields = b;
    }

    /**
     * Policiy for creation of Unicode extra fields: never, always or
     * not-encodeable.
     */
    public static final class UnicodeExtraField extends EnumeratedAttribute {
        private static final Map POLICIES = new HashMap();
        private static final String NEVER_KEY = "never";
        private static final String ALWAYS_KEY = "always";
        private static final String N_E_KEY = "not-encodeable";
        static {
            POLICIES.put(NEVER_KEY,
                         ZipArchiveOutputStream.UnicodeExtraFieldPolicy.NEVER);
            POLICIES.put(ALWAYS_KEY,
                         ZipArchiveOutputStream.UnicodeExtraFieldPolicy.ALWAYS);
            POLICIES.put(N_E_KEY,
                         ZipArchiveOutputStream.UnicodeExtraFieldPolicy
                         .NOT_ENCODEABLE);
        }

        public String[] getValues() {
            return new String[] {NEVER_KEY, ALWAYS_KEY, N_E_KEY};
        }

        public static final UnicodeExtraField NEVER =
            new UnicodeExtraField(NEVER_KEY);

        private UnicodeExtraField(String name) {
            setValue(name);
        }

        public UnicodeExtraField() {
        }

        public ZipArchiveOutputStream.UnicodeExtraFieldPolicy getPolicy() {
            return (ZipArchiveOutputStream.UnicodeExtraFieldPolicy)
                POLICIES.get(getValue());
        }
    }
}