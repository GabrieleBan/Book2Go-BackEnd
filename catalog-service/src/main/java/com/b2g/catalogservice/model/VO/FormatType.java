package com.b2g.catalogservice.model.VO;

public enum FormatType {
    PHYSICAL,
    EBOOK,
    AUDIOBOOK,
    HARDCOVER,
    PAPERBACK;
    public boolean isPhysical() {
        return this == PHYSICAL || this == HARDCOVER || this == PAPERBACK;
    }

    public boolean isDigital() {
        return !isPhysical();
    }

}
