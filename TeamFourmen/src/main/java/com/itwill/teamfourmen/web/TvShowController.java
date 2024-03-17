package com.itwill.teamfourmen.web;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import com.itwill.teamfourmen.domain.*;
import com.itwill.teamfourmen.dto.board.CommentDto;
import com.itwill.teamfourmen.dto.board.PostDto;
import com.itwill.teamfourmen.dto.person.PageAndListDto;
import com.itwill.teamfourmen.dto.playlist.PlaylistDto;
import com.itwill.teamfourmen.dto.post.PostCreateDto;
import com.itwill.teamfourmen.dto.review.CombineReviewDTO;
import com.itwill.teamfourmen.dto.tvshow.*;
import com.itwill.teamfourmen.service.BoardService;
import com.itwill.teamfourmen.service.CommentService;
import com.itwill.teamfourmen.service.FeatureService;
import com.itwill.teamfourmen.service.ImdbRatingUtil;
import com.itwill.teamfourmen.service.TvShowApiUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/tv")
public class TvShowController {

	@Value("${tmdb.api-key}")
	private String API_KEY;

	private final TvShowApiUtil apiUtil;

	private final ImdbRatingUtil imdbRatingUtil;
	private final FeatureService featureService;
	private final CommentService commentService;
	private final BoardService boardService;
	private final NicknameInterceptor nicknameIntercepter;
	private String category = "tv";
	

	@GetMapping("/main")
	public String getTvShowMain(Model model){
		log.info("GET TV SHOW MAIN VIEW");

		// Random 객체 생성 -> 랜덤한 페이지를 보내기 위해
		Random random = new Random();

		// 넷플릭스 tv 리스트를 MAIN으로 보냄
		TvShowListDTO NetflixListDTO = apiUtil.getOttTvShowList("netfilx", random.nextInt(10) + 1);
		List<TvShowDTO> Netfilx = NetflixListDTO.getResults();
		model.addAttribute("Netfilx", Netfilx);

		// 디즈니 tv 리스트를 Main으로 보냄
		TvShowListDTO DisenyPlusListDto = apiUtil.getOttTvShowList("disney_plus", random.nextInt(5) +1);
		List<TvShowDTO> Disney = DisenyPlusListDto.getResults();
		model.addAttribute("Disney", Disney);

		// 애플 tv 리스트를 Main으로 보냄
		TvShowListDTO AppleTvListDto = apiUtil.getOttTvShowList("apple_tv", random.nextInt(5) +1);
		List<TvShowDTO> Apple = AppleTvListDto.getResults();
		model.addAttribute("Apple", Apple);

		// 아마존 tv 리스트를 Main으로 보냄
		TvShowListDTO AmazoneListDto = apiUtil.getOttTvShowList("amazone_prime", random.nextInt(5) +1);
		List<TvShowDTO> Amazone = AmazoneListDto.getResults();
		model.addAttribute("Amazone", Amazone);

		// Watcha 리스트를 Main으로 보냄
		TvShowListDTO WatchaListDto = apiUtil.getOttTvShowList("watcha", random.nextInt(4)+1);
		List<TvShowDTO> Watcha = WatchaListDto.getResults();
		model.addAttribute("Watcha", Watcha);

		// Wavve 리스트를 Main으로 보냄
		TvShowListDTO WavveListDto = apiUtil.getOttTvShowList("wavve" , random.nextInt(5)+1);
		List<TvShowDTO> Wavve = WavveListDto.getResults();
		model.addAttribute("Wavve", Wavve);

		// 이 주의 인기 리스트
		TvShowListDTO PopularThisWeekTvShowList = apiUtil.getTrendTvShowList("week",1);
		List<TvShowDTO> popularThisWeekDto = PopularThisWeekTvShowList.getResults();
		model.addAttribute("popularThisWeek", popularThisWeekDto);

		return "tvshow/tvshow-main";
	}

	@GetMapping("/top_rated")
	public String getTopRatedTvShowList(Model model) throws ParseException {
		log.info("GET Top Rated Tv Show List");

		getInitialList("top_rated", model);

		return "tvshow/top-rated-list";
	}

	@GetMapping("/trending/{timeWindow}")
	public String getPopularTvShowList(Model model, @PathVariable(name = "timeWindow") String timeWindow){
		log.info("GET Trending Tv Show List");

		TvShowListDTO listDTO = apiUtil.getTrendTvShowList(timeWindow, 1);
		//log.info("listDto = {}", listDTO);

		model.addAttribute("listDTO", listDTO);

		log.info("TOTALPAGES = {}", listDTO.getTotal_pages());

		List<TvShowDTO> tvShowDto = listDTO.getResults();

		model.addAttribute("tvShowDto", tvShowDto);

		return "tvshow/trend-list";
	}

	/*
	필터링 -> TvShow 리스트 반영
	 */

	@GetMapping("/filter")
	public String getFilterTvShowList(Model model, @ModelAttribute TvShowQueryParamDTO filterDTO) {
		log.info("Get Filter Tv Show List - Filter Dto = {}", filterDTO);

		filterDTO.setListCategory("filter");

		getInitialList(filterDTO, model);

		return "tvshow/top-rated-list";
	}

  @GetMapping("/search")
	public String getSearchTvShowList(Model model, @ModelAttribute TvShowQueryParamDTO searchDTO) {
		log.info("Get Search Tv Show List - Search Dto = {}", searchDTO);

		searchDTO.setListCategory("search");

		getInitialList(searchDTO, model);

		return "tvshow/top-rated-list";
	}

	// 리스트에서 tvshow를 클릭했을때 상세페이지로 넘어가는 부분
	@GetMapping(value = {"/details/{id}" })
	public String getTvShowDetails(Model model, @PathVariable(name = "id") int id) {
		log.info("Get Tv Show Details = {}", id);
//		log.info("API KEY = {}", API_KEY);
		Review myReview = null;
		List<PlaylistDto> userPlaylist = null;
		
		RestTemplate restTemplate = new RestTemplate();
		
		int seriesId = id;

		String apiUri = "https://api.themoviedb.org/3/tv";
		// 드라마 정보
		TvShowDTO tvShowDTO = apiUtil.getTvShowDetails(id);

		//log.info("tvShowDto = {}", tvShowDTO.toString());

		List<TvShowSeasonDTO> seasonList = tvShowDTO.getSeasons();

		model.addAttribute("tvShowDto", tvShowDTO);

		model.addAttribute("seasonList", seasonList);

		// OTT 정보 (WatchProvider)

		TvShowWatchProviderListDTO tvShowWatchProviderListDTO = apiUtil.getTvShowProvider(id);

		String watchRegion = "KR";

		TvShowWatchProviderRegionDTO tvShowWatchProviderRegionDTO = tvShowWatchProviderListDTO.getResults().get(watchRegion);

		TvShowWatchProviderDTO[] tvShowWatchProviderDTO;

		try {
			tvShowWatchProviderDTO = tvShowWatchProviderRegionDTO.getFlatrate();
			model.addAttribute("watch_provider_list", tvShowWatchProviderDTO);

		} catch (NullPointerException e) {
			e.printStackTrace();
		}

		// 시청 등급
		String contentRatingsUrl = UriComponentsBuilder.fromUriString(apiUri)
				.path("/{seriesId}/content_ratings")
				.queryParam("api_key", API_KEY)
				.buildAndExpand(String.valueOf(seriesId))
				.toUriString();

		TvShowContentRatingsListDTO tvShowContentRatingsList = restTemplate.getForObject(contentRatingsUrl, TvShowContentRatingsListDTO.class);

		List<TvShowContentRatingsDTO> results = tvShowContentRatingsList.getResults();

		TvShowContentRatingsDTO rating = new TvShowContentRatingsDTO();

		for (TvShowContentRatingsDTO r : results) {
			if (r.getIso_3166_1().equals("KR")) {
				rating = r;
				break;
			} else if (r.getIso_3166_1().equals("US")) {
				rating = r;
			}
		}

		model.addAttribute("rating", rating);
		//log.info("rating = {}", rating);

		// 방송사? 배급사?
		//log.info("network?? = {}",tvShowDTO.getNetworks().get(0));
		List<TvShowNetworkDTO> networkList = tvShowDTO.getNetworks();

		model.addAttribute("networkList", networkList);

		// SNS 불러오기
		String getTvShowSnsUrl = UriComponentsBuilder.fromUriString(apiUri)
				.path("/{seriesId}/external_ids")
				.queryParam("api_key", API_KEY)
				.buildAndExpand(String.valueOf(seriesId))
				.toUriString();

		TvShowSnsDTO tvShowSnsDTO = restTemplate.getForObject(getTvShowSnsUrl, TvShowSnsDTO.class);

		model.addAttribute("sns", tvShowSnsDTO);

		// imdb rating 받아오기
		String imdbId = imdbRatingUtil.getImdbId(id, category);

		if(imdbId != null) {
			ImdbRatings imdbRatings = imdbRatingUtil.getImdbRating(imdbId);
			model.addAttribute("imdbRatings", imdbRatings);
		} else {
			model.addAttribute("imdbRatings", null);
		}

		// 배우, 스탭 목록
		String getTvShowCreditUrl = UriComponentsBuilder.fromUriString(apiUri)
				.path("/{seriesId}/credits")
				.queryParam("language", "ko")
				.queryParam("api_key", API_KEY)
				.buildAndExpand(String.valueOf(seriesId))
				.toUriString();

		TvShowCreditListDTO tvShowCreditListDTO = restTemplate.getForObject(getTvShowCreditUrl, TvShowCreditListDTO.class);

		List<TvShowCreditDTO> tvShowCast = tvShowCreditListDTO.getCast();

		List<TvShowCreditDTO> tvShowCrew = tvShowCreditListDTO.getCrew();

		model.addAttribute("tvShowCast", tvShowCast);

		// 장르
		String genresName = tvShowDTO.getGenres().stream()
				.map(TvShowGenreDTO::getName)
				.collect(Collectors.joining(", "));

		model.addAttribute("genres", genresName);

		// 제목 옆 최초 방영 년도 표기
		String dateString = tvShowDTO.getFirst_air_date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		try {
			Date date = dateFormat.parse(dateString);
			SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
			String year = yearFormat.format(date);

			model.addAttribute("releaseYear", year);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		// 관련 추천 드라마 목록...
		String getTvShowRecoUrl = UriComponentsBuilder.fromUriString(apiUri)
				.path("/{seriesId}/recommendations")
				.queryParam("language", "ko")
				.queryParam("api_key", API_KEY)
				.buildAndExpand(String.valueOf(seriesId))
				.toUriString();

		TvShowRecoListDTO tvShowRecoListDTO = restTemplate.getForObject(getTvShowRecoUrl, TvShowRecoListDTO.class);

		//log.info("tvShowRecoList = {}",tvShowRecoListDTO.toString());

		List<TvShowRecoDTO> tvShowRecoDTO = tvShowRecoListDTO.getResults();

		//log.info("RECO = {}",tvShowRecoDTO.size());

		model.addAttribute("tvShowReco", tvShowRecoDTO);

		// Tv Show 트레일러 가져오기
		TvShowVideoListDTO tvShowVideoDTOList = apiUtil.getTvShowVideo(id);

		List<TvShowVideoDTO> realTrailer = new ArrayList<>();

		List<TvShowVideoDTO> tvShowTrailerList = tvShowVideoDTOList.getResults();

		log.info("TVSHOWTRAILERLIST is empty? ={}", tvShowTrailerList.isEmpty());


		if (!tvShowTrailerList.isEmpty()) {
			for (TvShowVideoDTO trailer : tvShowTrailerList) {
				if (trailer.getType().equalsIgnoreCase("Trailer")) {
					realTrailer.add(trailer);
					model.addAttribute("trailer", realTrailer);
					log.info("TRAILER = {}",realTrailer.toString());
					break;
				}
			}
		} else {
			log.info("TV SHOW TRAILER IS EMPTY");
			model.addAttribute("trailer", null);
		}
		
		// 로그인한 유저의 플레이리스트 가져오기
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String email = authentication.getName();
				
		if (!email.equals("anonymousUser")) {
			userPlaylist = featureService.getPlaylist(email);
			log.info("userPlaylist={}", userPlaylist);
		}


		if(!email.equals("anonymousUser")){
			myReview = featureService.getMyReviewInTmdbWork(email , "tv" ,id);
		}

		model.addAttribute("userPlaylist", userPlaylist);

		model.addAttribute("myReview", myReview);

		// TV SHOW 좋아요 가져오기


		Member signedInUser = Member.builder().email(email).build();
		TmdbLike tmdbLike = featureService.didLikeTmdb(signedInUser, "tv", id);

		model.addAttribute("tmdbLike", tmdbLike);	// 좋아요 눌렀는지 확인하기 위해
				
		// Tv Show 별 리뷰 가져오기
		List<Review> tvShowReviewList = featureService.getReviews("tv", id);
		
		double ratingAverageDouble = 0; 
		
		if (tvShowReviewList != null && tvShowReviewList.size() != 0) {			
			ratingAverageDouble = tvShowReviewList.stream().mapToDouble((each) -> each.getRating()).average().orElse(0);
		}
		
		ratingAverageDouble = Math.round(ratingAverageDouble * 10) / 10.0;
		DecimalFormat df = new DecimalFormat("#.#");
		String ratingAverage = df.format(ratingAverageDouble);
		
		int endIndex = Math.min(4, tvShowReviewList.size());

		tvShowReviewList = tvShowReviewList.subList(0, endIndex);

		model.addAttribute("tvShowReviewList", tvShowReviewList);

		Map<Long, Integer> reviewComment = new HashMap<>();

		Map<Long, Long> reviewLiked = new HashMap<>();

		for(Review tvShowReview : tvShowReviewList) {
			Long reviewId = tvShowReview.getReviewId();
			int numOfComment = featureService.getNumOfReviewComment(reviewId);

			Long numOfLiked = featureService.getNumOfReviewLike(reviewId);

			reviewComment.put(reviewId, numOfComment);
			reviewLiked.put(reviewId,numOfLiked);
		}				
		
		model.addAttribute("ratingAverage", ratingAverage);
		model.addAttribute("numOfReviewLiked", reviewLiked);
		model.addAttribute("numOfReviewComment", reviewComment);

		return "tvshow/tvshow-details";
	}


	@GetMapping("/details/{id}/season/{season_number}")
	public String getTvShowSeasonDetails(Model model, @PathVariable(name= "id") int id , @PathVariable(name = "season_number") int season_number){

		String apiUri = "https://api.themoviedb.org/3/tv";

		log.info("GET TV SHOW SEASON DETAILS - ID = {} , SEASON_NUM = {}", id, season_number);

		TvShowDTO tvShowDto = apiUtil.getTvShowDetails(id);

		log.info("TVSHOW Name = {}", tvShowDto.getName());

		model.addAttribute("tvShowDto", tvShowDto);

		TvShowSeasonDTO getSeasonDto = apiUtil.getTvShowSeasonDetail(id, season_number);

		log.info("SEASON DETAIL = {}", getSeasonDto);

		model.addAttribute("seasonDto", getSeasonDto);

		// 제목 옆 최초 방영 년도 표기
		if(getSeasonDto.getEpisodes().get(0).getAir_date() != null) {
			String dateString = getSeasonDto.getEpisodes().get(0).getAir_date();

			log.info("==========AIR DATE========== = {}", dateString);

			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

			try {
				Date date = dateFormat.parse(dateString);
				SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
				String year = yearFormat.format(date);

				model.addAttribute("releaseYear", year);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		// 시즌 1화의 stillpath를 가져오기 위함.
		TvShowEpisodeDTO episodeDTO = apiUtil.getTvShowEpisodeDetail(id, season_number, getSeasonDto.getEpisodes().get(0).getEpisode_number());

		log.info("EPISODE DETAIL = {}", episodeDTO.getStill_path());

		model.addAttribute("episodeDTO", episodeDTO);

		// 시즌 별 배우 목록 가져오기

		String getTvShowCreditUrl = UriComponentsBuilder.fromUriString(apiUri)
				.path("/{seriesId}/season/{season_number}/credits")
				.queryParam("language", "ko-KR")
				.queryParam("api_key", API_KEY)
				.buildAndExpand(String.valueOf(id), String.valueOf(season_number))
				.toUriString();

		RestTemplate restTemplate = new RestTemplate();

		TvShowCreditListDTO tvShowCreditListDTO = restTemplate.getForObject(getTvShowCreditUrl, TvShowCreditListDTO.class);

		List<TvShowCreditDTO> tvShowCast = tvShowCreditListDTO.getCast();

		log.info("Season Tv Show Cast = {}",tvShowCast);

		model.addAttribute("castingList", tvShowCast);

		return "tvshow/season-details";
	}
	
	
	
	// 게시판 관련 컨트롤러 메서드
	
	@GetMapping("/board")
	public String tvBoardList(Model model, @RequestParam(name = "page", required = false, defaultValue = "0") int page) {
		log.info("게시판 리스트 들어옴");
		
		Page<PostDto> postDtoList = boardService.getPostList("tv", page);
		postDtoList.forEach((post) -> {
			Long likes = boardService.countLikes(post.getPostId());
			post.setLikes(likes);
		});
		
		// TODO: total element 타입 Long으로변경하는거 논의
		PageAndListDto pagingDto = PageAndListDto.getPagingDto(page, (int) postDtoList.getTotalElements(), postDtoList.getTotalPages(), 5, 20);		
		log.info("pagingDto={}", pagingDto);
		
		model.addAttribute("category", "tv");
		model.addAttribute("postDtoList", postDtoList);
		model.addAttribute("pagingDto", pagingDto);
		
		return "board/list";
	}
	
	@GetMapping("/board/details")
	public String tvBoardDetails(@RequestParam(name = "id") Long id, @RequestParam(name="page", required = false, defaultValue = "1") int page, Model model) {
		log.info("tvBoardDetails(id={})", id);
		
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
		model.addAttribute("category", "tv");
		model.addAttribute("postDetails", postDetails);
		model.addAttribute("numLikes", numLikes);
		model.addAttribute("haveLiked", haveLiked);
		model.addAttribute("boardName", "TV 게시판");
		model.addAttribute("commentDtoList", commentDtoList);
		model.addAttribute("numOfComments", numOfComments);
		
		
		return "board/details";
	}
	
	@GetMapping("/board/create")
	@PreAuthorize("isAuthenticated()")
	public String personBoardCreate(Model model) {
		log.info("티비 게시글 작성페이지");
		
		model.addAttribute("category", "tv");
		
		return "board/create";
	}
	
	/**
	 * 게시글 작성하는 컨트롤러 메서드
	 * @param postDto
	 * @return
	 */
	@PostMapping("/board/create")
	@PreAuthorize("isAuthenticated()")	
	public String postTvBoard(@ModelAttribute PostCreateDto postDto) {
		log.info("postTvBoard(postDto={})", postDto);
		
		Post savedPost = boardService.post(postDto);
		
		return "redirect:/tv/board/details?id=" + savedPost.getPostId();
	}
	
	/**
	 * 게시판 게시글 수정창으로 보내주는 컨트롤러 매서드
	 * @param post
	 * @param model
	 * @return
	 */
	@PostMapping("/board/edit")
	@PreAuthorize("isAuthenticated()")
	public String editTvBoard(@ModelAttribute Post post, Model model) {
		
		log.info("editTvBoard(post={})", post);
		model.addAttribute("post", post);
		model.addAttribute("category", "tv");
		return "board/edit";
	}
	
	
	@PostMapping("/board/do-edit")
	public String updateTvPost(@ModelAttribute Post post) {
		
		log.info("updateTvPost(post={})", post);
		boardService.updatePost(post);
		
		return "redirect:/tv/board/details?id=" + post.getPostId();
	}
	
	
	/**
	 * 검색 카테고리와 검색어를 기반으로 검색결과를 가져다주는 컨트롤러 메서드
	 * @return
	 */
	/**
	 * 검색 카테고리와 검색어를 기반으로 검색결과를 가져다주는 컨트롤러 메서드
	 * @return
	 */
	@GetMapping("/board/search")
	public String searchTvBoard(Model model, @RequestParam(name = "searchCategory") String searchCategory
			, @RequestParam(name = "searchContent") String searchContent, @RequestParam(name = "page", required = false, defaultValue = "0") int page) {
		log.info("searchTvBoard(searchCategory={}, searchContent={})", searchCategory, searchContent);
		
		Page<PostDto> searchedPostDtoList = boardService.searchPost(searchCategory, searchContent, "tv", page);
		
		searchedPostDtoList.forEach((post) -> {
			Long likes = boardService.countLikes(post.getPostId());
			post.setLikes(likes);
		});
		
		PageAndListDto pagingDto = PageAndListDto.getPagingDto(page, (int) searchedPostDtoList.getTotalElements(), searchedPostDtoList.getTotalPages(), 5, 5);
		
		model.addAttribute("category", "tv");
		model.addAttribute("isSearch", "검색 결과");
		model.addAttribute("totElements", searchedPostDtoList.getTotalElements());
		model.addAttribute("postDtoList", searchedPostDtoList);
		model.addAttribute("pagingDto", pagingDto);
		model.addAttribute("keyword", searchContent);
		model.addAttribute("searchCategory", searchCategory);
		
		
		return "board/list";
	}
	
	

	private void getInitialList(String pageName, Model model) {

		TvShowQueryParamDTO paramDTO = new TvShowQueryParamDTO();
		paramDTO.setListCategory(pageName);

		TvShowListDTO listDTO = apiUtil.getTvShowList(paramDTO);

		List<TvShowDTO> tvShowDto = listDTO.getResults();

		TvShowGenreListDTO tvShowGenreListDTO = apiUtil.getTvShowGenreList("ko-KR");

		List<TvShowGenreDTO> tvShowGenre = tvShowGenreListDTO.getGenres();

		model.addAttribute("listDTO", listDTO);
		model.addAttribute("tvShowDto", tvShowDto);
		model.addAttribute("tvShowGenreDTO", tvShowGenre);
	}

	private void getInitialList(TvShowQueryParamDTO paramDTO, Model model) {

		TvShowListDTO listDTO = apiUtil.getTvShowList(paramDTO);

		List<TvShowDTO> tvShowDto = listDTO.getResults();

		log.info("PARAMS = {}", paramDTO.toString());

		TvShowGenreListDTO tvShowGenreList = apiUtil.getTvShowGenreList("ko-KR");

		List<TvShowGenreDTO> tvShowGenre = tvShowGenreList.getGenres();

		model.addAttribute("params", paramDTO);
		model.addAttribute("listDTO", listDTO);
		model.addAttribute("tvShowDto", tvShowDto);
		model.addAttribute("tvShowGenreDTO", tvShowGenre);
	}
}
