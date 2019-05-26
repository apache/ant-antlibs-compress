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

import org.apache.ant.compress.util.ArjStreamFactory;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.arj.ArjArchiveEntry;
import org.apache.tools.ant.types.ArchiveScanner;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Resource;

/**
 * A ArjFileSet is a FileSet with extra attributes useful in the context of
 * Arj tasks.
 *
 * A ArjFileSet extends FileSets with the ability to extract a subset of the
 * entries of a Arj file for inclusion in another Arj file.  It also includes
 * a prefix attribute which is prepended to each entry in the output Arj file.
 *
 * @since Apache Compress Antlib 1.3
 */
public class ArjFileSet extends CommonsCompressFileSet {

    /** Constructor for ArjFileSet */
    public ArjFileSet() {
        super();
    }

    /**
     * Constructor using a fileset argument.
     * @param fileset the fileset to use
     */
    protected ArjFileSet(FileSet fileset) {
        super(fileset);
    }

    /**
     * Constructor using a arjfileset argument.
     * @param fileset the arjfileset to use
     */
    protected ArjFileSet(ArjFileSet fileset) {
        super(fileset);
    }

    /**
     * Constructor using a CommonsCompressFileSet argument.
     * @param fileset the fileset to use
     */
    protected ArjFileSet(CommonsCompressFileSet fileset) {
        super(fileset);
    }

    /**
     * Create a new scanner.
     * @return the created scanner.
     */
    @Override
    protected ArchiveScanner newArchiveScanner() {
        CommonsCompressArchiveScanner cs =
            new CommonsCompressArchiveScanner(new ArjStreamFactory(),
                                              new CommonsCompressArchiveScanner.ResourceBuilder() {
                @Override
                public Resource buildResource(Resource archive, String encoding,
                                              ArchiveEntry entry) {
                    return new ArjResource(archive, encoding, (ArjArchiveEntry) entry);
                }
            }, getSkipUnreadableEntries(), getProject());
        cs.setEncoding(getEncoding());
        return cs;
    }

    @Override
    protected CommonsCompressFileSet newFileSet(FileSet fs) {
        if (fs instanceof ArjFileSet) {
            return new ArjFileSet((ArjFileSet) fs);
        }
        if (fs instanceof CommonsCompressFileSet) {
            return new ArjFileSet((CommonsCompressFileSet) fs);
        }
        return new ArjFileSet(fs);
    }

}
