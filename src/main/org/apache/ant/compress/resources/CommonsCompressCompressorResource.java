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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.ant.compress.util.CompressorStreamFactory;
import org.apache.ant.compress.util.CompressorWithConcatenatedStreamsFactory;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.ContentTransformingResource;

/**
 * A compressed resource.
 *
 * <p>Wraps around another resource, delegates all queries to that
 * other resource but uncompresses/compresses streams on the fly.</p>
 */
public abstract class CommonsCompressCompressorResource
    extends ContentTransformingResource {

    private final String name;
    private CompressorStreamFactory factory;
    private boolean decompressConcatenated = false;

    protected CommonsCompressCompressorResource(String name) {
        this.name = name;
    }

    protected CommonsCompressCompressorResource(String name,
                                                CompressorStreamFactory factory) {
        this(name);
        setFactory(factory);
    }

    /**
     * Constructor with another resource to wrap.
     * @param other the resource to wrap.
     */
    protected CommonsCompressCompressorResource(String name,
                                                ResourceCollection other) {
        super(other);
        this.name = name;
    }

    /**
     * Constructor with another resource to wrap.
     * @param other the resource to wrap.
     */
    protected CommonsCompressCompressorResource(String name,
                                                CompressorStreamFactory factory,
                                                ResourceCollection other) {
        this(name, other);
        setFactory(factory);
    }

    /**
     * @since Apache Compress Antlib 1.4
     */
    protected final void setFactory(CompressorStreamFactory factory) {
        this.factory = factory;
    }

    /**
     * Whether to extract the whole resource if it contains multiple
     * concatenated streams.
     *
     * <p>Defaults to false for backwards compatibility.</p>
     *
     * @since Apache Compress Antlib 1.2
     */
    public void setDecompressConcatenated(boolean b) {
        if (!b || factory instanceof CompressorWithConcatenatedStreamsFactory) {
            decompressConcatenated = b;
        } else {
            throw new BuildException("concatenated streams are not supported"
                                     + "by this compression format.");
        }
    }

    /**
     * Decompress on the fly.
     * @param in the stream to wrap.
     * @return the wrapped stream.
     * @throws IOException if there is a problem.
     */
    @Override
    protected final InputStream wrapStream(InputStream in) throws IOException {
        if (factory instanceof CompressorWithConcatenatedStreamsFactory) {
            CompressorWithConcatenatedStreamsFactory f
                = (CompressorWithConcatenatedStreamsFactory) factory;
            return f.getCompressorStream(new BufferedInputStream(in),
                                         decompressConcatenated);
        } else {
            return factory.getCompressorStream(new BufferedInputStream(in));
        }
    }

    /**
     * Compress on the fly.
     * @param out the stream to wrap.
     * @return the wrapped stream.
     * @throws IOException if there is a problem.
     */
    @Override
    protected final OutputStream wrapStream(OutputStream out)
        throws IOException {
        return factory.getCompressorStream(new BufferedOutputStream(out));
    }

    /**
     * Get the string representation of this Resource.
     * @return this Resource formatted as a String.
     */
    @Override
    public String toString() {
        return name + " compressed " + super.toString();
    }

}
