package kz.narxoz.redis.middle02redis.service;

import kz.narxoz.redis.middle02redis.models.Product;
import kz.narxoz.redis.middle02redis.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;


@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final CacheService cacheService;

    public Product addProduct(Product product) {
        return productRepository.save(product);
    }

    public Product getProduct(Long id) {
        // return productRepository.findById(id).orElse(null);
        final String cacheKey = "product:" + id;
        Product cacheProduct = (Product) cacheService.getCacheObject(cacheKey);
        if (cacheProduct != null) {
            return cacheProduct;
        }
        Optional<Product> product = productRepository.findById(id);
        product.ifPresent(p -> cacheService.cacheObject(cacheKey, p, 1, TimeUnit.MINUTES)
        );
        return product.orElse(null);
    }

}
