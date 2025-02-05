package com.links.freud.backend;

import java.io.File;

public class FileRequestModel {

    // region Members

    private String type;

    private File file;

    // endregion

    // region Constructor

    public FileRequestModel(String type, File file) {
        this.type = type;
        this.file = file;
    }

    public File getFile() {
        return this.file;
    }

    public String getType() {
        return this.type;
    }

    // endregion

}
