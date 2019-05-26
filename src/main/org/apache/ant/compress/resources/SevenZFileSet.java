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

import org.apache.tools.ant.types.ArchiveScanner;
import org.apache.tools.ant.types.FileSet;

/**
 * A SevenZFileSet is a FileSet with extra attributes useful in the context of
 * SevenZ tasks.
 *
 * A SevenZFileSet extends FileSets with the ability to extract a subset of the
 * entries of a SevenZ file for inclusion in another SevenZ file.  It also includes
 * a prefix attribute which is prepended to each entry in the output SevenZ file.
 *
 * @since Apache Compress Antlib 1.3
 */
public class SevenZFileSet extends CommonsCompressFileSet {

    /** Constructor for SevenZFileSet */
    public SevenZFileSet() {
        super();
    }

    /**
     * Constructor using a fileset argument.
     * @param fileset the fileset to use
     */
    protected SevenZFileSet(FileSet fileset) {
        super(fileset);
    }

    /**
     * Constructor using a sevenzfileset argument.
     * @param fileset the sevenzfileset to use
     */
    protected SevenZFileSet(SevenZFileSet fileset) {
        super(fileset);
    }

    /**
     * Constructor using a CommonsCompressFileSet argument.
     * @param fileset the fileset to use
     */
    protected SevenZFileSet(CommonsCompressFileSet fileset) {
        super(fileset);
    }

    /**
     * Return a new archive scanner based on this one.
     * @return a new SevenZScanner with the same encoding as this one.
     */
    @Override
    protected ArchiveScanner newArchiveScanner() {
        return new SevenZScanner(getSkipUnreadableEntries(), getProject());
    }

    @Override
    protected CommonsCompressFileSet newFileSet(FileSet fs) {
        if (fs instanceof SevenZFileSet) {
            return new SevenZFileSet((SevenZFileSet) fs);
        }
        if (fs instanceof CommonsCompressFileSet) {
            return new SevenZFileSet((CommonsCompressFileSet) fs);
        }
        return new SevenZFileSet(fs);
    }

}
