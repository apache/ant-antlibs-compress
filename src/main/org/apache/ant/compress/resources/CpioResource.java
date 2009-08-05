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
package org.apache.ant.compress.resources;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.util.Date;

import org.apache.tools.ant.types.Resource;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.cpio.CpioArchiveEntry;
import org.apache.commons.compress.archivers.cpio.CpioArchiveInputStream;

/**
 * A Resource representation of an entry in a cpio archive.
 */
public class CpioResource extends CommonsCompressArchiveResource {

    private long    uid;
    private long    gid;

    /**
     * Default constructor.
     */
    public CpioResource() {
    }

    /**
     * Construct a CpioResource representing the specified
     * entry in the specified archive.
     * @param a the archive as File.
     * @param e the CpioEntry.
     */
    public CpioResource(File a, CpioArchiveEntry e) {
        super(a, e);
    }

    /**
     * Construct a CpioResource representing the specified
     * entry in the specified archive.
     * @param a the archive as Resource.
     * @param e the CpioEntry.
     */
    public CpioResource(Resource a, CpioArchiveEntry e) {
        super(a, e);
    }

    /**
     * @return the uid for the cpio entry
     */
    public long getUid() {
        if (isReference()) {
            return ((CpioResource) getCheckedRef()).getUid();
        }
        return uid;
    }

    /**
     * @return the uid for the cpio entry
     */
    public long getGid() {
        if (isReference()) {
            return ((CpioResource) getCheckedRef()).getGid();
        }
        return uid;
    }

    protected void setEntry(ArchiveEntry e) {
        super.setEntry(e);
        if (e != null) {
            CpioArchiveEntry ce = (CpioArchiveEntry) e;
            uid = ce.getUID();
            gid = ce.getGID();
        }
    }

    protected ArchiveInputStream getArchiveStream(InputStream is)
        throws IOException {
        return new CpioArchiveInputStream(is);
    }

    protected Date getLastModified(ArchiveEntry entry) {
        return new Date(((CpioArchiveEntry) entry).getTime() * 1000);
    }

    protected int getMode(ArchiveEntry e) {
        return (int) ((CpioArchiveEntry) e).getMode();
    }

    protected String getArchiveType() {
        return "cpio";
    }
}
