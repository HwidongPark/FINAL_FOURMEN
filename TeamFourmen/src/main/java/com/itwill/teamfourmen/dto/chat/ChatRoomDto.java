package com.itwill.teamfourmen.dto.chat;

import java.util.Set;

import com.itwill.teamfourmen.domain.Member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomDto {
	
	private String category;	// movie 또는 tvShow
	
	private int roomId;		// movieId 또는 tvShowId
	
	private Set<Member> members;	// 해당 방의 유저 목록
	
	private String type; // ROOM으로 넘겨주기
	
}
