package com.itwill.teamfourmen.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.html.HTMLDocument;

import org.joda.time.DateTimeZone;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.itwill.teamfourmen.config.S3Config;
import com.itwill.teamfourmen.domain.Comment;
import com.itwill.teamfourmen.domain.CommentLike;
import com.itwill.teamfourmen.domain.Member;
import com.itwill.teamfourmen.domain.MemberRepository;
import com.itwill.teamfourmen.domain.Post;
import com.itwill.teamfourmen.domain.PostImage;
import com.itwill.teamfourmen.domain.PostLike;
import com.itwill.teamfourmen.dto.board.CommentDto;
import com.itwill.teamfourmen.dto.board.PostDto;
import com.itwill.teamfourmen.dto.person.PageAndListDto;
import com.itwill.teamfourmen.dto.post.PostCreateDto;
import com.itwill.teamfourmen.repository.CommentLikeRepository;
import com.itwill.teamfourmen.repository.CommentRepository;
import com.itwill.teamfourmen.repository.PostImageRepository;
import com.itwill.teamfourmen.repository.PostLikeRepository;
import com.itwill.teamfourmen.repository.PostRepository;
import com.okta.spring.boot.oauth.env.OktaEnvironmentPostProcessorApplicationListener;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoardService {
	
	private final PostRepository postDao;
	private final PostLikeRepository postLikeDao;
	private final PostImageRepository postImageDao;
	private final CommentRepository commentDao;
	private final CommentLikeRepository commentLikeDao;
	private final MemberRepository memberDao;
	
	private final S3Config s3Config;
	
	@Value("${cloud.aws.s3.bucket}")
	private String bucketName;
	
	private int postsPerPage = 20;
	
	/**
	 * postDto를 아규먼트로 받아 게시글 작성하는 메서드
	 * @param postDto
	 */
	public Post post(PostCreateDto postDto) {
		log.info("post(postDto={})", postDto);
		
		String textContent = Jsoup.parse(postDto.getContent()).text();
		postDto.setTextContent(textContent);
		
		Post post = postDto.toEntity();
		
		Post savedPost = postDao.save(post);
		
		// 이미지만 빼네서 저장(S3에 저장된 이미지파일을 관리하기 위함)
		List<String> imageUrlList = Jsoup.parse(postDto.getContent()).select("img").eachAttr("src");		
		imageUrlList.forEach((imageUrl) -> {
			
			String regex = "https://teamfourmen-final\\.s3\\.ap-northeast-2\\.amazonaws\\.com/(images/.+)";
	        Pattern pattern = Pattern.compile(regex);
	        Matcher matcher = pattern.matcher(imageUrl);
	        
	        if (matcher.find()) {
	            String extractedValue = matcher.group(1);
	            PostImage postImage = PostImage.builder().postImage(extractedValue).post(savedPost).build();
	            postImageDao.save(postImage);
	        }
			
		});
		
		return savedPost;
	}
	
	/**
	 * postId를 아규먼트로 받아 게시글 삭제하는 메서드
	 * @param postId
	 */
	public void deletePost(Long postId) {
		log.info("deletePost(postId={})", postId);
		
		List<PostImage> postImageList = postImageDao.findAllByPostPostId(postId);
		
		postImageList.forEach((postImage) -> s3Config.amazonS3Client().deleteObject(bucketName, postImage.getPostImage()));		
		
		postDao.deleteById(postId);
	}
	
	/**
	 * 게시글을 업데이트하는 서비스 메서드
	 * @param post
	 */
	@Transactional
	public void updatePost(Post post) {
		log.info("updatePost(post={})", post);
		
		Optional<Post> postOptional = postDao.findById(post.getPostId());
		Post postToUpdate = postOptional.orElse(null);
		
		String textContent = Jsoup.parse(post.getContent()).text();
		
		postToUpdate.setTitle(post.getTitle());
		postToUpdate.setContent(post.getContent());
		postToUpdate.setTextContent(textContent);
		postToUpdate.setModifiedTime(LocalDateTime.now());
		
		// 이미지만 빼네서 저장(S3에 저장된 이미지파일을 관리하기 위함)
		List<PostImage> postImagesList = postImageDao.findAllByPostPostId(postToUpdate.getPostId()); // 기존 이미지 리스트
		
		List<String> imageUrlList = Jsoup.parse(postToUpdate.getContent()).select("img").eachAttr("src");		
		imageUrlList.forEach((imageUrl) -> {
			
			String regex = "https://teamfourmen-final\\.s3\\.ap-northeast-2\\.amazonaws\\.com/(images/.+)";
	        Pattern pattern = Pattern.compile(regex);
	        Matcher matcher = pattern.matcher(imageUrl);
	        
	        if (matcher.find()) {
	            String extractedValue = matcher.group(1);
	            
	            boolean isInTheDatabase = false;
	            
	            // 리스트에 포함돼 있지 않다면 포함시키기
	            for (PostImage eachPostImage : postImagesList) {
	            	log.info("eachPostImage={}, extractedValue={}", eachPostImage, extractedValue);
	            	if (eachPostImage.getPostImage().contains(extractedValue)) {
	            		isInTheDatabase = true;
	            	}
	            }
	            
	            if(!isInTheDatabase) {
	            	PostImage postImage = PostImage.builder().postImage(extractedValue).post(postToUpdate).build();
	            	postImageDao.save(postImage);
	            }

	            
	        }
			
		});
		
	}
	
	/**
	 * 아규먼트로 받은 카테고리의 게시물 리스트를 반환.
	 * 만약 반환받은 게시물이 없을 경우 영화, tv, 인물 게시판의 경우 빈 리스트를,
	 * 인기게시판의 경우 null을 반환함
	 * @param category
	 * @return
	 */
	public Page<PostDto> getPostList(String category, int page) {
		
		log.info("getPostList(category={}, page={}", category, page);				
		
		Page<Post> postList = null;
		
		if (!category.equals("popular")) {	// 인기게시판이 아닐 경우(영화, tv, 인물)			
			Pageable pageable = PageRequest.of(page, postsPerPage, Sort.by("postId").descending());			
			postList = postDao.findAllByCategoryOrderByCreatedTimeDesc(category, pageable);			
		} else {		// 인기 게시판일 경우
			Pageable pageable = PageRequest.of(page, 20);
			if (page >= 0 && page <= 4) {	// 1~5페이지의 경우에만 가져옴
				postList = postDao.findAllPopularPosts(pageable);
			}
		}
		
		Page<PostDto> postDtoList = postList.map((post) -> PostDto.fromEntity(post));
		
		postDtoList.forEach((postDto) -> {
			postDto.setTimeDifferenceInMinute(getMinuteDifferenceIfDateSame(postDto.getCreatedTime()));
			
			log.info("댓글개수={}", commentDao.countByPostPostId(postDto.getPostId()));
			postDto.setNumOfComments(commentDao.countByPostPostId(postDto.getPostId()));
		});
		
		log.info("postDtoList={}", postDtoList.getContent());		
		
		return postDtoList;
	}
	
	
	
	/**
	 * 검색 카테고리(제목, 제목 + 내용, 글쓴이)와 검색어를 바탕으로 Page<PostDto>타입의 검색결과를 리턴해주는 서비스 메서드
	 * @param searchCategory
	 * @param searchContent
	 * @param page
	 * @return
	 */
	public Page<PostDto> searchPost(String searchCategory, String searchContent, String boardCategory, int page) {
		log.info("searchPost(searchCategory={}, searchContent={}, boardCategory={})", searchCategory, searchContent, boardCategory);
		
		Page<Post> searchResultList = null;
		
		Pageable pageable = PageRequest.of(page, postsPerPage, Sort.by("postId").descending());
		
		switch(searchCategory) {
		
		case "title":
			 searchResultList = postDao.getSearchResultByTitle(searchContent, boardCategory, pageable);
			 log.info("검색결과={}", searchResultList);
			break;
		case "content":
			searchResultList = postDao.getSearchResultByContent(searchContent, boardCategory, pageable);
			break;
		case "titleContent":
			searchResultList = postDao.getSearchResultByTitleAndContent(searchContent, boardCategory, pageable);
			break;
		case "author":
			searchResultList = postDao.getSearchResultByAuthor(searchContent, boardCategory, pageable);
			break;
		default:
			log.info("잘못된 카테고리를 가져옴");
		}
		
		Page<PostDto> searchResultDtoList = searchResultList.map((post) -> PostDto.fromEntity(post));
		
		searchResultDtoList.forEach((postDto) -> {
			postDto.setTimeDifferenceInMinute(getMinuteDifferenceIfDateSame(postDto.getCreatedTime()));
			log.info("time difference={}", postDto.getTimeDifferenceInMinute());
		});
		
		log.info("searchResultDtoList={}", searchResultDtoList);
		
		return searchResultDtoList;
	}
	
	
	/**
	 * 인기 게시판 내에서 검색
	 * @param searchCategory
	 * @param searchContent
	 * @param page
	 * @return
	 */
	public List<PostDto> searchPopularPost(String searchCategory, String searchContent, int page) {
		
		long startingPost = (long) (postsPerPage * page);
		
		List<Post> searchResultList = null;
		
		switch(searchCategory) {
		case "title":
			searchResultList = postDao.findPopularBoardSearchByTitle(searchContent, startingPost, postsPerPage);
			break;
		case "content":
			searchResultList = postDao.findPopularBoardSearchByContent(searchContent, startingPost, postsPerPage);
			break;
		case "titleContent":
			searchResultList = postDao.findPopularBoardSearchByTitleContent(searchContent, startingPost, postsPerPage);
			break;
		case "author":			
			searchResultList = postDao.findPopularBoardSearchByAuthor(searchContent, startingPost, postsPerPage);
			break;
		default:
			log.info("잘못된 카테고리를 가져옴");
		}
		
		log.info("가져온 postList={}", searchResultList);
		
		List<PostDto> searchResultDtoList = searchResultList.stream().map((post) -> PostDto.fromEntity(post)).toList();
		
		searchResultDtoList.forEach((postDto) -> {
			postDto.setTimeDifferenceInMinute(getMinuteDifferenceIfDateSame(postDto.getCreatedTime()));
			log.info("time difference={}", postDto.getTimeDifferenceInMinute());
		});
		
		return searchResultDtoList;
	}
	
	/**
	 * 인기게시판에서 페이징 처리하지 않은 총 검색 결과를 가져오기 위한 메서드
	 * 만약 검색결과가 없을 시 빈 리스트로 리턴함
	 * @param searchCategory
	 * @param searchContent
	 * @return
	 */
	public Map<String, Integer> getPopularSearchTotalElementAndPostPerPage(String searchCategory, String searchContent) {
		
		Map<String, Integer> totElementsAndPostsPerPage = new HashMap<>(); 
		
		// 우선 페이지당 게시물 수 넣음
		totElementsAndPostsPerPage.put("postsPerPage", postsPerPage);
		
		List<Post> searchResultList = null;
		
		switch(searchCategory) {
		case "title":
			searchResultList = postDao.findAllPopularBoardSearchByTitle(searchContent);
			break;
		case "content":
			searchResultList = postDao.findAllPopularBoardSearchByContent(searchContent);
			break;
		case "titleContent":
			searchResultList = postDao.findAllPopularBoardSearchByTitleContent(searchContent);
			break;
		case "author":
			searchResultList = postDao.findAllPopularBoardSearchByAuthor(searchContent);
			break;
		default:
			log.info("잘못된 카테고리를 가져옴");
		}
		
		int totElements = searchResultList.size();
		totElementsAndPostsPerPage.put("totElements", totElements);
		
		return totElementsAndPostsPerPage;
	}
	
	
	/**
	 * id를 아규먼트로 받아, 해당 postId의 게시물을 Post 타입으로 반환함
	 * 
	 * @param id
	 * @return Post타입의 해당 postId의 게시물 객체
	 */
	public PostDto getPostDetail(Long id) {
		log.info("getPostDetail(id={})", id);
		
		Optional<Post> postDetailsOptional = postDao.findById(id);
		
		Post postDetails = postDetailsOptional.orElse(null);
		PostDto postDetailsDto = PostDto.fromEntity(postDetails);
		postDetailsDto.setTimeDifferenceInMinute(getMinuteDifferenceIfDateSame(postDetailsDto.getCreatedTime()));
		
		return postDetailsDto;
	}
	
	public PostLike haveLiked(Member signedInUser, Long postId) {
		log.info("haveLiked(signedInUser={}, postId={})", signedInUser, postId);
		
		Optional<PostLike> haveLikedOptional = postLikeDao.findByMemberEmailAndPostPostId(signedInUser.getEmail(), postId);
		PostLike haveLiked = haveLikedOptional.orElse(null);
		
		return haveLiked;
	}
	
	
	/**
	 * postId에 해당하는 게시물의 조회수를 1 늘려줌
	 * @param postId
	 */
	@Transactional
	public void addView(Long postId) {
		log.info("addView(postId={})", postId);
		
		Optional<Post> thePostOptional = postDao.findById(postId);
		
		Post thePost = thePostOptional.orElse(null);
		
		Long views = thePost.getViews();
		thePost.setViews(views + 1);
	}
	
	/**
	 * postId에 해당하는 게시글이 받은 좋아요 개수 표시하기 위한 서비스 메서드
	 * @param postId
	 * @return
	 */
	public Long countLikes(Long postId) {
		log.info("countLikes(postId={})", postId);
		
		return postLikeDao.countByPostPostId(postId);			
	}
	
	/**
	 * 좋아요 추가하는 메서드
	 * @param postLike
	 * @return
	 */
	@Transactional
	public PostLike addLike(PostLike postLike) {
		log.info("addLike(postLike={})", postLike);
		
		PostLike savedPostLike = postLikeDao.save(postLike);
		
		Post likedPost = postDao.findById(postLike.getPost().getPostId()).orElse(null);
		
		Long currentLikes = likedPost.getLikes();
		likedPost.setLikes(currentLikes + 1);
		
		return savedPostLike;
	}
	
	
	/**
	 * 좋아요 취소하는 메서드
	 * @param postLike
	 */
	@Transactional
	public void deleteLike(PostLike postLike) {
		log.info("deleteLike(postLike={})", postLike);
		
		postLikeDao.deleteByMemberEmailAndPostPostId(postLike.getMember().getEmail(), postLike.getPost().getPostId());
		
		Post likeCanceledPost = postDao.findById(postLike.getPost().getPostId()).orElse(null);
		
		Long currentLikes = likeCanceledPost.getLikes();
		Long likesAfterUpdate = currentLikes - 1 >= 0 ? currentLikes - 1 : 0; // 혹시모를 비동기 에러 대비해서..
		
		likeCanceledPost.setLikes(likesAfterUpdate);
	}
	
	/**
	 * postId에 달린 모든 댓글들의 리스트를 반환(리스트는 가장 상위 댓글을 포함하며, 상위댓글 dto의 필드로 답글들이 리스트로 있음)
	 * @param postId
	 * @return postId에 달린 모든 댓글들의 리스트를 반환(리스트는 가장 상위 댓글을 포함하며, 상위댓글 dto의 필드로 답글들이 리스트로 있음)
	 */
	public List<CommentDto> getCommentList(Long postId) {
		log.info("getCommentList(postId={})", postId);
		
		List<Comment> commentList = commentDao.findByPostPostIdAndReplyToOrderByCommentIdAsc(postId, null);						
		
		List<CommentDto> commentDtoList = commentList.stream().map((comment) -> CommentDto.fromEntity(comment)).toList();
		List<CommentDto> repliesList = new ArrayList<>();
		
		commentDtoList.forEach((comment) -> {
			List<CommentLike> commentLikeList = commentLikeDao.findAllByCommentCommentId(comment.getCommentId());
			comment.setCommentLikesList(commentLikeList);
			
			// 가장 부모댓글 timeDifference설정
			Long timeDifferenceInMinute = getMinuteDifferenceIfDateSame(comment.getCreatedTime());
			comment.setTimeDifferenceInMinute(timeDifferenceInMinute);
			
			// 해당 부모댓글로부터 이어진 모든 대댓글들을 가져옴
			List<Comment> initialRepliesList = commentDao.findAllByReplyTo(comment.getCommentId());
			List<CommentDto> initialRepliesDtoList = initialRepliesList.stream().map((replyComment) -> CommentDto.fromEntity(replyComment)).toList();
			
			initialRepliesDtoList.forEach((reply) -> {
				Comment commentReplied = commentDao.findById(reply.getReplyTo()).orElse(null);
				reply.setCommentReplied(commentReplied);
				
				List<CommentLike> replyCommentLikeList = commentLikeDao.findAllByCommentCommentId(reply.getCommentId());
				reply.setCommentLikesList(replyCommentLikeList);
				
			});
			
			comment.getRepliesList().addAll(initialRepliesDtoList);
			
			for (CommentDto initialReplyCommentDto : initialRepliesDtoList) {
				Long replyCommentTimeDifferenceInMinute = getMinuteDifferenceIfDateSame(initialReplyCommentDto.getCreatedTime());
				initialReplyCommentDto.setTimeDifferenceInMinute(replyCommentTimeDifferenceInMinute);
				
				addAllRepliesToComments(comment, initialReplyCommentDto);
			}
			
			
		});
		
		// 가져온 대댓글들을 commentId순으로 정렬
		commentDtoList.forEach((commentDto) -> Collections.sort(commentDto.getRepliesList(), CommentDto.ORDER_BY_COMMENT_ID_ASC));
		
		log.info("commentDtoList={}", commentDtoList);
		
		return commentDtoList;
	}
	
	public int getNumOfComments(List<CommentDto> commentDtoList) {
		log.info("getNumOfComments(commentDtoList={})", commentDtoList);
		
		int numOfComment = 0;
		numOfComment += commentDtoList.size();
		
		for (CommentDto commentDto : commentDtoList) {
			numOfComment += commentDto.getRepliesList().size();
		}
		
		return numOfComment;
	}
	
	/**
	 * comment를 아규먼트로 받아 DB에 댓글을 등록하는 메서드. 등록한 댓글을 리턴함
	 * @param comment
	 * @return
	 */
	@Transactional
	public CommentDto addComment(Comment comment) {
		log.info("addCommnet(comment={})", comment);
		
		Comment savedComment = commentDao.save(comment);		
		log.info("saved comment = {}", savedComment);
		
		CommentDto savedCommentDto = CommentDto.fromEntity(savedComment);
		
		savedCommentDto.setCommentLikesList(commentLikeDao.findAllByCommentCommentId(savedCommentDto.getCommentId()));
		
		Member member = memberDao.findByEmail(savedComment.getMember().getEmail()).orElse(null);
		savedCommentDto.setMember(member);
		
		return savedCommentDto;
	}
	
	/**
	 * 댓글 삭제하는 메서드
	 * @param commentId
	 */
	@Transactional
	public void deleteComment(Long commentId) {
		log.info("deleteComment(commentId={})", commentId);
		
		Comment commentToDelete = commentDao.findById(commentId).orElse(null);
		
		commentToDelete.setIsDeleted("Y");
		commentToDelete.setContent("삭제된 댓글입니다.");
	}
	
	/**
	 * 댓글 좋아요 추가하는 메서드
	 * @param commentLike
	 * @return
	 */
	@Transactional
	public CommentLike addCommentLike(CommentLike commentLike) {
		log.info("addCommentLike(commentLike={})", commentLike);
		
		CommentLike savedCommentLike = commentLikeDao.save(commentLike);
		
		return savedCommentLike;
	}
	
	/**
	 * 댓글 좋아요 취소 메서드
	 * @param commentLike
	 */
	@Transactional
	public void deleteCommentLike(CommentLike commentLike) {
		log.info("deleteCommentLike(commentLike={})", commentLike);
		
		commentLikeDao.deleteByCommentCommentIdAndMemberEmail(commentLike.getComment().getCommentId(), commentLike.getMember().getEmail());
	}
	
	
	// 보조 메서드
	/**
	 * 가장 상위 댓글 Dto에 모든 답변들과 관련한 모든 List를 가져옴
	 * @param commentDto 가장 상위 댓글 Dto
	 * @param replyDto 상위댓글에 대한 직접적인 답글 CommentDto
	 */
	public void addAllRepliesToComments(CommentDto commentDto, CommentDto replyDto) {
		
		List<Comment> repliesList = commentDao.findAllByReplyTo(replyDto.getCommentId());
		List<CommentDto> repliesDtoList = repliesList.stream().map((reply) -> CommentDto.fromEntity(reply)).toList();
		
		repliesDtoList.forEach((replyComment) -> {
			List<CommentLike> commetLikesListForReply = commentLikeDao.findAllByCommentCommentId(replyComment.getCommentId());
			replyComment.setCommentLikesList(commetLikesListForReply);
			
			Comment commentReplied = commentDao.findById(replyComment.getReplyTo()).orElse(null);
			replyComment.setCommentReplied(commentReplied);
			replyComment.setTimeDifferenceInMinute(getMinuteDifferenceIfDateSame(replyComment.getCreatedTime()));	
		});
		
		commentDto.getRepliesList().addAll(repliesDtoList);
		
		
		
		for(CommentDto repliesReplyDto : repliesDtoList) {
			addAllRepliesToComments(commentDto, repliesReplyDto);
		}
	}
	
	
	/**
	 * LocalDateTime 타입의 아규먼트와 현재 시간의 차이가 24시간 이내일 경우, 시간차이를 '분'(Long 타입)으로 반환
	 * @param timeVariable
	 * @return 시간차 24시간 이내일 경우 시간차를 '분'으로 반환, 24시간 이상일 경우 null
	 */
	public Long getMinuteDifferenceIfDateSame(LocalDateTime timeVariable) {
		
		log.info(timeVariable.toString());
		
		ZoneId KoreanZone = ZoneId.of("Asia/Seoul");
//		ZonedDateTime currentTime = ZonedDateTime.now(KoreanZone);
		LocalDateTime currentTime = LocalDateTime.now();

		
		Duration duration = Duration.between(timeVariable, currentTime);
		
		Long timeDifferenceInMinute = null;
		if (duration.toMinutes() < 60 * 24) {
			timeDifferenceInMinute = duration.toMinutes();
		}
		
		return timeDifferenceInMinute;
	}
	
}	// BoardService class끝
