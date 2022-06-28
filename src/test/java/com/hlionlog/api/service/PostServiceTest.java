package com.hlionlog.api.service;

import com.hlionlog.api.domain.Post;
import com.hlionlog.api.exception.PostNotFound;
import com.hlionlog.api.repository.PostRepository;
import com.hlionlog.api.request.PostCreate;
import com.hlionlog.api.request.PostEdit;
import com.hlionlog.api.request.PostSearch;
import com.hlionlog.api.response.PostResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PostServiceTest {

    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    @BeforeEach
    void clear() {
        postRepository.deleteAll();
    }

    @Test
    @DisplayName("글 작성")
    void test1() {
        // given
        PostCreate postCreate = PostCreate.builder()
                .title("title")
                .content("content")
                .build();

        // when
        postService.write(postCreate);

        // then
        assertEquals(1L, postRepository.count());
        List<Post> all = postRepository.findAll();
        assertEquals(postCreate.getTitle(), all.get(0).getTitle());
        assertEquals(postCreate.getContent(), all.get(0).getContent());
    }

    @Test
    @DisplayName("글 한 개 조회")
    void test2() {
        // given
        Post requestPost = Post.builder()
                .title("title")
                .content("content")
                .build();
        postRepository.save(requestPost);

        // when
        PostResponse response = postService.get(requestPost.getId());

        // then
        assertNotNull(response);
        assertEquals(1L, postRepository.count());
        assertEquals("title", response.getTitle());
        assertEquals("content", response.getContent());
    }

    @Test
    @DisplayName("글 1페이지 조회")
    void test5() {
        // given
        List<Post> requestPosts = IntStream.range(0, 20)
                        .mapToObj(i -> Post.builder()
                                    .title("title " + i)
                                    .content("content " + i)
                                    .build()
                        ).collect(Collectors.toList());
        postRepository.saveAll(requestPosts);

        PostSearch postSearch = PostSearch.builder()
                .page(1)
                .build();

        // sql -> select, limit, offset

        // when
        List<PostResponse> posts = postService.getList(postSearch);

        // then
        assertEquals(10L, posts.size());
        assertEquals("title 19", posts.get(0).getTitle());
    }

    @Test
    @DisplayName("글 제목 수정")
    void test6() {
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

        // when
        postService.edit(post.getId(), postEdit);

        // then
        Post changedPost = postRepository.findById(post.getId())
                .orElseThrow(() -> new RuntimeException("글이 존재하지 않습니다. id=" + post.getId()));
        assertEquals("edited title", changedPost.getTitle());
        assertEquals("content", changedPost.getContent());
    }

    @Test
    @DisplayName("글 내용 수정")
    void test7() {
        // given
        Post post = Post.builder()
                .title("title")
                .content("content")
                .build();

        postRepository.save(post);

        PostEdit postEdit = PostEdit.builder()
                .title("title")
                .content("edited content")
                .build();

        // when
        postService.edit(post.getId(), postEdit);

        // then
        Post changedPost = postRepository.findById(post.getId())
                .orElseThrow(() -> new RuntimeException("글이 존재하지 않습니다. id=" + post.getId()));
        assertEquals("title", changedPost.getTitle());
        assertEquals("edited content", changedPost.getContent());
    }

    @Test
    @DisplayName("글 삭제")
    void test8() {
        // given
        Post post = Post.builder()
                .title("title")
                .content("content")
                .build();

        postRepository.save(post);

        // when
        postService.delete(post.getId());

        // then
        assertEquals(0, postRepository.count());
    }

    @Test
    @DisplayName("글 한 개 조회 실패")
    void test9() {
        // given
        Post post = Post.builder()
                .title("title")
                .content("content")
                .build();
        postRepository.save(post);

        // expected
        PostNotFound e = assertThrows(PostNotFound.class, () -> {
            postService.get(post.getId() + 1L);
        });

        assertEquals("존재하지 않는 글입니다.", e.getMessage());
    }
}