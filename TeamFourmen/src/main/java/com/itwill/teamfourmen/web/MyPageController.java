package com.itwill.teamfourmen.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.itwill.teamfourmen.domain.*;
import com.itwill.teamfourmen.domain.Member;
import com.itwill.teamfourmen.domain.MemberRepository;
import com.itwill.teamfourmen.domain.NicknameInterceptor;
import com.itwill.teamfourmen.domain.Playlist;
import com.itwill.teamfourmen.domain.Review;
import com.itwill.teamfourmen.domain.TmdbLike;
import com.itwill.teamfourmen.dto.MemberModifyDto;
import com.itwill.teamfourmen.dto.MemberSearchDto;
import com.itwill.teamfourmen.dto.movie.MovieDetailsDto;
import com.itwill.teamfourmen.dto.mypage.MypageDTO;
import com.itwill.teamfourmen.dto.person.DetailsPersonDto;
import com.itwill.teamfourmen.dto.playlist.PlaylistDto;
import com.itwill.teamfourmen.dto.playlist.PlaylistItemDto;
import com.itwill.teamfourmen.dto.review.CombineReviewDTO;
import com.itwill.teamfourmen.dto.tvshow.TvShowDTO;
import com.itwill.teamfourmen.repository.PostRepository;
import com.itwill.teamfourmen.service.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

import java.io.File;
import java.io.IOException;
import java.util.*;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/mypage")
public class MyPageController {

    private final FeatureService featureService;
    private final TvShowApiUtil tvShowApiUtil;
    private final MovieApiUtil movieApiUtil;
    private final MyPageService myPageService;
    private final PersonService personService;
    private final MemberService memberservice;
    private final NicknameInterceptor myname;
    private final FollowService followService;

    @GetMapping("/")
    public void mypage() {
    }

    @GetMapping("/details/{memberId}/profile")
    public String getMyPageDetails(Model model, @PathVariable (name = "memberId") Long memberId) throws JsonMappingException, JsonProcessingException{
        log.info("get MY PAGE DETAILS MEMBERID = {}", memberId);

        Member profile = myPageService.getMember(memberId);
        
        model.addAttribute("profile", profile);

        int endIndex = 0;

        List<TmdbLike> getMovieLikeList = featureService.getLikedList(profile, "movie");

        endIndex = Math.min(4, getMovieLikeList.size());

        getMovieLikeList = getMovieLikeList.subList(0, endIndex);

        List<MypageDTO> movieLikedList = new ArrayList<>();

        for(TmdbLike movie : getMovieLikeList){
            MypageDTO mypageDTO = new MypageDTO();
            MovieDetailsDto movieDetailsDto = movieApiUtil.getMovieDetails(movie.getTmdbId());
            mypageDTO.setImagePath(movieDetailsDto.getPosterPath());
            mypageDTO.setName(movieDetailsDto.getTitle());
            mypageDTO.setId(movieDetailsDto.getId());

            movieLikedList.add(mypageDTO);
        }

        model.addAttribute("favMovies" , movieLikedList);

        List<TmdbLike> getTvLikedList = featureService.getLikedList(profile, "tv");

        endIndex = Math.min(4, getTvLikedList.size());

        getTvLikedList = getTvLikedList.subList(0, endIndex);

        List<MypageDTO> tvLikedList = new ArrayList<>();

        for(TmdbLike tv : getTvLikedList) {
            MypageDTO mypageDTO = new MypageDTO();
            TvShowDTO tvShowDTO = tvShowApiUtil.getTvShowDetails(tv.getTmdbId());
            mypageDTO.setImagePath(tvShowDTO.getPoster_path());
            mypageDTO.setName(tvShowDTO.getName());
            mypageDTO.setId(tvShowDTO.getId());

            tvLikedList.add(mypageDTO);
        }

        model.addAttribute("favTv", tvLikedList);

        List<TmdbLike> getPersonLikedList = featureService.getLikedList(profile, "person");

        endIndex = Math.min(4, getPersonLikedList.size());

        getPersonLikedList = getPersonLikedList.subList(0, endIndex);

        List<MypageDTO> personLikedList = new ArrayList<>();

        for(TmdbLike person : getPersonLikedList) {
            MypageDTO mypageDTO = new MypageDTO();
            DetailsPersonDto detailsPersonDto = personService.getPersonDetailsEnUS(person.getTmdbId());
            mypageDTO.setImagePath(detailsPersonDto.getProfilePath());
            mypageDTO.setName(detailsPersonDto.getName());
            mypageDTO.setId(detailsPersonDto.getId());

            personLikedList.add(mypageDTO);
        }

        model.addAttribute("favPerson" , personLikedList);

        List<Review> targetUserReviewList = featureService.getAllMyReview(memberId);

        double sumRating = 0;

        for(Review userReview : targetUserReviewList) {
            log.info(" 타겟 유저의 리뷰 평점 = {}" ,userReview.getRating());
            sumRating += userReview.getRating();
        }

        double averageRating = sumRating/targetUserReviewList.size();

        String ratingComment = "";

        if(0D < averageRating && averageRating <= 1D ){
            ratingComment = "괜찮은 영화가 있긴 한가요..?";
        } else if (1D < averageRating && averageRating <= 2D ){
            ratingComment = "굉장히 깐깐하시군요!!";
        } else if (2D < averageRating && averageRating <= 3D){
            ratingComment = "신중한 타입..!";
        } else if (3D < averageRating && averageRating <= 4D ){
            ratingComment = "영화를 즐기는 자";
        } else {
            ratingComment = "모든 영화를 좋아하시는군요 !!!";
        }

        model.addAttribute("reviewList",targetUserReviewList);
        model.addAttribute("ratingComment", ratingComment);
        model.addAttribute("averageRating" , averageRating);

        List<Review> getRecentlyReview = featureService.recentReview(memberId);

        try {
            Review recentReview = getRecentlyReview.get(0);

            MypageDTO mypageDTO = new MypageDTO();

            switch (recentReview.getCategory()) {
                case "movie" :
                    MovieDetailsDto movieDetailsDto = movieApiUtil.getMovieDetails(recentReview.getTmdbId());
                    mypageDTO.setName(movieDetailsDto.getTitle());
                    mypageDTO.setBackdropPath(movieDetailsDto.getBackdropPath());
                    mypageDTO.setCategory(recentReview.getCategory());
                    mypageDTO.setId(movieDetailsDto.getId());
                    break;
                case "tv":
                    TvShowDTO tvShowDTO = tvShowApiUtil.getTvShowDetails(recentReview.getTmdbId());
                    mypageDTO.setName(tvShowDTO.getName());
                    mypageDTO.setBackdropPath(tvShowDTO.getBackdrop_path());
                    mypageDTO.setCategory(recentReview.getCategory());
                    mypageDTO.setId(tvShowDTO.getId());
                    break;
                default:
                    log.error("없어요!!");
                    break;
            }
            model.addAttribute("recentReviewInfo", mypageDTO);
            model.addAttribute("recentReview" , getRecentlyReview.get(0));
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("recentReviewInfo", null);
            model.addAttribute("recentReview", null);
        }

        int postCount = featureService.getPostCount(memberId);

        model.addAttribute("postCount", postCount);

        int playListCount = featureService.getPlaylist(memberId).size();

        model.addAttribute("playListCount", playListCount);

        return "mypage/details-profile";
    }
    
    @GetMapping("/details/{id}/management")
    public String getManagementDetails(@RequestParam(name = "p", defaultValue = "0") int p,Model model, @PathVariable (name = "id") String email){
        log.info("get MY PAGE DETAILS USER EMAIL = {}", email);

		Page<Member> data = memberservice.getmemberlist(p);
	    model.addAttribute("data",data);
	    model.addAttribute("adminuser",email);

        return "mypage/admin";
    }
    
    @GetMapping("/details/{id}/edit")
    public String mypageedit() {
    	
    	return "mypage/edit";
    }

    
    @GetMapping("/details/{id}/admindetail/{email}")
    public String adminDetailPage(@PathVariable("id") String adminuser,
                                  @PathVariable("email") String email,
                                  Model model, HttpSession session) {
    	
        Member member = memberservice.getmemberdetail(email);
        model.addAttribute("members",member);  
        session.setAttribute("adminuser", adminuser);
        
        return "mypage/admindetail";
    	
    };
    
    @PostMapping("/myedit/update")
    public String updateUser(@ModelAttribute MemberModifyDto dto, HttpSession session, HttpServletRequest request) throws IllegalStateException, IOException {
        // 여기서 비밀번호를 비교하고 처리하면 됩니다.		 		
//		String rootDirectory = File.listRoots()[0].getAbsolutePath();
//		log.info("rootDirectory = {}", rootDirectory);
//		String sDirectory = rootDirectory + "ojng" + File.separator + "image";
		
    	String sDirectory = request.getServletContext().getRealPath("");
    	
    	
    	
    	memberservice.update(dto, sDirectory);
		
		   // 세션에서 adminuser 가져오기
        long adminUserFromSession = myname.getMember().getMemberId();

        // 리다이렉트할 URL을 생성
        String redirectUrl = "/mypage/details/" + adminUserFromSession + "/edit";
		
        return "redirect:" + redirectUrl;

    }
    
    @PostMapping("/detail/update")
    public String updateadmin(@ModelAttribute MemberModifyDto dto, HttpSession session, HttpServletRequest request) throws IllegalStateException, IOException {
    	String sDirectory = request.getServletContext().getRealPath("");
    	
    	
    	
    	memberservice.update(dto, sDirectory);
        
        // 세션에서 adminuser 가져오기
        String adminUserFromSession = (String) session.getAttribute("adminuser");

        // 리다이렉트할 URL을 생성
        String redirectUrl = "/mypage/details/" + adminUserFromSession + "/management";

        // 리다이렉트
        return "redirect:" + redirectUrl;
    }
    
    @GetMapping("/admindelete")
    public String admindelete(@RequestParam(name = "email") String email, HttpSession session) {
       
        
        memberservice.deleteByEmail(email);
        
        // 세션에서 adminuser 가져오기
        String adminUserFromSession = (String) session.getAttribute("adminuser");

        // 리다이렉트할 URL을 생성
        String redirectUrl = "/mypage/details/" + adminUserFromSession + "/management";

        // 리다이렉트
        return "redirect:" + redirectUrl;
    }
    
    
    @GetMapping("/delete")
    public String delete(@RequestParam(name = "email") String email) {
       
        
        memberservice.deleteByEmail(email);
        
        return "redirect:/logout";
    }
    
   
 
	  @GetMapping("/details/{id}/search")
	    public String search(@ModelAttribute MemberSearchDto dto, @PathVariable("id") String adminuser, Model model) {
	        log.info("search(dto={})", dto);
	        
	        // Service 메서드 호출 -> 검색 결과 -> Model -> View
	        Page<Member> data = memberservice.search(dto);
	        model.addAttribute("data", data);
	        model.addAttribute("adminuser",adminuser);
	        log.info("data={}",data);
	        
	        return "mypage/search";
	    };
    
	    @GetMapping("/details/{memberId}/reviews")
	    public String getReviews(Model model, @PathVariable( name = "memberId") Long memberId) throws JsonMappingException, JsonProcessingException{

        List<Review> myAllReview =  featureService.getAllMyReview(memberId);

        model.addAttribute("myAllReview", myAllReview);

        List<CombineReviewDTO> combineInfoList = new ArrayList<>();

        Map<Long, Integer> reviewComment = new HashMap<>();
        Map<Long, Long> reviewLiked = new HashMap<>();

        for(Review myReview : myAllReview) {
            int tmdb_id = myReview.getTmdbId();

            Long reviewId = myReview.getReviewId();

            int numOfComment = featureService.getNumOfReviewComment(reviewId);
            Long numOfLiked = featureService.getNumOfReviewLike(reviewId);

            reviewLiked.put(reviewId, numOfLiked);
            reviewComment.put(reviewId, numOfComment);

            String category = myReview.getCategory();

            CombineReviewDTO combineReviewDTO = new CombineReviewDTO();
            switch (category) {
                case "tv":
                    TvShowDTO tvShowDTO = tvShowApiUtil.getTvShowDetails(tmdb_id);

                    combineReviewDTO.setId(tvShowDTO.getId());
                    combineReviewDTO.setName(tvShowDTO.getName());
                    combineReviewDTO.setCategory(category);
                    combineReviewDTO.setPosterPath(tvShowDTO.getPoster_path());

                    combineInfoList.add(combineReviewDTO);

                    continue;
                case "movie":
                    MovieDetailsDto movieDTO = movieApiUtil.getMovieDetails(tmdb_id);

                    combineReviewDTO.setId(movieDTO.getId());
                    combineReviewDTO.setName(movieDTO.getTitle());
                    combineReviewDTO.setCategory(category);
                    combineReviewDTO.setPosterPath(movieDTO.getPosterPath());

                    combineInfoList.add(combineReviewDTO);

                    continue;
                default:
                    log.info("NOPE");
            }
        }

        model.addAttribute("numOfReviewLiked", reviewLiked);
        model.addAttribute("numOfReviewComment", reviewComment);

        log.info("COMBINE LIST = {} ",combineInfoList);

        model.addAttribute("combineInfoList" , combineInfoList);

        return "mypage/details-review-list";
    }
	    
	/**
	 * memberId에 해당하는 유저의 playlist로 가는 컨트롤러 메서드    
	 * @param memberId
	 * @param model
	 * @return
	 */
    @GetMapping("/details/{memberId}/playlist")
    public String getPlaylists(@PathVariable(name = "memberId") Long memberId, Model model) {
    	log.info("getPlaylists(memberId={})", memberId);
    	
    	List<PlaylistDto> playlistDtoList = featureService.getPlaylist(memberId);
    	
    	
    	// 로그인한 유저
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String email = authentication.getName();		
		Member signedInUser = memberservice.getmemberdetail(email);
		
		Member myPageUser = memberservice.getMemberByMemberId(memberId);
		
		// 여기서 public playlist가 없으면 isPlaylistEmpty에 true
		List<PlaylistDto> publicPlaylistList = playlistDtoList.stream().filter((playlist) -> playlist.getIsPrivate().equals("N")).toList();
		log.info("플레이리스트 공개 개수 = {}", playlistDtoList.size());
		
		// 플레이리스트 공개, 비공개 설정에 따라 보여줄 플레이리스트가 없으면 true, 있으면 false
		
		boolean isPlaylistEmpty = false;
		
		if (signedInUser != null && myPageUser.getMemberId() == signedInUser.getMemberId()) {	// my page가 로그인한 유저의 마이페이지인 경우
			
			if (playlistDtoList.size() == 0) {
				isPlaylistEmpty = true;
			}
			
		} else {	// 로그인을 안했거나 로그인한 유저의 마이페이직 아닌 경우
			
			if (publicPlaylistList.size() == 0) {
				isPlaylistEmpty = true;
			}
			
		}
		
		
		
		
		model.addAttribute("myPageUser", myPageUser);
    	model.addAttribute("signedInUser", signedInUser);
    	model.addAttribute("playlistDtoList", playlistDtoList);
    	model.addAttribute("isPlaylistEmpty", isPlaylistEmpty);

    	return "mypage/details-playlist";
    }
	
    
    @GetMapping("/details/{memberId}/playlist/like-list")
    public String getLikedPlaylists(@PathVariable(name = "memberId") Long memberId, Model model) {
    	log.info("getLikedPlaylists(memberId={})", memberId);
    	
    	List<PlaylistDto> likedPlaylistDtoList = featureService.getLikedPlaylist(memberId);
    	Member myPageUser = memberservice.getMemberByMemberId(memberId);
    	
    	boolean isPlaylistEmpty = likedPlaylistDtoList.isEmpty();
    	
    	model.addAttribute("isPlaylistEmpty", isPlaylistEmpty);
		model.addAttribute("myPageUser", myPageUser);    	
    	model.addAttribute("playlistDtoList", likedPlaylistDtoList);
    	model.addAttribute("likedPlaylistPage", "likedPlaylistPage");
    	
    	return "mypage/details-playlist";
    }
    
    
    /**
     * playlistId에 해당하는 플레이리스트의 디테일 페이지
     * @param memberId
     * @param playlistId
     * @param model
     * @return
     * @throws JsonProcessingException 
     * @throws JsonMappingException 
     */
    @GetMapping("/details/{memberId}/playlist/{playlistId}")
    public String getPlaylistDetails(@PathVariable(name = "memberId") Long memberId, @PathVariable(name = "playlistId") Long playlistId, Model model) throws JsonMappingException, JsonProcessingException {
    	log.info("getPlaylistsDetails(memberId={}, playlistId={})", memberId, playlistId);
    	
    	// 플레이리스트 가져옴
    	Playlist playlist = featureService.getPlaylistByPlaylistId(playlistId);
    	// 플레이리스트에 속한 아이템들 가져옴
    	List<PlaylistItemDto> playlistItemDtoList = featureService.getItemsInPlaylist(playlistId);
    	
    	// 마이페이지 주인 가져옴
    	Member myPageUser = memberservice.getMemberByMemberId(memberId);
    	
    	// 로그인한 사람 가져옴 없을 경우 null임
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String email = authentication.getName();
		Member signedInUser = null;
    	
		if (!email.equals("anonymousUser")) {
			signedInUser = memberservice.getmemberdetail(email);			
		}
		
		
		if(playlist.getIsPrivate().equals("Y")) {	//만약 private 플레이리스트일 경우
			// private 플레이리스트이고, 로그인을 안했거나, 로그인한 유저가 my page의 주인이 아니라면
			if (signedInUser == null || (signedInUser != null && !signedInUser.getEmail().equals(myPageUser.getEmail()))) {
				
				model.addAttribute("message", "비밀 플레이리스트입니다. 마이페이지 주인만 접근 가능 합니다.");
				return "alert";
			}
		}
		
    	model.addAttribute("myPageUser", myPageUser);
    	model.addAttribute("playlist", playlist);
    	model.addAttribute("playlistItemDtoList", playlistItemDtoList);
    	
    	return "mypage/details-playlist-items";
    }
    
    @GetMapping("/details/{memberId}/{category}")
    public String getLikedList(Model model, @PathVariable(name = "memberId") Long memberId, @PathVariable(name = "category") String category) throws JsonMappingException, JsonProcessingException{
        log.info("GET LIKED LIST - MEMBERID = {}, CATEGORY = {}", memberId, category);

        Member member = Member.builder().memberId(memberId).build();

        List<TmdbLike> likedList = featureService.getLikedList(member, category);
        List<MypageDTO> myPageLikedList = new ArrayList<>();

        for (TmdbLike works : likedList) {
            MypageDTO mypageDTO = new MypageDTO(); // 각 반복에서 새로운 객체 생성

            switch (category) {
                case "movie":
                    MovieDetailsDto movieDto = movieApiUtil.getMovieDetails(works.getTmdbId());
                    log.info("movieDTO = {}", movieDto);
                    mypageDTO.setId(movieDto.getId());
                    mypageDTO.setName(movieDto.getTitle());
                    mypageDTO.setCategory(category);
                    mypageDTO.setImagePath(movieDto.getPosterPath());

                    myPageLikedList.add(mypageDTO);

                    continue;

                case "tv":
                    TvShowDTO tvShowDTO = tvShowApiUtil.getTvShowDetails(works.getTmdbId());
                    mypageDTO.setId(tvShowDTO.getId());
                    mypageDTO.setName(tvShowDTO.getName());
                    mypageDTO.setCategory(category);
                    mypageDTO.setImagePath(tvShowDTO.getPoster_path());

                    myPageLikedList.add(mypageDTO);

                    continue;

                case "person":
                    DetailsPersonDto personDto = personService.getPersonDetailsEnUS(works.getTmdbId());
                    mypageDTO.setId(personDto.getId());
                    mypageDTO.setName(personDto.getName());
                    mypageDTO.setCategory(category);
                    mypageDTO.setImagePath(personDto.getProfilePath());

                    myPageLikedList.add(mypageDTO);

                    continue;

                default:
                    log.info("없어요!!!");
                    break;
            }
        }

        log.info("LIKED LIST = {}", myPageLikedList);
        model.addAttribute("category" , category);
        model.addAttribute("likedList", myPageLikedList);

        return "mypage/details-liked-list";
    }

    @GetMapping("/details/{memberId}/followers")
    public String followerPage(Model model, @PathVariable(name = "memberId") Long memberId, @RequestParam(name = "page", required = false, defaultValue = "0") int page){
        log.info("get Follwers Page Member Id = {}", memberId);

        Page<Follow> followPage = followService.getFollowPage(memberId, page);

        model.addAttribute("followers", followPage);

        return "mypage/details-social-list";
    }

    @GetMapping("/details/{memberId}/followings")
    public String followingsPage(Model model, @PathVariable(name = "memberId") Long memberId, @RequestParam(name = "page", required = false, defaultValue = "0") int page){
        log.info("get Follwers Page Member Id = {}", memberId);

        Page<Follow> followingPage = followService.getFollowingPage(memberId ,page);

        model.addAttribute("followings", followingPage);

        return "mypage/details-social-list";
    }


}
