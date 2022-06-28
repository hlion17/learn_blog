package com.hlionlog.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hlionlog.api.domain.Post;
import com.hlionlog.api.repository.PostRepository;
import com.hlionlog.api.request.PostCreate;
import com.hlionlog.api.request.PostEdit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest
class PostControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostRepository postRepository;

    @BeforeEach
    void clear() {
        postRepository.deleteAll();
    }

    @Test
    @DisplayName("/post 요청시 Hello World를 출력한다.")
    void test() throws Exception {
        // given
        PostCreate request = PostCreate.builder()
                .title("title")
                .content("content")
                .build();

        String json = objectMapper.writeValueAsString(request);

        // expected
        mockMvc.perform(get("/posts")
                        .contentType(APPLICATION_JSON)
                        .content(json)
                )
                .andExpectAll(
                        status().isOk()
                        , content().string("{}")
                )
                .andDo(print());
    }

    @Test
    @DisplayName("/posts 요청시 title 값은 필수다")
    void test2() throws Exception {
        // given
        PostCreate request = PostCreate.builder()
                .content("content")
                .build();

        String json = objectMapper.writeValueAsString(request);

        // expected
        mockMvc.perform(get("/posts")
                        .contentType(APPLICATION_JSON)
                        .content(json)
                )
                .andExpectAll(
                        status().isBadRequest()
                        , jsonPath("$.code").value("400")
                        , jsonPath("$.message").value("잘못된 요청입니다.")
                        , jsonPath("$.validation.title").value("타이틀을 입력해주세요.")
                )
                .andDo(print());
    }

    @Test
    @DisplayName("/posts POST 요청시 게시글 저장")
    void test3() throws Exception {
        // given
        PostCreate request = PostCreate.builder()
                .title("title")
                .content("content")
                .build();

        String json = objectMapper.writeValueAsString(request);

        // expected
        mockMvc.perform(post("/posts")
                        .contentType(APPLICATION_JSON)
                        .content(json)
                )
                .andExpectAll(
                        status().isOk()
                )
                .andDo(print());

        assertEquals(1L, postRepository.count());
        List<Post> all = postRepository.findAll();
        assertEquals(request.getTitle(), all.get(0).getTitle());
        assertEquals(request.getContent(), all.get(0).getContent());
    }

    @Test
    @DisplayName("글 1개 조회")
    void test4() throws Exception {
        // given
        Post post = Post.builder()
                .title("title")
                .content("content")
                .build();
        postRepository.save(post);

        // expected
        mockMvc.perform(get("/posts/{postId}", post.getId())
                .contentType(APPLICATION_JSON)
        ).andExpectAll(
                status().isOk()
                , jsonPath("$.id").value(post.getId())
                , jsonPath("$.title").value(post.getTitle())
                , jsonPath("$.content").value(post.getContent())
        ).andDo(print());
    }

    @Test
    @DisplayName("글  여러개 조회")
    void test5() throws Exception {
        // given
        List<Post> requestPosts = IntStream.range(0, 20)
                .mapToObj(i -> Post.builder()
                        .title("title " + i)
                        .content("content " + i)
                        .build()
                ).collect(Collectors.toList());
        postRepository.saveAll(requestPosts);

        // expected
        mockMvc.perform(get("/posts/all?page=1&size=10")
                .contentType(APPLICATION_JSON)
        ).andExpectAll(
                status().isOk(),
                jsonPath("$.length()", is(10)),
                jsonPath("$[0].title").value("title 19"),
                jsonPath("$[0].content").value("content 19")
        ).andDo(print());
    }

    @Test
    @DisplayName("페이지를 0으로 요청하면 첫 페이지를 가져온다.")
    void test6() throws Exception {
        // given
        List<Post> requestPosts = IntStream.range(0, 20)
                .mapToObj(i -> Post.builder()
                        .title("title " + i)
                        .content("content " + i)
                        .build()
                ).collect(Collectors.toList());
        postRepository.saveAll(requestPosts);

        // expected
        mockMvc.perform(get("/posts/all?page=0&size=10")
                .contentType(APPLICATION_JSON)
        ).andExpectAll(
                status().isOk(),
                jsonPath("$.length()", is(10)),
                jsonPath("$[0].title").value("title 19"),
                jsonPath("$[0].content").value("content 19")
        ).andDo(print());
    }

    @Test
    @DisplayName("글 제목 수정")
    void test7() throws Exception {
        // given
        Post post = Post.builder()
                .title("title")
                .content("content")
                .build();
        postRepository.save(post);

        PostEdit postEdit = PostEdit.builder()
                .title("edited title")
                .content("content")
                .build();

        // expected
        mockMvc.perform(patch("/posts/{postId}", post.getId())
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(postEdit))
        ).andExpectAll(
                status().isOk()
        ).andDo(print());

    }

    @Test
    @DisplayName("게시글 삭제")
    void test8() throws Exception {
        // given
        Post post = Post.builder()
                .title("title")
                .content("content")
                .build();
        postRepository.save(post);

        // expected
        mockMvc.perform(delete("/posts/{postId}", post.getId())
                .contentType(APPLICATION_JSON)
        ).andExpectAll(
                status().isOk()
        ).andDo(print());
    }

    @Test
    @DisplayName("존재하지 않는 게시글 조회")
    void test9() throws Exception {
        // given
        Post post = Post.builder()
                .title("title")
                .content("content")
                .build();
        postRepository.save(post);

        // expected
        mockMvc.perform(get("/posts/{postId}", post.getId() + 1L)
                .contentType(APPLICATION_JSON)
        ).andExpectAll(
                status().isNotFound()
        ).andDo(print());
    }

    @Test
    @DisplayName("존재하지 않는 게시글 수정")
    void test10() throws Exception {
        // given
        PostEdit postEdit = PostEdit.builder()
                .title("title")
                .content("content")
                .build();

        // expected
        mockMvc.perform(patch("/posts/{postId}", 1L)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(postEdit))
        ).andExpectAll(
                status().isNotFound()
        ).andDo(print());
    }

    @Test
    @DisplayName("존재하지 않는 게시글 삭제")
    void test11() throws Exception {
        // given
        PostEdit postEdit = PostEdit.builder()
                .title("title")
                .content("content")
                .build();

        // expected
        mockMvc.perform(delete("/posts/{postId}", 1L)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(postEdit))
        ).andExpectAll(
                status().isNotFound()
        ).andDo(print());
    }

    @Test
    @DisplayName("게시글 작성시 제목에 '바보'는 포함될 수 없다.")
    void test12() throws Exception {
        // given
        PostCreate requestPost = PostCreate.builder()
                .title("나는 바보 입니다.")
                .content("content")
                .build();

        String json = objectMapper.writeValueAsString(requestPost);

        // expected
        mockMvc.perform(post("/posts")
                .contentType(APPLICATION_JSON)
                .content(json)
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(print());

    }

}