package org.xedox.webaide;

import java.io.File;
import org.xedox.webaide.io.FileX;
import org.xedox.webaide.io.IFile;

public class Project {
    public String name;
    public IFile path;
    public IFile indexHtml = null;

    public Project(String name) {
        this.path = new FileX(IDE.PROJECTS_PATH, name);
        this.name = this.path.getName();
        findIndexHtml(this.path.toFile());
    }

    public String getName() {
        return name;
    }

    public IFile getPath() {
        return path;
    }

    public IFile getIndexHtml() {
        return indexHtml;
    }

    private void findIndexHtml(File directory) {
        try {
            for (File file : directory.listFiles()) {
                if (file.isDirectory()) {
                    findIndexHtml(file);
                } else if ("index.html".equals(file.getName())) {
                    indexHtml = new FileX(file);
                    return;
                }
            }
        } catch (Exception err) {
        }
    }
}
