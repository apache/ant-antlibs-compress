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
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

/**
 * A Resource representation of an entry in a tar archive.
 */
public class TarResource extends CommonsCompressArchiveResource {

    private String userName = "";
    private String groupName = "";
    private int    uid;
    private int    gid;

    /**
     * Default constructor.
     */
    public TarResource() {
    }

    /**
     * Construct a TarResource representing the specified
     * entry in the specified archive.
     * @param a the archive as File.
     * @param e the TarEntry.
     */
    public TarResource(File a, TarArchiveEntry e) {
        super(a, e);
    }

    /**
     * Construct a TarResource representing the specified
     * entry in the specified archive.
     * @param a the archive as Resource.
     * @param e the TarEntry.
     */
    public TarResource(Resource a, TarArchiveEntry e) {
        super(a, e);
    }

    /**
     * @return the user name for the tar entry
     */
    public String getUserName() {
        if (isReference()) {
            return ((TarResource) getCheckedRef()).getUserName();
        }
        return userName;
    }

    /**
     * @return the group name for the tar entry
     */
    public String getGroup() {
        if (isReference()) {
            return ((TarResource) getCheckedRef()).getGroup();
        }
        return groupName;
    }

    /**
     * @return the uid for the tar entry
     */
    public int getUid() {
        if (isReference()) {
            return ((TarResource) getCheckedRef()).getUid();
        }
        return uid;
    }

    /**
     * @return the uid for the tar entry
     */
    public int getGid() {
        if (isReference()) {
            return ((TarResource) getCheckedRef()).getGid();
        }
        return uid;
    }

    protected void setEntry(ArchiveEntry e) {
        super.setEntry(e);
        if (e != null) {
            TarArchiveEntry te = (TarArchiveEntry) e;
            userName = te.getUserName();
            groupName = te.getGroupName();
            uid = te.getUserId();
            gid = te.getGroupId();
        }
    }

    protected ArchiveInputStream getArchiveStream(InputStream is)
        throws IOException {
        return new TarArchiveInputStream(is);
    }

    protected Date getLastModified(ArchiveEntry entry) {
        return ((TarArchiveEntry) entry).getModTime();
    }

    protected int getMode(ArchiveEntry e) {
        return ((TarArchiveEntry) e).getMode();
    }

    protected String getArchiveType() {
        return "tar";
    }
}
