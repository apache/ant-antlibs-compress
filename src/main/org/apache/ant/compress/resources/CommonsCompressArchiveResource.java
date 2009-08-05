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
import java.io.OutputStream;
import java.io.IOException;
import java.util.Date;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.resources.ArchiveResource;
import org.apache.tools.ant.util.FileUtils;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;

/**
 * A Resource representation of an entry in a commons compress archive.
 */
public abstract class CommonsCompressArchiveResource extends ArchiveResource {

    /**
     * Default constructor.
     */
    public CommonsCompressArchiveResource() {
    }

    /**
     * Construct a Resource representing the specified
     * entry in the specified archive.
     * @param a the archive as File.
     * @param e the ArchiveEntry.
     */
    public CommonsCompressArchiveResource(File a, ArchiveEntry e) {
        super(a, true);
        setEntry(e);
    }

    /**
     * Construct a Resource representing the specified
     * entry in the specified archive.
     * @param a the archive as Resource.
     * @param e the ArchiveEntry.
     */
    public CommonsCompressArchiveResource(Resource a, ArchiveEntry e) {
        super(a, true);
        setEntry(e);
    }

    /**
     * Provides an ArchiveInputStream to a given archive.
     */
    protected abstract ArchiveInputStream getArchiveStream(InputStream is)
        throws IOException;

    /**
     * Return an InputStream for reading the contents of this Resource.
     * @return an InputStream object.
     * @throws IOException if the archive cannot be opened,
     *         or the entry cannot be read.
     */
    public InputStream getInputStream() throws IOException {
        if (isReference()) {
            return ((Resource) getCheckedRef()).getInputStream();
        }
        Resource archive = getArchive();
        final ArchiveInputStream i = getArchiveStream(archive.getInputStream());
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
     * The name of the archive type.
     */
    protected abstract String getArchiveType();

    /**
     * Get an OutputStream for the Resource.
     * @return an OutputStream to which content can be written.
     * @throws IOException if unable to provide the content of this
     *         Resource as a stream.
     * @throws UnsupportedOperationException if OutputStreams are not
     *         supported for this Resource type.
     */
    public OutputStream getOutputStream() throws IOException {
        if (isReference()) {
            return ((Resource) getCheckedRef()).getOutputStream();
        }
        throw new UnsupportedOperationException("Use the " + getArchiveType()
                                                + " task for "
                                                + getArchiveType()
                                                + " output.");
    }

    /**
     * fetches information from the named entry inside the archive.
     */
    protected void fetchEntry() {
        Resource archive = getArchive();
        ArchiveInputStream i = null;
        try {
            i = getArchiveStream(archive.getInputStream());
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
        } finally {
            if (i != null) {
                FileUtils.close(i);
            }
        }
        setEntry(null);
    }

    /**
     * Determines the mode for the given entry.
     */
    protected abstract int getMode(ArchiveEntry e);

    /**
     * Determines the last modified time for the given entry.
     */
    protected abstract Date getLastModified(ArchiveEntry entry);

    protected void setEntry(ArchiveEntry e) {
        if (e == null) {
            setExists(false);
            return;
        }
        setName(e.getName());
        setExists(true);
        setLastModified(getLastModified(e).getTime());
        setDirectory(e.isDirectory());
        setSize(e.getSize());
        setMode(getMode(e));
    }

}
