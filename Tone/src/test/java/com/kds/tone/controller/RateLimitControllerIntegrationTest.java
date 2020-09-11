package com.kds.tone.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles(profiles = "integration_test")
public class RateLimitControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void allow_should_respond_with_200_ok_when_number_of_requests_are_lt_max_allowed() throws Exception {
        MvcResult result =
                mockMvc.perform(MockMvcRequestBuilders
                        .get("/allowed")
                        .header("userId", "1234"))
                        // should have value true at $.allowed
                        .andExpect(jsonPath("$.allowed", is(true)))
                        .andReturn();
        int status = result.getResponse().getStatus();
        // status should be 200 OK
        assertThat(status, is(200));
    }

    @Test
    public void allow_should_respond_with_429_too_many_requests_when_number_of_requests_are_gt_max_allowed_within_time_window() throws Exception {

        for (int i = 0; i < 3; i++) {

            mockMvc.perform(MockMvcRequestBuilders
                    .get("/allowed")
                    .header("userId", "4567"))
                    .andExpect(jsonPath("$.allowed", is(true)))
                    .andReturn();
        }

        // invoke again

        mockMvc.perform(MockMvcRequestBuilders
                .get("/allowed")
                .header("userId", "4567"))
                // should have value true at $.allowed
                .andExpect(jsonPath("$.allowed", is(false)))
                .andExpect(jsonPath("$.message", matchesPattern("Rate limit exceeded\\. Try again in [0-9]+ seconds")))
                .andReturn();
    }
}
