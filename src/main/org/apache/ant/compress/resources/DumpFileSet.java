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

import org.apache.ant.compress.util.DumpStreamFactory;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.dump.DumpArchiveEntry;
import org.apache.tools.ant.types.ArchiveScanner;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Resource;

/**
 * A DumpFileSet is a FileSet with extra attributes useful in the context of
 * Dump tasks.
 *
 * A DumpFileSet extends FileSets with the ability to extract a subset of the
 * entries of a Dump file for inclusion in another Dump file.  It also includes
 * a prefix attribute which is prepended to each entry in the output Dump file.
 *
 * @since Apache Compress Antlib 1.1
 */
public class DumpFileSet extends CommonsCompressFileSet {

    /** Constructor for DumpFileSet */
    public DumpFileSet() {
        super();
    }

    /**
     * Constructor using a fileset argument.
     * @param fileset the fileset to use
     */
    protected DumpFileSet(FileSet fileset) {
        super(fileset);
    }

    /**
     * Constructor using a dumpfileset argument.
     * @param fileset the dumpfileset to use
     */
    protected DumpFileSet(DumpFileSet fileset) {
        super(fileset);
    }

    /**
     * Constructor using a CommonsCompressFileSet argument.
     * @param fileset the fileset to use
     */
    protected DumpFileSet(CommonsCompressFileSet fileset) {
        super(fileset);
    }

    /**
     * Create a new scanner.
     * @return the created scanner.
     */
    @Override
    protected ArchiveScanner newArchiveScanner() {
        CommonsCompressArchiveScanner cs =
            new CommonsCompressArchiveScanner(new DumpStreamFactory(),
                                              new CommonsCompressArchiveScanner.ResourceBuilder() {
                @Override
                public Resource buildResource(Resource archive, String encoding,
                                              ArchiveEntry entry) {
                    return new DumpResource(archive, encoding, (DumpArchiveEntry) entry);
                }
            }, getSkipUnreadableEntries(), getProject());
        cs.setEncoding(getEncoding());
        return cs;
    }

    @Override
    protected CommonsCompressFileSet newFileSet(FileSet fs) {
        if (fs instanceof DumpFileSet) {
            return new DumpFileSet((DumpFileSet) fs);
        }
        if (fs instanceof CommonsCompressFileSet) {
            return new DumpFileSet((CommonsCompressFileSet) fs);
        }
        return new DumpFileSet(fs);
    }

}
