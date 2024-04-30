package com.example.conprgKZ.Service;

import com.example.conprgKZ.Entity.Product;
import com.example.conprgKZ.Entity.Variation;
import com.example.conprgKZ.Repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Service
public class ProductService {

    @Value("${upload.path}")
    private String uploadPath; // Path where images will be stored


    private final ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;

    }

    public Product createProduct(Product product, List<MultipartFile> productImages, MultipartFile variationImage) {
        // Save product images to the file system and set their paths in the product entity
        List<String> productImagePaths = new ArrayList<>();

        for (MultipartFile file : productImages) {
            String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
            Path path = Paths.get(uploadPath + fileName);
            try {
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                productImagePaths.add(path.toString());
            } catch (IOException e) {
                e.printStackTrace(); // Handle the exception properly
            }
        }

        product.setImages(productImagePaths);

        // Handle variations
        List<Variation> variations = new ArrayList<>();
        for (Variation variation : product.getVariation()) {
            // Set variation image if present
            if (variationImage != null && !variationImage.isEmpty()) {
                String variationFileName = StringUtils.cleanPath(Objects.requireNonNull(variationImage.getOriginalFilename()));
                Path variationPath = Paths.get(uploadPath + variationFileName);
                try {
                    Files.copy(variationImage.getInputStream(), variationPath, StandardCopyOption.REPLACE_EXISTING);
                    variation.setImage(variationPath.toString());
                } catch (IOException e) {
                    e.printStackTrace(); // Handle the exception properly
                }
            }
            variations.add(variation);
        }
        product.setVariation(variations);

        return productRepository.save(product);
    }



    public Product getProductById(Long productId) {
        return productRepository.findById(productId).orElse(null);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

}