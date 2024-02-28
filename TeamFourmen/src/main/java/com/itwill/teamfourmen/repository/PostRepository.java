package com.itwill.teamfourmen.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.itwill.teamfourmen.domain.Post;

public interface PostRepository extends JpaRepository<Post, Long> {
	
	List<Post> findAllByCategoryOrderByCreatedTimeDesc(String category);
		
	
}
