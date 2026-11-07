package org.example.vgtuaventory.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.vgtuaventory.model.SaleType;

/**
 * Body of POST /sales. ownerId is optional (0 = not sent); when sent it must match the authenticated user.
 * Kept as a setter-bound bean on purpose: Jackson 3 binds records and all-args constructors through
 * the constructor and then rejects a missing primitive (e.g. ownerId), while clients may omit it.
 */
@Getter
@Setter
@NoArgsConstructor
public class SaleRequest {
    private int productId;
    private int ownerId;
    private int quantity;
    private String saleLocation;
    private SaleType saleType;
    private String saleNote;
}
