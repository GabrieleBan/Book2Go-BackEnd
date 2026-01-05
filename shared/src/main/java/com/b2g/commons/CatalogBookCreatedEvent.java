package com.b2g.commons;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CatalogBookCreatedEvent{
    public UUID id;
    public String title;
    public String author;
    public String publisher;
    public Set<CategoryDTO> categories;
}
