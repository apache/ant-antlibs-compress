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

import org.apache.ant.compress.util.ArStreamFactory;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ar.ArArchiveEntry;
import org.apache.tools.ant.types.ArchiveScanner;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Resource;

/**
 * A ArFileSet is a FileSet with extra attributes useful in the context of
 * Ar tasks.
 *
 * A ArFileSet extends FileSets with the ability to extract a subset of the
 * entries of a Ar file for inclusion in another Ar file.  It also includes
 * a prefix attribute which is prepended to each entry in the output Ar file.
 *
 */
public class ArFileSet extends CommonsCompressFileSet {

    /** Constructor for ArFileSet */
    public ArFileSet() {
        super();
    }

    /**
     * Constructor using a fileset argument.
     * @param fileset the fileset to use
     */
    protected ArFileSet(FileSet fileset) {
        super(fileset);
    }

    /**
     * Constructor using a arfileset argument.
     * @param fileset the arfileset to use
     */
    protected ArFileSet(ArFileSet fileset) {
        super(fileset);
    }

    /**
     * Constructor using a CommonsCompressFileSet argument.
     * @param fileset the fileset to use
     */
    protected ArFileSet(CommonsCompressFileSet fileset) {
        super(fileset);
    }

    /**
     * Create a new scanner.
     * @return the created scanner.
     */
    @Override
    protected ArchiveScanner newArchiveScanner() {
        return new CommonsCompressArchiveScanner(new ArStreamFactory(),
                                                 new CommonsCompressArchiveScanner.ResourceBuilder() {
                @Override
                public Resource buildResource(Resource archive, String encoding,
                                              ArchiveEntry entry) {
                    return new ArResource(archive, (ArArchiveEntry) entry);
                }
            }, getSkipUnreadableEntries(), getProject());
    }

    @Override
    protected CommonsCompressFileSet newFileSet(FileSet fs) {
        if (fs instanceof ArFileSet) {
            return new ArFileSet((ArFileSet) fs);
        }
        if (fs instanceof CommonsCompressFileSet) {
            return new ArFileSet((CommonsCompressFileSet) fs);
        }
        return new ArFileSet(fs);
    }

}
