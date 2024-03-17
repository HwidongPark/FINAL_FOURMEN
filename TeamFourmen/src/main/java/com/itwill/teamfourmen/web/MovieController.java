package com.itwill.teamfourmen.web;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import com.itwill.teamfourmen.domain.ImdbRatings;
import com.itwill.teamfourmen.dto.review.CombineReviewDTO;
import com.itwill.teamfourmen.service.ImdbRatingUtil;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.itwill.teamfourmen.domain.Comment;
import com.itwill.teamfourmen.domain.CommentLike;
import com.itwill.teamfourmen.domain.ImdbRatings;
import com.itwill.teamfourmen.domain.Member;
import com.itwill.teamfourmen.domain.Playlist;
import com.itwill.teamfourmen.domain.Post;
import com.itwill.teamfourmen.domain.PostLike;
import com.itwill.teamfourmen.domain.Review;
import com.itwill.teamfourmen.domain.TmdbLike;
import com.itwill.teamfourmen.dto.board.CommentDto;
import com.itwill.teamfourmen.dto.board.PostDto;
import com.itwill.teamfourmen.dto.movie.MovieCastDto;
import com.itwill.teamfourmen.dto.movie.MovieCreditDto;
import com.itwill.teamfourmen.dto.movie.MovieCrewDto;
import com.itwill.teamfourmen.dto.movie.MovieDetailsDto;
import com.itwill.teamfourmen.dto.movie.MovieExternalIdDto;
import com.itwill.teamfourmen.dto.movie.MovieGenreDto;
import com.itwill.teamfourmen.dto.movie.MovieListDto;
import com.itwill.teamfourmen.dto.movie.MovieProviderDto;
import com.itwill.teamfourmen.dto.movie.MovieProviderItemDto;
import com.itwill.teamfourmen.dto.movie.MovieQueryParamDto;
import com.itwill.teamfourmen.dto.movie.MovieReleaseDateItemDto;
import com.itwill.teamfourmen.dto.movie.MovieVideoDto;
import com.itwill.teamfourmen.dto.person.PageAndListDto;
import com.itwill.teamfourmen.dto.playlist.PlaylistDto;
import com.itwill.teamfourmen.dto.post.PostCreateDto;
import com.itwill.teamfourmen.service.BoardService;
import com.itwill.teamfourmen.service.FeatureService;
import com.itwill.teamfourmen.service.ImdbRatingUtil;
import com.itwill.teamfourmen.service.MovieApiUtil;
import com.itwill.teamfourmen.service.MovieDetailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/movie")
public class MovieController {
	
	private final MovieApiUtil apiUtil;
	private final MovieDetailService detailService;
	private final FeatureService featureService;
	private final BoardService boardService;
	
	// IMDB RATING을 가져오기 위함.
	private String category = "movie";
	private final ImdbRatingUtil imdbRatingUtil;
	
	

	/**
	 * 인기영화 리스트 컨트롤러
	 * @param model
	 * @return
	 * @throws JsonMappingException
	 * @throws JsonProcessingException
	 */
	@GetMapping("/popular")
	public String popularMovieList(Model model) throws JsonMappingException, JsonProcessingException {
		
		log.info("popularMovieList()");
		
		getInitialList("popular", model);
		
		log.info("보드서비스={}", boardService);
		
		return "movie/movie-list";
	}
	
	
	/**
	 * 현재 상영 영화 리스트 컨트롤러
	 * @param model
	 * @return
	 * @throws JsonMappingException
	 * @throws JsonProcessingException
	 */
	@GetMapping("/now_playing")
	public String nowPlayingMovieList(Model model) throws JsonMappingException, JsonProcessingException {
		
		log.info("nowPlayingMovieList()");
		
		getInitialList("now_playing", model);
		
		
		return "movie/movie-list";
	}
	
	
	/**
	 * 평점 높은 영화 리스트 컨트롤러
	 * @param model
	 * @return
	 * @throws JsonMappingException
	 * @throws JsonProcessingException
	 */
	@GetMapping("/top_rated")
	public String topRatedMovieList(Model model) throws JsonMappingException, JsonProcessingException {
		
		log.info("topRatedMovieList()");
		
		getInitialList("top_rated", model);
		
		return "movie/movie-list";
	}
	
	
	/**
	 * 상영 예정 영화 리스트 컨트롤러
	 * @param model
	 * @return
	 * @throws JsonMappingException
	 * @throws JsonProcessingException
	 */
	@GetMapping("/upcoming")
	public String upcomingMovieList(Model model) throws JsonMappingException, JsonProcessingException {
		
		log.info("upcomingMovieList()");
		
		getInitialList("upcoming", model);
		
		return "movie/movie-list";
	}
	
	
	/**
	 * 유저가 선택한 필터내용을 반영한 영화 리스트를 반환함
	 * @param filterDto
	 * @param model
	 * @return
	 * @throws JsonMappingException
	 * @throws JsonProcessingException
	 */
	@GetMapping("/filter")
	public String filterMovieList(@ModelAttribute MovieQueryParamDto filterDto, Model model) throws JsonMappingException, JsonProcessingException {
		
		log.info("filterMovieList(filterDto={})", filterDto);
		filterDto.setListCategory("filter");
		
		getInitialList(filterDto, model);		
		
		return "movie/movie-list";
	}
	
	
	@GetMapping("/search")
	public String searchMovieList(@ModelAttribute MovieQueryParamDto searchDto, Model model) throws JsonMappingException, JsonProcessingException {
		
		log.info("searchMovieList(searchDto=${})", searchDto);
		searchDto.setListCategory("search");
		
		getInitialList(searchDto, model);
		
		return "movie/movie-list";
	}
	
	
	/**
	 * id에 해당하는 영화의 상세 페이지
	 * @param id
	 * @param model
	 * @return
	 */
	@GetMapping("/details/{id}")
	public String movieDetails(@PathVariable(name = "id") int id, Model model) {
		
		log.info("movieDetails(id={})", id);
		
		Review myReview = null;
		List<PlaylistDto> userPlaylist = null;
		
		
		// 영화 디테일 정보 가져오기
		MovieDetailsDto movieDetailsDto = apiUtil.getMovieDetails(id);
		//log.info("movieDetailsDto={}", movieDetailsDto);
		
		// TODO: 만약 credit dto가 없을 리가 있을까 생각해보자..
		MovieCreditDto movieCreditDto = apiUtil.getMovieCredit(id);
		//log.info("movieCreditDto={}", movieCreditDto);
		List<MovieCrewDto> directorList = movieCreditDto.getCrew().stream().filter((x) -> x.getJob().equals("Director")).toList();	// 감독만 꺼내온 리스트
		List<MovieCastDto> castList = movieCreditDto.getCast();	// 출연진만 꺼내온 리스트
		
		// 영화의 비디오 가져오기
		List<MovieVideoDto> movieVideoList = apiUtil.getMovieVideoList(id);
		List<MovieVideoDto> movieTrailerList = null;	// 여기에 트레일러 리스트만 가져올거임
		
		if (movieVideoList != null) {	// 영화의 비디오 리스트가 있는 경우에.
			movieTrailerList = movieVideoList.stream().filter((x) -> x.getType().equals("Trailer")).toList();
		}
		
		//log.info("movieVideoList = {}", movieVideoList);
		
		
		// 영화 provider 리스트 가져오기
		// 무비 provider, 각각 플랫폼마다 어떤 서비스가 있는지 확인하기 위해 MovieService에서 메서드 사용
		MovieProviderDto movieProviderDto = apiUtil.getMovieProviderList(id);
		// log.info("movieProviderDto={}", movieProviderDto);
		
		
		// releasedate관련 정보 가져옴 (작품 연령제한 포함된 정보)
		List<MovieReleaseDateItemDto> releaseDateItemList = apiUtil.getMovieReleaseDateInfo(id, "KR");
		MovieReleaseDateItemDto releaseItemDto = detailService.getType3MovieReleaseDateItem(releaseDateItemList);
		if (releaseItemDto == null || releaseItemDto.getCertification() != null) {	// 한국 release date정보 없으면 미국꺼 가져오도록 함
			releaseDateItemList = apiUtil.getMovieReleaseDateInfo(id, "US");
			releaseItemDto = detailService.getType3MovieReleaseDateItem(releaseDateItemList);	// 미국꺼 가져옴. 미국정보마져 없으면 null
		}
		
		
		List<MovieProviderItemDto> movieProviderList = null;
		if (movieProviderDto != null) {
			movieProviderList = detailService.getOrganizedMovieProvider(movieProviderDto);
			// log.info("movieProviderList={}", movieProviderList);
		}
		
		
		// 해당 영화의 Collection이 있으면 Collection리스트 가져오기
		if(movieDetailsDto.getBelongsToCollection() != null) {
			List<MovieDetailsDto> movieCollectionList = apiUtil.getMovieCollectionList(movieDetailsDto.getBelongsToCollection().getId());
			model.addAttribute("movieCollectionList", movieCollectionList);
		}
		
		// 해당영화의 social media id 가져옴
		MovieExternalIdDto movieExternalIdDto = apiUtil.getMovieExternalId(id);
		
		// 해당 영화와 관련된 추천영화 목록 가져옴
		List<MovieDetailsDto> recommendedList = apiUtil.getRecommendedMovie(id);
		
		// 좋아요 눌렀는지 여부 가져옴
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String email = authentication.getName();
		Member signedInUser = Member.builder().email(email).build();
		TmdbLike tmdbLike = featureService.didLikeTmdb(signedInUser, "movie", id);	// 만약 좋아요 이미 눌렀으면 TmdbLike객체 리턴됨
		
		log.info("email={}", email);
		if (!email.equals("anonymousUser")) {
			myReview = featureService.getMyReviewInTmdbWork(email, "movie", id);			
		}

		// 관련 리뷰 가져옴
		List<Review> movieReviewList = featureService.getReviews("movie", id);
		
		double ratingAverageDouble = 0;
				
		if (movieReviewList != null && movieReviewList.size() != 0) {
		    ratingAverageDouble = movieReviewList.stream().mapToDouble((each) -> each.getRating()).average().orElse(0);
		}
		
		DecimalFormat df = new DecimalFormat("#.#");
		
		ratingAverageDouble = Math.round(ratingAverageDouble * 10) / 10.0;
		String ratingAverage = df.format(ratingAverageDouble);
		
		
		
		// 해당 영화 관련된 4개의 리뷰만 남김.
		int endIndex = Math.min(4, movieReviewList.size());		
		movieReviewList = movieReviewList.subList(0, endIndex);				
		
		Map<Long, Integer> reviewComment = new HashMap<>();
		Map<Long, Long> reviewLiked = new HashMap<>();

		for(Review movieReview : movieReviewList){
			Long reviewId = movieReview.getReviewId();

			int numOfComment = featureService.getNumOfReviewComment(reviewId);
			Long numOfLiked = featureService.getNumOfReviewLike(reviewId);

			reviewComment.put(reviewId, numOfComment);
			reviewLiked.put(reviewId, numOfLiked);
		}
		
		
		// 로그인한 유저의 플레이리스트 가져오기
		if (!email.equals("anonymousUser")) {
			userPlaylist = featureService.getPlaylist(email);
			// log.info("userPlaylist={}", userPlaylist);
		}
		
		model.addAttribute("numOfReviewLiked", reviewLiked);
		model.addAttribute("numOfReviewComment", reviewComment);


//		List<Review> movieReviewList = featureService.getReviews("movie", id);
		
		// 내가 좋아요 누른 리뷰들 가져옴
		
		model.addAttribute("movieDetailsDto", movieDetailsDto);
		model.addAttribute("movieCreditDto", movieCreditDto);
		model.addAttribute("directorList", directorList);
		model.addAttribute("castList", castList);
		model.addAttribute("movieTrailerList", movieTrailerList);
		model.addAttribute("movieProviderList", movieProviderList);
		model.addAttribute("movieExternalIdDto", movieExternalIdDto);
		model.addAttribute("recommendedList", recommendedList);
		model.addAttribute("releaseItemDto", releaseItemDto);
		model.addAttribute("movieReviewList", movieReviewList);
		model.addAttribute("ratingAverage", ratingAverage);
		model.addAttribute("myReview", myReview);
		model.addAttribute("userPlaylist", userPlaylist);
		
		// imdb rating을 가져오기 위함...
		// id = TMDB의 movie id , category = 제일 상단에 이미 선언 "movie"
		String imdbId = imdbRatingUtil.getImdbId(id, category);
		ImdbRatings imdbRatings = imdbRatingUtil.getImdbRating(imdbId);

		if(imdbRatings != null) {
			log.info("IMDB RATINGS = {}", imdbRatings);
		} else {
			log.info("IMDB RATINGS IS NULL");
		}
		
		// 객체로 넘어감. 원하는 값은 imdbRatings -> getter를 통해서
		// IMDB 아이콘은 static/icons/imdb-icon.svg 파일...!
		model.addAttribute("imdbRatings", imdbRatings);

		model.addAttribute("tmdbLike", tmdbLike);	// 좋아요 눌렀는지 확인하기 위해

    
//		model.addAttribute("movieReviewList", movieReviewList);
		
		return "movie/movie-details";
	}
	
	@GetMapping("/board")
	public String movieBoardList(Model model, @RequestParam(name = "page", required = false, defaultValue = "0") int page) {
		log.info("게시판 리스트 들어옴");
		
		Page<PostDto> postDtoList = boardService.getPostList("movie", page);
		postDtoList.forEach((post) -> {
			Long likes = boardService.countLikes(post.getPostId());
			post.setLikes(likes);
		});
		
		// TODO: total element 타입 Long으로변경하는거 논의
		PageAndListDto pagingDto = PageAndListDto.getPagingDto(page, (int) postDtoList.getTotalElements(), postDtoList.getTotalPages(), 5, 20);		
		log.info("pagingDto={}", pagingDto);
		
		model.addAttribute("category", "movie");
		model.addAttribute("postDtoList", postDtoList);
		model.addAttribute("pagingDto", pagingDto);
		
		return "board/list";
	}
	
	@GetMapping("/board/details")
	public String movieBoardDetails(@RequestParam(name = "id") Long id, @RequestParam(name="page", required = false, defaultValue = "1") int page, Model model) {
		log.info("movieBoardDetails(id={})", id);
		
		PostDto postDetails = boardService.getPostDetail(id);
		// log.info("postDetails={}", postDetails);
		
		// 조회수 1 추가
		boardService.addView(id);
		
		// 해당 게시물의 좋아요 개수
		Long numLikes = boardService.countLikes(id);
		
		// 로그인한 유저가 해당 게시물을 좋아했는지 보기위해
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String email = authentication.getName();
		Member signedInUser = Member.builder().email(email).build();
		PostLike haveLiked = boardService.haveLiked(signedInUser, id);
		
		// 해당 게시물의 댓글 리스트 가져옴
		List<CommentDto> commentDtoList = boardService.getCommentList(id);
		log.info("commentDtoList={}", commentDtoList);
		
		int numOfComments = boardService.getNumOfComments(commentDtoList);
		
		model.addAttribute("page", page);
		model.addAttribute("category", "movie");
		model.addAttribute("postDetails", postDetails);
		model.addAttribute("numLikes", numLikes);
		model.addAttribute("haveLiked", haveLiked);
		model.addAttribute("boardName", "영화게시판");
		model.addAttribute("commentDtoList", commentDtoList);
		model.addAttribute("numOfComments", numOfComments);
		
		
		return "board/details";
	}
	
	@GetMapping("/board/create")
	@PreAuthorize("isAuthenticated()")
	public String movieBoardCreate(Model model) {
		log.info("영화 게시글 작성페이지");
		
		// 일단 create페이지에 들어오면 db에 글 작성을 insert하고 나중에 update하도록 함
		// 이미지 파일을 관리하기 위해서				
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String email = authentication.getName();
		Member signedInUser = Member.builder().email(email).build();
				
		model.addAttribute("category", "movie");
		
		return "board/create";
	}
	
	/**
	 * 게시글 작성하는 컨트롤러 메서드
	 * @param postDto
	 * @return
	 */
	@PostMapping("/board/create")
	@PreAuthorize("isAuthenticated()")	
	public String postMovieBoard(@ModelAttribute PostCreateDto postDto) {
		log.info("postMovieBoard(postDto={})", postDto);
		
		Post savedPost = boardService.post(postDto);
		
		return "redirect:/movie/board/details?id=" + savedPost.getPostId();
	}
	
	/**
	 * 게시판 게시글 수정창으로 보내주는 컨트롤러 매서드
	 * @param post
	 * @param model
	 * @return
	 */
	@PostMapping("/board/edit")
	@PreAuthorize("isAuthenticated()")
	public String editMovieBoard(@ModelAttribute Post post, Model model) {
		
		log.info("editMovieBoard(post={})", post);
		model.addAttribute("post", post);
		model.addAttribute("category", "movie");
		return "board/edit";
	}
	
	
	@PostMapping("/board/do-edit")
	public String updateMoviePost(@ModelAttribute Post post) {
		
		log.info("updateMoviePost(post={})", post);
		boardService.updatePost(post);
		
		return "redirect:/movie/board/details?id=" + post.getPostId();
	}
	
	
	/**
	 * 검색 카테고리와 검색어를 기반으로 검색결과를 가져다주는 컨트롤러 메서드
	 * @return
	 */
	@GetMapping("/board/search")
	public String searchMovieBoard(Model model, @RequestParam(name = "searchCategory") String searchCategory
			, @RequestParam(name = "searchContent") String searchContent, @RequestParam(name = "page", required = false, defaultValue = "0") int page) {
		log.info("searchMovieBoard(searchCategory={}, searchContent={})", searchCategory, searchContent);
		
		Page<PostDto> searchedPostDtoList = boardService.searchPost(searchCategory, searchContent, "movie", page);
		
		searchedPostDtoList.forEach((post) -> {
			Long likes = boardService.countLikes(post.getPostId());
			post.setLikes(likes);
		});
		
		PageAndListDto pagingDto = PageAndListDto.getPagingDto(page, (int) searchedPostDtoList.getTotalElements(), searchedPostDtoList.getTotalPages(), 5, 5);
		
		model.addAttribute("category", "movie");
		model.addAttribute("isSearch", "검색 결과");
		model.addAttribute("totElements", searchedPostDtoList.getTotalElements());
		model.addAttribute("postDtoList", searchedPostDtoList);
		model.addAttribute("pagingDto", pagingDto);
		model.addAttribute("keyword", searchContent);
		model.addAttribute("searchCategory", searchCategory);
		
		
		return "board/list";
	}
	
	// 이 아래로는 일반 메서드 모음
	
	/**
	 * 페이지 이름을 String으로 받아 List<MovieAdditionalListDto>를 반환해주는 메서드
	 * PageName은 반드시 "popular", "now_playing", "top_rated", "upcoming" 중 하나여야 함!
	 * @param pageName 반드시 "popular", "now_playing", "top_rated", "upcoming" 중 하나
	 * @param model
	 * @throws JsonProcessingException 
	 * @throws JsonMappingException 
	 */
	private void getInitialList(String pageName, Model model) throws JsonMappingException, JsonProcessingException {
		
		MovieQueryParamDto paramDto = new MovieQueryParamDto();
		paramDto.setListCategory(pageName);
		
		MovieListDto listDto = apiUtil.getMovieList(paramDto);
		//log.info("listDto={}", listDto);		
				
		List<MovieGenreDto> movieGenreList = apiUtil.getMovieGenreList();
		
		model.addAttribute("listDto", listDto);
		model.addAttribute("movieGenreList", movieGenreList);
	}
	
	
	/**
	 * 페이지 이름을 String으로 받아 List<MovieAdditionalListDto>를 반환해주는 메서드
	 * pageName은 반드시 "filter" ~ 중 하나여아 함! (만드는 중,,,)
	 * @param pageName "filter", ~ 중 하나
	 * @param model
	 * @param filterDto
	 * @throws JsonMappingException
	 * @throws JsonProcessingException
	 */
	private void getInitialList(MovieQueryParamDto paramDto, Model model) throws JsonMappingException, JsonProcessingException {
		
		
		MovieListDto listDto = apiUtil.getMovieList(paramDto);
		// log.info("listDto={}", listDto);		
				
		List<MovieGenreDto> movieGenreList = apiUtil.getMovieGenreList();
				
		model.addAttribute("listDto", listDto);
		model.addAttribute("movieGenreList", movieGenreList);
	}
	
	
	
	
}
