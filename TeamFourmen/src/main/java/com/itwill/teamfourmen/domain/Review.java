package com.itwill.teamfourmen.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.springframework.data.annotation.CreatedDate;
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

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@EntityListeners(AuditingEntityListener.class)
@Table(name = "reviews")
public class Review {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long reviewId;
	
	@Basic(optional = false)
	private Integer tmdbId;
	
	@Basic(optional = false)
	private String category;
	
	@Basic(optional = false)
	@ManyToOne
	@JoinColumn(name = "email")
	private Member member;
	
	@Basic(optional = false)
	private String content;
	
	private String isSpoiler;
	
	private Double rating;
	
	@CreatedDate
	private LocalDateTime createdDate;

	public LocalDate changeDateType(LocalDateTime time){
		return createdDate.toLocalDate();
	}
	

	@OneToMany(mappedBy = "review", fetch = FetchType.EAGER)
	@JsonIgnore
	@ToString.Exclude
	private List<ReviewComments> reviewComments;
	
}
