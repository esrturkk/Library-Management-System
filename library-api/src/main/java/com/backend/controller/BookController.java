package com.backend.controller;

import com.backend.dto.BookCreateRequest;
import com.backend.dto.MyBookResponse;
import com.backend.model.Book;
import com.backend.model.Member;
import com.backend.repository.BookRepository;
import com.backend.repository.MemberRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/books")
public class BookController {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private MemberRepository memberRepository;

    @PostMapping("/create")
    public ResponseEntity<String> createBook(@RequestBody BookCreateRequest request, Authentication authentication) {
        // Yeni kitap oluştur
        Book book = new Book();
        book.setIsbn(request.getIsbn());
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setPublished_date(request.getPublishedDate());
        book.setIsAvailable(true); // Yeni bir kitap varsayılan olarak ödünç alınabilir.
        book.setDate_of_receipt(null); // Henüz ödünç alınmadı.
        book.setOwner(null); // Kitabın sahibi yok.

        // Kitabı kaydet
        bookRepository.save(book);

        return ResponseEntity.ok("Book created successfully with title: " + book.getTitle());
    }

    // Mevcut (is_available = true) kitapları listeleme
    @GetMapping("/available")
    public ResponseEntity<List<Book>> getAvailableBooks() {
        // is_available = true olan kitapları sorgula
        List<Book> availableBooks = bookRepository.findByIsAvailableTrue();

        // Listeyi döndür
        return ResponseEntity.ok(availableBooks);
    }

    @DeleteMapping("/{bookId}")
    public ResponseEntity<String> deleteBook(@PathVariable Integer bookId) {
        // Kitabı kontrol et
        if (!bookRepository.existsById(bookId)) {
            return ResponseEntity.status(404).body("Book not found with ID: " + bookId);
        }

        // Kitabı sil
        bookRepository.deleteById(bookId);
        return ResponseEntity.ok("Book deleted successfully with ID: " + bookId);
    }

    // Kullanıcının aldığı kitapları listeleyen endpoint
    // Kullanıcının aldığı kitapları listeleyen endpoint
    @GetMapping("/my-books")
    public ResponseEntity<List<MyBookResponse>> getMyBooks(Authentication authentication) {
        // Giriş yapmış kullanıcının bilgilerini al
        String username = authentication.getName();
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Kullanıcının kitaplarını al
        List<Book> books = bookRepository.findByOwner(member);

        // Book nesnelerini MyBookDTO'ya dönüştür
        List<MyBookResponse> myBookDTOs = books.stream()
                .map(book -> new MyBookResponse(
                        book.getId(),
                        book.getTitle(),
                        book.getAuthor(),
                        book.getIsbn(),
                        book.getPublished_date(),
                        book.getDate_of_receipt()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(myBookDTOs);
    }
}
