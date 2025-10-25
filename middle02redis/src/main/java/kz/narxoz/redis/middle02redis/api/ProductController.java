package kz.narxoz.redis.middle02redis.api;

import kz.narxoz.redis.middle02redis.models.Product;
import kz.narxoz.redis.middle02redis.repository.ProductRepository;
import kz.narxoz.redis.middle02redis.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/product")
@RequiredArgsConstructor
public class ProductController {
    private final ProductRepository productRepository;
    private final ProductService productService;

    @PostMapping
    public Product addProduct(@RequestBody Product product) {
        return productService.addProduct(product);
    }
    @GetMapping(value = "{id}")
    private Product getProduct(@PathVariable(name = "id") Long id) {
        return productService.getProduct(id);
    }

}
