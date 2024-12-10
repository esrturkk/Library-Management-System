package com.backend.controller;

import com.backend.dto.BookLoanRequest;
import com.backend.dto.BookReturnRequest;
import com.backend.model.Book;
import com.backend.model.Member;
import com.backend.repository.BookRepository;
import com.backend.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/api/books")
public class BookLoanController {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private MemberRepository memberRepository;

    @PostMapping("/loan")
    public ResponseEntity<String> loanBook(@RequestBody BookLoanRequest request, Authentication authentication) {
        // Kitabı bul
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new RuntimeException("Book not found with ID: " + request.getBookId()));

        // Kitap zaten ödünç alınmışsa hata döndür
        if (!book.getIsAvailable()) {
            return ResponseEntity.badRequest().body("Book is not available for loaning.");
        }

        // Mevcut kullanıcıyı bul
        Member member = memberRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Kitap durumunu güncelle
        book.setIsAvailable(false);
        book.setDate_of_receipt(new Date());
        book.setOwner(member);

        // Kitabı kaydet
        bookRepository.save(book);

        return ResponseEntity.ok("Book loaned successfully.");
    }

    @PostMapping("/return")
    public ResponseEntity<String> returnBook(@RequestBody BookReturnRequest request, Authentication authentication) {
        // Kitabı bul
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new RuntimeException("Book not found with ID: " + request.getBookId()));

        // Kitabın sahibiyle eşleşme kontrolü
        Member member = memberRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (book.getOwner() == null || !book.getOwner().getId().equals(member.getId())) {
            return ResponseEntity.badRequest().body("You are not the owner of this book.");
        }

        // Kitap durumunu güncelle
        book.setIsAvailable(true);
        book.setDate_of_receipt(null);
        book.setOwner(null);

        // Güncellenen kitap kaydedilir
        bookRepository.save(book);

        return ResponseEntity.ok("Book returned successfully.");
    }
}
