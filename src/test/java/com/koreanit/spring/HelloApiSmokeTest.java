package com.koreanit.spring;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class HelloApiSmokeTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void hello_users_api_runs_with_real_db() throws Exception {

    mockMvc.perform(get("/hello/users").param("limit", "10"));
  }

  @Test
  void hello_posts_api_runs_with_real_db() throws Exception {

    mockMvc.perform(get("/hello/posts").param("limit", "10"));
  }

}
