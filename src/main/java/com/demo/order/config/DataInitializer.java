package com.demo.order.config;

import com.demo.order.entity.Product;
import com.demo.order.entity.SysUser;
import com.demo.order.repository.ProductRepository;
import com.demo.order.repository.SysUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** demo profile 初始化演示数据 */
@Slf4j
@Component
@Profile("demo")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final SysUserRepository userRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.findByUsername("admin").isEmpty()) {
            userRepository.save(user("admin", "admin123", "ROLE_ADMIN"));
            userRepository.save(user("user", "user123", "ROLE_USER"));
            log.info("demo users created -> admin/admin123 (ADMIN), user/user123 (USER)");
        }
        if (productRepository.count() == 0) {
            Object[][] seed = {
                    {"机械键盘 K8", "399.00", 50},
                    {"人体工学椅", "1299.00", 20},
                    {"27寸显示器", "1599.00", 15},
                    {"无线鼠标", "129.00", 100},
                    {"USB-C 扩展坞", "259.00", 30},
            };
            for (Object[] s : seed) {
                Product p = new Product();
                p.setName((String) s[0]);
                p.setPrice(new BigDecimal((String) s[1]));
                p.setStock((Integer) s[2]);
                p.setActive(true);
                productRepository.save(p);
            }
            log.info("demo products seeded -> {}", productRepository.count());
        }
    }

    private SysUser user(String name, String rawPassword, String role) {
        SysUser u = new SysUser();
        u.setUsername(name);
        u.setPassword(passwordEncoder.encode(rawPassword));
        u.setRole(role);
        return u;
    }
}
