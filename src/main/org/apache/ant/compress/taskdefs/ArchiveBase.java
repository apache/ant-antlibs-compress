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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipException;

import org.apache.ant.compress.resources.ArFileSet;
import org.apache.ant.compress.resources.CommonsCompressArchiveResource;
import org.apache.ant.compress.resources.CpioFileSet;
import org.apache.ant.compress.resources.TarFileSet;
import org.apache.ant.compress.resources.TarResource;
import org.apache.ant.compress.resources.ZipFileSet;
import org.apache.ant.compress.resources.ZipResource;
import org.apache.ant.compress.util.EntryHelper;
import org.apache.ant.compress.util.StreamFactory;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ExtraFieldUtils;
import org.apache.commons.compress.archivers.zip.ZipExtraField;
import org.apache.commons.compress.archivers.zip.ZipShort;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Expand;
import org.apache.tools.ant.types.ArchiveFileSet;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.ArchiveResource;
import org.apache.tools.ant.types.resources.FileResource;
import org.apache.tools.ant.util.FileUtils;

/**
 * Base implementation of tasks creating archives.
 */
public abstract class ArchiveBase extends Task {
    private final StreamFactory factory;
    private final EntryBuilder builder;

    private Resource dest;
    private List/*<ResourceCollection>*/ sources = new ArrayList();
    private Mode mode = new Mode();
    private String encoding;
    private boolean filesOnly = true;

    protected ArchiveBase(StreamFactory factory, EntryBuilder builder) {
        this.factory = factory;
        this.builder = builder;
    }

    /**
     * The archive to create.
     */
    public void setDestfile(File f) {
        addDest(new FileResource(f));
    }

    /**
     * The archive to create.
     */
    public void addDest(Resource r) {
        if (dest != null) {
            throw new BuildException("Can only have one destination resource"
                                     + " for archive.");
        }
        dest = r;
    }

    /**
     * Sources for the archive.
     */
    public void add(ResourceCollection c) {
        sources.add(c);
    }

    /**
     * How to treat the target archive.
     */
    public void setMode(Mode m) {
        mode = m;
    }

    /**
     * Encoding of file names.
     */
    public void setEncoding(String e) {
        encoding = e;
    }

    /**
     * Whether only file entries should be added to the archive.
     */
    public void setFilesOnly(boolean b) {
        filesOnly = b;
    }

    public void execute() {
        validate();
        if (!dest.isExists()) {
            // create mode
            mode = new Mode();
        }
        ResourceWithFlags[] toAdd;
        try {
            toAdd = findSources();
        } catch (IOException ioex) {
            throw new BuildException("Failed to read sources", ioex);
        }
        if (toAdd.length == 0) {
            log("No sources, nothing to do", Project.MSG_WARN);
        } else {
            try {
                writeArchive(toAdd);
            } catch (IOException ioex) {
                throw new BuildException("Failed to write archive", ioex);
            }
        }
    }

    /**
     * Argument validation.
     */
    protected void validate() throws BuildException {
        if (dest == null) {
            throw new BuildException("must provide a destination resource");
        }
        if (sources.size() == 0) {
            throw new BuildException("must provide sources");
        }
    }

    /**
     * Find all the resources with their flags that should be added to
     * the archive.
     */
    protected ResourceWithFlags[] findSources() throws IOException {
        ArrayList l = new ArrayList();
        for (Iterator rcs = sources.iterator(); rcs.hasNext(); ) {
            ResourceCollection rc = (ResourceCollection) rcs.next();
            ResourceCollectionFlags rcFlags = getFlags(rc);
            for (Iterator rs = rc.iterator(); rs.hasNext(); ) {
                Resource r = (Resource) rs.next();
                if (!filesOnly || !r.isDirectory()) {
                    l.add(new ResourceWithFlags(r, rcFlags, getFlags(r)));
                }
            }
        }
        return (ResourceWithFlags[]) l.toArray(new ResourceWithFlags[l.size()]);
    }

    /**
     * Creates the archive archiving the given resources.
     */
    protected void writeArchive(ResourceWithFlags[] src)
        throws IOException {
        FileUtils fu = FileUtils.getFileUtils();
        ArchiveOutputStream out = null;
        try {
            out =
                factory.getArchiveStream(new BufferedOutputStream(dest
                                                                  .getOutputStream()),
                                         Expand.NATIVE_ENCODING.equals(encoding)
                                         ? null : encoding);
            for (int i = 0; i < src.length; i++) {

                ArchiveEntry ent = builder.buildEntry(src[i]);
                out.putArchiveEntry(ent);
                if (!src[i].getResource().isDirectory()) {
                    InputStream in = null;
                    try {
                        in = src[i].getResource().getInputStream();

                        byte[] buffer = new byte[8192];
                        int count = 0;
                        do {
                            out.write(buffer, 0, count);
                            count = in.read(buffer, 0, buffer.length);
                        } while (count != -1);
                    } finally {
                        fu.close(in);
                    }
                }
                out.closeArchiveEntry();

            }
        } finally {
            fu.close(out);
        }
    }

    /**
     * Extracts flags from a resource.
     *
     * <p>ZipExceptions are only here for the code that translates
     * Ant's ZipExtraFields to CC ZipExtraFields and should never
     * actually be thrown.</p>
     */
    protected ResourceFlags getFlags(Resource r) throws ZipException {
        if (r instanceof ArchiveResource) {
            if (r instanceof CommonsCompressArchiveResource) {
                if (r instanceof TarResource) {
                    TarResource tr = (TarResource) r;
                    return new ResourceFlags(tr.getMode(), tr.getUid(),
                                             tr.getGid(), tr.getUserName(),
                                             tr.getGroup());
                } else if (r instanceof ZipResource) {
                    ZipResource zr = (ZipResource) r;
                    return new ResourceFlags(zr.getMode(), zr.getExtraFields());
                } else {
                    CommonsCompressArchiveResource cr =
                        (CommonsCompressArchiveResource) r;
                    return new ResourceFlags(cr.getMode(), cr.getUid(),
                                             cr.getGid());
                }
            } else if (r instanceof
                       org.apache.tools.ant.types.resources.TarResource) {
                org.apache.tools.ant.types.resources.TarResource tr =
                    (org.apache.tools.ant.types.resources.TarResource) r;
                return new ResourceFlags(tr.getMode(), tr.getUid(),
                                         tr.getGid(), tr.getUserName(),
                                         tr.getGroup());
            } else if (r instanceof
                       org.apache.tools.ant.types.resources.ZipResource) {
                org.apache.tools.ant.types.resources.ZipResource zr =
                    (org.apache.tools.ant.types.resources.ZipResource) r;

                org.apache.tools.zip.ZipExtraField[] extra = zr.getExtraFields();
                ZipExtraField[] ex =
                    new ZipExtraField[extra == null ? 0 : extra.length];
                if (extra != null && extra.length > 0) {
                    for (int i = 0; i < extra.length; i++) {
                        try {
                            ex[i] = ExtraFieldUtils
                                .createExtraField(new ZipShort(extra[i]
                                                               .getHeaderId()
                                                               .getValue()));
                        } catch (InstantiationException e) {
                            throw new BuildException(e);
                        } catch (IllegalAccessException e) {
                            throw new BuildException(e);
                        }
                        byte[] b = extra[i].getCentralDirectoryData();
                        ex[i].parseFromCentralDirectoryData(b, 0, b.length);
                        b = extra[i].getLocalFileDataData();
                        ex[i].parseFromLocalFileData(b, 0, b.length);
                    }
                }

                return new ResourceFlags(zr.getMode(), ex);
            } else {
                ArchiveResource ar = (ArchiveResource) r;
                return new ResourceFlags(ar.getMode());
            }
        }
        return new ResourceFlags();
    }

    /**
     * Extracts flags from a resource collection.
     */
    protected ResourceCollectionFlags getFlags(ResourceCollection rc) {
        if (rc instanceof ArchiveFileSet) {
            if (rc instanceof ArFileSet) {
                ArFileSet ar = (ArFileSet) rc;
                return new ResourceCollectionFlags(ar.getPrefix(getProject()),
                                                   ar.getFullpath(getProject()),
                                                   ar.hasFileModeBeenSet()
                                                   ? ar.getFileMode(getProject())
                                                   : -1,
                                                   ar.hasDirModeBeenSet()
                                                   ? ar.getDirMode(getProject())
                                                   : -1,
                                                   ar.hasUserIdBeenSet()
                                                   ? ar.getUid()
                                                   : EntryHelper.UNKNOWN_ID,
                                                   ar.hasGroupIdBeenSet()
                                                   ? ar.getGid()
                                                   : EntryHelper.UNKNOWN_ID);
            } else if (rc instanceof CpioFileSet) {
                CpioFileSet cr = (CpioFileSet) rc;
                return new ResourceCollectionFlags(cr.getPrefix(getProject()),
                                                   cr.getFullpath(getProject()),
                                                   cr.hasFileModeBeenSet()
                                                   ? cr.getFileMode(getProject())
                                                   : -1,
                                                   cr.hasDirModeBeenSet()
                                                   ? cr.getDirMode(getProject())
                                                   : -1,
                                                   cr.hasUserIdBeenSet()
                                                   ? cr.getUid()
                                                   : EntryHelper.UNKNOWN_ID,
                                                   cr.hasGroupIdBeenSet()
                                                   ? cr.getGid()
                                                   : EntryHelper.UNKNOWN_ID);
            } else if (rc instanceof TarFileSet) {
                TarFileSet tr = (TarFileSet) rc;
                return new ResourceCollectionFlags(tr.getPrefix(getProject()),
                                                   tr.getFullpath(getProject()),
                                                   tr.hasFileModeBeenSet()
                                                   ? tr.getFileMode(getProject())
                                                   : -1,
                                                   tr.hasDirModeBeenSet()
                                                   ? tr.getDirMode(getProject())
                                                   : -1,
                                                   tr.hasUserIdBeenSet()
                                                   ? tr.getUid()
                                                   : EntryHelper.UNKNOWN_ID,
                                                   tr.hasGroupIdBeenSet()
                                                   ? tr.getGid()
                                                   : EntryHelper.UNKNOWN_ID,
                                                   tr.hasUserNameBeenSet()
                                                   ? tr.getUserName() : null,
                                                   tr.hasGroupBeenSet()
                                                   ? tr.getGroup() : null);
            } else if (rc instanceof
                       org.apache.tools.ant.types.TarFileSet) {
                org.apache.tools.ant.types.TarFileSet tr =
                    (org.apache.tools.ant.types.TarFileSet) rc;
                return new ResourceCollectionFlags(tr.getPrefix(getProject()),
                                                   tr.getFullpath(getProject()),
                                                   tr.hasFileModeBeenSet()
                                                   ? tr.getFileMode(getProject())
                                                   : -1,
                                                   tr.hasDirModeBeenSet()
                                                   ? tr.getDirMode(getProject())
                                                   : -1,
                                                   tr.hasUserIdBeenSet()
                                                   ? tr.getUid()
                                                   : EntryHelper.UNKNOWN_ID,
                                                   tr.hasGroupIdBeenSet()
                                                   ? tr.getGid()
                                                   : EntryHelper.UNKNOWN_ID,
                                                   tr.hasUserNameBeenSet()
                                                   ? tr.getUserName() : null,
                                                   tr.hasGroupBeenSet()
                                                   ? tr.getGroup() : null);
            } else {
                ArchiveFileSet ar = (ArchiveFileSet) rc;
                return new ResourceCollectionFlags(ar.getPrefix(getProject()),
                                                   ar.getFullpath(getProject()),
                                                   ar.hasFileModeBeenSet()
                                                   ? ar.getFileMode(getProject())
                                                   : -1,
                                                   ar.hasDirModeBeenSet()
                                                   ? ar.getDirMode(getProject())
                                                   : -1);
            }
        }
        return new ResourceCollectionFlags();
    }

    /**
     * Valid Modes for create/update/replace.
     */
    public static final class Mode extends EnumeratedAttribute {

        /**
         * Create a new archive.
         */
        private static final String CREATE = "create";
        /**
         * Update an existing archive.
         */
        private static final String UPDATE = "update";
        /**
         * Update an existing archive, replacing all existing entries
         * with those from sources.
         */
        private static final String REPLACE = "replace";

        public Mode() {
            super();
            setValue(CREATE);
        }

        public String[] getValues() {
            return new String[] {CREATE, UPDATE, REPLACE};
        }

    }

    /**
     * Various flags a (archive) resource may hold in addition to
     * being a plain resource.
     */
    public static class ResourceFlags {
        private final int mode;
        private final int gid;
        private final int uid;
        private final ZipExtraField[] extraFields;
        private final String userName;
        private final String groupName;

        public ResourceFlags() {
            this(-1);
        }

        public ResourceFlags(int mode) {
            this(mode, new ZipExtraField[0]);
        }

        public ResourceFlags(int mode, ZipExtraField[] extraFields) {
            this(mode, extraFields, EntryHelper.UNKNOWN_ID,
                 EntryHelper.UNKNOWN_ID, null, null);
        }

        public ResourceFlags(int mode, int uid, int gid) {
            this(mode, new ZipExtraField[0], uid, gid, null, null);
        }

        public ResourceFlags(int mode, int uid, int gid, String userName,
                             String groupName) {
            this(mode, new ZipExtraField[0], uid, gid, userName, groupName);
        }

        private ResourceFlags(int mode, ZipExtraField[] extraFields,
                              int uid, int gid,
                              String userName, String groupName) {
            this.mode = mode;
            this.extraFields = extraFields;
            this.gid = gid;
            this.uid = uid;
            this.userName = userName;
            this.groupName = groupName;
        }

        public boolean hasModeBeenSet() { return mode >= 0; }
        public int getMode() { return mode; }

        public ZipExtraField[] getZipExtraFields() { return extraFields; }

        public boolean hasUserIdBeenSet() {
            return uid != EntryHelper.UNKNOWN_ID;
        }
        public int getUserId() { return uid; }

        public boolean hasGroupIdBeenSet() {
            return gid != EntryHelper.UNKNOWN_ID;
        }
        public int getGroupId() { return gid; }

        public boolean hasUserNameBeenSet() { return userName != null; }
        public String getUserName() { return userName; }

        public boolean hasGroupNameBeenSet() { return groupName != null; }
        public String getGroupName() { return groupName; }
    }

    /**
     * Various flags a (archive) resource collection may hold.
     */
    public static class ResourceCollectionFlags extends ResourceFlags {
        private final String prefix, fullpath;
        private final int dirMode;

        public ResourceCollectionFlags() {
            this(null, null);
        }

        public ResourceCollectionFlags(String prefix, String fullpath) {
            this(prefix, fullpath, -1, -1);
        }

        public ResourceCollectionFlags(String prefix, String fullpath,
                                       int fileMode, int dirMode) {
            this(prefix, fullpath, fileMode, dirMode, EntryHelper.UNKNOWN_ID,
                 EntryHelper.UNKNOWN_ID);
        }

        public ResourceCollectionFlags(String prefix, String fullpath,
                                       int fileMode, int dirMode,
                                       int uid, int gid) {
            this(prefix, fullpath, fileMode, dirMode, uid, gid, null, null);
        }

        public ResourceCollectionFlags(String prefix, String fullpath,
                                       int fileMode, int dirMode,
                                       int uid, int gid,
                                       String userName, String groupName) {
            super(fileMode, uid, gid, userName, groupName);
            this.dirMode = dirMode;
            this.prefix = prefix;
            this.fullpath = fullpath;
        }

        public boolean hasDirModeBeenSet() { return dirMode >= 0; }
        public int getDirMode() { return dirMode; }

        public boolean hasPrefix() { return prefix != null; }
        public String getPrefix() { return prefix; }

        public boolean hasFullpath() { return fullpath != null; }
        public String getFullpath() { return fullpath; }
    }

    /**
     * Binds a resource to additional data that may be present.
     */
    public static class ResourceWithFlags {
        private final Resource r;
        private final ResourceCollectionFlags rcFlags;
        private final ResourceFlags rFlags;

        public ResourceWithFlags(Resource r, ResourceCollectionFlags rcFlags,
                                 ResourceFlags rFlags) {
            this.r = r;
            this.rcFlags = rcFlags;
            this.rFlags = rFlags;
        }

        public Resource getResource() { return r; }
        public ResourceCollectionFlags getCollectionFlags() { return rcFlags; }
        public ResourceFlags getResourceFlags() { return rFlags; }

        /**
         * The name the target entry will have.
         *
         * <p>Already takes fullpath and prefix into account.</p>
         *
         * <p>Ensures directory names end in slashes while file names
         * never will.</p>
         */
        public String getName() {
            String name = r.getName();
            if (rcFlags.hasFullpath()) {
                name = rcFlags.getFullpath();
            } else if (rcFlags.hasPrefix()) {
                String prefix = rcFlags.getPrefix();
                if (!prefix.endsWith("/")) {
                    prefix = prefix + "/";
                }
                name = prefix + name;
            }
            if (r.isDirectory() && !name.endsWith("/")) {
                name += "/";
            } else if (r.isDirectory() && name.endsWith("/")) {
                name = name.substring(0, name.length() - 1);
            }
            return name;
        }
    }

    /**
     * Creates an archive entry for the concrete format.
     */
    public static interface EntryBuilder {
        ArchiveEntry buildEntry(ResourceWithFlags resource);
    }
}
