package com.demo.order;

import com.demo.order.entity.Product;
import com.demo.order.entity.SysUser;
import com.demo.order.repository.OrderRepository;
import com.demo.order.repository.ProductRepository;
import com.demo.order.repository.SysUserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 接口集成测试：鉴权 / RBAC 权限 / 下单主流程 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderApiIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired SysUserRepository userRepository;
    @Autowired ProductRepository productRepository;
    @Autowired OrderRepository orderRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private Long productId;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();

        userRepository.save(user("admin", "admin123", "ROLE_ADMIN"));
        userRepository.save(user("user", "user123", "ROLE_USER"));

        Product p = new Product();
        p.setName("人体工学椅");
        p.setPrice(new BigDecimal("1299.00"));
        p.setStock(10);
        p.setActive(true);
        productId = productRepository.save(p).getId();
    }

    private SysUser user(String name, String raw, String role) {
        SysUser u = new SysUser();
        u.setUsername(name);
        u.setPassword(passwordEncoder.encode(raw));
        u.setRole(role);
        return u;
    }

    /** 登录并取回 JWT */
    private String loginAs(String username, String password) throws Exception {
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn().getResponse().getContentAsString();
        JsonNode node = objectMapper.readTree(body);
        return node.path("data").path("token").asText();
    }

    @Test
    @DisplayName("商品列表：无需登录即可访问")
    void listProducts_public() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    @DisplayName("未登录访问受保护接口：401")
    void protectedEndpoint_withoutCredential_shouldReturn401() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"x\",\"price\":1,\"stock\":1}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("RBAC：普通用户新增商品 -> 403")
    void createProduct_asUser_shouldReturn403() throws Exception {
        String jwt = loginAs("user", "user123");
        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"新品\",\"price\":9.9,\"stock\":5}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    @DisplayName("RBAC：管理员新增商品 -> 成功")
    void createProduct_asAdmin_shouldSucceed() throws Exception {
        String jwt = loginAs("admin", "admin123");
        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"新品键盘\",\"price\":299.00,\"stock\":5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.name").value("新品键盘"));
    }

    @Test
    @DisplayName("下单主流程：扣库存 + 金额正确")
    void createOrder_endToEnd() throws Exception {
        String jwt = loginAs("user", "user123");
        mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"productId\":" + productId + ",\"quantity\":2}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("CREATED"))
                .andExpect(jsonPath("$.data.totalAmount").value(2598.00));

        assertThat(productRepository.findById(productId).orElseThrow().getStock()).isEqualTo(8);
    }

    @Test
    @DisplayName("参数校验：数量为 0 -> 400")
    void createOrder_invalidQuantity_shouldReturn400() throws Exception {
        String jwt = loginAs("user", "user123");
        mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"productId\":" + productId + ",\"quantity\":0}]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }
}
