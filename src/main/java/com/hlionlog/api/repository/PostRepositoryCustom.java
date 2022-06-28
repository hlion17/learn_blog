package com.hlionlog.api.repository;

import com.hlionlog.api.domain.Post;
import com.hlionlog.api.request.PostSearch;

import java.util.List;

public interface PostRepositoryCustom {

    List<Post> getList(PostSearch postSearch);
}
