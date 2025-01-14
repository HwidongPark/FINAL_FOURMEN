package com.itwill.teamfourmen.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.itwill.teamfourmen.domain.*;
import com.itwill.teamfourmen.dto.comment.ReviewLikeDTO;
import com.itwill.teamfourmen.dto.movie.MovieDetailsDto;
import com.itwill.teamfourmen.dto.playlist.PlaylistDto;
import com.itwill.teamfourmen.dto.playlist.PlaylistItemDto;
import com.itwill.teamfourmen.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.itwill.teamfourmen.domain.Member;
import com.itwill.teamfourmen.domain.Review;
import com.itwill.teamfourmen.domain.ReviewLike;
import com.itwill.teamfourmen.domain.TmdbLike;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class FeatureService {
	
	private final MovieApiUtil movieApiUtil;
	private final TvShowApiUtil tvShowApiUtil;
	private final ReviewDao reviewDao;
	private final TmdbLikeDao tmdbLikeDao;
	private final ReviewLikeRepository reviewLikeDao;
	private final ReviewCommentsRepository commentDao;
	private final PlaylistRepository playlistDao;
	private final PlaylistItemRepository playlistItemDao;
	private final PlaylistLikeRepository playlistLikeDao;	
	private final MemberRepository memberDao;
	private final PostRepository postRepository;
	
	public void postReview(Review review) {

		log.info("postReview(reviewDto={})", review);

		review = reviewDao.save(review);

	}
  
  	public void addReviewLike(ReviewLike reviewLike) {
		
		log.info("addReviewLike(reviewLike={})", reviewLike);
		
		reviewLike = reviewLikeDao.save(reviewLike);
		
		log.info("저장 후 reviewLike={}", reviewLike);
	}

	public List<Review> getReviews(String category, int tmdbId) {

		log.info("getReviews(category={}, id={})", category, tmdbId);

		List<Review> tmdbReviewList = reviewDao.findByCategoryAndTmdbId(category, tmdbId);

		return tmdbReviewList;
	}

	public Integer getNumOfReviewComment(Long reivewId){
		log.info("GET NUM OF REVIEW COMMENT REVIEW ID  = {} ",reivewId);

		Review review = Review.builder().reviewId(reivewId).build();

		List<ReviewComments> listComment = commentDao.findByReview(review);

		int numOfComment = 0;

		if(listComment != null) {
			numOfComment = listComment.size();
		} else {
			 numOfComment = 0;
		}

		log.info("COMMENT NUM = {}" , numOfComment);

		return numOfComment;
	}

	public Review getMyReviewInTmdbWork(String email, String category, int tmdbId) {

		log.info("getMyReviewInTmdbWork(email={}, category={}, tmdbId={}", email, category, tmdbId);

		Optional<Review> myTmdbReviewOptional = reviewDao.findByMemberEmailAndCategoryAndTmdbId(email, category, tmdbId);
		Review myTmdbReview = myTmdbReviewOptional.orElse(null);

		return myTmdbReview;
	}


	public TmdbLike didLikeTmdb(Member member, String category, int tmdbId) {

		log.info("didLikeTmdb(member={})", member);

		Optional<TmdbLike> tmdbLikeOptional = tmdbLikeDao.findByMemberEmailAndCategoryAndTmdbId(member.getEmail(), category, tmdbId);

		TmdbLike tmdbLike = tmdbLikeOptional.orElse(null);
		log.info("tmdbLike={}", tmdbLike);


		return tmdbLike;
	}


	public void addLike(TmdbLike tmdbLike) {

		log.info("addLike(tmdbLike={})", tmdbLike);

		tmdbLike = tmdbLikeDao.save(tmdbLike);

		log.info("저장된 tmdbLike={}", tmdbLike);

	}

	public void deleteLike(TmdbLike tmdbLike) {

		log.info("deleteLike(tmdbLike={})", tmdbLike);
		tmdbLikeDao.deleteByMemberEmailAndCategoryAndTmdbId(tmdbLike.getMember().getEmail(), tmdbLike.getCategory(), tmdbLike.getTmdbId());

	}

	public Long getNumOfReviewLike(Long reviewId){
		log.info("GET NUM OF REVIEW LIKE = {}" ,reviewId);

		Review review = reviewDao.findByReviewId(reviewId);

		Long countReviewLike = reviewLikeDao.countByReview(review);

		if(countReviewLike == 0L){
		   return 0L;
		} else {
			return countReviewLike;
		}
	}


	public void controllReviewLike(ReviewLikeDTO reviewLikeDTO) {
		log.info("addReviewLike(reviewLike={})", reviewLikeDTO);

		Review review = Review.builder()
				.reviewId(reviewLikeDTO.getReviewId())
				.build();

		Member member = Member.builder()
				.email(reviewLikeDTO.getEmail())
				.build();

		ReviewLike isLikedReview = reviewLikeDao.findByReviewAndMember(review , member);

		log.info("IS REVIEW LIKED? = {}", isLikedReview);

		boolean didReviewLike = didReviewLike(review, member);

		if(!didReviewLike) {
			ReviewLike entity = ReviewLike.builder()
					.review(review)
					.member(member)
					.build();

			reviewLikeDao.save(entity);
		} else {


			ReviewLike entity = reviewLikeDao.findByReviewAndMember(review, member);

			if(entity != null) {
				reviewLikeDao.delete(entity);
			}
		}
	}

	public boolean didReviewLike(Review review, Member member) {
		log.info("DID REVIEW LIKED ?? REVIEW = {}, MEMBER = {}", review, member);

		Review targetReview = reviewDao.findByReviewId(review.getReviewId());

		log.info("TARGET REVIEW REVIEW ID = {}, REVIEW CONTENT = {}", targetReview.getReviewId(), targetReview.getContent());

		ReviewLike reviewLike = reviewLikeDao.findByReviewAndMember(targetReview, member);

		log.info("target Review LiKE = {}", reviewLike);

		if (reviewLike != null) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * MyPage에서 내가 작성한 모든 리뷰를 가져오기 위함
	 * - 작성자 권오중
	 * @param email
	 * @return Review 리스트
	 */
	public List<Review> getAllMyReview (String email) {
		log.info("GET ALL MY REVIEW E-MAIL = {}", email);

		Member member = Member.builder().email(email).build();

		List<Review> myAllReivew = reviewDao.findByMemberEmail(member.getEmail());

		for(Review review : myAllReivew) {
			log.info("My review = {}", review);
		}

		return myAllReivew;
	}
	
	public List<Review> getAllMyReview (Long memberId) {
		log.info("GET ALL MY REVIEW MEMBERID = {}", memberId);
		
		List<Review> myAllReview = reviewDao.findAllByMemberMemberId(memberId);
		
//		for(Review review : myAllReview) {
//			log.info("My review = {}", review);
//		}
		
		return myAllReview;
	}

	public List<TmdbLike> getLikedList (Member member, String category){
		log.info("GET LIKED LIST OF CATEGORY = {}, MemberID = {}", category, member.getMemberId());
		Member theMember = memberDao.findByMemberId(member.getMemberId()).orElse(null);
		
		List<TmdbLike> likedList = tmdbLikeDao.findByMemberEmailAndCategory(theMember.getEmail(), category);
		
		return likedList;
	}

	public void deleteReview (Long reviewId , String email) {
		log.info("DELETE REVIEW SERVICE REVIEW_ID = {} , EMAIl = {}", reviewId, email);

		Review review = reviewDao.findByReviewId(reviewId);

		Member member = Member.builder().email(email).build();

		if (review.getMember().getEmail().equalsIgnoreCase(member.getEmail())) {
			reviewDao.delete(review);
		}
	}
	/**
	 * category가 pesron인 경우에 tmdbId의 개수를 가져와서 특정 인물의 좋아요 개수를 조회.
	 * @param tmdbId
	 * tmdbId: 조회하고자 하는 Tmdb ID(인물의 id)
	 * @return 좋아요 개수
	 */
	public int getLikesCountFOrPersonCategory(int tmdbId) {
		String category = "person";
		int likesCount = tmdbLikeDao.countByTmdbIdAndCategory(tmdbId, category);

		log.info("특정 인물의 좋아요 개수: tmdbID={}, likesCount={}", tmdbId, likesCount);

		return likesCount;

	}
	
	/**
	 * 유저의 아이디(email)을 아규먼트로 받아, 해당 유저의 플레이리스트들을 리턴함.
	 * 플레이리스트가 없을 경우 빈 리스트를 리턴
	 * @param email
	 * @return
	 */
	public List<PlaylistDto> getPlaylist(String email) {
		log.info("getPlaylist(email={})", email);
		
		List<Playlist> playlist = playlistDao.findAllByMemberEmailOrderByPlaylistId(email);
		
		List<PlaylistDto> playlistDtoList = playlist.stream().map((eachPlaylist) -> PlaylistDto.fromEntity(eachPlaylist)).toList();
		
		playlistDtoList.forEach((dto) -> {
			// getItemsInPlaylist 메서드 사용해서 해당 플레이리스트에 있는 아이템들을 매핑
			try {
				dto.setPlaylistItemDtoList(getItemsInPlaylist(dto.getPlaylistId()));
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			List<PlaylistLike> playlistLikeList = playlistLikeDao.findAllByPlaylistPlaylistId(dto.getPlaylistId());
			dto.setPlaylistLikeList(playlistLikeList);
		});
		
		// log.info("playlistDtoList = {}", playlistDtoList);
		
		return playlistDtoList;
	}
	
	/**
	 * 유저의 회원번호를 아규먼트로 받아, 해당유저의 플레이리스트Dto리스트 리턴함
	 * @param memberId
	 * @return
	 */
	public List<PlaylistDto> getPlaylist(Long memberId) {
		log.info("getPlaylist(memberId={})", memberId);
		
		List<Playlist> playlist = playlistDao.findAllByMemberMemberIdOrderByPlaylistId(memberId);
		List<PlaylistDto> playlistDtoList = playlist.stream().map((eachPlaylist) -> PlaylistDto.fromEntity(eachPlaylist)).toList();
		playlistDtoList.forEach((dto) -> {
			// getItemsInPlaylist 메서드 사용해서 해당 플레이리스트에 있는 아이템들을 매핑
			try {
				dto.setPlaylistItemDtoList(getItemsInPlaylist(dto.getPlaylistId()));
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			List<PlaylistLike> playlistLikeList = playlistLikeDao.findAllByPlaylistPlaylistId(dto.getPlaylistId());
			dto.setPlaylistLikeList(playlistLikeList);
		});
		
		log.info("playlistDtoList = {}", playlistDtoList);
		
		return playlistDtoList;
	}
	
	/**
	 * memberId에 해당하는 마이페이지유저가 좋아요를 누른 플레이리스트의 리스트를 가져옴
	 * @param memberId
	 * @return
	 */
	public List<PlaylistDto> getLikedPlaylist(Long memberId) {
		log.info("getLikedPlaylist(memberId={})", memberId);
		
		List<PlaylistLike> likedPlaylist = playlistLikeDao.findAllByMemberMemberIdOrderByPlaylistLikeId(memberId);
		
		// 내가 좋아하는 플레이 리스트 중 private으로 설정 바뀐 리스트 걸르기 위해서..
		List<PlaylistLike> filteredLikedPlaylist = likedPlaylist.stream().filter((each) -> each.getPlaylist().getIsPrivate().equals("N")).toList();		
		log.info("private거른후 내가 좋아하는 리스트={}", filteredLikedPlaylist);
		
		// 해당 유저가 좋아요 누른 플레이리스트 항목들을 리스트로 매핑시킴
		List<Playlist> likedPlaylistList = new ArrayList<>();
		filteredLikedPlaylist.forEach((eachLike) -> {
			Long eachPlaylistId = eachLike.getPlaylist().getPlaylistId();
			Playlist eachPlaylist = playlistDao.findById(eachPlaylistId).orElse(null);
			
			if (eachPlaylist != null) {
				likedPlaylistList.add(eachPlaylist);
			}
		});
		
		// 플레이리스트의 리스트를 Dto로 매핑시켜서 각 playlist에 대한 추가정보들을 매핑
		List<PlaylistDto> likedPlaylistDtoList = likedPlaylistList.stream().map((eachPlaylist) -> PlaylistDto.fromEntity(eachPlaylist)).toList();
		likedPlaylistDtoList.forEach((dto) -> {
			try {
				dto.setPlaylistItemDtoList(getItemsInPlaylist(dto.getPlaylistId()));
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			List<PlaylistLike> playlistLikeList = playlistLikeDao.findAllByPlaylistPlaylistId(dto.getPlaylistId());
			dto.setPlaylistLikeList(playlistLikeList);
		});
		
		return likedPlaylistDtoList;
	}
	
	/**
	 * playlist id에 해당하는 playlist를 반환하는 서비스메서드
	 * @param playlistId
	 * @return
	 */
	public Playlist getPlaylistByPlaylistId(Long playlistId) {
		return playlistDao.findById(playlistId).orElse(null);
	}
	
	/**
	 * playlistId를 아규먼트로 받아 privacy를 업데이트시켜주는 서비스 메서드.
	 * isPrivate는 playlistId에 해당하는 playlist의 isPrivate칼럼에 업데이트 될 값임
	 * 예를 들어, 아규먼트 isPrivate이 'Y'일 경우 isPrivate는 'Y'로 업데이트되고, 'N'일 경우 'N'으로 업데이트 됨.
	 * @param playlistId
	 * @param isPrivate
	 */
	@Transactional
	public void setPlaylistPrivacy(Long playlistId, String isPrivate) {
		log.info("setPlaylistPrivacy(playlistId={}, isPrivate={})", playlistId, isPrivate);
		
		Playlist playlistToUpdate = playlistDao.findById(playlistId).orElse(null);
		
		playlistToUpdate.setIsPrivate(isPrivate);
		
	}
	
	/**
	 * PlaylistLike타입의 객체를 아규먼트로 받아 해당 정보와 일치하는 playlist like열을 삭제
	 * @param playlistLike
	 */
	@Transactional
	public void deletePlaylistLike(PlaylistLike playlistLike) {
		log.info("deletePlaylistLike(playlistLike={})", playlistLike);
		
		playlistLikeDao.deleteByMemberEmailAndPlaylistPlaylistId(playlistLike.getMember().getEmail(), playlistLike.getPlaylist().getPlaylistId());
	}
	
	
	/**
	 * playlistId를 아규먼트로 받아 해당 playlist를 삭제하는 서비스 메서드 
	 * @param playlistId
	 */
	public void deletePlaylist(Long playlistId) {
		log.info("deletePlaylist(playlistId={})", playlistId);
		
		playlistDao.deleteById(playlistId);
	}
	
	/**
	 * playlistId아규먼트로 받아 playlistItem타입을 객체로 받아,
	 * 해당 playlist에 포함된 playlist items들의 리스트를 리턴
	 * @param playlistItem
	 * @return
	 * @throws JsonProcessingException 
	 * @throws JsonMappingException 
	 */
	public List<PlaylistItemDto> getItemsInPlaylist(Long playlistId) throws JsonMappingException, JsonProcessingException {		
		log.info("getItemsInPlaylist(playlistId={})", playlistId);
		
		List<PlaylistItem> playlistItemsList = playlistItemDao.findAllByPlaylistPlaylistIdOrderByNthInPlaylist(playlistId);
		List<PlaylistItemDto> playlistItemDtoList = playlistItemsList.stream().map(item -> PlaylistItemDto.fromEntity(item)).toList();
		
		// playlistItemDto에 poster 정보 가져옴
		for (PlaylistItemDto playlistItemDto : playlistItemDtoList) {
			setWorkDetails(playlistItemDto);
		}
		
		return playlistItemDtoList;
	}
	
	/**
	 * 특정 플레이리스트 아이템을 삭제하는 서비스 메서드
	 * @param playlistItemId
	 */
	@Transactional
	public void deletePlaylistItem(Long playlistItemId) {
		log.info("deletePlaylistItem(playlistItemId={})", playlistItemId);
		
		playlistItemDao.deleteById(playlistItemId);		
	}
	
	
	/**
	 * playlist타입의 객체를 아규먼트로 받아 새로운 플레이리스트를 만드는 서비스 메서드
	 * @param playlist
	 */
	@Transactional
	public void createPlaylist(Playlist playlist) {
		
		log.info("createPlaylist(playlist={})", playlist);
		
		playlistDao.save(playlist);
				
	}
	
	/**
	 * playlistItem을 받아, 해당 playlistItem이 저장될 playlist의 몇번째 아이템인지 매핑 후 db에 저장
	 * @param playlistItem
	 * @return
	 */
	@Transactional
	public PlaylistItem addItemToPlaylist(PlaylistItem playlistItem) {
		log.info("playlistItem={}", playlistItem);
		
		Long nthInPlaylist = playlistItemDao.countByPlaylistPlaylistId(playlistItem.getPlaylist().getPlaylistId());
		log.info("추가할 플레이리스트 내 현재 작품 수(추가전) = {}", nthInPlaylist);
		playlistItem.setNthInPlaylist(nthInPlaylist + 1);
		PlaylistItem savedPlaylistItem = playlistItemDao.save(playlistItem);
		
		return savedPlaylistItem;
	}
	
	/**
	 * 플레이리스트 좋아요 누르면 추가해주는 서비스 메서드
	 * @param playlistLike
	 * @return
	 */
	@Transactional
	public PlaylistLike addPlaylistLike(PlaylistLike playlistLike) {
		log.info("addPlaylistLike(playlistLike={})", playlistLike);
		
		PlaylistLike savedPlaylistLike = playlistLikeDao.save(playlistLike);
		log.info("저장된 playlistLike={}", savedPlaylistLike);
		
		return savedPlaylistLike;
	}
	
	
	/**
	 * 유저가 바꾼 순서를 업데이트하는 메서드
	 * @param playlistItemList
	 */
	@Transactional
	public void reorderPlaylist(List<PlaylistItem> playlistItemList) {
		
		for (PlaylistItem playlistItem : playlistItemList) {
			PlaylistItem retrievedPlaylistItem = playlistItemDao.findById(playlistItem.getPlaylistItemId()).orElse(null);
			
			retrievedPlaylistItem.setNthInPlaylist(playlistItem.getNthInPlaylist());
			
			playlistItemDao.save(retrievedPlaylistItem);
		}
		
	}
	
	
	
	public Page<ReviewLike> getUserWhoLikedReview(Long reviewId, int page){

		Review review = Review.builder().reviewId(reviewId).build();

		Pageable pageable = PageRequest.of(page, 10, Sort.by("reviewLikeId").descending());

		Page<ReviewLike> likedUser = reviewLikeDao.findByReview(review, pageable);

		return likedUser;
	}
	
	
	
	// 보조 메서드
	/**
	 * PlaylistItemDto를 아규먼트로 받아, 해당 해당 작품에 대한 디테일 정보 가져옴
	 * @return
	 * @throws JsonProcessingException 
	 * @throws JsonMappingException 
	 */
	private PlaylistItemDto setWorkDetails(PlaylistItemDto playlistItemDto) throws JsonMappingException, JsonProcessingException {
		log.info("setPoster(playlistItemDto={})", playlistItemDto);
		
		switch (playlistItemDto.getCategory()) {
		
		case "movie":
			playlistItemDto.setWorkDetails(movieApiUtil.getMovieDetails(playlistItemDto.getTmdbId()));
			break;
			
		case "tv":
			// 변수명이 달라서 그냥 MovieDetailsDto로 타입 통일시킴.
			playlistItemDto.setWorkDetails(MovieDetailsDto.fromTvShowDto(tvShowApiUtil.getTvShowDetails(playlistItemDto.getTmdbId())));
			break;
			
			
		default:
			log.info("poster매핑실패");	
		}
		
		// log.info("매핑 후 playlistItemDto={}", playlistItemDto);
		
		return playlistItemDto;
	}

	public List<Review> recentReview(Long memberId){
		List<Review> recentReview = reviewDao.findByMemberMemberIdOrderByCreatedDateDesc(memberId);

		if(recentReview !=null && !recentReview.isEmpty()){
			return recentReview;
		} else {
			return new ArrayList<>();
		}
	}

	public int getPostCount(Long memberId){
		List<Post> getAllPost = postRepository.findByMemberMemberId(memberId);

		if(!getAllPost.isEmpty()){
			return getAllPost.size();
		} else {
			return 0;
		}
	}

}
