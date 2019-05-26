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
import java.io.FilterInputStream;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.resources.FileProvider;
import org.apache.tools.ant.util.FileUtils;

import org.apache.ant.compress.util.ZipStreamFactory;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipExtraField;
import org.apache.commons.compress.archivers.zip.ZipFile;

/**
 * A Resource representation of an entry in a zipfile.
 */
public final class ZipResource extends CommonsCompressArchiveResource {

    private ZipExtraField[] extras;
    private int method;

    /**
     * Default constructor.
     */
    public ZipResource() {
        super(new ZipStreamFactory(), "zip");
    }

    /**
     * Construct a ZipResource representing the specified
     * entry in the specified zipfile.
     * @param z the zipfile as File.
     * @param enc the encoding used for filenames.
     * @param e the ZipEntry.
     */
    public ZipResource(File z, String enc, ZipArchiveEntry e) {
        super(new ZipStreamFactory(), "zip", z, e);
        setEncoding(enc);
        setEntry(e);
    }

    /**
     * Construct a ZipResource representing the specified
     * entry in the specified zip archive.
     * @param z the zipfile as File.
     * @param enc the encoding used for filenames.
     * @param e the ZipEntry.
     */
    public ZipResource(Resource z, String enc, ZipArchiveEntry e) {
        super(new ZipStreamFactory(), "zip", z, e);
        setEncoding(enc);
        setEntry(e);
    }

    /**
     * Set the zipfile that holds this ZipResource.
     * @param z the zipfile as a File.
     */
    public void setZipfile(File z) {
        setArchive(z);
    }

    /**
     * Set the zipfile that holds this ZipResource.
     * @param z the zipfile as a Resource.
     */
    public void setZipResource(Resource z) {
        addConfigured(z);
    }

    /**
     * Get the zipfile that holds this ZipResource.
     * @return the zipfile as a File or null if it is not a file.
     */
    public File getZipfile() {
        FileProvider fp = (FileProvider) getArchive().as(FileProvider.class);
        return fp != null ? fp.getFile() : null;
    }

    /**
     * Return an InputStream for reading the contents of this Resource.
     * @return an InputStream object.
     * @throws IOException if the zip file cannot be opened,
     *         or the entry cannot be read.
     */
    @Override
    public InputStream getInputStream() throws IOException {
        if (isReference()) {
            return ((Resource) getCheckedRef()).getInputStream();
        }
        File f = getZipfile();
        if (f == null) {
            return super.getInputStream();
        }

        final ZipFile z = new ZipFile(f, getEncoding());
        ZipArchiveEntry ze = z.getEntry(getName());
        if (ze == null) {
            z.close();
            throw new BuildException("no entry " + getName() + " in "
                                     + getArchive());
        }
        return new FilterInputStream(z.getInputStream(ze)) {
            @Override
            public void close() throws IOException {
                FileUtils.close(in);
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

    /**
     * Retrieves extra fields.
     * @return an array of the extra fields
     */
    public ZipExtraField[] getExtraFields() {
        if (isReference()) {
            return ((ZipResource) getCheckedRef()).getExtraFields();
        }
        checkEntry();
        if (extras == null) {
            return new ZipExtraField[0];
        }
        return extras;
    }

    /**
     * The compression method that has been used.
     */
    public int getMethod() {
        return method;
    }

    /**
     * fetches information from the named entry inside the archive.
     */
    @Override
    protected void fetchEntry() {
        File f = getZipfile();
        if (f == null) {
            super.fetchEntry();
            return;
        }

        try (ZipFile z = new ZipFile(getZipfile(), getEncoding())) {
            setEntry(z.getEntry(getName()));
        } catch (IOException e) {
            log(e.getMessage(), Project.MSG_DEBUG);
            throw new BuildException(e);
        }
    }

    protected void setEntry(ArchiveEntry e) {
        super.setEntry(e);
        if (e != null) {
            ZipArchiveEntry ze = (ZipArchiveEntry) e;
            extras = ze.getExtraFields(true);
            method = ze.getMethod();
        }
    }

}
