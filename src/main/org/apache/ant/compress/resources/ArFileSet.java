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
package org.apache.ant.compress.resources;

import org.apache.ant.compress.util.ArStreamFactory;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ar.ArArchiveEntry;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.AbstractFileSet;
import org.apache.tools.ant.types.ArchiveFileSet;
import org.apache.tools.ant.types.ArchiveScanner;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.types.Resource;

/**
 * A ArFileSet is a FileSet with extra attributes useful in the context of
 * Ar/Jar tasks.
 *
 * A ArFileSet extends FileSets with the ability to extract a subset of the
 * entries of a Ar file for inclusion in another Ar file.  It also includes
 * a prefix attribute which is prepended to each entry in the output Ar file.
 *
 */
public class ArFileSet extends ArchiveFileSet {

    private boolean userIdSet;
    private boolean groupIdSet;

    private int    uid;
    private int    gid;

    /** Constructor for ArFileSet */
    public ArFileSet() {
        super();
    }

    /**
     * Constructor using a fileset arguement.
     * @param fileset the fileset to use
     */
    protected ArFileSet(FileSet fileset) {
        super(fileset);
    }

    /**
     * Constructor using a arfileset arguement.
     * @param fileset the arfileset to use
     */
    protected ArFileSet(ArFileSet fileset) {
        super(fileset);
    }

    /**
     * The uid for the ar entry
     * This is not the same as the User name.
     * @param uid the id of the user for the ar entry.
     */
    public void setUid(int uid) {
        checkArFileSetAttributesAllowed();
        userIdSet = true;
        this.uid = uid;
    }

    /**
     * @return the uid for the ar entry
     */
    public int getUid() {
        if (isReference()) {
            return ((ArFileSet) getCheckedRef()).getUid();
        }
        return uid;
    }

    /**
     * @return whether the user id has been explicitly set.
     */
    public boolean hasUserIdBeenSet() {
        return userIdSet;
    }

    /**
     * The GID for the ar entry; optional, default="0"
     * This is not the same as the group name.
     * @param gid the group id.
     */
    public void setGid(int gid) {
        checkArFileSetAttributesAllowed();
        groupIdSet = true;
        this.gid = gid;
    }

    /**
     * @return the group identifier.
     */
    public int getGid() {
        if (isReference()) {
            return ((ArFileSet) getCheckedRef()).getGid();
        }
        return gid;
    }

    /**
     * @return whether the group id has been explicitly set.
     */
    public boolean hasGroupIdBeenSet() {
        return groupIdSet;
    }

    /**
     * Create a new scanner.
     * @return the created scanner.
     */
    protected ArchiveScanner newArchiveScanner() {
        return new CommonsCompressArchiveScanner(new ArStreamFactory(),
                                                 new CommonsCompressArchiveScanner.ResourceBuilder() {
                public Resource buildResource(Resource archive, String encoding,
                                              ArchiveEntry entry) {
                    return new ArResource(archive, (ArArchiveEntry) entry);
                }
            });
    }

    /**
     * Makes this instance in effect a reference to another instance.
     *
     * <p>You must not set another attribute or nest elements inside
     * this element if you make it a reference.</p>
     * @param r the <code>Reference</code> to use.
     * @throws BuildException on error
     */
    public void setRefid(Reference r) throws BuildException {
        if (userIdSet || groupIdSet) {
            throw tooManyAttributes();
        }
        super.setRefid(r);
    }

    /**
     * A ArFileset accepts another ArFileSet or a FileSet as reference
     * FileSets are often used by the war task for the lib attribute
     * @param p the project to use
     * @return the abstract fileset instance
     */
    protected AbstractFileSet getRef(Project p) {
        dieOnCircularReference(p);
        Object o = getRefid().getReferencedObject(p);
        if (o instanceof ArFileSet) {
            return (AbstractFileSet) o;
        } else if (o instanceof FileSet) {
            ArFileSet zfs = new ArFileSet((FileSet) o);
            configureFileSet(zfs);
            return zfs;
        } else {
            String msg = getRefid().getRefId() + " doesn\'t denote a arfileset or a fileset";
            throw new BuildException(msg);
        }
    }

    /**
     * Configure a fileset based on this fileset.
     * If the fileset is a ArFileSet copy in the arfileset
     * specific attributes.
     * @param zfs the archive fileset to configure.
     */
    protected void configureFileSet(ArchiveFileSet zfs) {
        super.configureFileSet(zfs);
        if (zfs instanceof ArFileSet) {
            ArFileSet tfs = (ArFileSet) zfs;
            tfs.setUid(uid);
            tfs.setGid(gid);
        }
    }

    /**
     * Return a ArFileSet that has the same properties
     * as this one.
     * @return the cloned arFileSet
     */
    public Object clone() {
        if (isReference()) {
            return ((ArFileSet) getRef(getProject())).clone();
        } else {
            return super.clone();
        }
    }

    /**
     * A check attributes for ArFileSet.
     * If there is a reference, and
     * it is a ArFileSet, the ar fileset attributes
     * cannot be used.
     */
    private void checkArFileSetAttributesAllowed() {
        if (getProject() == null
            || (isReference()
                && (getRefid().getReferencedObject(
                        getProject())
                    instanceof ArFileSet))) {
            checkAttributesAllowed();
        }
    }

}
