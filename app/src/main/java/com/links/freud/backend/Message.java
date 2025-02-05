package com.links.freud.backend;

public class Message {

    // region Members

    public String role;

    public Content[] content;

    // endregion

    // region Constructor

    public Message(String role, Content content) {
        this.role = role;
        this.content = new Content[1];
        this.content[0] = content;
    }

    // endregion

}