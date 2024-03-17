package com.itwill.teamfourmen.web;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.itwill.teamfourmen.domain.Comment;
import com.itwill.teamfourmen.domain.EmailProvider;
import com.itwill.teamfourmen.domain.Member;
import com.itwill.teamfourmen.domain.NicknameInterceptor;
import com.itwill.teamfourmen.domain.PhonemessageProvider;
import com.itwill.teamfourmen.domain.Post;
import com.itwill.teamfourmen.domain.Postmain;
import com.itwill.teamfourmen.domain.Review;
import com.itwill.teamfourmen.domain.ReviewComments;
import com.itwill.teamfourmen.domain.Reviewmain;
import com.itwill.teamfourmen.dto.MemberCreateDto;
import com.itwill.teamfourmen.dto.MemberCreateNaverDto;
import com.itwill.teamfourmen.dto.MemberModifyDto;
import com.itwill.teamfourmen.dto.MemberSearchDto;
import com.itwill.teamfourmen.dto.loginDto;
import com.itwill.teamfourmen.dto.movie.MovieCombinedDto;
import com.itwill.teamfourmen.dto.movie.MovieGenreDto;
import com.itwill.teamfourmen.dto.movie.MovieListDto;
import com.itwill.teamfourmen.dto.movie.MovieListItemDto;
import com.itwill.teamfourmen.dto.movie.MovieQueryParamDto;
import com.itwill.teamfourmen.dto.movie.MovieVideoDto;
import com.itwill.teamfourmen.dto.person.CombinedCreditsCastDto;
import com.itwill.teamfourmen.dto.person.CombinedCreditsDto;
import com.itwill.teamfourmen.dto.person.DetailsPersonDto;
import com.itwill.teamfourmen.dto.person.MainCombinedActor;
import com.itwill.teamfourmen.dto.person.PageAndListDto;
import com.itwill.teamfourmen.dto.person.PopularPersonDto;
import com.itwill.teamfourmen.dto.tvshow.CombinedDto;
import com.itwill.teamfourmen.dto.tvshow.TvShowDTO;
import com.itwill.teamfourmen.dto.tvshow.TvShowListDTO;
import com.itwill.teamfourmen.dto.tvshow.TvShowVideoDTO;
import com.itwill.teamfourmen.dto.tvshow.TvShowVideoListDTO;
import com.itwill.teamfourmen.repository.PostRepository;
import com.itwill.teamfourmen.repository.ReviewDao;
import com.itwill.teamfourmen.service.HomeService;
import com.itwill.teamfourmen.service.ImdbRatingUtil;
import com.itwill.teamfourmen.service.MemberService;
import com.itwill.teamfourmen.service.MovieApiUtil;
import com.itwill.teamfourmen.service.PersonService;
import com.itwill.teamfourmen.service.TvShowApiUtil;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;

@Slf4j
@Controller
@RequiredArgsConstructor
public class HomeController {
	@Value("${tmdb.api-key}")
	private String API_KEY;
	private final HomeService homeservice;
	private final EmailProvider emailprovider;
	private final PhonemessageProvider phoneprovider;
	private final MemberService memberservice;
	private final MemberCreateDto membercreateDto;
	private final MemberCreateNaverDto membercreatenaverdto;
	private final PasswordEncoder passwordEncoder;
	private final NicknameInterceptor interceptor;
	private final ReviewDao reviewdao;
	private final PostRepository postdao;
	private final MovieApiUtil movieapiUtil;
	private final TvShowApiUtil apiUtil;

	private final ImdbRatingUtil imdbRatingUtil;
	private final PersonService personService;
		
	private String category = "tv";
	
	@GetMapping("/")
	public String home(Model model) throws JsonMappingException, JsonProcessingException {
		//log.info("HOME()");
		//log.info("listDto = {}", listDTO);

//		model.addAttribute("listDTO", listDTO);

		//log.info("TOTALPAGES = {}", listDTO.getTotal_pages());

		List<TvShowDTO> tvShowDto = new ArrayList<>();
		
		for (int week = 1; week <= 3; week++) {
		    TvShowListDTO listDTO = apiUtil.getTrendTvShowList("week", week);
		    List<TvShowDTO> tvShowDt = listDTO.getResults();
		    tvShowDto.addAll(tvShowDt);
		}
		
		List<CombinedDto> combineList = new ArrayList<>();
		
		// 각 TV 쇼에 대해 동영상을 가져오고, 모든 동영상을 하나의 리스트에 추가
		for (TvShowDTO tvShow : tvShowDto) {
			CombinedDto combined = new CombinedDto();
			//combined.setTvShowDto(tvShow);
		    TvShowVideoListDTO tvShowVideoDTOList = apiUtil.getTvShowVideo(tvShow.getId());
		    List<TvShowVideoDTO> tvShowTrailerList = tvShowVideoDTOList.getResults();
		   
		    if (!tvShowTrailerList.isEmpty()) {
		        TvShowVideoDTO firstVideo = tvShowTrailerList.get(0);
		        if(firstVideo.getKey() != null) {
		        	combined.setTvShowDto(tvShow);
		        	combined.setKey(firstVideo.getKey());
		        	combineList.add(combined); 
		        }
		       
		    }    	
		       
		}
		    
		
		
//			log.info("combined={}",combineList);
			log.info("combinedsisize={}",combineList.size());

		
		model.addAttribute("combineList",combineList);
		
		
		List<MovieListItemDto> movielist = new ArrayList<>();

		// 3 페이지까지 반복해서 영화 목록을 가져와서 리스트에 추가
		for (int page = 1; page <= 3; page++) {
		    // MovieQueryParamDto 설정
		    MovieQueryParamDto paramDto = new MovieQueryParamDto();
		    paramDto.setListCategory("popular");
		    paramDto.setPage(page);
		    
		    // 영화 목록 가져오기
		    MovieListDto movieListDto = movieapiUtil.getMovieList(paramDto);
		    
		    // 현재 페이지의 영화 목록 가져오기
		    List<MovieListItemDto> movielistt = movieListDto.getResults();
		    
		    // 현재 페이지의 영화 목록을 전체 영화 리스트에 추가
		    movielist.addAll(movielistt);
		}
		
//		log.info("movielist={}",movielist);

		
		List<MovieCombinedDto> moviecombineList = new ArrayList<>();
		
		// 각 TV 쇼에 대해 동영상을 가져오고, 모든 동영상을 하나의 리스트에 추가
		for (MovieListItemDto movie : movielist) {
			MovieCombinedDto moviecombined = new MovieCombinedDto();		    
			List<MovieVideoDto> MovieVideoDTOList = movieapiUtil.getMovieVideoList(movie.getId());

		    if (!MovieVideoDTOList.isEmpty()) {
		    	String firstVideo = MovieVideoDTOList.get(0).getKey();

		    	//log.info(" key={}", firstVideo);
		    	 if(firstVideo != null) {
		    		 moviecombined.setMovielistitemdto(movie);
			        	moviecombined.setKey(firstVideo);
			            moviecombineList.add(moviecombined); 
			        }
		        
		    }    	
		   
		}
		
//		log.info(" moviecombineList={}", moviecombineList);
		log.info(" moviecombineListsszie={}", moviecombineList.size());

		model.addAttribute("moviecombineList", moviecombineList);

		MovieQueryParamDto paramDtoo = new MovieQueryParamDto();
		paramDtoo.setListCategory("top_rated");
		
		MovieListDto movielistDtoo = movieapiUtil.getMovieList(paramDtoo);
		//log.info("listDto={}", listDto);		
		
		 List<MovieListItemDto> topmovielist =movielistDtoo.getResults();
		
//		log.info("topmovielist={}",topmovielist);

		model.addAttribute("topmovielist", topmovielist);
		
		TvShowListDTO toplistDTO = apiUtil.getTvShowList("top_rated", 1);


		List<TvShowDTO> toptvShowDto = toplistDTO.getResults();
		
		
		model.addAttribute("toptvShowDto",toptvShowDto);
		
		PageAndListDto pageAndListDtoEnUS = personService.getPersonListEnUS(1);
		List<PopularPersonDto> popularactor = pageAndListDtoEnUS.getResults();
		
		List<MainCombinedActor> actorlist = new ArrayList<>();
		
		// 각 TV 쇼에 대해 동영상을 가져오고, 모든 동영상을 하나의 리스트에 추가
		for (PopularPersonDto actor : popularactor) {
			MainCombinedActor actorcombined = new MainCombinedActor();		    
			DetailsPersonDto detailsPersonDtoEnUS = personService.getPersonDetailsEnUS(actor.getId());
	
		    if (detailsPersonDtoEnUS != null) {
		    	String birthday = detailsPersonDtoEnUS.getBirthday();
		    	String placeOfBirth =detailsPersonDtoEnUS.getPlaceOfBirth();
		    	//log.info(" key={}", firstVideo);
		    	 if(actor.getProfilePath() != null) {
		    		actorcombined.setActorinfo(actor);
		    		actorcombined.setBirthday(birthday);
		    		actorcombined.setPlaceOfBirth(placeOfBirth);
		    		actorlist.add(actorcombined); 
			        }
		        
		    }    	
		   
		}
		
	
		model.addAttribute("actorlist",actorlist);	
		
		
		  Random random = new Random();
	        int randomIndex = random.nextInt(popularactor.size());
	        
	        // 랜덤 인덱스를 사용하여 PopularPersonDto 선택
	        PopularPersonDto randomActor = popularactor.get(randomIndex);
	       // log.info("radomactor={}",randomActor.getId());

    	Set<String> uniqueCastPosterPath = new HashSet<>();

			
			CombinedCreditsDto combinedCreditsDtoKoKR = personService.getCombinedCreditsKoKR(randomActor.getId());
			model.addAttribute("randomactorname",randomActor.getName());
			model.addAttribute("randomactorphoto",randomActor.getProfilePath());
			List<CombinedCreditsCastDto> castListKoKR = combinedCreditsDtoKoKR.getCast();
			List<CombinedCreditsCastDto> sortedCastListKoKR = new ArrayList<>(castListKoKR);
			sortedCastListKoKR.sort(Comparator.comparingDouble(CombinedCreditsCastDto::getVoteCount).reversed());
			List<CombinedCreditsCastDto> uniqueCastListPosterKoKR = castListKoKR .stream()
					.filter(cast -> cast.getBackdropPath() != null) // posterPath가 null이 아닌 경우만 필터링합니다.
			        .filter(cast -> uniqueCastPosterPath.add(cast.getBackdropPath())) // 중복되지 않는 posterPath만 선택합니다.
			        .limit(30) // 최대 30개의 항목을 선택합니다.
			        .collect(Collectors.toList());
			// 필터링한 Cast 를 voteCount 기준 내림차순 정렬.
			
			uniqueCastListPosterKoKR.sort(Comparator.comparingDouble(CombinedCreditsCastDto::getVoteCount).reversed());
			//log.info("listdfsfd={}",uniqueCastListPosterKoKR);
			model.addAttribute("randomactor",uniqueCastListPosterKoKR);
			
			
			  Random randomx = new Random();
		        int randomIndexx = randomx.nextInt(popularactor.size());
		        while (randomIndexx == randomIndex) {
		            randomIndexx = random.nextInt(popularactor.size());
		        }

		        
		        // 랜덤 인덱스를 사용하여 PopularPersonDto 선택
		        PopularPersonDto randomActorx = popularactor.get(randomIndexx);
		        //log.info("radomactor={}",randomActorx.getId());
		        
	        
	    	Set<String> uniqueCastPosterPathx = new HashSet<>();

				
				CombinedCreditsDto combinedCreditsDtoKoKRx = personService.getCombinedCreditsKoKR(randomActorx.getId());
				model.addAttribute("randomactornamex",randomActorx.getName());
				model.addAttribute("randomactorphotox",randomActorx.getProfilePath());
				List<CombinedCreditsCastDto> castListKoKRx = combinedCreditsDtoKoKRx.getCast();
				List<CombinedCreditsCastDto> sortedCastListKoKRx = new ArrayList<>(castListKoKRx);
				sortedCastListKoKRx.sort(Comparator.comparingDouble(CombinedCreditsCastDto::getVoteCount).reversed());
				List<CombinedCreditsCastDto> uniqueCastListPosterKoKRx = castListKoKRx .stream()
						.filter(castx -> castx.getBackdropPath() != null) // posterPath가 null이 아닌 경우만 필터링합니다.
				        .filter(castx -> uniqueCastPosterPathx.add(castx.getBackdropPath())) // 중복되지 않는 posterPath만 선택합니다.
				        .limit(30) // 최대 30개의 항목을 선택합니다.
				        .collect(Collectors.toList());
				// 필터링한 Cast 를 voteCount 기준 내림차순 정렬.
				
				uniqueCastListPosterKoKRx.sort(Comparator.comparingDouble(CombinedCreditsCastDto::getVoteCount).reversed());
				//log.info("listdfsfd={}",uniqueCastListPosterKoKRx);
				model.addAttribute("randomactorx",uniqueCastListPosterKoKRx);
				
				// 리뷰를 저장할 리스트
				List<Review> allReviews = reviewdao.findAll();

				List<Reviewmain> reviewmainlist  = new ArrayList<>();
				
				
				for (Review review : allReviews) {
					Reviewmain reviewmain = new Reviewmain();
					String content = review.getContent();
					long reviewid = review.getReviewId();
					String category = review.getCategory();
				    List<ReviewComments> comments = review.getReviewComments();
				    int size = comments.size();
				    reviewmain.setContent(content);
				    reviewmain.setReviewid(reviewid);
				    reviewmain.setCategory(category);			    reviewmain.setSize(size);
		    		reviewmainlist.add(reviewmain);
				
				}
				
				reviewmainlist.sort(Comparator.comparingInt(Reviewmain::getSize).reversed());
				model.addAttribute("reviewmainlist",reviewmainlist);
				
				
				// 리뷰를 저장할 리스트
				List<Post> allposts = postdao.findAll();

				List<Postmain> postmainlist  = new ArrayList<>();
				
				
				for (Post post : allposts) {
					Postmain postmain = new Postmain();
					long postid = post.getPostId();
					String postcontent = post.getTitle();
					String postcategory = post.getCategory();
				    List<Comment> postcomment = post.getComment();
				    int postsize = postcomment.size();
				    postmain.setPostid(postid);
				    postmain.setContent(postcontent);
				    postmain.setCategory(postcategory);
				    postmain.setSize(postsize);
		    		postmainlist.add(postmain);
				
				}
				
				postmainlist.sort(Comparator.comparingInt(Postmain::getSize).reversed());
				model.addAttribute("postmainlist",postmainlist);
				
				
		return "index";
	}
	



	
	

	@GetMapping("/login")
	public void login() {
		log.info("HOME()");

	        
	}

	@GetMapping("/login/email/{email}")
	@ResponseBody
	public ResponseEntity<String> checkNickname(@PathVariable("email") String email) {
		log.info(email);
		String certificationNumber = "";

		for (int count = 0; count < 4; count++) {
			certificationNumber += (int) (Math.random() * 10);

		}

		log.info(certificationNumber);

		boolean result = emailprovider.sendCertificationMail(email, certificationNumber);

		if (result) {
			return ResponseEntity.ok(certificationNumber);
		} else {
			return ResponseEntity.ok(certificationNumber);
		}
	}
	
	@GetMapping("/login/phone/{phone}")
	@ResponseBody
	public ResponseEntity<String> sendSmsToFindEmail(@PathVariable("phone") String phone) {
		
		String certificationNumber = "";

		for (int count = 0; count < 4; count++) {
			certificationNumber += (int) (Math.random() * 10);

		}
		boolean result = phoneprovider.sendCertificationphone(phone, certificationNumber);

		if (result) {
			return ResponseEntity.ok(certificationNumber);
		} else {
			return ResponseEntity.ok(certificationNumber);
		}
    }
	

	
	
	@PostMapping("login/naver")
	@ResponseBody
	public ResponseEntity<String> postNaverLogin(@RequestBody Map<String, String> naverData) {
		membercreatenaverdto.setEmail(naverData.get("email"));
		membercreatenaverdto.setName(naverData.get("name"));
		membercreatenaverdto.setPassword(naverData.get("password"));
		membercreatenaverdto.setNickname(naverData.get("nickname"));
		membercreatenaverdto.setPhone(naverData.get("phone"));
		membercreatenaverdto.setUsersaveprofile(naverData.get("usersaveprofile"));
		membercreatenaverdto.setType(naverData.get("type"));
	    
	    String result =memberservice.createnaver(membercreatenaverdto);
	    log.debug("result={}",result);
	    
	    if (result.equals("Y")) {
	        // 값이 없는 경우 빈 문자열 반환
	        return ResponseEntity.ok("good");
	    } else {
	    	return null;
	    }
	}
	
	
	
	
		@GetMapping("/login/checknickname/{nickname}")
	    @ResponseBody
	    public ResponseEntity<String> checknamenick(@PathVariable("nickname") String nickname){
	    	boolean result = memberservice.checkNickname(nickname);
	    	if(result) {
	    		return ResponseEntity.ok("Y");
	    	} else {
	    		return ResponseEntity.ok("N");
	    	}
	    }
	
		 	
		    
		 	@GetMapping("/image")
			public ResponseEntity<Resource> getImage(@RequestParam(name= "photo") String photo) {
				log.info("photo={}",photo);
		 		String rootDirectory = File.listRoots()[0].getAbsolutePath();
			    try {

			        // 이미지 파일의 경로를 지정하여 Resource 객체 생성
//			     File file = new File("C:/image/" + photo);
//					 File file = new File( File.listRoots()[0].getAbsolutePath() + "Users/ojng/image" + File.separator + photo);

			        // 이미지 파일의 경로를 지정하여 Resource 객체 생성3
			        File file = new File(rootDirectory + "ojng" + File.separator + "image" + File.separator + photo);
			        Resource resource = new FileSystemResource(file);

			        // Resource 객체를 반환하여 이미지 파일을 클라이언트에게 전송
			        return ResponseEntity.ok()
			                .contentType(MediaType.IMAGE_JPEG) // 이미지 형식으로 전달함을 명시
			                .body(resource);
			    } catch (Exception e) {
			        // 이미지 파일이 존재하지 않는 경우 404 에러 반환
			        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
			    }
			}
		 	 
		
		@GetMapping("/login/checkemail/{email}")
	    @ResponseBody
	    public ResponseEntity<String> checkemail(@PathVariable("email") String email){
	    	boolean result = memberservice.checkEmail(email);
	    	if(result) {
	    		return ResponseEntity.ok("Y");
	    	} else {
	    		return ResponseEntity.ok("N");
	    	}
	    }
		
		@GetMapping("/login/checkphone/{phone}")
	    @ResponseBody
	    public ResponseEntity<String> checkphone(@PathVariable("phone") String phone){
	    	boolean result = memberservice.checkPhone(phone);
	    	if(result) {
	    		return ResponseEntity.ok("Y");
	    	} else {
	    		return ResponseEntity.ok("N");
	    	}
	    }
		
		@GetMapping("/login/findemail/{name}/{phone}")
	    @ResponseBody
	    public ResponseEntity<String> findemail(@PathVariable("name") String name, @PathVariable("phone") String phone){
	    	Member result = memberservice.findEmail(name,phone);
		    log.debug("result={}",result);
		    
		    if (result == null) {
		        // 값이 없는 경우 빈 문자열 반환
		        return ResponseEntity.ok("");
		    } else {
		        // 값이 있는 경우 첫 번째 결과의 reserveseat 반환
		        log.debug("result={}", result.getEmail());
		        return ResponseEntity.ok(result.getEmail());
		    }
	    }
		
		@GetMapping("/login/findpassword/{email}/{name}")
	    @ResponseBody
	    public ResponseEntity<String> findpassword(@PathVariable("email") String email, @PathVariable("name") String name){
	    	Member result = memberservice.findPassword(email, name);
		    log.debug("result={}",result);
		    
		    if (result == null) {
		        // 값이 없는 경우 빈 문자열 반환
		        return ResponseEntity.ok("");
		    } else {
		        // 값이 있는 경우 첫 번째 결과의 reserveseat 반환
		        log.debug("result={}", result);
		        return ResponseEntity.ok(result.getType());
		    }
	    }
	
	
		@GetMapping("/login/findpassword/{email}/{name}/{password}")
	    @ResponseBody
	    public ResponseEntity<String> changepassword(@PathVariable("email") String email, @PathVariable("name") String name, @PathVariable("password") String password){
	    	Member result = memberservice.changePassword(email, name,password);
		    log.debug("result={}",result);
		    
		    if (result == null) {
		        // 값이 없는 경우 빈 문자열 반환
		        return ResponseEntity.ok("");
		    } else {
		        // 값이 있는 경우 첫 번째 결과의 reserveseat 반환
		        log.debug("result={}", result);
		        return ResponseEntity.ok(result.getPassword());
		    }
	    }
	
		@GetMapping("/checkpassword/{password}")
	    @ResponseBody
	    public ResponseEntity<String> checkpassword(@PathVariable("password") String password){
			 String orginpassword= interceptor.getMember().getPassword();
			 
		    boolean result = passwordEncoder.matches(password, orginpassword);
		    
		    if (result == true) {
		        // 값이 없는 경우 빈 문자열 반환
		        return ResponseEntity.ok("Y");
		    } else {
		    	return ResponseEntity.ok("N");
		    }
	    }
		
    @PostMapping("/signup")
    public String signup(@ModelAttribute MemberCreateDto dto) {
        log.info("POST - signup(dto={})", dto);
        
        memberservice.createMember(dto);
        
        
        return "redirect:/login";
    }


    

    

    

	    

}
