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

package org.apache.ant.compress.taskdefs;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

import org.apache.ant.compress.resources.CommonsCompressCompressorResource;
import org.apache.ant.compress.util.CompressorStreamFactory;
import org.apache.ant.compress.util.StreamHelper;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.FileProvider;
import org.apache.tools.ant.types.resources.FileResource;
import org.apache.tools.ant.types.resources.Resources;
import org.apache.tools.ant.util.FileUtils;

/**
 * Abstract Base class for pack tasks.
 */
public abstract class PackBase extends Task {

    private static final int BUFFER_SIZE = 8 * 1024;

    private CompressorStreamFactory factory;
    private final ResourceWrapper wrapper;

    private Resource src;
    private ArchiveBase srcArchiveTask;
    private PackBase srcPackTask;
    private Resource dest;

    protected PackBase(CompressorStreamFactory factory,
                       ResourceWrapper wrapper) {
        this(wrapper);
        setFactory(factory);
    }

    /**
     * @since Apache Compress Antlib 1.1
     */
    protected PackBase(ResourceWrapper wrapper) {
        this.wrapper = wrapper;
    }

    /**
     * @since Apache Compress Antlib 1.1
     */
    protected final void setFactory(CompressorStreamFactory factory) {
        this.factory = factory;
    }

    /**
     * the required destination file.
     */
    public void setDestfile(File dest) {
        setDest(new FileResource(dest));
    }

    /**
     * The resource to pack; required.
     * @param dest resource to expand
     */
    public void setDest(Resource dest) {
        if (this.dest != null) {
            throw new BuildException("Can only have one destination resource.");
        }
        this.dest = dest;
    }

    /**
     * The archive to create.
     */
    public void addConfiguredDest(Resources r) {
        for (Iterator it = r.iterator(); it.hasNext(); ) {
            setDest((Resource) it.next());
        }
    }

    /**
     * the file to compress; required.
     * @param src the source file
     */
    public void setSrcfile(File src) {
        setSrc(new FileResource(src));
    }

    /**
     * The resource to pack; required.
     * @param src resource to expand
     */
    public void setSrc(Resource src) {
        if (this.src != null || srcArchiveTask != null || srcPackTask != null) {
            throw new BuildException("Can only have one source.");
        }
        if (src.isDirectory()) {
            throw new BuildException("the source can't be a directory");
        }
        this.src = src;
    }

    /**
     * Set the source resource.
     * @param a the resource to pack as a single element Resource collection.
     */
    public void addConfigured(ResourceCollection a) {
        for (Iterator it = a.iterator(); it.hasNext(); ) {
            setSrc((Resource) it.next());
        }
    }

    public void add(ArchiveBase task) {
        if (src != null || srcArchiveTask != null || srcPackTask != null) {
            throw new BuildException("Can only have one source.");
        }
        srcArchiveTask = task;
    }

    /**
     * @since Apache Compress Antlib 1.1
     */
    public void add(PackBase task) {
        if (src != null || srcArchiveTask != null || srcPackTask != null) {
            throw new BuildException("Can only have one source.");
        }
        srcPackTask = task;
    }

    /**
     * validation routine
     * @throws BuildException if anything is invalid
     */
    private void validate() throws BuildException {
        if (src == null && srcArchiveTask == null && srcPackTask == null) {
            throw new BuildException("source is required.",
                                     getLocation());
        }

        if (src !=  null) {
            if (src.isDirectory()) {
                throw new BuildException("source resource must not "
                                         + "represent a directory!",
                                         getLocation());
            }

            if (!src.isExists()) {
                throw new BuildException("source resource must exist.");
            }
        }

        if (dest == null) {
            throw new BuildException("dest resource is required.",
                                     getLocation());
        }

        if (dest.isDirectory()) {
            throw new BuildException("dest resource must not "
                                     + "represent a directory!", getLocation());
        }

    }

    /**
     * validate, then hand off to the subclass
     * @throws BuildException on error
     */
    @Override
    public void execute() throws BuildException {
        validate();

        if (srcArchiveTask != null) {
            srcArchiveTask.setDest(wrapper.wrap(dest));
            srcArchiveTask.setTaskName(getTaskName());
            srcArchiveTask.execute();
        } else if (srcPackTask != null) {
            srcPackTask.setDest(wrapper.wrap(dest));
            srcPackTask.setTaskName(getTaskName());
            srcPackTask.execute();
        } else if (dest.isExists()
                   && dest.getLastModified() > src.getLastModified()) {
            log("Nothing to do: " + dest.getName() + " is up to date.");
        } else {
            log("Building: " + dest.getName());
            pack();
        }
    }

    /**
     * packs a resource to an output stream
     * @throws IOException on error
     */
    private void pack() {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = src.getInputStream();
            out = StreamHelper.getOutputStream(factory, dest);
            if (out == null) {
                out =
                    factory.getCompressorStream(new BufferedOutputStream(dest.getOutputStream()));
            }
            IOUtils.copy(in, out, BUFFER_SIZE);
        } catch (IOException e) {
            throw new BuildException("Error compressing " + src.getName()
                                     + " to " + dest.getName(), e);
        } finally {
            FileUtils.close(in);
            FileUtils.close(out);
        }
    }

    /**
     * @since Apache Compress Antlib 1.5
     */
    protected final Resource getSrc() {
        return src;
    }

    public static interface ResourceWrapper {
        CommonsCompressCompressorResource wrap(Resource dest);
    }
}
