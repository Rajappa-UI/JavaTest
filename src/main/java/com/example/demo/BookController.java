package com.example.demo;

import com.example.demo.db.Book;
import com.example.demo.db.BookRepository;
import com.example.demo.google.GoogleBook;
import com.example.demo.google.GoogleBookService;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@RestController
public class BookController {
    private final BookRepository bookRepository;
    private final GoogleBookService googleBookService;

    @Autowired
    public BookController(BookRepository bookRepository, GoogleBookService googleBookService) {
        this.bookRepository = bookRepository;
        this.googleBookService = googleBookService;
    }

    @GetMapping("/books")
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    @GetMapping("/google")
    public GoogleBook searchGoogleBooks(@RequestParam("q") String query,
                                        @RequestParam(value = "maxResults", required = false) Integer maxResults,
                                        @RequestParam(value = "startIndex", required = false) Integer startIndex) {
        return googleBookService.searchBooks(query, maxResults, startIndex);
    }

    @PostMapping("/books/{googleId}")
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public Book addBook(@PathVariable String googleId) {

        GoogleBook.Item googleBook = googleBookService.getBookById(googleId);

        if (googleBook == null || googleBook.volumeInfo() == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "Invalid Google Book ID"
            );
        }

        var volume = googleBook.volumeInfo();

        Book book = new Book();
        book.setId(googleBook.id());
        book.setTitle(volume.title());
        book.setAuthor(
                volume.authors() != null && !volume.authors().isEmpty()
                        ? volume.authors().get(0)
                        : "Unknown"
        );
        book.setPageCount(volume.pageCount());

        return bookRepository.save(book);
    }    
}
