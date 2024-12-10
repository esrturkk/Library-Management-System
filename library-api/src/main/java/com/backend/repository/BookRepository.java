package com.backend.repository;

import com.backend.model.Book;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Integer> {
    // is_available = true olan kitapları döndür
    List<Book> findByIsAvailableTrue();
}
