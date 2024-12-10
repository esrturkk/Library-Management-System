package com.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookCreateRequest {
    private String isbn;
    private String title;
    private String author;
    private Date publishedDate;
}
