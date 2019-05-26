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

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.AbstractFileSet;
import org.apache.tools.ant.types.ArchiveFileSet;
import org.apache.tools.ant.types.ArchiveScanner;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.types.Resource;

/**
 * A fileset based on the contents of a commons compress archive.
 *
 * A CommonsCompressFileSet extends FileSets with the ability to
 * extract a subset of the entries of an archive for inclusion in
 * another archive.  It also includes a prefix attribute which is
 * prepended to each entry in the output archive.
 *
 * @since Compress Antlib 1.3
 */
public abstract class CommonsCompressFileSet extends ArchiveFileSet {

    private boolean userIdSet;
    private boolean groupIdSet;

    // not supported for zip
    private int    uid;
    private int    gid;

    private boolean skipUnreadable = false;

    // not supported for ar
    private String encoding = null;

    /** Constructor for CommonsCompressFileSet */
    public CommonsCompressFileSet() {
        super();
    }

    /**
     * Constructor using a fileset argument.
     * @param fileset the fileset to use
     */
    protected CommonsCompressFileSet(FileSet fileset) {
        super(fileset);
    }

    /**
     * Constructor using a CommonsCompressFileSet argument.
     * @param fileset the fileset to use
     */
    protected CommonsCompressFileSet(CommonsCompressFileSet fileset) {
        super(fileset);
        encoding = fileset.encoding;
    }

    /**
     * Set the encoding used for this CommonsCompressFileSet.
     * @param enc encoding as String.
     */
    public void setEncoding(String enc) {
        checkCommonsCompressFileSetAttributesAllowed();
        this.encoding = enc;
    }

    /**
     * Get the encoding used for this TarFileSet.
     * @return String encoding.
     */
    public String getEncoding() {
        if (isReference()) {
            AbstractFileSet ref = getRef(getProject());
            if (ref instanceof CommonsCompressFileSet) {
                return ((CommonsCompressFileSet) ref).getEncoding();
            } else {
                return null;
            }
        }
        return encoding;
    }

    /**
     * The uid for the entry
     * This is not the same as the User name.
     * @param uid the id of the user for the entry.
     */
    public void setUid(int uid) {
        checkCommonsCompressFileSetAttributesAllowed();
        userIdSet = true;
        this.uid = uid;
    }

    /**
     * @return the uid for the entry
     */
    public int getUid() {
        if (isReference()) {
            return ((CommonsCompressFileSet) getCheckedRef()).getUid();
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
     * The GID for the entry; optional, default="0"
     * This is not the same as the group name.
     * @param gid the group id.
     */
    public void setGid(int gid) {
        checkCommonsCompressFileSetAttributesAllowed();
        groupIdSet = true;
        this.gid = gid;
    }

    /**
     * @return the group identifier.
     */
    public int getGid() {
        if (isReference()) {
            return ((CommonsCompressFileSet) getCheckedRef()).getGid();
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
     * Whether to skip entries that Commons Compress signals it cannot read.
     */
    public void setSkipUnreadableEntries(boolean b) {
        skipUnreadable = b;
    }

    /**
     * Whether to skip entries that Commons Compress signals it cannot read.
     */
    public boolean getSkipUnreadableEntries() {
        return skipUnreadable;
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
     * A CommonsCompressFileSet accepts another FileSet as reference
     * @param p the project to use
     * @return the abstract fileset instance
     */
    protected AbstractFileSet getRef(Project p) {
        dieOnCircularReference(p);
        Object o = getRefid().getReferencedObject(p);
        if (o.getClass().equals(getClass())) {
            return (AbstractFileSet) o;
        } else if (o instanceof FileSet) {
            CommonsCompressFileSet zfs = newFileSet((FileSet) o);
            configureFileSet(zfs);
            return zfs;
        } else {
            String msg = getRefid().getRefId() + " doesn\'t denote a fileset";
            throw new BuildException(msg);
        }
    }

    /**
     * Configure a fileset based on this fileset.
     * If the fileset is a CommonsCompressFileSet copy in the
     * specific attributes.
     * @param zfs the archive fileset to configure.
     */
    protected void configureFileSet(ArchiveFileSet zfs) {
        super.configureFileSet(zfs);
        if (zfs instanceof CommonsCompressFileSet) {
            CommonsCompressFileSet ccfs = (CommonsCompressFileSet) zfs;
            ccfs.setUid(uid);
            ccfs.setGid(gid);
        }
    }

    /**
     * Return a CommonsCompressFileSet that has the same properties
     * as this one.
     * @return the cloned tarFileSet
     */
    public CommonsCompressFileSet clone() {
        if (isReference()) {
            return ((CommonsCompressFileSet) getRef(getProject())).clone();
        } else {
            return (CommonsCompressFileSet) super.clone();
        }
    }

    /**
     * Creates a new CommonsCompressFileSet based on the given fileset.
     */
    protected abstract CommonsCompressFileSet newFileSet(FileSet fs);

    /**
     * A check attributes for CommonsCompressFileSet.
     * If there is a reference, and
     * it is a CommonsCompressFileSet, the specific attributes
     * cannot be used.
     */
    protected final void checkCommonsCompressFileSetAttributesAllowed() {
        if (getProject() == null
            || (isReference()
                && (getRefid().getReferencedObject(
                        getProject())
                    instanceof CommonsCompressFileSet))) {
            checkAttributesAllowed();
        }
    }

}
