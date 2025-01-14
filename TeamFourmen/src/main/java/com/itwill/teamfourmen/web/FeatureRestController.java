package com.itwill.teamfourmen.web;

import com.itwill.teamfourmen.dto.comment.ReviewLikeDTO;
import com.itwill.teamfourmen.dto.playlist.PlaylistDto;
import com.itwill.teamfourmen.dto.playlist.PlaylistItemDto;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.itwill.teamfourmen.domain.NicknameInterceptor;
import com.itwill.teamfourmen.domain.Playlist;
import com.itwill.teamfourmen.domain.PlaylistItem;
import com.itwill.teamfourmen.domain.Review;
import com.itwill.teamfourmen.domain.ReviewLike;
import com.itwill.teamfourmen.domain.TmdbLike;
import com.itwill.teamfourmen.service.FeatureService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/feature")
public class FeatureRestController {

	private final NicknameInterceptor nicknameInterceptor;
	private final FeatureService featureService;
	
	
	/**
	 * 리뷰 타입의 객체를 아규먼트로 받아 리뷰를 추가하는 컨트롤러 메서드
	 * @param review
	 */
	@PostMapping("/review/post")
	public void postReview(@RequestBody Review review) {

		log.info("postReview(reviewdto={})", review);
		featureService.postReview(review);

	}

	
	/**
	 * 영화, tv쇼 좋아요 추가하는 메서드
	 * @param tmdbLike
	 */
	@Transactional
	@PostMapping("/like/add")
	public void addLike(@RequestBody TmdbLike tmdbLike) {

		log.info("addLike(tmdbLike = {})", tmdbLike);

		nicknameInterceptor.getMember().addRole(null);

		featureService.addLike(tmdbLike);

	}

	@Transactional
	@PostMapping("/like/delete")
	public void deleteLike(@RequestBody TmdbLike tmdbLike) {

		log.info("deleteLike(tmdbLike={})", tmdbLike);

		featureService.deleteLike(tmdbLike);

	}

//	@Transactional
	@PostMapping("/review/like/add")
	public void addReviewLike(@RequestBody ReviewLikeDTO reviewLikeDto) {
		log.info("addReviewLike(reviewLike={})", reviewLikeDto);
		featureService.controllReviewLike(reviewLikeDto);
	}

	@DeleteMapping("/review")
	public void deleteReview(@RequestParam (name = "reviewId") Long reviewId, @RequestParam (name = "email") String email){
		log.info("DELETE REVIEW REVIEW_Id = {} , REVIEWER_EMAIL = {}", reviewId, email);
		featureService.deleteReview(reviewId,email);
  }

	/**
	 * 특정 tmdbId와 category가 "person"인 경우의 좋아요 개수를 조회하는 API 엔드포인트.
	 * @param tmdbId 조회하고자 하는 Tmdb ID
	 * @return 좋아요 개수
	 */
	@GetMapping("/like/count")
	public ResponseEntity<Integer> getLikesCountForPerson(@RequestParam int tmdbId) {
		int likesCount = featureService.getLikesCountFOrPersonCategory(tmdbId);
		log.info("getLikesCountForPerson: tmdbId={}, likesCount={}", tmdbId, likesCount);
		return ResponseEntity.ok(likesCount);
	}
	
	/**
	 * 해당 유저의 모든 플레이리스트를 가져오는 컨트롤러 메서드
	 * @param email
	 * @return
	 */
	@GetMapping("/playlist")
	public ResponseEntity<List<PlaylistDto>> getPlaylist(@RequestParam(name="email") String email) {
		log.info("getPlaylist(email={})", email);
		
		List<PlaylistDto> playlist = featureService.getPlaylist(email);
		
		return ResponseEntity.ok(playlist);
	}
		
	
	/**
	 * playlist타입의 객체를 아규먼트로 받아 데이터베이스에 추가시키는 컨트롤러 메서드
	 * @param playlist
	 */
	@PostMapping("/playlist/create")
	public void createPlaylist(@RequestBody Playlist playlist) {
		log.info("createPlaylist(playlist={})", playlist);
		
		featureService.createPlaylist(playlist);
	}
	
	/**
	 * 플레이리스트 id를 아규먼트로 받아 해당 playlist에 있는 모든 playlist items들을 반환
	 * @param playlistId
	 * @return
	 * @throws JsonProcessingException 
	 * @throws JsonMappingException 
	 */
	@GetMapping("/playlist/get-items")
	public ResponseEntity<List<PlaylistItemDto>> getItemsInPlaylist(@RequestParam(name="playlistId") Long playlistId) throws JsonMappingException, JsonProcessingException {
		log.info("getItemsInPlaylist(playlistId={})", playlistId);
		
		List<PlaylistItemDto> playlistItemsList = featureService.getItemsInPlaylist(playlistId);
		log.info("playlistItemList={}", playlistItemsList);
		
		return ResponseEntity.ok(playlistItemsList);
	}
	
	
	/**
	 * playlistItem타입의 객체를 아규먼트로 받아 해당 playlist item을 playlistItem의 playlistId필드에 해당하는 플레이리스트에 추가
	 * @param playlistItem
	 */
	@PostMapping("/playlist/add")
	public void addItemToPlaylist(@RequestBody PlaylistItem playlistItem) {
		log.info("addItemToPlaylist={}", playlistItem);
		
		PlaylistItem savedPlaylistItem = featureService.addItemToPlaylist(playlistItem);
		log.info("savedPlaylistItem={}", savedPlaylistItem);
		
	}
	
}

