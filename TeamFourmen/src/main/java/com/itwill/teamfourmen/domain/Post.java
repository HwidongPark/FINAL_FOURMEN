package com.itwill.teamfourmen.domain;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Basic;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "post")
@DynamicInsert
public class Post {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long postId;
	
	@ManyToOne
	@JoinColumn(name = "email")
	private Member member;
	
	@Basic(optional = false)
	private String title;
	
	@Basic(optional = false)
	private String content;
	
	private String textContent;
	
	private String category;
	
	@CreatedDate
	private LocalDateTime createdTime;
	
	
	private LocalDateTime modifiedTime;
	
	
	private Long views;
	
	
	private Long likes;
	
	@OneToMany(mappedBy = "post", fetch = FetchType.EAGER)
	@JsonIgnore
	@ToString.Exclude
	private List<Comment> Comment;
	
	
}
