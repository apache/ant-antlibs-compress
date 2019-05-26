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
import java.io.InputStream;
import java.io.IOException;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.resources.FileProvider;
import org.apache.tools.ant.util.FileUtils;

import org.apache.ant.compress.util.SevenZStreamFactory;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.sevenz.SevenZMethodConfiguration;

/**
 * A Resource representation of an entry in a sevenzfile.
 *
 * @since Apache Compress Antlib 1.3
 */
public final class SevenZResource extends CommonsCompressArchiveResource {

    private Iterable<? extends SevenZMethodConfiguration> contentMethods;

    /**
     * Default constructor.
     */
    public SevenZResource() {
        super(new SevenZStreamFactory(), "7z");
    }

    /**
     * Construct a SevenZResource representing the specified
     * entry in the specified 7z file.
     * @param z the 7z file as File.
     * @param enc the encoding used for filenames - ignored.
     * @param e the SevenZEntry.
     */
    public SevenZResource(File z, String enc, SevenZArchiveEntry e) {
        super(new SevenZStreamFactory(), "7z", z, e);
        setEntry(e);
    }

    /**
     * Construct a SevenZResource representing the specified
     * entry in the specified 7z archive.
     * @param z the 7z archive
     * @param enc the encoding used for filenames - ignored.
     * @param e the SevenZEntry.
     */
    public SevenZResource(Resource z, String enc, SevenZArchiveEntry e) {
        super(new SevenZStreamFactory(), "sevenz", z, e);
        setEntry(e);
    }

    /**
     * Set the 7z that holds this SevenZResource.
     * @param z the 7z file as a File.
     */
    public void setSevenZFile(File z) {
        setArchive(z);
    }

    /**
     * Set the 7z file that holds this SevenZResource.
     * @param z the 7z as a Resource.
     */
    public void setSevenZResource(Resource z) {
        addConfigured(z);
    }

    /**
     * Get the 7z file that holds this SevenZResource.
     * @return the 7z file as a File or null if it is not a file.
     */
    public File getSevenZFile() {
        FileProvider fp = (FileProvider) getArchive().as(FileProvider.class);
        return fp != null ? fp.getFile() : null;
    }

    /**
     * Return an InputStream for reading the contents of this Resource.
     * @return an InputStream object.
     * @throws IOException if the sevenz file cannot be opened,
     *         or the entry cannot be read.
     */
    @Override
    public InputStream getInputStream() throws IOException {
        if (isReference()) {
            return ((Resource) getCheckedRef()).getInputStream();
        }
        File f = getSevenZFile();
        if (f == null) {
            return super.getInputStream();
        }

        final SevenZFile z = new SevenZFile(f);
        SevenZArchiveEntry ze = z.getNextEntry();
        while (ze != null) {
            if (ze.getName().equals(getName())) {
                return new InputStream() {
                    @Override
                    public int read() throws IOException {
                        return z.read();
                    }
                    @Override
                    public int read(byte[] b) throws IOException {
                        return z.read(b);
                    }
                    @Override
                    public void close() throws IOException {
                        z.close();
                    }
                    @Override
                    protected void finalize() throws Throwable {
                        try {
                            close();
                        } finally {
                            super.finalize();
                        }
                    }
                };
            }
            ze = z.getNextEntry();
        }
        z.close();
        throw new BuildException("no entry " + getName() + " in "
                                 + getArchive());
    }

    /**
     * Gets the (compression) methods to used for entry's content.
     *
     * @since 1.5
     */
    public Iterable/*<? extends SevenZMethodConfiguration>*/ getContentMethods() {
        return contentMethods;
    }

    /**
     * fetches information from the named entry inside the archive.
     */
    @Override
    protected void fetchEntry() {
        File f = getSevenZFile();
        if (f == null) {
            super.fetchEntry();
            return;
        }

        try (SevenZFile z = new SevenZFile(f)) {
            SevenZArchiveEntry ze = z.getNextEntry();
            while (ze != null) {
                if (ze.getName().equals(getName())) {
                    setEntry(ze);
                    return;
                }
                ze = z.getNextEntry();
            }
            setEntry(null);
        } catch (IOException e) {
            log(e.getMessage(), Project.MSG_DEBUG);
            throw new BuildException(e);
        }
    }

    @Override
    protected void setEntry(ArchiveEntry e) {
        super.setEntry(e);
        if (e != null) {
            SevenZArchiveEntry ze = (SevenZArchiveEntry) e;
            contentMethods = ze.getContentMethods();
        }
    }

}
