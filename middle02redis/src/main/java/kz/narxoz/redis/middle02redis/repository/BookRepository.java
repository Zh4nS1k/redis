package kz.narxoz.redis.middle02redis.repository;

import kz.narxoz.redis.middle02redis.models.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
}
