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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.resources.ArchiveResource;
import org.apache.tools.ant.util.FileUtils;
import org.apache.ant.compress.util.ArchiveStreamFactory;
import org.apache.ant.compress.util.EntryHelper;
import org.apache.ant.compress.util.StreamHelper;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;

/**
 * A Resource representation of an entry in a commons compress archive.
 */
public abstract class CommonsCompressArchiveResource extends ArchiveResource {

    private String encoding;
    private final ArchiveStreamFactory factory;
    private final String archiveType;

    // not supported for zip
    private int gid, uid;

    /**
     * Default constructor.
     */
    protected CommonsCompressArchiveResource(ArchiveStreamFactory factory,
                                             String archiveType) {
        this.factory = factory;
        this.archiveType = archiveType;
    }

    /**
     * Construct a Resource representing the specified
     * entry in the specified archive.
     * @param a the archive as File.
     * @param e the ArchiveEntry.
     */
    protected CommonsCompressArchiveResource(ArchiveStreamFactory factory,
                                             String archiveType,
                                             File a, ArchiveEntry e) {
        super(a, true);
        this.factory = factory;
        this.archiveType = archiveType;
    }

    /**
     * Construct a Resource representing the specified
     * entry in the specified archive.
     * @param a the archive as Resource.
     * @param e the ArchiveEntry.
     */
    protected CommonsCompressArchiveResource(ArchiveStreamFactory factory,
                                             String archiveType,
                                             Resource a, ArchiveEntry e) {
        super(a, true);
        this.factory = factory;
        this.archiveType = archiveType;
    }

    /**
     * Set the encoding to use with the archive.
     * @param enc the String encoding.
     */
    public void setEncoding(String enc) {
        checkAttributesAllowed();
        encoding = enc;
    }

    /**
     * Get the encoding to use with the zipfile.
     * @return String encoding.
     */
    public String getEncoding() {
        return isReference()
            ? ((CommonsCompressArchiveResource) getCheckedRef()).getEncoding()
            : encoding;
    }

    /**
     * Overrides the super version.
     * @param r the Reference to set.
     */
    @Override
    public void setRefid(Reference r) {
        if (getEncoding() != null) {
            throw tooManyAttributes();
        }
        super.setRefid(r);
    }

    /**
     * Return an InputStream for reading the contents of this Resource.
     * @return an InputStream object.
     * @throws IOException if the archive cannot be opened,
     *         or the entry cannot be read.
     */
    @Override
    public InputStream getInputStream() throws IOException {
        if (isReference()) {
            return ((Resource) getCheckedRef()).getInputStream();
        }
        final ArchiveInputStream i = getStream();
        ArchiveEntry ae = null;
        while ((ae = i.getNextEntry()) != null) {
            if (ae.getName().equals(getName())) {
                return i;
            }
        }

        FileUtils.close(i);
        throw new BuildException("no entry " + getName() + " in "
                                 + getArchive());
    }

    /**
     * Get an OutputStream for the Resource.
     * @return an OutputStream to which content can be written.
     * @throws IOException if unable to provide the content of this
     *         Resource as a stream.
     * @throws UnsupportedOperationException if OutputStreams are not
     *         supported for this Resource type.
     */
    @Override
    public OutputStream getOutputStream() throws IOException {
        if (isReference()) {
            return ((Resource) getCheckedRef()).getOutputStream();
        }
        throw new UnsupportedOperationException("Use the " + archiveType
                                                + " task for "
                                                + archiveType
                                                + " output.");
    }

    /**
     * @return the uid for the entry
     */
    public int getUid() {
        if (isReference()) {
            return ((CommonsCompressArchiveResource) getCheckedRef()).getUid();
        }
        checkEntry();
        return uid;
    }

    /**
     * @return the gid for the entry
     */
    public int getGid() {
        if (isReference()) {
            return ((CommonsCompressArchiveResource) getCheckedRef()).getGid();
        }
        checkEntry();
        return gid;
    }

    /**
     * fetches information from the named entry inside the archive.
     */
    @Override
    protected void fetchEntry() {
        try (ArchiveInputStream i = getStream()) {
            ArchiveEntry ae = null;
            while ((ae = i.getNextEntry()) != null) {
                if (ae.getName().equals(getName())) {
                    setEntry(ae);
                    return;
                }
            }
        } catch (IOException e) {
            log(e.getMessage(), Project.MSG_DEBUG);
            throw new BuildException(e);
        }
        setEntry(null);
    }

    protected void setEntry(ArchiveEntry e) {
        if (e == null) {
            setExists(false);
            return;
        }
        setName(e.getName());
        setExists(true);
        setLastModified(e.getLastModifiedDate().getTime());
        setDirectory(e.isDirectory());
        setSize(e.getSize());
        setMode(EntryHelper.getMode(e));
        uid = EntryHelper.getUserId(e);
        gid = EntryHelper.getGroupId(e);
    }

    private ArchiveInputStream getStream() throws IOException {
        Resource archive = getArchive();
        ArchiveInputStream s = StreamHelper.getInputStream(factory, archive,
                                                           getEncoding());
        return s != null ? s :
            factory.getArchiveStream(new BufferedInputStream(archive
                                                             .getInputStream()),
                                     getEncoding());
    }
}
