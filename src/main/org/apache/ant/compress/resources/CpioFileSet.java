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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.AbstractFileSet;
import org.apache.tools.ant.types.ArchiveFileSet;
import org.apache.tools.ant.types.ArchiveScanner;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Reference;

/**
 * A CpioFileSet is a FileSet with extra attributes useful in the context of
 * Cpio/Jar tasks.
 *
 * A CpioFileSet extends FileSets with the ability to extract a subset of the
 * entries of a Cpio file for inclusion in another Cpio file.  It also includes
 * a prefix attribute which is prepended to each entry in the output Cpio file.
 *
 */
public class CpioFileSet extends ArchiveFileSet {

    private boolean userIdSet;
    private boolean groupIdSet;

    private int    uid;
    private int    gid;

    /** Constructor for CpioFileSet */
    public CpioFileSet() {
        super();
    }

    /**
     * Constructor using a fileset arguement.
     * @param fileset the fileset to use
     */
    protected CpioFileSet(FileSet fileset) {
        super(fileset);
    }

    /**
     * Constructor using a cpiofileset arguement.
     * @param fileset the cpiofileset to use
     */
    protected CpioFileSet(CpioFileSet fileset) {
        super(fileset);
    }

    /**
     * The uid for the cpio entry
     * This is not the same as the User name.
     * @param uid the id of the user for the cpio entry.
     */
    public void setUid(int uid) {
        checkCpioFileSetAttributesAllowed();
        userIdSet = true;
        this.uid = uid;
    }

    /**
     * @return the uid for the cpio entry
     */
    public int getUid() {
        if (isReference()) {
            return ((CpioFileSet) getCheckedRef()).getUid();
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
     * The GID for the cpio entry; optional, default="0"
     * This is not the same as the group name.
     * @param gid the group id.
     */
    public void setGid(int gid) {
        checkCpioFileSetAttributesAllowed();
        groupIdSet = true;
        this.gid = gid;
    }

    /**
     * @return the group identifier.
     */
    public int getGid() {
        if (isReference()) {
            return ((CpioFileSet) getCheckedRef()).getGid();
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
        CpioScanner zs = new CpioScanner();
        return zs;
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
     * A CpioFileset accepts another CpioFileSet or a FileSet as reference
     * FileSets are often used by the war task for the lib attribute
     * @param p the project to use
     * @return the abstract fileset instance
     */
    protected AbstractFileSet getRef(Project p) {
        dieOnCircularReference(p);
        Object o = getRefid().getReferencedObject(p);
        if (o instanceof CpioFileSet) {
            return (AbstractFileSet) o;
        } else if (o instanceof FileSet) {
            CpioFileSet zfs = new CpioFileSet((FileSet) o);
            configureFileSet(zfs);
            return zfs;
        } else {
            String msg = getRefid().getRefId() + " doesn\'t denote a cpiofileset or a fileset";
            throw new BuildException(msg);
        }
    }

    /**
     * Configure a fileset based on this fileset.
     * If the fileset is a CpioFileSet copy in the cpiofileset
     * specific attributes.
     * @param zfs the archive fileset to configure.
     */
    protected void configureFileSet(ArchiveFileSet zfs) {
        super.configureFileSet(zfs);
        if (zfs instanceof CpioFileSet) {
            CpioFileSet tfs = (CpioFileSet) zfs;
            tfs.setUid(uid);
            tfs.setGid(gid);
        }
    }

    /**
     * Return a CpioFileSet that has the same properties
     * as this one.
     * @return the cloned cpioFileSet
     */
    public Object clone() {
        if (isReference()) {
            return ((CpioFileSet) getRef(getProject())).clone();
        } else {
            return super.clone();
        }
    }

    /**
     * A check attributes for CpioFileSet.
     * If there is a reference, and
     * it is a CpioFileSet, the cpio fileset attributes
     * cannot be used.
     */
    private void checkCpioFileSetAttributesAllowed() {
        if (getProject() == null
            || (isReference()
                && (getRefid().getReferencedObject(
                        getProject())
                    instanceof CpioFileSet))) {
            checkAttributesAllowed();
        }
    }

}
