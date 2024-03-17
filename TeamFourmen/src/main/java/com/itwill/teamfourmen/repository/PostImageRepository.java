package com.itwill.teamfourmen.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.itwill.teamfourmen.domain.PostImage;

public interface PostImageRepository extends JpaRepository<PostImage, Long> {
	
	List<PostImage> findAllByPostPostId(Long postId);
	
}
