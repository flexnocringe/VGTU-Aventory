package org.example.vgtuaventory.controller;

import org.example.vgtuaventory.exception.GlobalExceptionHandler;
import org.example.vgtuaventory.model.Category;
import org.example.vgtuaventory.model.Product;
import org.example.vgtuaventory.model.User;
import org.example.vgtuaventory.repository.CategoryRepository;
import org.example.vgtuaventory.repository.ProductRepository;
import org.example.vgtuaventory.service.ProductService;
import org.example.vgtuaventory.utils.AuthSessionAttributes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Charakterizaciniai ProductController testai (2 ND, parasyti pries pertvarkyma).
 * Tikrinama tik isorine elgsena: HTTP kodas ir atsakymo turinys.
 * Pertvarkymo metu keiciamas tik setUp(); visi tikrinimai lieka nepakeisti.
 */
@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    private static final int USER_ID = 1;
    private static final int OTHER_USER_ID = 2;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ProductService productService = new ProductService(productRepository, categoryRepository);
        ProductController controller = new ProductController(productService);
                mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ---------- pagalbiniai metodai ----------

    private static Product product(int id, String name, Integer ownerId) {
        Product p = new Product();
        p.setProductId(id);
        p.setProductName(name);
        if (ownerId != null) {
            User owner = new User();
            owner.setId(ownerId);
            p.setOwner(owner);
        }
        return p;
    }

    private static Category category(int id) {
        Category c = new Category();
        c.setCategoryId(id);
        c.setCategoryName("Kanceliarija");
        return c;
    }

    private static MockHttpServletRequestBuilder asUser(MockHttpServletRequestBuilder request) {
        return request.requestAttr(AuthSessionAttributes.CURRENT_USER_ID, USER_ID);
    }

    private static MockHttpServletRequestBuilder withJson(MockHttpServletRequestBuilder request, String json) {
        return asUser(request).contentType(MediaType.APPLICATION_JSON).content(json);
    }

    private void saveReturnsArgument() {
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private void verifySavedOnce() {
        verify(productRepository, times(1)).save(any(Product.class));
    }

    // ---------- GET /api/products/all ----------

    @Test
    void list_returnsOwnProducts() throws Exception {
        when(productRepository.findAllByOwner_Id(USER_ID))
                .thenReturn(List.of(product(1, "Pen", USER_ID), product(2, "Notebook", USER_ID)));

        mockMvc.perform(asUser(get("/api/products/all")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].productName").value("Pen"))
                .andExpect(jsonPath("$[1].productName").value("Notebook"));
    }

    // ---------- GET /api/products/{id} ----------

    @Test
    void getById_ownProduct_returns200() throws Exception {
        when(productRepository.findById(7)).thenReturn(Optional.of(product(7, "Marker", USER_ID)));

        mockMvc.perform(asUser(get("/api/products/7")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(7))
                .andExpect(jsonPath("$.productName").value("Marker"));
    }

    @Test
    void getById_otherUsersProduct_returns403() throws Exception {
        when(productRepository.findById(7)).thenReturn(Optional.of(product(7, "Marker", OTHER_USER_ID)));

        mockMvc.perform(asUser(get("/api/products/7")))
                .andExpect(status().isForbidden())
                .andExpect(content().string("You can only access your own products"));
    }

    @Test
    void getById_productWithoutOwner_returns403() throws Exception {
        when(productRepository.findById(7)).thenReturn(Optional.of(product(7, "Marker", null)));

        mockMvc.perform(asUser(get("/api/products/7")))
                .andExpect(status().isForbidden())
                .andExpect(content().string("You can only access your own products"));
    }

    @Test
    void getById_missing_returns404() throws Exception {
        when(productRepository.findById(999)).thenReturn(Optional.empty());

        mockMvc.perform(asUser(get("/api/products/999")))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Product not found"));
    }

    // ---------- GET /api/products/scan ----------

    @Test
    void scan_found_returnsProductWithCategory() throws Exception {
        Product p = product(3, "Stapler", USER_ID);
        p.setPrice(4.5);
        p.setProductDescription("Metal");
        p.setPhotoUrl("stapler.png");
        p.setQuantity(12);
        p.setQrCode("QR-3");
        p.setCategory(category(5));
        when(productRepository.findByQrCodeAndOwner_Id("QR-3", USER_ID)).thenReturn(Optional.of(p));

        mockMvc.perform(asUser(get("/api/products/scan").param("qrCode", "QR-3")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(3))
                .andExpect(jsonPath("$.productName").value("Stapler"))
                .andExpect(jsonPath("$.price").value(4.5))
                .andExpect(jsonPath("$.productDescription").value("Metal"))
                .andExpect(jsonPath("$.photoUrl").value("stapler.png"))
                .andExpect(jsonPath("$.quantity").value(12))
                .andExpect(jsonPath("$.qrCode").value("QR-3"))
                .andExpect(jsonPath("$.categoryId").value(5));
    }

    @Test
    void scan_foundWithoutCategory_returnsNullCategoryId() throws Exception {
        when(productRepository.findByQrCodeAndOwner_Id("QR-4", USER_ID))
                .thenReturn(Optional.of(product(4, "Glue", USER_ID)));

        mockMvc.perform(asUser(get("/api/products/scan").param("qrCode", "QR-4")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryId").value(nullValue()));
    }

    @Test
    void scan_notFound_returns404() throws Exception {
        when(productRepository.findByQrCodeAndOwner_Id("NONE", USER_ID)).thenReturn(Optional.empty());

        mockMvc.perform(asUser(get("/api/products/scan").param("qrCode", "NONE")))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Product not found"));
    }

    // ---------- POST /api/products ----------

    @Test
    void create_valid_returns201AndTrimsName() throws Exception {
        when(productRepository.existsByProductNameAndOwner_Id("Pen", USER_ID)).thenReturn(false);
        saveReturnsArgument();

        mockMvc.perform(withJson(post("/api/products"), """
                        {"productName":"  Pen  ","price":1.99,"productDescription":"Blue",
                         "photoUrl":"pen.png","quantity":5,"qrCode":"QR-1"}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productName").value("Pen"))
                .andExpect(jsonPath("$.price").value(1.99))
                .andExpect(jsonPath("$.productDescription").value("Blue"))
                .andExpect(jsonPath("$.photoUrl").value("pen.png"))
                .andExpect(jsonPath("$.quantity").value(5))
                .andExpect(jsonPath("$.qrCode").value("QR-1"))
                .andExpect(jsonPath("$.owner.id").value(USER_ID))
                .andExpect(jsonPath("$.category").value(nullValue()));

        verifySavedOnce();
        verify(categoryRepository, never()).findByCategoryIdAndOwner_Id(anyInt(), anyInt());
    }

    @Test
    void create_withValidCategoryAndNoQuantity_returns201() throws Exception {
        when(productRepository.existsByProductNameAndOwner_Id("Pen", USER_ID)).thenReturn(false);
        when(categoryRepository.findByCategoryIdAndOwner_Id(5, USER_ID)).thenReturn(Optional.of(category(5)));
        saveReturnsArgument();

        mockMvc.perform(withJson(post("/api/products"), """
                        {"productName":"Pen","categoryId":5}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.category.categoryId").value(5))
                .andExpect(jsonPath("$.quantity").value(0));

        verifySavedOnce();
    }

    @Test
    void create_missingName_returns400() throws Exception {
        mockMvc.perform(withJson(post("/api/products"), """
                        {"price":1.99}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("productName is required"));

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void create_blankName_returns400() throws Exception {
        mockMvc.perform(withJson(post("/api/products"), """
                        {"productName":"   "}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("productName is required"));

        verify(productRepository, never()).existsByProductNameAndOwner_Id(anyString(), anyInt());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void create_duplicateName_returns409() throws Exception {
        when(productRepository.existsByProductNameAndOwner_Id("Pencil", USER_ID)).thenReturn(true);

        mockMvc.perform(withJson(post("/api/products"), """
                        {"productName":"Pencil"}
                        """))
                .andExpect(status().isConflict())
                .andExpect(content().string("productName already exists"));

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void create_invalidCategory_returns400() throws Exception {
        when(productRepository.existsByProductNameAndOwner_Id("Pen", USER_ID)).thenReturn(false);
        when(categoryRepository.findByCategoryIdAndOwner_Id(99, USER_ID)).thenReturn(Optional.empty());

        mockMvc.perform(withJson(post("/api/products"), """
                        {"productName":"Pen","categoryId":99}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid categoryId (not found or not yours)"));

        verify(productRepository, never()).save(any(Product.class));
    }

    // ---------- PUT /api/products/{id} ----------

    @Test
    void update_notOwnedOrMissing_returns403() throws Exception {
        when(productRepository.findByProductIdAndOwner_Id(10, USER_ID)).thenReturn(Optional.empty());

        mockMvc.perform(withJson(put("/api/products/10"), """
                        {"productName":"New"}
                        """))
                .andExpect(status().isForbidden())
                .andExpect(content().string("You can only modify your own products"));

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void update_allFields_returns200() throws Exception {
        when(productRepository.findByProductIdAndOwner_Id(10, USER_ID))
                .thenReturn(Optional.of(product(10, "Old", USER_ID)));
        when(productRepository.findByProductNameAndOwner_Id("New", USER_ID)).thenReturn(Optional.empty());
        when(categoryRepository.findByCategoryIdAndOwner_Id(5, USER_ID)).thenReturn(Optional.of(category(5)));
        saveReturnsArgument();

        mockMvc.perform(withJson(put("/api/products/10"), """
                        {"productName":" New ","price":2.5,"productDescription":"Desc",
                         "photoUrl":"new.png","quantity":7,"qrCode":"QR-10","categoryId":5}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("New"))
                .andExpect(jsonPath("$.price").value(2.5))
                .andExpect(jsonPath("$.productDescription").value("Desc"))
                .andExpect(jsonPath("$.photoUrl").value("new.png"))
                .andExpect(jsonPath("$.quantity").value(7))
                .andExpect(jsonPath("$.qrCode").value("QR-10"))
                .andExpect(jsonPath("$.category.categoryId").value(5))
                .andExpect(jsonPath("$.owner.id").value(USER_ID));

        verifySavedOnce();
    }

    @Test
    void update_sameNameOnSameProduct_isNotConflict() throws Exception {
        Product existing = product(10, "Pen", USER_ID);
        when(productRepository.findByProductIdAndOwner_Id(10, USER_ID)).thenReturn(Optional.of(existing));
        when(productRepository.findByProductNameAndOwner_Id("Pen", USER_ID)).thenReturn(Optional.of(existing));
        saveReturnsArgument();

        mockMvc.perform(withJson(put("/api/products/10"), """
                        {"productName":"Pen"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("Pen"));

        verifySavedOnce();
    }

    @Test
    void update_nameTakenByOtherProduct_returns409() throws Exception {
        when(productRepository.findByProductIdAndOwner_Id(10, USER_ID))
                .thenReturn(Optional.of(product(10, "Pen", USER_ID)));
        when(productRepository.findByProductNameAndOwner_Id("Marker", USER_ID))
                .thenReturn(Optional.of(product(11, "Marker", USER_ID)));

        mockMvc.perform(withJson(put("/api/products/10"), """
                        {"productName":"Marker"}
                        """))
                .andExpect(status().isConflict())
                .andExpect(content().string("productName already exists"));

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void update_emptyBody_keepsAllValues() throws Exception {
        Product existing = product(10, "Pen", USER_ID);
        existing.setPrice(1.0);
        existing.setProductDescription("Blue");
        existing.setPhotoUrl("pen.png");
        existing.setQuantity(3);
        existing.setQrCode("QR-10");
        existing.setCategory(category(5));
        when(productRepository.findByProductIdAndOwner_Id(10, USER_ID)).thenReturn(Optional.of(existing));
        saveReturnsArgument();

        mockMvc.perform(withJson(put("/api/products/10"), "{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("Pen"))
                .andExpect(jsonPath("$.price").value(1.0))
                .andExpect(jsonPath("$.productDescription").value("Blue"))
                .andExpect(jsonPath("$.photoUrl").value("pen.png"))
                .andExpect(jsonPath("$.quantity").value(3))
                .andExpect(jsonPath("$.qrCode").value("QR-10"))
                .andExpect(jsonPath("$.category.categoryId").value(5));

        verifySavedOnce();
        verify(productRepository, never()).findByProductNameAndOwner_Id(anyString(), anyInt());
        verify(categoryRepository, never()).findByCategoryIdAndOwner_Id(anyInt(), anyInt());
    }

    @Test
    void update_blankName_keepsOldName() throws Exception {
        when(productRepository.findByProductIdAndOwner_Id(10, USER_ID))
                .thenReturn(Optional.of(product(10, "Pen", USER_ID)));
        saveReturnsArgument();

        mockMvc.perform(withJson(put("/api/products/10"), """
                        {"productName":"   "}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("Pen"));

        verifySavedOnce();
        verify(productRepository, never()).findByProductNameAndOwner_Id(anyString(), anyInt());
    }

    @Test
    void update_invalidCategory_returns400() throws Exception {
        when(productRepository.findByProductIdAndOwner_Id(10, USER_ID))
                .thenReturn(Optional.of(product(10, "Pen", USER_ID)));
        when(categoryRepository.findByCategoryIdAndOwner_Id(99, USER_ID)).thenReturn(Optional.empty());

        mockMvc.perform(withJson(put("/api/products/10"), """
                        {"categoryId":99}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid categoryId (not found or not yours)"));

        verify(productRepository, never()).save(any(Product.class));
    }

    // ---------- DELETE /api/products/{id} ----------

    @Test
    void delete_ownProduct_returns204() throws Exception {
        when(productRepository.findById(10)).thenReturn(Optional.of(product(10, "Pen", USER_ID)));

        mockMvc.perform(asUser(delete("/api/products/10")))
                .andExpect(status().isNoContent());

        verify(productRepository).deleteById(10);
    }

    @Test
    void delete_missing_returns404() throws Exception {
        when(productRepository.findById(10)).thenReturn(Optional.empty());

        mockMvc.perform(asUser(delete("/api/products/10")))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Product not found"));

        verify(productRepository, never()).deleteById(anyInt());
    }

    @Test
    void delete_otherUsersProduct_returns403() throws Exception {
        when(productRepository.findById(10)).thenReturn(Optional.of(product(10, "Pen", OTHER_USER_ID)));

        mockMvc.perform(asUser(delete("/api/products/10")))
                .andExpect(status().isForbidden())
                .andExpect(content().string("You can only delete your own products"));

        verify(productRepository, never()).deleteById(anyInt());
    }

    @Test
    void delete_productWithoutOwner_returns403() throws Exception {
        when(productRepository.findById(10)).thenReturn(Optional.of(product(10, "Pen", null)));

        mockMvc.perform(asUser(delete("/api/products/10")))
                .andExpect(status().isForbidden())
                .andExpect(content().string("You can only delete your own products"));

        verify(productRepository, never()).deleteById(anyInt());
    }
}