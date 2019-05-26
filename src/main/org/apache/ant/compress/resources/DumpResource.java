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

import org.apache.tools.ant.types.Resource;
import org.apache.ant.compress.util.DumpStreamFactory;
import org.apache.commons.compress.archivers.dump.DumpArchiveEntry;

/**
 * A Resource representation of an entry in a dump archive.
 * @since Apache Compress Antlib 1.1
 */
public final class DumpResource extends CommonsCompressArchiveResource {

    /**
     * Default constructor.
     */
    public DumpResource() {
        super(new DumpStreamFactory(), "dump");
    }

    /**
     * Construct a DumpResource representing the specified
     * entry in the specified archive.
     * @param a the archive as File.
     * @param e the DumpEntry.
     */
    public DumpResource(File a, DumpArchiveEntry e) {
        this(a, null, e);
    }

    /**
     * Construct a DumpResource representing the specified
     * entry in the specified archive.
     * @param a the archive as Resource.
     * @param e the DumpEntry.
     */
    public DumpResource(Resource a, DumpArchiveEntry e) {
        this(a, null, e);
    }

    /**
     * Construct a DumpResource representing the specified
     * entry in the specified archive.
     * @param a the archive as File.
     * @param enc the encoding used for filenames.
     * @param e the DumpEntry.
     * @since Compress Antlib 1.3
     */
    public DumpResource(File a, String enc, DumpArchiveEntry e) {
        super(new DumpStreamFactory(), "dump", a, e);
        setEncoding(enc);
        setEntry(e);
    }

    /**
     * Construct a DumpResource representing the specified
     * entry in the specified archive.
     * @param a the archive as Resource.
     * @param enc the encoding used for filenames.
     * @param e the DumpEntry.
     * @since Compress Antlib 1.3
     */
    public DumpResource(Resource a, String enc, DumpArchiveEntry e) {
        super(new DumpStreamFactory(), "dump", a, e);
        setEncoding(enc);
        setEntry(e);
    }

}
