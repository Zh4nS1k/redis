package kz.narxoz.redis.middle02redis.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import kz.narxoz.redis.middle02redis.models.Book;

import java.math.BigDecimal;

public record BookRequest(
        @NotBlank(message = "Title is required")
        String title,

        @NotBlank(message = "Author is required")
        String author,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be positive")
        BigDecimal price,

        @NotBlank(message = "Genre is required")
        String genre,

        @Size(max = 2048, message = "Description is too long")
        String description
) {

    public Book toEntity() {
        return Book.builder()
                .title(title)
                .author(author)
                .price(price)
                .genre(genre)
                .description(description)
                .build();
    }
}
