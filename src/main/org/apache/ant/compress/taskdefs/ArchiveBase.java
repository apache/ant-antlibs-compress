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

package org.apache.ant.compress.taskdefs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.ant.compress.util.StreamFactory;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.FileResource;

/**
 * Base implementation of tasks creating archives.
 */
public abstract class ArchiveBase extends Task {
    private final StreamFactory factory;

    private Resource dest;
    private List/*<ResourceCollection>*/ sources = new ArrayList();
    private Mode mode = new Mode();

    protected ArchiveBase(StreamFactory factory) {
        this.factory = factory;
    }

    /**
     * The archive to create.
     */
    public void setDestfile(File f) {
        addDest(new FileResource(f));
    }

    /**
     * The archive to create.
     */
    public void addDest(Resource r) {
        if (dest != null) {
            throw new BuildException("Can only have one destination resource"
                                     + " for archive.");
        }
        dest = r;
    }

    /**
     * Sources for the archive.
     */
    public void add(ResourceCollection c) {
        sources.add(c);
    }

    /**
     * How to treat the target archive.
     */
    public void setMode(Mode m) {
        mode = m;
    }

    public void execute() {
        if (dest == null) {
            throw new BuildException("must provide a destination resource");
        }
        if (sources.size() == 0) {
            throw new BuildException("must provide sources");
        }
        if (!dest.isExists()) {
            // create mode
            mode = new Mode();
        }
    }

    /**
     * Valid Modes for create/update/replace.
     */
    public static final class Mode extends EnumeratedAttribute {

        /**
         * Create a new archive.
         */
        private static final String CREATE = "create";
        /**
         * Update an existing archive.
         */
        private static final String UPDATE = "update";
        /**
         * Update an existing archive, replacing all existing entries
         * with those from sources.
         */
        private static final String REPLACE = "replace";

        public Mode() {
            super();
            setValue(CREATE);
        }

        public String[] getValues() {
            return new String[] {CREATE, UPDATE, REPLACE};
        }

    }
}
