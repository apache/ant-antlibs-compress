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

package org.apache.ant.compress.util;

import java.util.Date;

import org.apache.tools.ant.BuildException;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ar.ArArchiveEntry;
import org.apache.commons.compress.archivers.cpio.CpioArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;

/**
 * Helper methods that gloss over API differences between the
 * ArchiveEntry implementations of Apache Commons Compress 1.0.
 */
public class EntryHelper {
    private EntryHelper() {}

    /**
     * Can be replaced with entry.getLastModifiedDate in ACC 1.1
     */
    public static Date getLastModified(ArchiveEntry entry) {
        if (entry == null) {
            throw new IllegalArgumentException("entry must not be null.");
        }

        if (entry instanceof ArArchiveEntry) {
            return new Date(((ArArchiveEntry) entry).getLastModified() * 1000);
        }
        if (entry instanceof CpioArchiveEntry) {
            return new Date(((CpioArchiveEntry) entry).getTime() * 1000);
        }
        if (entry instanceof TarArchiveEntry) {
            return ((TarArchiveEntry) entry).getModTime();
        }
        if (entry instanceof ZipArchiveEntry) {
            return new Date(((ZipArchiveEntry) entry).getTime());
        }
        throw new BuildException("archive entry " + entry.getClass()
                                 + " is not supported.");
    }
}