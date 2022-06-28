package com.hlionlog.api.service;

import com.hlionlog.api.domain.Post;
import com.hlionlog.api.domain.PostEditor;
import com.hlionlog.api.exception.PostNotFound;
import com.hlionlog.api.repository.PostRepository;
import com.hlionlog.api.request.PostCreate;
import com.hlionlog.api.request.PostEdit;
import com.hlionlog.api.request.PostSearch;
import com.hlionlog.api.response.PostResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    public Long write(PostCreate postCreate) {
        Post savedPost = postRepository.save(postCreate.toEntity());
        return savedPost.getId();
    }

    public PostResponse get(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(PostNotFound::new);
        PostResponse response = PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .build();
        return response;

    }

    // 글이 너무 많은 경우 -> 비용이 많이 든다.
    // 글이 1억개 있는 경우 -> DB에서 1억건의 글을 조회하는 경우 DB가 뻣을 가능성
    // DB -> Application 서버로 전달하는 시간, 트랙픽 비용 등이 많이 발생

    public List<PostResponse> getList(PostSearch postSearch) {
        return postRepository.getList(postSearch).stream()
                .map(PostResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void edit(Long id, PostEdit postEdit) {
        Post post = postRepository.findById(id)
                .orElseThrow(PostNotFound::new);

        PostEditor.PostEditorBuilder editorBuilder = post.toEditor();

        PostEditor postEditor = editorBuilder.title(postEdit.getTitle())
                .content(postEdit.getContent())
                .build();

        post.edit(postEditor);
    }

    @Transactional
    public void delete(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(PostNotFound::new);

        postRepository.delete(post);
    }
}
