package com.itwill.teamfourmen.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.itwill.teamfourmen.domain.*;
import com.itwill.teamfourmen.dto.comment.ReviewCommentLikeDTO;
import com.itwill.teamfourmen.dto.movie.MovieDetailsDto;
import com.itwill.teamfourmen.dto.movie.MovieListItemDto;
import com.itwill.teamfourmen.dto.review.CombineReviewDTO;
import com.itwill.teamfourmen.dto.tvshow.TvShowDTO;
import com.itwill.teamfourmen.repository.ReviewCommentLikeRepository;
import com.itwill.teamfourmen.repository.ReviewDao;
import com.itwill.teamfourmen.repository.ReviewLikeRepository;
import com.itwill.teamfourmen.service.*;
import jakarta.persistence.Basic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewDao reviewDao;
    private final MovieApiUtil movieApiUtil;
    private final TvShowApiUtil tvShowApiUtil;
    private final ImdbRatingUtil imdbRatingUtil;
    private final CommentService commentService;
    private final FeatureService featureService;
    private final ReviewCommentLikeRepository reviewCommentLikeRepository;
    private final ReviewLikeRepository reviewLikeRepository;

    // 특정 영화 / TV 의 전체 리뷰를 불러옴
    @GetMapping("/{category}/{id}")
    public String getAllReviews(Model model,@PathVariable(name = "category") String category ,@PathVariable(name = "id") int tmdbId) throws JsonMappingException, JsonProcessingException{
        log.info("GET {} REVIEWS = {}", category, tmdbId);

        CombineReviewDTO combineReviewDTO = new CombineReviewDTO();

        switch (category) {
            case "movie" :
                MovieDetailsDto moviesInfo = movieApiUtil.getMovieDetails(tmdbId);
                combineReviewDTO.setId(moviesInfo.getId());
                combineReviewDTO.setCategory(category);
                combineReviewDTO.setName(moviesInfo.getTitle());
                combineReviewDTO.setPosterPath(moviesInfo.getPosterPath());
                break;
            case "tv" :
                TvShowDTO tvInfo = tvShowApiUtil.getTvShowDetails(tmdbId);
                combineReviewDTO.setId(tvInfo.getId());
                combineReviewDTO.setCategory(category);
                combineReviewDTO.setName(tvInfo.getName());
                combineReviewDTO.setPosterPath(tvInfo.getPoster_path());
                break;
            default:
                log.info("WRONG CATEGORY");
                break;
        }

        model.addAttribute("worksDto", combineReviewDTO);

        List<Review> worksReviewList = featureService.getReviews(category, tmdbId);

        Map<Long, Integer> reviewComment = new HashMap<>();
        Map<Long, Long> reviewLiked = new HashMap<>();

        for (Review review : worksReviewList){
            Long reviewId = review.getReviewId();

            int numOfComment = featureService.getNumOfReviewComment(reviewId);
            Long numOfLiked = featureService.getNumOfReviewLike(reviewId);

            reviewLiked.put(reviewId, numOfLiked);
            reviewComment.put(reviewId, numOfComment);
        }

        model.addAttribute("numOfReviewLiked", reviewLiked);
        model.addAttribute("numOfReviewComment", reviewComment);

        model.addAttribute("worksReviewList", worksReviewList);

        return "review/reviews";
    }

    // 리뷰 ID를 바탕으로 한가지 리뷰를 보여주는 페이지
    @GetMapping("/{review_id}")
    public String getReviewDetails(Model model, @PathVariable(name = "review_id") long reviewId) throws JsonMappingException, JsonProcessingException {
        log.info("GET REVIEW DETAILS = {}", reviewId);

        // review ID를 통해서 특정 리뷰 객체를 가져와서 view로 전달
        Review singleReview =  reviewDao.findByReviewId(reviewId);
        model.addAttribute("review", singleReview);

        // 가져온 리뷰 객체의 TMDB ID와 Category를 통해서 작품의 정보를 가져옴
        int tmdbId = singleReview.getTmdbId();
        String category = singleReview.getCategory();

        CombineReviewDTO combineReviewDTO = new CombineReviewDTO();

        String imdbId = imdbRatingUtil.getImdbId(tmdbId, category);
        combineReviewDTO.setImdbId(imdbId);

        ImdbRatings imdbRatings = imdbRatingUtil.getImdbRating(imdbId);
        combineReviewDTO.setImdbRatings(imdbRatings);

        // 가져온 카테고리를 바탕으로 movie 또는 tv의 한가지 works를 가져옴.
        switch (category) {
            case "movie" :
                MovieDetailsDto moviesInfo = movieApiUtil.getMovieDetails(tmdbId);
                combineReviewDTO.setId(moviesInfo.getId());
                combineReviewDTO.setCategory(category);
                combineReviewDTO.setName(moviesInfo.getTitle());
                combineReviewDTO.setPosterPath(moviesInfo.getPosterPath());
                LocalDate releaseDate = moviesInfo.getReleaseDate();
                int releaseYear =  releaseDate.getYear();
                combineReviewDTO.setYear(releaseYear);
                break;
            case "tv" :
                TvShowDTO tvInfo = tvShowApiUtil.getTvShowDetails(tmdbId);
                combineReviewDTO.setId(tvInfo.getId());
                combineReviewDTO.setCategory(category);
                combineReviewDTO.setName(tvInfo.getName());
                combineReviewDTO.setPosterPath(tvInfo.getPoster_path());
                String firstAirDate = tvInfo.getFirst_air_date().substring(0,4);
                log.info("불러온 첫 방영일 = {}",firstAirDate);
                combineReviewDTO.setYear(Integer.parseInt(firstAirDate));
                break;
            default:
                log.info("WRONG CATEGORY");
                break;
        }

        model.addAttribute("works", combineReviewDTO);

        Page<ReviewComments> reviewComments = commentService.getAllComments(reviewId,0);

        model.addAttribute("reviewComments", reviewComments);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Member signedInUser = Member.builder().email(email).build();

        log.info("signedInUser = {}", signedInUser.getEmail());

        Long countReviewLiked = featureService.getNumOfReviewLike(reviewId);

        model.addAttribute("countReviewLiked", countReviewLiked);

        Page<ReviewLike> likedUser = featureService.getUserWhoLikedReview(reviewId, 0);

        model.addAttribute("likedUser", likedUser.getContent());

        log.info("SIZE = {}",likedUser.getContent().size());

        boolean didUserLikedReview = featureService.didReviewLike(singleReview, signedInUser);

        model.addAttribute("likedReview", didUserLikedReview);


        if(signedInUser!=null){
            List<ReviewCommentLikeDTO> likedDTO = new ArrayList<>();

            for(ReviewComments comment : reviewComments){

                ReviewCommentLikeDTO reviewCommentLikeDTO = new ReviewCommentLikeDTO();

                Long commentId = comment.getCommentId();

                log.info("@@@@@@@@ CONTROLLER => comment ID = {}", commentId);

                boolean isLiked = commentService.didReviewCommentLike(comment, signedInUser);

                reviewCommentLikeDTO.setCommentId(commentId);
                reviewCommentLikeDTO.setLiked(isLiked);

                if(isLiked) {
                    likedDTO.add(reviewCommentLikeDTO);
                }
            }
            Map<Long, Boolean> isLikedMap = new HashMap<>();

            for(ReviewCommentLikeDTO likeDTO : likedDTO){
                log.info("commentID = {}, isLiked = {}", likeDTO.getCommentId(), likeDTO.isLiked());
                isLikedMap.put(likeDTO.getCommentId(), likeDTO.isLiked());
            }

            log.info("IS LIKED MAP = {} ",isLikedMap);

            model.addAttribute("isLIkedMap", isLikedMap);
        }

        return "review/review-details";
    }

}
