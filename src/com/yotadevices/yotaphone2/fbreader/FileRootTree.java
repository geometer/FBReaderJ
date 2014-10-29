package com.yotadevices.yotaphone2.fbreader;

import org.geometerplus.fbreader.Paths;
import org.geometerplus.fbreader.book.IBookCollection;
import org.geometerplus.fbreader.library.FileTree;
import org.geometerplus.fbreader.library.LibraryTree;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.resources.ZLResource;


public class FileRootTree extends LibraryTree {
    protected FileRootTree(IBookCollection collection) {
        super(collection);
        waitForOpening();
    }

    public LibraryTree getLibraryTree(LibraryTree.Key key) {
        if (key == null) {
            return null;
        }
        if (key.Parent == null) {
            return key.Id.equals(getUniqueKey().Id) ? this : null;
        }
        final LibraryTree parentTree = getLibraryTree(key.Parent);
        return parentTree != null ? (LibraryTree)parentTree.getSubtree(key.Id) : null;
    }

    @Override
    protected String getStringId() {
        return "@FBReaderFileChooseRoot";
    }

    @Override
    public String getName() {
        return resource().getValue();
    }

    @Override
    public String getSummary() {
        return resource().getValue();
    }
    @Override
    public String getTreeTitle() {
        return resource().getResource("fileTreeRoot").getValue();
    }

    @Override
    public Status getOpeningStatus() {
        return Status.ALWAYS_RELOAD_BEFORE_OPENING;
    }

    @Override
    public void waitForOpening() {
        clear();
        for (String dir : Paths.BookPathOption.getValue()) {
            addChild(dir, "fileTreeLibrary", dir);
        }
        addChild("/", "fileTreeRoot", null);
        addChild(Paths.cardDirectory(), "fileTreeCard", null);
    }

    private void addChild(String path, String resourceKey, String summary) {
        final ZLFile file = ZLFile.createFileByPath(path);
        if (file != null) {
            final ZLResource resource = resource().getResource(resourceKey);
            new FileTree(
                    this,
                    file,
                    resource.getValue(),
                    summary != null ? summary : resource.getResource("summary").getValue()
            );
        }
    }

}
