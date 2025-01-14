package com.itwill.teamfourmen.service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itwill.teamfourmen.dto.movie.MovieCreditDto;
import com.itwill.teamfourmen.dto.movie.MovieDetailsDto;
import com.itwill.teamfourmen.dto.movie.MovieExternalIdDto;
import com.itwill.teamfourmen.dto.movie.MovieQueryParamDto;
import com.itwill.teamfourmen.dto.movie.MovieReleaseDateDto;
import com.itwill.teamfourmen.dto.movie.MovieReleaseDateItemDto;
import com.itwill.teamfourmen.dto.movie.MovieGenreDto;
import com.itwill.teamfourmen.dto.movie.MovieListDto;
import com.itwill.teamfourmen.dto.movie.MovieProviderDto;
import com.itwill.teamfourmen.dto.movie.MovieProviderItemDto;
import com.itwill.teamfourmen.dto.movie.MovieVideoDto;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class MovieApiUtil {
	
	@Value("${tmdb.hd.token}")
	private String token;
	
	@Value("${tmdb.api.baseurl}")
	private String baseUrl;
	
	private WebClient client;
	
	@PostConstruct
	public void init() {
		this.client = WebClient.builder()
				.baseUrl(this.baseUrl)
				.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token)
				.defaultHeader(HttpHeaders.ACCEPT, "application/json")
			    .filter((request, next) -> {
			        System.out.println("Request: " + request.method() + " " + request.url());
			        request.headers().forEach((name, values) -> values.forEach(value -> System.out.println(name + "=" + value)));
			        return next.exchange(request);
			    })
				.build();
	}
	
	/**
	 * 영화 리스트를 MovieListDto객체로 돌려주는 메서드.
	 * 파라미터는 무조건 "popular", "now_playing", "top_rated", "upcoming", "filter" 중 하나여야 함!
	 * @param listCategory 무조건 "popular", "now_playing", "top_rated", "upcoming" 중 하나
	 * @return 받아온 json데이터를 매핑한 MovieListDto객체
	 * @throws JsonProcessingException 
	 * @throws JsonMappingException 
	 */
	public MovieListDto getMovieList(MovieQueryParamDto paramDto) throws JsonMappingException, JsonProcessingException {
//		log.info("getMovieList(param={})", paramDto);
		
		String uri = "";
		String genresVariable = null;
		String providersVariable = null;
		String watchRegionVariable = null;
//		String queryParam = "?language=ko&page=" + page;
				
		
		switch(paramDto.getListCategory()) {
		case "now_playing":
			uri = "/movie/now_playing";
			break;
			
		case "popular":
			uri = "/movie/popular";
			break;
			
		case "top_rated":
			uri = "/movie/top_rated";
			break;			
		case "upcoming":
			uri = "/movie/upcoming";
			break;
		case "filter":
			uri="/discover/movie";
			break;
		case "search":
			uri="/search/movie";
			break;
		default:
			log.info("getMovieList()에 잘못된 파라미터 입력함");
			break;			
		}
		
		String uriToPassIn = uri;
		
		// genres 파라미터 포매팅
		List<Integer> genreList = paramDto.getWithGenres();
		if (genreList != null) {
			genresVariable = genreList.stream().map((x) -> x.toString()).collect(Collectors.joining("|")).toString();
//			log.info("genres={}", genresVariable);
		}
		
		String genres = genresVariable;
		
		
		// provider 파라미터 포매팅
		List<Integer> providerList = paramDto.getWithWatchProviders();
		if (providerList != null) {
			providersVariable = providerList.stream().map((x) -> x.toString()).collect(Collectors.joining("|")).toString();
//			log.info("providers={}", providersVariable);
		}
		
		String providers = providersVariable;
			// provider필터하려면 watch region필요하기 때문에 있는경우에 추가.
		if(providers != null) {
			watchRegionVariable = "KR";
		} else {
			watchRegionVariable = null;
		}
		
		String watchRegion = watchRegionVariable;			
		
		MovieListDto movieListDto = this.client.get()
				.uri(uriBuilder -> uriBuilder
						.path(uriToPassIn)
						.queryParam("page", paramDto.getPage())
						.queryParam("language", "ko")
						.queryParam("sort_by", paramDto.getSortBy())
						.queryParam("primary_release_date.gte", paramDto.getPrimaryReleaseDateGte())
						.queryParam("primary_release_date.lte", paramDto.getPrimaryReleaseDateLte())
						.queryParam("with_original_language", paramDto.getWithOriginalLanguage())
						.queryParam("with_runtime.gte", paramDto.getWithRuntimeGte())
						.queryParam("with_runtime.lte", paramDto.getWithRuntimeLte())
						.queryParam("with_genres", genres)
						.queryParam("watch_region", watchRegion)
						.queryParam("with_watch_providers", providers)	// 고치기
						.queryParam("query", paramDto.getQuery())
						.build())						
				.retrieve()				
				.bodyToMono(MovieListDto.class)
				.block();
		
		return movieListDto;
	}
	
	
	/**
	 * 영화 장르 모음들을 가져오는 메서드
	 * @return
	 * @throws JsonMappingException
	 * @throws JsonProcessingException
	 */
	public List<MovieGenreDto> getMovieGenreList() {
		
		
		String json = this.client.get()
			.uri("/genre/movie/list?language=ko")
			.retrieve()
			.bodyToMono(String.class)
			.block();			
		
		ObjectMapper mapper = new ObjectMapper();		
		
		List<MovieGenreDto> movieGenreList = null;
		
		try {
			JsonNode array = mapper.readValue(json, JsonNode.class);
			JsonNode genres = array.get("genres");			
			MovieGenreDto[] movieGenreArray = mapper.treeToValue(genres, MovieGenreDto[].class);		
			movieGenreList = Arrays.asList(movieGenreArray);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return null;
		}
		
		
		
		return movieGenreList;
	}
	
	
	/**
	 * Movie Details Dto를 반환함.
	 * @param id details을 보고싶은 영화의 id
	 * @return id에 해당하는 영화의 detail정보를 포함한 MovieDetailsDto 반환 
	 */
	public MovieDetailsDto getMovieDetails(int id) {
		
		String queryParam = "?language=ko";
		
		WebClient client = WebClient.builder()
				.baseUrl(this.baseUrl)
				.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token)
				.defaultHeader(HttpHeaders.ACCEPT, "application/json")
				.build();
		
		MovieDetailsDto movieDetailsDto = client.get()
			.uri("/movie/" + id + queryParam)
			.retrieve()
			.bodyToMono(MovieDetailsDto.class)
			.block();
		
		 log.info("movieDetailsDto={}", movieDetailsDto);
		
		return movieDetailsDto;
	}
	
	
	/**
	 * 아규먼트로 받은 id에 해당하는 영화의 배우, 크루들의 목록을 MovieCreditDto로 반환
	 * @param id
	 * @return
	 */
	public MovieCreditDto getMovieCredit(int id) {
		
//		log.info("MovieCreditDto(id={})", id);
		
		String queryParam = "?language=ko";
				
		MovieCreditDto movieCreditDto = this.client.get()
				.uri("/movie/"+ id + "/credits")
				.retrieve()
				.bodyToMono(MovieCreditDto.class)
				.block();
		
		// log.info("movieCreditDto={}", movieCreditDto);
		
		return movieCreditDto;
	}
	
	/**
	 * 아규먼트로 받은 id에 해당하는 영화의 MovieVideoDto리스트를 줌.
	 * 트레일러 리스트만 따로 받고 싶다면 filter를 통해 MovieVideoDto의 type="Trailer"인 요소들만 걸러내면 됨
	 * 
	 * @param id
	 * @return 영화의 비디오 리스트, 없을시 null을 반환
	 */
	public List<MovieVideoDto> getMovieVideoList(int id) {
				
//		log.info("getMovieVideoList()");

		
		String queryParam = "?language=ko";
		
		String json = this.client.get()
			.uri("/movie/" + id + "/videos" + queryParam)
			.retrieve()
			.bodyToMono(String.class)
			.block();
		
		ObjectMapper mapper = new ObjectMapper();
		
		List<MovieVideoDto> movieVideoList = null;
		
		try {
			JsonNode node = mapper.readValue(json, JsonNode.class);
			JsonNode results = node.get("results");
			MovieVideoDto[] movieVideoArray = mapper.treeToValue(results, MovieVideoDto[].class);
			movieVideoList = Arrays.asList(movieVideoArray);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return null;
		}
		
		
		//log.info("movie video list = {}", movieVideoList);

		
		return movieVideoList;
	}	
	
	
	/**
	 * id에 해당하는 영화의 provider list를 반환. 해당 영화의 provider가 없을 경우 null을 반환함
	 * @param id
	 * @return List<MovieProviderDto> 해당 영화의 provider 리스트, provider가 없을 시 null을 반환
	 */
	public MovieProviderDto getMovieProviderList(int id) {
		
//		log.info("getMovieProviderList(id={})", id);

		String json = this.client.get()
			.uri("/movie/" + id + "/watch/providers")
			.retrieve()
			.bodyToMono(String.class)
			.block();
		
		ObjectMapper mapper = new ObjectMapper();
		
		List<MovieProviderItemDto> movieProviderList = null;
		
		try {
			JsonNode node = mapper.readValue(json, JsonNode.class);
			JsonNode resultsNode = node.get("results");
			JsonNode countryNode = resultsNode.get("KR");
			MovieProviderDto movieProviderDto = mapper.treeToValue(countryNode, MovieProviderDto.class);
			
//			log.info("movieProviderDto={}", movieProviderDto);
			
			return movieProviderDto;
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return null;
		}

	}
	
	
	/**
	 * collectionID에 해당하는 MovieDetailsDto타입의 collection 리스트를 리턴
	 * @param collectionId
	 * @return MovieDetailsDto타입의 collection 리스트. 중간 로직 에러시 null 반환..
	 */
	public List<MovieDetailsDto> getMovieCollectionList(int collectionId) {
		
//		log.info("getMovieCollectionList(id={})", collectionId);
		
		String queryParam = "?language=ko";
		
		JsonNode json = this.client.get()
			.uri("/collection/" + collectionId + queryParam)
			.retrieve()
			.bodyToMono(JsonNode.class)
			.block();
		
		ObjectMapper mapper = new ObjectMapper();
		
		JsonNode collectionListNode = json.get("parts");
		try {
			MovieDetailsDto[] movieCollectionArray = mapper.treeToValue(collectionListNode, MovieDetailsDto[].class);
			List<MovieDetailsDto> movieCollectionList = Arrays.asList(movieCollectionArray);
			
			return movieCollectionList;
		} catch (JsonProcessingException | IllegalArgumentException e) {			
			e.printStackTrace();
			return null;
		}
		
	}
	
	/**
	 * id에 해당하는 영화의 외부링크 접근을 위한 id를 가져옴
	 * 여기서 외부링크란 facebook, twitter 등을 의미
	 * 예) 페이스북: https://www.facebook.com/아이디 하면 해당 페이스북 페이지로 가짐
	 * @param id
	 * @return 외부링크 return 없을경우 null
	 */
	public MovieExternalIdDto getMovieExternalId(int id) {
		
		MovieExternalIdDto externalIdDto = this.client.get()
				.uri("/movie/" + id + "/external_ids")
				.retrieve()
				.bodyToMono(MovieExternalIdDto.class)
				.block();
		
		return externalIdDto;
	}
	
	
	/**
	 * id에 해당하는 영화와 관련된 추천영화 리스트 반환
	 * @param id
	 * @return
	 */
	public List<MovieDetailsDto> getRecommendedMovie(int id) {
		
//		log.info("getRecommendedMovie(id={})", id);
		
		String queryParam = "?language=ko";
		
		JsonNode node = this.client.get()
				.uri("/movie/" + id + "/recommendations" + queryParam)
				.retrieve()
				.bodyToMono(JsonNode.class)
				.block();
		
		JsonNode resultsNode = node.get("results");
		
		ObjectMapper mapper = new ObjectMapper();
		try {
			MovieDetailsDto[] recommendeArray = mapper.treeToValue(resultsNode, MovieDetailsDto[].class);
			List<MovieDetailsDto> recommendedList = Arrays.asList(recommendeArray);
			
			return recommendedList;
			
		} catch (JsonProcessingException | IllegalArgumentException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	/**
	 * 영화 id와 국가코드를 아규먼트로 받아, 해당 영화의 해당 국가에서의 MovieReleaseDateItemDto리스트를 리턴해줌.
	 * MovieReleaseDateItemDto에는 연령제한 등의 정보가 있다.
	 * @param id
	 * @param countryCode
	 * @return 만약 해당 국가코드의 releaseDate정보가 있으면 List<MovieReleaseDateItemDto>를 리턴, 없으면 null을 리턴
	 */
	public List<MovieReleaseDateItemDto> getMovieReleaseDateInfo(int id, String countryCode) {
				
		JsonNode node = this.client.get()
				.uri("/movie/" + id + "/release_dates")
				.retrieve()
				.bodyToMono(JsonNode.class)
				.block();
		
		ObjectMapper mapper = new ObjectMapper();
		
		JsonNode resultsNode = node.get("results");
		try {
			MovieReleaseDateDto[] releaseDateArray = mapper.treeToValue(resultsNode, MovieReleaseDateDto[].class);
			List<MovieReleaseDateDto> releaseDateList = Arrays.asList(releaseDateArray);
			
			List<MovieReleaseDateDto> filteredReleaseDateList = releaseDateList.stream().filter((x) -> x.getIso_3166_1().equals(countryCode)).toList();
			
			if (filteredReleaseDateList.size() != 0) {
				List<MovieReleaseDateItemDto> releaseDateItemList = filteredReleaseDateList.get(0).getRelease_dates();
				return releaseDateItemList;
			} else {
				return null;
			}
			
		} catch (JsonProcessingException | IllegalArgumentException e) {
			e.printStackTrace();
			return null;
		}
		
	}
	
	/**
	 * 영화의 모든 provider의 리스트를 반화하는 메서드
	 * @return 영화의 MovieProviderItemDto타입의 모든 provider의 리스트
	 * @throws JsonProcessingException
	 * @throws IllegalArgumentException
	 */
	public List<MovieProviderItemDto> getAllMovieProviders() throws JsonProcessingException, IllegalArgumentException {
				
		JsonNode node = this.client.get()
				.uri("/watch/providers/movie")
				.retrieve()
				.bodyToMono(JsonNode.class)
				.block();
		
		JsonNode resultsNode = node.get("results");
		
		ObjectMapper mapper = new ObjectMapper();
		MovieProviderItemDto[] providerArray = mapper.treeToValue(resultsNode, MovieProviderItemDto[].class);
		List<MovieProviderItemDto> providerList = Arrays.asList(providerArray);
		
		return providerList;
	}
	
}	// MovieApiUtil 클래스 끝
