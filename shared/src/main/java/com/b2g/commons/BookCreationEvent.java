package com.b2g.commons;

import java.util.List;
import java.util.UUID;

public class BookCreationEvent {
    private UUID id;
    private String title;

    private List<String> authors;
    private String publisher;
    private List<Tag> tags;

    private static class Tag {
        private UUID id;
        private String name;
    }
}

