
package com.backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class MyBookResponse {
    private Integer id;
    private String title;
    private String author;
    private String isbn;
    private Date publishedDate;
    private Date dateOfReceipt;

    public MyBookResponse(Integer id, String title, String author, String isbn, Date publishedDate, Date dateOfReceipt) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.publishedDate = publishedDate;
        this.dateOfReceipt = dateOfReceipt;
    }
}