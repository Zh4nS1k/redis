package kz.narxoz.redis.middle02redis.repository;

import kz.narxoz.redis.middle02redis.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Long id(Long id);
}
