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
package org.apache.ant.compress.resources;

import java.io.File;

import org.apache.tools.ant.types.Resource;
import org.apache.ant.compress.util.TarStreamFactory;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;

/**
 * A Resource representation of an entry in a tar archive.
 */
public final class TarResource extends CommonsCompressArchiveResource {

    private String userName = "";
    private String groupName = "";

    /**
     * Default constructor.
     */
    public TarResource() {
        super(new TarStreamFactory(), "tar");
    }

    /**
     * Construct a TarResource representing the specified
     * entry in the specified archive.
     * @param a the archive as File.
     * @param e the TarEntry.
     */
    public TarResource(File a, TarArchiveEntry e) {
        super(new TarStreamFactory(), "tar", a, e);
        setEntry(e);
    }

    /**
     * Construct a TarResource representing the specified
     * entry in the specified archive.
     * @param a the archive as Resource.
     * @param e the TarEntry.
     */
    public TarResource(Resource a, TarArchiveEntry e) {
        super(new TarStreamFactory(), "tar", a, e);
        setEntry(e);
    }

    /**
     * Construct a TarResource representing the specified
     * entry in the specified archive.
     * @param a the archive as File.
     * @param enc the encoding used for filenames.
     * @param e the TarEntry.
     * @since Compress Antlib 1.2
     */
    public TarResource(File a, String enc, TarArchiveEntry e) {
        this(a, e);
        setEncoding(enc);
    }

    /**
     * Construct a TarResource representing the specified
     * entry in the specified archive.
     * @param a the archive as Resource.
     * @param enc the encoding used for filenames.
     * @param e the TarEntry.
     * @since Compress Antlib 1.2
     */
    public TarResource(Resource a, String enc, TarArchiveEntry e) {
        this(a, e);
        setEncoding(enc);
    }

    /**
     * @return the user name for the tar entry
     */
    public String getUserName() {
        if (isReference()) {
            return ((TarResource) getCheckedRef()).getUserName();
        }
        checkEntry();
        return userName;
    }

    /**
     * @return the group name for the tar entry
     */
    public String getGroup() {
        if (isReference()) {
            return ((TarResource) getCheckedRef()).getGroup();
        }
        checkEntry();
        return groupName;
    }

    @Override
    protected void setEntry(ArchiveEntry e) {
        super.setEntry(e);
        if (e != null) {
            TarArchiveEntry te = (TarArchiveEntry) e;
            userName = te.getUserName();
            groupName = te.getGroupName();
        }
    }

}
