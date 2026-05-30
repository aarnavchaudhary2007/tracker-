package com.trackit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trackit.dto.SigninRequest;
import com.trackit.dto.SignupRequest;
import com.trackit.model.User;
import com.trackit.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void testAuthenticationLifecycleAndInterceptor() throws Exception {
        // 1. Verify Access to Secured /api/applications is Blocked initially
        mockMvc.perform(get("/api/applications"))
                .andExpect(status().isUnauthorized());

        // 2. Successful Signup
        SignupRequest signup = new SignupRequest("alexsecurity", "securepwd123");
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signup)))
                .andExpect(status().isCreated());

        // 3. Prevent Duplicate Username Signup
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signup)))
                .andExpect(status().isBadRequest());

        // 4. Failed Signin with Invalid Password
        SigninRequest invalidSignin = new SigninRequest("alexsecurity", "wrongpwd");
        mockMvc.perform(post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidSignin)))
                .andExpect(status().isUnauthorized());

        // 5. Successful Signin
        SigninRequest validSignin = new SigninRequest("alexsecurity", "securepwd123");
        MvcResult signinResult = mockMvc.perform(post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validSignin)))
                .andExpect(status().isOk())
                .andReturn();

        // Extract token
        String responseContent = signinResult.getResponse().getContentAsString();
        Map<?, ?> responseMap = objectMapper.readValue(responseContent, Map.class);
        String token = (String) responseMap.get("token");
        assertNotNull(token);
        assertFalse(token.isBlank());

        // 6. Access Secured Route with Valid JWT Bearer Token
        mockMvc.perform(get("/api/applications")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // 7. Successful Signout
        mockMvc.perform(post("/api/auth/signout")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // 8. Access Blocked post-Signout (Blacklisted Token check)
        mockMvc.perform(get("/api/applications")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }
}
