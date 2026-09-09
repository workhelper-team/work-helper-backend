package com.workhelper.domain.community.dto;

import com.workhelper.domain.community.entity.Post;

import java.time.LocalDateTime;

public record PostResponse(
        Long id,
        Long authorId,
        String authorName,
        String title,
        String content,
        long viewCount,
        LocalDateTime createdAt
) {
    public static PostResponse from(Post post) {
        return new PostResponse(
                post.getId(),
                post.getAuthor().getId(),
                post.getAuthor().getName(),
                post.getTitle(),
                post.getContent(),
                post.getViewCount(),
                post.getCreatedAt()
        );
    }
}
