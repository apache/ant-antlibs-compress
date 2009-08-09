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

import java.io.BufferedInputStream;
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
import org.apache.ant.compress.util.EntryHelper;
import org.apache.ant.compress.util.StreamFactory;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;

/**
 * A Resource representation of an entry in a commons compress archive.
 */
public abstract class CommonsCompressArchiveResource extends ArchiveResource {

    private String encoding;
    private final StreamFactory factory;

    /**
     * Default constructor.
     */
    protected CommonsCompressArchiveResource(StreamFactory factory) {
        this.factory = factory;
    }

    /**
     * Construct a Resource representing the specified
     * entry in the specified archive.
     * @param a the archive as File.
     * @param e the ArchiveEntry.
     */
    protected CommonsCompressArchiveResource(StreamFactory factory, File a,
                                             ArchiveEntry e) {
        super(a, true);
        this.factory = factory;
        setEntry(e);
    }

    /**
     * Construct a Resource representing the specified
     * entry in the specified archive.
     * @param a the archive as Resource.
     * @param e the ArchiveEntry.
     */
    protected CommonsCompressArchiveResource(StreamFactory factory, Resource a,
                                             ArchiveEntry e) {
        super(a, true);
        this.factory = factory;
        setEntry(e);
    }

    /**
     * Set the encoding to use with the zipfile.
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
        final ArchiveInputStream i =
            factory.getArchiveStream(new BufferedInputStream(archive.getInputStream()),
                                     getEncoding());
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
            i = factory.getArchiveStream(archive.getInputStream(),
                                         getEncoding());
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

    protected void setEntry(ArchiveEntry e) {
        if (e == null) {
            setExists(false);
            return;
        }
        setName(e.getName());
        setExists(true);
        setLastModified(EntryHelper.getLastModified(e).getTime());
        setDirectory(e.isDirectory());
        setSize(e.getSize());
        setMode(getMode(e));
    }

}
