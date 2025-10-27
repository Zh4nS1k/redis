package kz.narxoz.redis.middle02redis.service;

import kz.narxoz.redis.middle02redis.models.Book;
import kz.narxoz.redis.middle02redis.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookService {

    private static final String BOOK_CACHE_KEY_PREFIX = "book:";
    private static final String POPULAR_BOOKS_CACHE_KEY = "books:popular";
    private static final String POPULARITY_SORTED_SET_KEY = "books:popularity";
    private static final long BOOK_CACHE_TTL_MINUTES = 10;
    private static final long POPULAR_CACHE_TTL_MINUTES = 5;
    private static final int POPULAR_CACHE_SIZE = 20;

    private final BookRepository bookRepository;
    private final CacheService cacheService;
    private final RedisUtility redisUtility;

    @Transactional
    public Book createBook(Book book) {
        Book saved = bookRepository.save(book);
        cacheService.cacheObject(buildBookCacheKey(saved.getId()), saved, BOOK_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        cacheService.deleteCachedObject(POPULAR_BOOKS_CACHE_KEY);
        return saved;
    }

    public List<Book> listBooks() {
        return bookRepository.findAll();
    }

    public Book getBook(Long id) {
        String cacheKey = buildBookCacheKey(id);
        Book cached = cacheService.getCacheObject(cacheKey, Book.class);
        if (cached != null) {
            redisUtility.incrementPopularity(POPULARITY_SORTED_SET_KEY, id);
            return cached;
        }

        return bookRepository.findById(id)
                .map(book -> {
                    cacheService.cacheObject(cacheKey, book, BOOK_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
                    redisUtility.incrementPopularity(POPULARITY_SORTED_SET_KEY, id);
                    return book;
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found: " + id));
    }

    @Transactional
    public Book updateBook(Long id, Book payload) {
        Book target = bookRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found: " + id));

        target.setTitle(payload.getTitle());
        target.setAuthor(payload.getAuthor());
        target.setPrice(payload.getPrice());
        target.setGenre(payload.getGenre());
        target.setDescription(payload.getDescription());

        Book updated = bookRepository.save(target);
        cacheService.cacheObject(buildBookCacheKey(id), updated, BOOK_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        cacheService.deleteCachedObject(POPULAR_BOOKS_CACHE_KEY);
        return updated;
    }

    @Transactional
    public void deleteBook(Long id) {
        boolean exists = bookRepository.existsById(id);
        if (!exists) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found: " + id);
        }

        bookRepository.deleteById(id);
        cacheService.deleteCachedObject(buildBookCacheKey(id));
        cacheService.deleteCachedObject(POPULAR_BOOKS_CACHE_KEY);
        redisUtility.removePopularity(POPULARITY_SORTED_SET_KEY, id);
    }

    public List<Book> getPopularBooks(int limit) {
        @SuppressWarnings("unchecked")
        List<Book> cached = cacheService.getCacheObject(POPULAR_BOOKS_CACHE_KEY, List.class);
        if (cached != null && !cached.isEmpty()) {
            return cached.subList(0, Math.min(limit, cached.size()));
        }

        List<Long> popularIds = redisUtility.fetchPopularIds(POPULARITY_SORTED_SET_KEY, POPULAR_CACHE_SIZE);
        if (popularIds.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, Book> booksById = bookRepository.findAllById(popularIds).stream()
                .collect(Collectors.toMap(Book::getId, Function.identity()));

        List<Book> ordered = popularIds.stream()
                .map(booksById::get)
                .filter(Objects::nonNull)
                .toList();

        cacheService.cacheObject(POPULAR_BOOKS_CACHE_KEY, ordered, POPULAR_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        return ordered.subList(0, Math.min(limit, ordered.size()));
    }

    private String buildBookCacheKey(Long id) {
        return BOOK_CACHE_KEY_PREFIX + id;
    }
}
