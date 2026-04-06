package org.example.vgtuaventory.dto;

import lombok.Getter;
import lombok.Setter;
import org.example.vgtuaventory.model.Product;

@Getter
@Setter
public class ProductDTO {
    private int productId;
    private String productName;
    private int quantity;

    public ProductDTO(Product p) {
        this.productId = p.getProductId();
        this.productName = p.getProductName();
        this.quantity = p.getQuantity();
    }

}