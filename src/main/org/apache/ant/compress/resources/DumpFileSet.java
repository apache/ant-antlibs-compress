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

import org.apache.ant.compress.util.DumpStreamFactory;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.dump.DumpArchiveEntry;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.AbstractFileSet;
import org.apache.tools.ant.types.ArchiveFileSet;
import org.apache.tools.ant.types.ArchiveScanner;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.types.Resource;

/**
 * A DumpFileSet is a FileSet with extra attributes useful in the context of
 * Dump tasks.
 *
 * A DumpFileSet extends FileSets with the ability to extract a subset of the
 * entries of a Dump file for inclusion in another Dump file.  It also includes
 * a prefix attribute which is prepended to each entry in the output Dump file.
 *
 * @since Apache Compress Antlib 1.1
 */
public class DumpFileSet extends ArchiveFileSet {

    private boolean userIdSet;
    private boolean groupIdSet;

    private int    uid;
    private int    gid;

    private boolean skipUnreadable = false;
    private String encoding = null;

    /** Constructor for DumpFileSet */
    public DumpFileSet() {
        super();
    }

    /**
     * Constructor using a fileset arguement.
     * @param fileset the fileset to use
     */
    protected DumpFileSet(FileSet fileset) {
        super(fileset);
    }

    /**
     * Constructor using a dumpfileset arguement.
     * @param fileset the dumpfileset to use
     */
    protected DumpFileSet(DumpFileSet fileset) {
        super(fileset);
        encoding = fileset.encoding;
    }

    /**
     * The uid for the dump entry
     * This is not the same as the User name.
     * @param uid the id of the user for the dump entry.
     */
    public void setUid(int uid) {
        checkDumpFileSetAttributesAllowed();
        userIdSet = true;
        this.uid = uid;
    }

    /**
     * @return the uid for the dump entry
     */
    public int getUid() {
        if (isReference()) {
            return ((DumpFileSet) getCheckedRef()).getUid();
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
     * The GID for the dump entry; optional, default="0"
     * This is not the same as the group name.
     * @param gid the group id.
     */
    public void setGid(int gid) {
        checkDumpFileSetAttributesAllowed();
        groupIdSet = true;
        this.gid = gid;
    }

    /**
     * @return the group identifier.
     */
    public int getGid() {
        if (isReference()) {
            return ((DumpFileSet) getCheckedRef()).getGid();
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
     * Set the encoding used for this CpioFileSet.
     * @param enc encoding as String.
     * @since Compress Antlib 1.3
     */
    public void setEncoding(String enc) {
        checkDumpFileSetAttributesAllowed();
        this.encoding = enc;
    }

    /**
     * Create a new scanner.
     * @return the created scanner.
     */
    protected ArchiveScanner newArchiveScanner() {
        CommonsCompressArchiveScanner cs =
            new CommonsCompressArchiveScanner(new DumpStreamFactory(),
                                              new CommonsCompressArchiveScanner.ResourceBuilder() {
                public Resource buildResource(Resource archive, String encoding,
                                              ArchiveEntry entry) {
                    return new DumpResource(archive, encoding, (DumpArchiveEntry) entry);
                }
            }, skipUnreadable, getProject());
        cs.setEncoding(encoding);
        return cs;
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
     * A DumpFileset accepts another DumpFileSet or a FileSet as reference
     * FileSets are often used by the war task for the lib attribute
     * @param p the project to use
     * @return the abstract fileset instance
     */
    protected AbstractFileSet getRef(Project p) {
        dieOnCircularReference(p);
        Object o = getRefid().getReferencedObject(p);
        if (o instanceof DumpFileSet) {
            return (AbstractFileSet) o;
        } else if (o instanceof FileSet) {
            DumpFileSet zfs = new DumpFileSet((FileSet) o);
            configureFileSet(zfs);
            return zfs;
        } else {
            String msg = getRefid().getRefId() + " doesn\'t denote a dumpfileset or a fileset";
            throw new BuildException(msg);
        }
    }

    /**
     * Configure a fileset based on this fileset.
     * If the fileset is a DumpFileSet copy in the dumpfileset
     * specific attributes.
     * @param zfs the archive fileset to configure.
     */
    protected void configureFileSet(ArchiveFileSet zfs) {
        super.configureFileSet(zfs);
        if (zfs instanceof DumpFileSet) {
            DumpFileSet tfs = (DumpFileSet) zfs;
            tfs.setUid(uid);
            tfs.setGid(gid);
        }
    }

    /**
     * Return a DumpFileSet that has the same properties
     * as this one.
     * @return the cloned dumpFileSet
     */
    public Object clone() {
        if (isReference()) {
            return ((DumpFileSet) getRef(getProject())).clone();
        } else {
            return super.clone();
        }
    }

    /**
     * A check attributes for DumpFileSet.
     * If there is a reference, and
     * it is a DumpFileSet, the dump fileset attributes
     * cannot be used.
     */
    private void checkDumpFileSetAttributesAllowed() {
        if (getProject() == null
            || (isReference()
                && (getRefid().getReferencedObject(
                        getProject())
                    instanceof DumpFileSet))) {
            checkAttributesAllowed();
        }
    }

}
