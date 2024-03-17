package com.itwill.teamfourmen.dto.board;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.itwill.teamfourmen.domain.Comment;
import com.itwill.teamfourmen.domain.CommentLike;
import com.itwill.teamfourmen.domain.Member;
import com.itwill.teamfourmen.domain.Post;

import jakarta.persistence.Basic;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommentDto {
	
	private Long commentId;
	
	private Post post;
	
	private Member member;

	private String content;
	
	private LocalDateTime createdTime;
	
	// 현재시간과 createdTime이 24시간 이내로 차이날 경우, 시간차를 분으로 나타내는 변수
	private Long timeDifferenceInMinute;
	
	private Long likes;
	
	private Long replyTo;
	
	private Comment commentReplied;
	
	private String isDeleted;
	
	private List<CommentLike> commentLikesList = new ArrayList<>();
	
	private List<CommentDto> repliesList = new ArrayList<>();
	
	
	/**
	 * Comment타입의 객체를 CommentDto로 변환시키는 메서드
	 * 단, commentDto의 replyTo필드의 타입은 Comment이며, Comment의 replyTo필드의 타입은 Long이기 때문에
	 * 따로 매핑시켜줘야 함
	 * @param comment
	 * @return
	 */
	public static CommentDto fromEntity(Comment comment) {
		
		return CommentDto.builder()
					.commentId(comment.getCommentId())
					.post(comment.getPost())
					.member(comment.getMember())
					.content(comment.getContent())
					.createdTime(comment.getCreatedTime())
					.likes(comment.getLikes())
					.replyTo(comment.getReplyTo())
					.isDeleted(comment.getIsDeleted())
					.commentLikesList(new ArrayList<CommentLike>())
					.repliesList(new ArrayList<CommentDto>())
					.build();
	}
	
	
	public static final Comparator<CommentDto> ORDER_BY_COMMENT_ID_ASC = new Comparator<CommentDto>() {

		@Override
		public int compare(CommentDto commentDto1, CommentDto commentDto2) {
			
			return Long.compare(commentDto1.getCommentId(), commentDto2.getCommentId());
		}
				
	};
	
}
