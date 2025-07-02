package com.autoalert.autoalert.Service;

import com.autoalert.autoalert.Model.User;
import com.autoalert.autoalert.Repository.UserRepo;
import com.autoalert.autoalert.Service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepo userRepo;

    @Test
    public void testCreateUser_NoInput() {
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(null));
    }

    @Test
    public void testCreateUser_WrongInput_NullEmailAndPassword() {
        User user = new User();
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(user));
    }

    @Test
    public void testCreateUser_OneValidUser() {
        User user = new User();
        user.setEmail("valid1@test.com");
        user.setPassword("secure123");

        userService.createUser(user);

        User saved = userRepo.findByEmail("valid1@test.com").orElse(null);
        assert saved != null;
        assertNotNull(saved);
        assertFalse(saved.getPassword().isEmpty());
    }

    @Test
    public void testCreateUser_TwoValidUsers() {
        User user1 = new User();
        user1.setEmail("first@test.com");
        user1.setPassword("first123");

        User user2 = new User();
        user2.setEmail("second@test.com");
        user2.setPassword("second123");

        userService.createUser(user1);
        userService.createUser(user2);

        List<User> users = userRepo.findAll();
        assertEquals(2, users.size());
    }

    @Test
    public void testUpdateUser_NoInput() {
        assertThrows(InvalidDataAccessApiUsageException.class, () -> userService.updateUser(null));
    }

    @Test
    public void testUpdateUser_OneValid() {
        User user = new User();
        user.setEmail("valid@test.com");
        user.setPassword("123");
        userRepo.save(user);

        user.setEmail("updated@test.com");
        userService.updateUser(user);

        assertEquals("updated@test.com", userRepo.findById(user.getId()).get().getEmail());
    }

    @Test
    public void testUpdateUser_TwoUsers() {
        User u1 = new User();
        u1.setEmail("u1@test.com");
        u1.setPassword("1");
        User u2 = new User();
        u2.setEmail("u2@test.com");
        u2.setPassword("2");
        userRepo.saveAll(List.of(u1, u2));

        u1.setEmail("new1@test.com");
        u2.setEmail("new2@test.com");

        userService.updateUser(u1);
        userService.updateUser(u2);

        assertEquals("new1@test.com", userRepo.findById(u1.getId()).get().getEmail());
        assertEquals("new2@test.com", userRepo.findById(u2.getId()).get().getEmail());
    }

    @Test
    public void testUpdateUser_WrongInput_InvalidId() {
        User fake = new User();
        fake.setId(999999);
        fake.setEmail("fail@test.com");
        assertThrows(Exception.class, () -> userService.updateUser(fake));
    }

    @Test
    public void testGenerateVerificationCode_NoInput() {
        String code = userService.generateVerificationCode();
        Assertions.assertNotNull(code);
        assertEquals(6, code.length());
    }

    @Test
    public void testGenerateVerificationCode_OneCall() {
        String code = userService.generateVerificationCode();
        assertTrue(code.matches("\\d{6}"));
    }

    @Test
    public void testGenerateVerificationCode_ManyCalls() {
        String code1 = userService.generateVerificationCode();
        String code2 = userService.generateVerificationCode();
        assertNotEquals(code1, code2);
    }

    @Test
    public void testGenerateVerificationCode_WrongInput_NotApplicable() {

        String code = userService.generateVerificationCode();
        assertDoesNotThrow(() -> Integer.parseInt(code));
    }

    @Test
    public void testIsUserPremium_NoInput_NullEmail() {
        assertThrows(RuntimeException.class, () -> userService.isUserPremium(null));
    }

    @Test
    public void testIsUserPremium_WrongInput_EmailNotFound() {
        assertThrows(RuntimeException.class, () -> userService.isUserPremium("notfound@test.com"));
    }

    @Test
    public void testIsUserPremium_OnePremiumUser() {
        User user = new User();
        user.setEmail("premium@test.com");
        user.setPassword("p");
        user.setPremium(true);
        userRepo.save(user);

        boolean result = userService.isUserPremium("premium@test.com");
        assertTrue(result);
    }

    @Test
    public void testIsUserPremium_TwoUsers() {
        User u1 = new User();
        u1.setEmail("user1@test.com");
        u1.setPassword("123");
        u1.setPremium(false);

        User u2 = new User();
        u2.setEmail("user2@test.com");
        u2.setPassword("456");
        u2.setPremium(true);

        userRepo.saveAll(List.of(u1, u2));

        boolean result1 = userService.isUserPremium("user1@test.com");
        boolean result2 = userService.isUserPremium("user2@test.com");

        assertFalse(result1);
        assertTrue(result2);
    }


}