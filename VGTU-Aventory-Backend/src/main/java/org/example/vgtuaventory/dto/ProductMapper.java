package org.example.vgtuaventory.dto;

import org.example.vgtuaventory.model.Category;
import org.example.vgtuaventory.model.Product;
import org.example.vgtuaventory.model.User;

/**
 * Laukų perkėlimas tarp užklausų, atsakymų ir Product esybės vienoje vietoje.
 */
public final class ProductMapper {

    private ProductMapper() {
    }

    /** Naujas produktas: visi laukai perkeliami, kiekis tik jei nurodytas (kitaip 0). */
    public static Product toNewProduct(ProductRequest request, String name, Category category, int ownerId) {
        User owner = new User();
        owner.setId(ownerId);

        Product product = new Product();
        product.setOwner(owner);
        product.setProductName(name);
        product.setPrice(request.price());
        product.setProductDescription(request.productDescription());
        product.setPhotoUrl(request.photoUrl());
        if (request.quantity() != null) {
            product.setQuantity(request.quantity());
        }
        product.setQrCode(request.qrCode());
        product.setCategory(category);
        return product;
    }

    /** Redagavimas: keičiami tik užklausoje nurodyti (ne null) laukai. */
    public static void applyOptionalFields(ProductRequest request, Product product) {
        if (request.price() != null) {
            product.setPrice(request.price());
        }
        if (request.productDescription() != null) {
            product.setProductDescription(request.productDescription());
        }
        if (request.photoUrl() != null) {
            product.setPhotoUrl(request.photoUrl());
        }
        if (request.quantity() != null) {
            product.setQuantity(request.quantity());
        }
        if (request.qrCode() != null) {
            product.setQrCode(request.qrCode());
        }
    }

    public static ProductScanResponse toScanResponse(Product product) {
        Integer categoryId = product.getCategory() != null ? product.getCategory().getCategoryId() : null;
        return new ProductScanResponse(
                product.getProductId(),
                product.getProductName(),
                product.getPrice(),
                product.getProductDescription(),
                product.getPhotoUrl(),
                product.getQuantity(),
                product.getQrCode(),
                categoryId
        );
    }
}
