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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.ArchiveFileSet;
import org.apache.tools.ant.types.DataType;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.Union;
import org.apache.tools.ant.util.CollectionUtils;

/**
 * A resource collection that treats all nested resources as archives
 * and returns the contents of the archives as its content.
 */
public class Archives extends DataType
    implements ResourceCollection, Cloneable {

    private Union zips = new Union();
    private Union tars = new Union();
    private Union ars = new Union();
    private Union cpios = new Union();
    private Union arjs = new Union();
    private Union dumps = new Union();
    private Union sevenzs = new Union();

    /**
     * Wrapper to identify nested resource collections as ZIP
     * archives.
     */
    public Union createZips() {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        setChecked(false);
        return zips;
    }

    /**
     * Wrapper to identify nested resource collections as ZIP
     * archives.
     */
    public Union createTars() {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        setChecked(false);
        return tars;
    }

    /**
     * Wrapper to identify nested resource collections as AR
     * archives.
     */
    public Union createArs() {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        setChecked(false);
        return ars;
    }

    /**
     * Wrapper to identify nested resource collections as ARJ
     * archives.
     * @since Apache Compress Antlib 1.5
     */
    public Union createArjs() {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        setChecked(false);
        return arjs;
    }

    /**
     * Wrapper to identify nested resource collections as CPIO
     * archives.
     */
    public Union createCpios() {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        setChecked(false);
        return cpios;
    }

    /**
     * Wrapper to identify nested resource collections as Unix dump
     * archives.
     * @since Apache Compress Antlib 1.5
     */
    public Union createDumps() {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        setChecked(false);
        return dumps;
    }

    /**
     * Wrapper to identify nested resource collections as 7z
     * archives.
     * @since Apache Compress Antlib 1.5
     */
    public Union createSevenzs() {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        setChecked(false);
        return sevenzs;
    }

    /**
     * Sums the sizes of nested archives.
     */
    @Override
    public int size() {
        if (isReference()) {
            return ((Archives) getCheckedRef()).size();
        }
        dieOnCircularReference();
        int total = 0;
        for (Iterator i = grabArchives(); i.hasNext(); ) {
            total += ((ResourceCollection) i.next()).size();
        }
        return total;
    }

    /**
     * Merges the nested collections.
     */
    @Override
    public Iterator iterator() {
        if (isReference()) {
            return ((Archives) getCheckedRef()).iterator();
        }
        dieOnCircularReference();
        List l = new LinkedList();
        for (Iterator i = grabArchives(); i.hasNext(); ) {
            l.addAll(CollectionUtils
                     .asCollection(((ResourceCollection) i.next()).iterator()));
        }
        return l.iterator();
    }

    /**
     * @return false
     */
    @Override
    public boolean isFilesystemOnly() {
        if (isReference()) {
            return ((Archives) getCheckedRef()).isFilesystemOnly();
        }
        dieOnCircularReference();
        return false;
    }

    /**
     * Overrides the base version.
     * @param r the Reference to set.
     */
    @Override
    public void setRefid(Reference r) {
        if (zips.getResourceCollections().size() > 0
            || ars.getResourceCollections().size() > 0
            || cpios.getResourceCollections().size() > 0
            || arjs.getResourceCollections().size() > 0
            || dumps.getResourceCollections().size() > 0
            || sevenzs.getResourceCollections().size() > 0
            || tars.getResourceCollections().size() > 0) {
            throw tooManyAttributes();
        }
        super.setRefid(r);
    }

    /**
     * Implement clone.  The nested resource collections are cloned as
     * well.
     * @return a cloned instance.
     */
    @Override
    public Object clone() {
        try {
            Archives a = (Archives) super.clone();
            a.zips = (Union) zips.clone();
            a.tars = (Union) tars.clone();
            a.ars = (Union) ars.clone();
            a.cpios = (Union) cpios.clone();
            a.arjs = (Union) arjs.clone();
            a.dumps = (Union) dumps.clone();
            a.sevenzs = (Union) sevenzs.clone();
            return a;
        } catch (CloneNotSupportedException e) {
            throw new BuildException(e);
        }
    }

    // TODO this is a pretty expensive operation and so the result
    // should be cached.
    /**
     * Turns all nested resources into corresponding ArchiveFileSets
     * and returns an iterator over the collected archives.
     */
    protected Iterator/*<ArchiveFileset>*/ grabArchives() {
        List l = new LinkedList();
        for (Iterator iter = zips.iterator(); iter.hasNext(); ) {
            l.add(configureArchive(new ZipFileSet(),
                                   (Resource) iter.next()));
        }
        for (Iterator iter = tars.iterator(); iter.hasNext(); ) {
            l.add(configureArchive(new TarFileSet(),
                                   (Resource) iter.next()));
        }
        for (Iterator iter = ars.iterator(); iter.hasNext(); ) {
            l.add(configureArchive(new ArFileSet(),
                                   (Resource) iter.next()));
        }
        for (Iterator iter = cpios.iterator(); iter.hasNext(); ) {
            l.add(configureArchive(new CpioFileSet(),
                                   (Resource) iter.next()));
        }
        for (Iterator iter = arjs.iterator(); iter.hasNext(); ) {
            l.add(configureArchive(new ArjFileSet(),
                                   (Resource) iter.next()));
        }
        for (Iterator iter = dumps.iterator(); iter.hasNext(); ) {
            l.add(configureArchive(new DumpFileSet(),
                                   (Resource) iter.next()));
        }
        for (Iterator iter = sevenzs.iterator(); iter.hasNext(); ) {
            l.add(configureArchive(new SevenZFileSet(),
                                   (Resource) iter.next()));
        }
        return l.iterator();
    }

    /**
     * Configures the archivefileset based on this type's settings,
     * set the source.
     */
    protected ArchiveFileSet configureArchive(ArchiveFileSet afs,
                                              Resource src) {
        afs.setProject(getProject());
        afs.setSrcResource(src);
        return afs;
    }

    /**
     * Overrides the version of DataType to recurse on all DataType
     * child elements that may have been added.
     * @param stk the stack of data types to use (recursively).
     * @param p   the project to use to dereference the references.
     * @throws BuildException on error.
     */
    @Override
    protected synchronized void dieOnCircularReference(Stack stk, Project p)
        throws BuildException {
        if (isChecked()) {
            return;
        }
        if (isReference()) {
            super.dieOnCircularReference(stk, p);
        } else {
            pushAndInvokeCircularReferenceCheck(zips, stk, p);
            pushAndInvokeCircularReferenceCheck(tars, stk, p);
            pushAndInvokeCircularReferenceCheck(ars, stk, p);
            pushAndInvokeCircularReferenceCheck(cpios, stk, p);
            pushAndInvokeCircularReferenceCheck(arjs, stk, p);
            pushAndInvokeCircularReferenceCheck(dumps, stk, p);
            pushAndInvokeCircularReferenceCheck(sevenzs, stk, p);
            setChecked(true);
        }
    }

}
