package org.example.vgtuaventory.dto;

import org.example.vgtuaventory.model.SaleType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SaleRequestDTO {
    private int productId;
    private int ownerId;
    private int quantity;
    private String saleLocation;
    private SaleType saleType;
    private String saleNote; // optional
}