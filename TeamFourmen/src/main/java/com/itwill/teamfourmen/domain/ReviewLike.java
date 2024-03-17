package com.itwill.teamfourmen.domain;

import jakarta.persistence.Basic;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "review_likes")
@SequenceGenerator(name = "review_likes_seq", sequenceName = "review_likes_seq", allocationSize = 1)
public class ReviewLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewLikeId;

    @Basic(optional = false)
    @ManyToOne
    @JoinColumn(name = "review_id")
    private Review review;

    @Basic(optional = false)
    @ManyToOne
    @JoinColumn(name = "email")
    private Member member;

}

