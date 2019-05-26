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

import org.apache.ant.compress.util.CpioStreamFactory;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.cpio.CpioArchiveEntry;
import org.apache.tools.ant.types.ArchiveScanner;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Resource;

/**
 * A CpioFileSet is a FileSet with extra attributes useful in the context of
 * Cpio tasks.
 *
 * A CpioFileSet extends FileSets with the ability to extract a subset of the
 * entries of a Cpio file for inclusion in another Cpio file.  It also includes
 * a prefix attribute which is prepended to each entry in the output Cpio file.
 *
 */
public class CpioFileSet extends CommonsCompressFileSet {

    /** Constructor for CpioFileSet */
    public CpioFileSet() {
        super();
    }

    /**
     * Constructor using a fileset argument.
     * @param fileset the fileset to use
     */
    protected CpioFileSet(FileSet fileset) {
        super(fileset);
    }

    /**
     * Constructor using a cpiofileset argument.
     * @param fileset the cpiofileset to use
     */
    protected CpioFileSet(CpioFileSet fileset) {
        super(fileset);
    }

    /**
     * Constructor using a CommonsCompressFileSet argument.
     * @param fileset the fileset to use
     */
    protected CpioFileSet(CommonsCompressFileSet fileset) {
        super(fileset);
    }

    /**
     * Create a new scanner.
     * @return the created scanner.
     */
    @Override
    protected ArchiveScanner newArchiveScanner() {
        CommonsCompressArchiveScanner cs =
            new CommonsCompressArchiveScanner(new CpioStreamFactory(),
                                              new CommonsCompressArchiveScanner.ResourceBuilder() {
                @Override
                public Resource buildResource(Resource archive, String encoding,
                                              ArchiveEntry entry) {
                    return new CpioResource(archive, encoding, (CpioArchiveEntry) entry);
                }
            }, getSkipUnreadableEntries(), getProject());
        cs.setEncoding(getEncoding());
        return cs;
    }

    @Override
    protected CommonsCompressFileSet newFileSet(FileSet fs) {
        if (fs instanceof CpioFileSet) {
            return new CpioFileSet((CpioFileSet) fs);
        }
        if (fs instanceof CommonsCompressFileSet) {
            return new CpioFileSet((CommonsCompressFileSet) fs);
        }
        return new CpioFileSet(fs);
    }

}
