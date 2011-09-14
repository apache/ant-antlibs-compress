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
import java.io.IOException;
import java.io.InputStream;

import org.apache.ant.compress.util.Pack200StreamFactory;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.pack200.Pack200CompressorInputStream;

/**
 * Expands a pack200 archive.
 * @since Apache Compress Antlib 1.1
 */
public final class UnPack200 extends UnpackBase {

    private Pack200.Pack200StrategyEnum strategy =
        Pack200.Pack200StrategyEnum.IN_MEMORY;

    public UnPack200() {
        super(".pack");
        setFactory(new Pack200StreamFactory() {
                public CompressorInputStream
                    getCompressorStream(InputStream stream)
                    throws IOException {
                    return new Pack200CompressorInputStream(stream,
                                                            strategy
                                                            .getStrategy());
                }
                public CompressorInputStream getCompressorInputStream(File file)
                    throws IOException {
                    return new Pack200CompressorInputStream(file,
                                                            strategy
                                                            .getStrategy());
                }
            });
    }

    /**
     * Whether to cache archive data in memory (the default) or a
     * temporary file.
     *
     * @since Commons Compress 1.1
     */
    public void setPack200Strategy(Pack200.Pack200StrategyEnum s) {
        strategy = s;
    }

}