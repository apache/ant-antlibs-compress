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

import java.io.IOException;
import java.io.OutputStream;

import org.apache.ant.compress.resources.SnappyResource;
import org.apache.ant.compress.resources.CommonsCompressCompressorResource;
import org.apache.ant.compress.util.SnappyStreamFactory;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.snappy.SnappyCompressorOutputStream;
import org.apache.tools.ant.types.Resource;

/**
 * Compresses using snappy.
 * @since Apache Compress Antlib 1.5
 */
public final class Snappy extends PackBase {

    private final InnerSnappyStreamFactory factory;

    public Snappy() {
        super(new PackBase.ResourceWrapper() {
                @Override
                public CommonsCompressCompressorResource wrap(Resource dest) {
                    return new SnappyResource(dest);
                }
            });
        setFactory(factory = new InnerSnappyStreamFactory());
    }

    /**
     * Whether to use the "framing format".
     *
     * <p>Defaults to true.</p>
     */
    public void setFramed(boolean framed) {
        factory.setFramed(framed);
    }

    private class InnerSnappyStreamFactory extends SnappyStreamFactory {
        @Override
        public CompressorOutputStream getCompressorStream(OutputStream stream)
            throws IOException {
            if (isFramed() || getSrc() == null || getSrc().getSize() < 0) {
                return super.getCompressorStream(stream);
            }
            return new SnappyCompressorOutputStream(stream, getSrc().getSize());
        }
    }
}
