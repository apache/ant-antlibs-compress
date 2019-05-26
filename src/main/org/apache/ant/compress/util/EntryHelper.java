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

package org.apache.ant.compress.util;

import java.util.Date;

import org.apache.tools.ant.BuildException;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ar.ArArchiveEntry;
import org.apache.commons.compress.archivers.arj.ArjArchiveEntry;
import org.apache.commons.compress.archivers.cpio.CpioArchiveEntry;
import org.apache.commons.compress.archivers.dump.DumpArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;

/**
 * Helper methods that gloss over API differences between the
 * ArchiveEntry implementations of Apache Commons Compress 1.3.
 */
public class EntryHelper {
    private EntryHelper() {}

    // REVISIT: are the "mode" formats really compatible with each other?
    /**
     * Extracts the permission bits from an entry.
     */
    public static int getMode(ArchiveEntry entry) {
        if (entry == null) {
            throw new IllegalArgumentException("entry must not be null.");
        }

        if (entry instanceof ArArchiveEntry) {
            return ((ArArchiveEntry) entry).getMode();
        }
        if (entry instanceof ArjArchiveEntry) {
            return ((ArjArchiveEntry) entry).getUnixMode();
        }
        if (entry instanceof CpioArchiveEntry) {
            return (int) ((CpioArchiveEntry) entry).getMode();
        }
        if (entry instanceof SevenZArchiveEntry) {
            return UNKNOWN_ID;
        }
        if (entry instanceof TarArchiveEntry) {
            return ((TarArchiveEntry) entry).getMode();
        }
        if (entry instanceof ZipArchiveEntry) {
            return ((ZipArchiveEntry) entry).getUnixMode();
        }
        if (entry instanceof DumpArchiveEntry) {
            return ((DumpArchiveEntry) entry).getMode();
        }
        throw new BuildException("archive entry " + entry.getClass()
                                 + " is not supported.");
    }

    public static int UNKNOWN_ID = Integer.MIN_VALUE;

    /**
     * Extracts the user id an entry.
     */
    public static int getUserId(ArchiveEntry entry) {
        if (entry == null) {
            throw new IllegalArgumentException("entry must not be null.");
        }

        if (entry instanceof ArArchiveEntry) {
            return ((ArArchiveEntry) entry).getUserId();
        }
        if (entry instanceof ArjArchiveEntry) {
            return UNKNOWN_ID;
        }
        if (entry instanceof CpioArchiveEntry) {
            return (int) ((CpioArchiveEntry) entry).getUID();
        }
        if (entry instanceof SevenZArchiveEntry) {
            return UNKNOWN_ID;
        }
        if (entry instanceof TarArchiveEntry) {
            return ((TarArchiveEntry) entry).getUserId();
        }
        if (entry instanceof ZipArchiveEntry) {
            return UNKNOWN_ID;
        }
        if (entry instanceof DumpArchiveEntry) {
            return ((DumpArchiveEntry) entry).getUserId();
        }
        throw new BuildException("archive entry " + entry.getClass()
                                 + " is not supported.");
    }

    /**
     * Extracts the group id an entry.
     */
    public static int getGroupId(ArchiveEntry entry) {
        if (entry == null) {
            throw new IllegalArgumentException("entry must not be null.");
        }

        if (entry instanceof ArArchiveEntry) {
            return ((ArArchiveEntry) entry).getGroupId();
        }
        if (entry instanceof ArjArchiveEntry) {
            return UNKNOWN_ID;
        }
        if (entry instanceof CpioArchiveEntry) {
            return (int) ((CpioArchiveEntry) entry).getGID();
        }
        if (entry instanceof SevenZArchiveEntry) {
            return UNKNOWN_ID;
        }
        if (entry instanceof TarArchiveEntry) {
            return ((TarArchiveEntry) entry).getGroupId();
        }
        if (entry instanceof ZipArchiveEntry) {
            return UNKNOWN_ID;
        }
        if (entry instanceof DumpArchiveEntry) {
            return (int) ((DumpArchiveEntry) entry).getGroupId();
        }
        throw new BuildException("archive entry " + entry.getClass()
                                 + " is not supported.");
    }
}
