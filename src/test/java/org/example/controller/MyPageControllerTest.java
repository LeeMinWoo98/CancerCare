package org.example.controller;

import org.example.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MyPageControllerTest {
    @Autowired MockMvc mockMvc;
    @Autowired UserRepository userRepository;

    @Test
    @WithMockUser(username = "dummy")
    void getMyPage_requiresUser() throws Exception {
        // This will likely 500 without a seeded user; presence test only
        mockMvc.perform(get("/mypage"))
                .andExpect(status().is3xxRedirection());
    }
}
