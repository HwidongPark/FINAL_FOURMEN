package com.itwill.teamfourmen.dto.chat;

import java.util.HashMap;
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
	
	private HashMap<Member, Integer> members;	// 해당 방의 유저 목록.. value는 유저가 채팅창 띄운 횟수
	 
	
}
