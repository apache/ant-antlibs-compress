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
import org.apache.commons.compress.archivers.ar.ArArchiveEntry;
import org.apache.commons.compress.archivers.ar.ArArchiveInputStream;

/**
 * A Resource representation of an entry in a ar archive.
 */
public class ArResource extends CommonsCompressArchiveResource {

    private int    uid;
    private int    gid;

    /**
     * Default constructor.
     */
    public ArResource() {
    }

    /**
     * Construct a ArResource representing the specified
     * entry in the specified archive.
     * @param a the archive as File.
     * @param e the ArEntry.
     */
    public ArResource(File a, ArArchiveEntry e) {
        super(a, e);
    }

    /**
     * Construct a ArResource representing the specified
     * entry in the specified archive.
     * @param a the archive as Resource.
     * @param e the ArEntry.
     */
    public ArResource(Resource a, ArArchiveEntry e) {
        super(a, e);
    }

    /**
     * @return the uid for the ar entry
     */
    public int getUid() {
        if (isReference()) {
            return ((ArResource) getCheckedRef()).getUid();
        }
        return uid;
    }

    /**
     * @return the uid for the ar entry
     */
    public int getGid() {
        if (isReference()) {
            return ((ArResource) getCheckedRef()).getGid();
        }
        return uid;
    }

    protected void setEntry(ArchiveEntry e) {
        super.setEntry(e);
        if (e != null) {
            ArArchiveEntry ae = (ArArchiveEntry) e;
            uid = ae.getUserId();
            gid = ae.getGroupId();
        }
    }

    protected ArchiveInputStream getArchiveStream(InputStream is)
        throws IOException {
        return new ArArchiveInputStream(is);
    }

    protected Date getLastModified(ArchiveEntry entry) {
        return new Date(((ArArchiveEntry) entry).getLastModified() * 1000);
    }

    protected int getMode(ArchiveEntry e) {
        return ((ArArchiveEntry) e).getMode();
    }

    protected String getArchiveType() {
        return "ar";
    }
}
