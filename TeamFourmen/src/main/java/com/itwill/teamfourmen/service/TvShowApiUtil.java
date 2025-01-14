package com.itwill.teamfourmen.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.itwill.teamfourmen.dto.tvshow.*;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TvShowApiUtil {

    @Value("${tmdb.hd.token}")
    private String TOKEN;

    private TvShowDTO tvShowDTO;

    private final String BASE_URL = "https://api.themoviedb.org/3";

    private final String BASE_DISCOVER_URL = "https://api.themoviedb.org/3/discover/tv";

    public WebClient getWebClient(String baseUrl) {
    	WebClient webClient = WebClient.builder()
				.baseUrl(baseUrl)
				.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN)
				.defaultHeader(HttpHeaders.ACCEPT, "application/json")
				.build();
    	
    	return webClient;
    }

    /**
     * Tv Show List를 TvShowListDTO 객체로 돌려주는 메서드
     * 파라미터 =  {"popular" , "top_rated}
     * queryParam language = ko
     * int page
     */

    /*
    https://api.themoviedb.org/3/discover/tv
    ?page=1&first_air_date.gte=2005-01-01&first_air_date.lte=2023-12-31
    &include_adult=false&include_null_first_air_dates=false
    &language=ko-KR&sort_by=vote_count.desc&watch_region=KR
    &with_genres=80&with_origin_country=US&with_status=3
    &with_watch_providers=8
    &api_key=390e779304bcd53af3b649f4e27c6452
     */
    public TvShowListDTO getTvShowList (TvShowQueryParamDTO paramDTO) {
//        log.info("Get Tv Show List Param = {}", paramDTO);

        RestTemplate restTemplate = new RestTemplate();

        String pathUri = "";
        String targetUrl = "";

        String genreVariable = "";
        String providerVariable = "";
        String watchRegionVariable = null;

        switch (paramDTO.getListCategory()){

            case "top_rated":
                pathUri = "/tv/top_rated";
                break;
            case "filter":
                pathUri = "/discover/tv";
                break;
            case "search":
                pathUri = "/search/tv";
                break;

        }

        String path_URI = pathUri;

        List<Integer> genreList = paramDTO.getWith_genres();
        if(genreList != null) {
            genreVariable =genreList.stream().map((x) -> x.toString()).collect(Collectors.joining("|"));
//            log.info("GENRES = {}", genreVariable);
        }

        String genres = genreVariable;
//        log.info("genres = {}", genres);

        List<Integer> providerList = paramDTO.getWith_watch_provider();
        if(providerList != null) {
            providerVariable = providerList.stream().map((x) -> x.toString()).collect(Collectors.joining("|"));
//            log.info("PROVIDER = {}", providerVariable);
        }

        String providers = providerVariable;

        if(providers != null) {
            watchRegionVariable = "KR";
        } else {
            watchRegionVariable = "null";
        }

        String watchRegion = watchRegionVariable;

//        WebClient client = WebClient.create(BASE_URL);
        WebClient client = this.getWebClient(BASE_URL);

        TvShowListDTO tvShowListDTO = client.get()
                .uri(uriBuilder -> uriBuilder
                        .path(path_URI)
                                .queryParam("page", paramDTO.getPage())
                                .queryParam("language", "ko-KR")
                                .queryParam("sort_by" , paramDTO.getSortBy())
                                .queryParam("first_air_date.gte", paramDTO.getFirst_air_date_gte())
                                .queryParam("first_air_date.lte", paramDTO.getFirst_air_date_lte())
                                .queryParam("with_genres", genres)
                                .queryParam("with_status", paramDTO.getWith_status())
                                .queryParam("watch_region" ,  watchRegion)
                                .queryParam("with_watch_providers", providers)
                                .queryParam("with_original_language", paramDTO.getWith_original_language())
                                .queryParam("query", paramDTO.getQuery())
                                .build())
                .retrieve()
                .bodyToMono(TvShowListDTO.class)
                .block();

        return tvShowListDTO;
    }


    public TvShowListDTO getTvShowList (String listCategory, int page) throws JsonMappingException, JsonProcessingException {
        //log.info("GET TV SHOW LIST Category = {}, Page = {}", listCategory, page);


        RestTemplate restTemplate = new RestTemplate();
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("accept", "application/json");
        headers.set("Authorization", "Bearer " + TOKEN);
        
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String baseUrl =  BASE_URL + "/tv";

        //"https://api.themoviedb.org/3/tv";

        String targetUrl = "";

        switch (listCategory) {
            case "popular", "top_rated":
                targetUrl = UriComponentsBuilder.fromUriString(baseUrl)
                        .path("/{listCategory}")
                        .queryParam("language", "ko-KR")
                        .queryParam("page", page)
                        .buildAndExpand(String.valueOf(listCategory))
                        .toUriString();
                //log.info("targetURL = {}", targetUrl);

                break;
            default:
//                log.info("Wrong Pram - getTvShowList()");
                break;
        }

        ResponseEntity<String> response = restTemplate.exchange(
                targetUrl, 
                HttpMethod.GET, 
                entity, 
                String.class
        );
        
//        TvShowListDTO tvShowListDTO = restTemplate.getForObject(targetUrl, TvShowListDTO.class);
        ObjectMapper objectMapper = new ObjectMapper();
        TvShowListDTO tvShowListDTO = objectMapper.readValue(response.getBody(), TvShowListDTO.class);

        return tvShowListDTO;
    }

    public TvShowListDTO getTrendTvShowList (String timeWindow, int page) throws JsonMappingException, JsonProcessingException {
        //log.info("Get Trend Tv Show List - TimeWindow = {} , Page = {}", timeWindow, page);


        RestTemplate restTemplate = new RestTemplate();
                
        HttpHeaders headers = new HttpHeaders();
        headers.set("accept", "application/json");
        headers.set("Authorization", "Bearer " + TOKEN);
        
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String baseUrl = "https://api.themoviedb.org/3/trending/tv";

        String targetUrl = "";

        switch (timeWindow){
            case "day" :
            case "week":
            targetUrl = UriComponentsBuilder.fromUriString(baseUrl)
                    .path("/{timeWindow}")
                    .queryParam("language", "ko-KR")
                    .queryParam("page", page)
                    .buildAndExpand(String.valueOf(timeWindow))
                    .toUriString();
//            log.info("targetURL = {}", targetUrl);
            break;
            default:
//                log.info("WRONG PARAM - getTrendTvShowList");
                break;
        }

//        TvShowListDTO tvShowListDTO = restTemplate.getForObject(targetUrl, TvShowListDTO.class);

        ResponseEntity<String> response = restTemplate.exchange(
                targetUrl, 
                HttpMethod.GET, 
                entity, 
                String.class
        );
        
        ObjectMapper objectMapper = new ObjectMapper();
        TvShowListDTO tvShowListDTO = objectMapper.readValue(response.getBody(), TvShowListDTO.class);
        
        
        return tvShowListDTO;
    }

    /*
    OTT 별 Tv Show List 불러오기

    Ex URL ) https://api.themoviedb.org/3/discover/tv?language=ko-KR&page=4&sort_by=vote_count.desc&watch_region=KR&with_watch_providers=8&api_key=390e779304bcd53af3b649f4e27c6452

    language = ko-KR
    page
    sort_by = vote_count
    watch_region=KR
    with_watch_provider = {parameter} 넷플릭스, 애플티비, 디즈니, 아마존, 왓챠, 웨이브 등등을 이름과 아이디를 매핑을 해놔야함.
    api_key =
     */

    public TvShowListDTO getOttTvShowList (String platform, int page) throws JsonMappingException, JsonProcessingException{
//        log.info("Get Ott Tv Show List platform = {}, page = {}" , platform, page);

        RestTemplate restTemplate = new RestTemplate();
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("accept", "application/json");
        headers.set("Authorization", "Bearer " + TOKEN);
        
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String baseUrl = "https://api.themoviedb.org/3/discover/tv";

        String targetUrl = "";

        switch (platform){
            case "netfilx":
                targetUrl = UriComponentsBuilder.fromUriString(baseUrl)
                        .queryParam("language", "ko-KR")
                        .queryParam("page", page)
                        .queryParam("sort_by", "vote_count.desc")
                        .queryParam("watch_region", "KR")
                        .queryParam("with_watch_providers", 8)
                        .toUriString();
//                log.info("targetURL = {}", targetUrl);
                break;
            case "disney_plus":
                targetUrl = UriComponentsBuilder.fromUriString(baseUrl)
                        .queryParam("language", "ko-KR")
                        .queryParam("page", page)
                        .queryParam("sort_by", "vote_count.desc")
                        .queryParam("watch_region", "KR")
                        .queryParam("with_watch_providers", 337)
                        .toUriString();
//                log.info("targetURL = {}", targetUrl);
                break;
            case "apple_tv":
                targetUrl = UriComponentsBuilder.fromUriString(baseUrl)
                        .queryParam("language", "ko-KR")
                        .queryParam("page", page)
                        .queryParam("sort_by", "vote_count.desc")
                        .queryParam("watch_region", "KR")
                        .queryParam("with_watch_providers", 350)
                        .toUriString();
//                log.info("targetURL = {}", targetUrl);
                break;
            case "amazone_prime":
                targetUrl = UriComponentsBuilder.fromUriString(baseUrl)
                        .queryParam("language", "ko-KR")
                        .queryParam("page", page)
                        .queryParam("sort_by", "vote_count.desc")
                        .queryParam("watch_region", "KR")
                        .queryParam("with_watch_providers", 119)
                        .toUriString();
//                log.info("targetURL = {}", targetUrl);
                break;
            case "watcha":
                targetUrl = UriComponentsBuilder.fromUriString(baseUrl)
                        .queryParam("language", "ko-KR")
                        .queryParam("page", page)
                        .queryParam("sort_by", "vote_count.desc")
                        .queryParam("watch_region", "KR")
                        .queryParam("with_watch_providers", 97)
                        .toUriString();
//                log.info("targetURL = {}", targetUrl);
                break;
            case "wavve":
                targetUrl = UriComponentsBuilder.fromUriString(baseUrl)
                        .queryParam("language", "ko-KR")
                        .queryParam("page", page)
                        .queryParam("sort_by", "vote_count.desc")
                        .queryParam("watch_region", "KR")
                        .queryParam("with_watch_providers", 356)
                        .toUriString();
//                log.info("targetURL = {}", targetUrl);
                break;
            default:
//                log.info("WRONG PARAM - getOttTvShowList");
                break;
        }

//        TvShowListDTO tvShowListDTO = restTemplate.getForObject(targetUrl, TvShowListDTO.class);
        ResponseEntity<String> response = restTemplate.exchange(
                targetUrl, 
                HttpMethod.GET, 
                entity, 
                String.class
        );
        
        ObjectMapper objectMapper = new ObjectMapper();
        TvShowListDTO tvShowListDTO = objectMapper.readValue(response.getBody(), TvShowListDTO.class);        
        
        return tvShowListDTO;
    }

    // 장르 가져오기
    public TvShowGenreListDTO getTvShowGenreList (String language) throws JsonMappingException, JsonProcessingException {
//        log.info("get TvShowGenreList - Language = {}", language);

        HttpHeaders headers = new HttpHeaders();
        headers.set("accept", "application/json");
        headers.set("Authorization", "Bearer " + TOKEN);
        
        HttpEntity<String> entity = new HttpEntity<>(headers);
    	
        RestTemplate restTemplate = new RestTemplate();

        String baseUrl = BASE_URL + "/genre/tv/list";

        String targetUrl = "";

        targetUrl = UriComponentsBuilder.fromUriString(baseUrl)
                .queryParam("language", language)
                .queryParam("api_key", TOKEN)
                .toUriString();
//        log.info("TARGET URL = {}", targetUrl);

//        TvShowGenreListDTO tvShowGenreListDTO = restTemplate.getForObject(targetUrl, TvShowGenreListDTO.class);
        ResponseEntity<String> response = restTemplate.exchange(
                targetUrl, 
                HttpMethod.GET, 
                entity, 
                String.class
        );
        
        ObjectMapper objectMapper = new ObjectMapper();
        
        TvShowGenreListDTO tvShowGenreListDTO = objectMapper.readValue(response.getBody(), TvShowGenreListDTO.class); 
        
        return tvShowGenreListDTO;
    }


    // 장르별 TvShowList 출력
    public TvShowListDTO getGenreTvShowList (String genre, int page) throws JsonMappingException, JsonProcessingException {
//        log.info("get GenreTvShowList - Genre = {}, page = {}", genre, page);

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("accept", "application/json");
        headers.set("Authorization", "Bearer " + TOKEN);
        
        HttpEntity<String> entity = new HttpEntity<>(headers);
    	
        
        String baseUrl = "https://api.themoviedb.org/3/discover/tv";

        String targetUrl = "";

        targetUrl = UriComponentsBuilder.fromUriString(baseUrl)
                .queryParam("language", "ko-KR")
                .queryParam("page", page)
                .queryParam("sort_by", "vote_count.desc")
                .queryParam("watch_region", "KR")
                .queryParam("with_genres", genre)
                .toUriString();
//        log.info("TARGET URL = {}", targetUrl);

//        TvShowListDTO tvShowListDTO = restTemplate.getForObject(targetUrl,TvShowListDTO.class);
        
        ResponseEntity<String> response = restTemplate.exchange(
                targetUrl, 
                HttpMethod.GET, 
                entity, 
                String.class
        );
        
        ObjectMapper objectMapper = new ObjectMapper();
        TvShowListDTO tvShowListDTO = objectMapper.readValue(response.getBody(), TvShowListDTO.class);
        
        return  tvShowListDTO;
    }


    public TvShowDTO getTvShowDetails (int tvshow_id) throws JsonMappingException, JsonProcessingException {
//        log.info("get TvShow Season Detail - TVSHOW ID = {}", tvshow_id);

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("accept", "application/json");
        headers.set("Authorization", "Bearer " + TOKEN);
        
        HttpEntity<String> entity = new HttpEntity<>(headers);    	
        
        String baseUrl = "https://api.themoviedb.org/3/tv";

        String targetUrl = "";

        targetUrl = UriComponentsBuilder.fromUriString(baseUrl)
                .path("/{tvshow_id}")
                .queryParam("language", "ko")
                .buildAndExpand(String.valueOf(tvshow_id))
                .toUriString();
//        log.info("TARGET URL = {}",targetUrl);

//        TvShowDTO tvShowDTO = restTemplate.getForObject(targetUrl, TvShowDTO.class);
        ResponseEntity<String> response = restTemplate.exchange(
                targetUrl, 
                HttpMethod.GET, 
                entity, 
                String.class
        );
        
        ObjectMapper objectMapper = new ObjectMapper();
        TvShowDTO tvShowDTO = objectMapper.readValue(response.getBody(), TvShowDTO.class);
        
        return  tvShowDTO;
    }

    public TvShowVideoListDTO getTvShowVideo (int id){
//        log.info ("get TvShow Trailer Video - TVSHOW ID = {}", id);


        String baseUrl = BASE_URL + "/tv";

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("accept", "application/json");
        headers.set("Authorization", "Bearer " + TOKEN);
        
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String json = UriComponentsBuilder.fromUriString(baseUrl)
                .path("/{id}/videos")
                .buildAndExpand(String.valueOf(id))
                .toUriString();

//        ResponseEntity<String> responseEntity = restTemplate.getForEntity(json, String.class);
//        String jsonString = responseEntity.getBody();
        ResponseEntity<String> response = restTemplate.exchange(
                json, 
                HttpMethod.GET, 
                entity, 
                String.class
        );
                

        ObjectMapper mapper = new ObjectMapper();

        TvShowVideoListDTO tvShowVideoDTOList = null;

        try {
            tvShowVideoDTOList = mapper.readValue(response.getBody(), TvShowVideoListDTO.class);
        } catch (JsonProcessingException e){
            e.printStackTrace();
        }

        //log.info("TVSHOW TRAILER VIDEO LIST = {}", tvShowVideoDTOList);


        return tvShowVideoDTOList;
    }

    public TvShowWatchProviderListDTO getTvShowProvider(int tvshow_id){
//        log.info ("get TvShow Watch Provider List - TVSHOW_ID = {}", tvshow_id);

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("accept", "application/json");
        headers.set("Authorization", "Bearer " + TOKEN);
        
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String baseUrl = BASE_URL + "/tv";

        String targetUrl = "";

        targetUrl = UriComponentsBuilder.fromUriString(baseUrl)
                .path("/{tvshow_id}/watch/providers")
                .buildAndExpand(String.valueOf(tvshow_id))
                .toUriString();
//        log.info("TARGET URL = {}", targetUrl);

        ResponseEntity<String> response = restTemplate.exchange(
                targetUrl, 
                HttpMethod.GET, 
                entity, 
                String.class
        );

        ObjectMapper objectMapper = new ObjectMapper();

        TvShowWatchProviderListDTO tvShowWatchProviderListDTO = null;

        try {
            tvShowWatchProviderListDTO = objectMapper.readValue(response.getBody(), TvShowWatchProviderListDTO.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return tvShowWatchProviderListDTO;
    }

    public TvShowSeasonDTO getTvShowSeasonDetail (int tvshow_id ,int season_number){
//        log.info("get TvShow Season Detail - TVSHOW ID = {} , SEASON_NUM = {}", tvshow_id, season_number);

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("accept", "application/json");
        headers.set("Authorization", "Bearer " + TOKEN);
        
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        String baseUrl = "https://api.themoviedb.org/3/tv";

        String targetUrl = "";

        targetUrl = UriComponentsBuilder.fromUriString(baseUrl)
                .path("/{tvshow_id}")
                .path("/season")
                .path("/{season_number}")
                .queryParam("language", "ko-KR")
                .buildAndExpand(String.valueOf(tvshow_id), String.valueOf(season_number))
                .toUriString();
//        log.info("TARGET URL = {}",targetUrl);

//        TvShowSeasonDTO seasonDTO = restTemplate.getForObject(targetUrl, TvShowSeasonDTO.class);
        
        ResponseEntity<String> response = restTemplate.exchange(
                targetUrl, 
                HttpMethod.GET, 
                entity, 
                String.class
        );        
        
        ObjectMapper objectMapper = new ObjectMapper();
        
        TvShowSeasonDTO seasonDTO = null;
        try {        
        	objectMapper.readValue(response.getBody(), TvShowSeasonDTO.class);
        } catch (JsonProcessingException e) {
        	
        }
        

        return  seasonDTO;
    }

    public TvShowEpisodeDTO getTvShowEpisodeDetail(int tvshow_id, int season_number, int episode_number){
//        log.info("get TvShow Episode Detail - TVSHOW ID = {} , SEASON_NUMBER = {}, EPISODE_NUMBER = {}", tvshow_id, season_number, episode_number);

        RestTemplate restTemplate = new RestTemplate();
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("accept", "application/json");
        headers.set("Authorization", "Bearer " + TOKEN);
        
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String baseUrl = "https://api.themoviedb.org/3/tv";

        String targetUrl = "";

        targetUrl = UriComponentsBuilder.fromUriString(baseUrl)
                .path("/{tvshow_id}")
                .path("/season")
                .path("/{season_number}")
                .path("/episode")
                .path("/{episode_number}")
                .queryParam("language", "ko-KR")
                .buildAndExpand(String.valueOf(tvshow_id), String.valueOf(season_number), String.valueOf(episode_number))
                .toUriString();
//        log.info("TARGET URL = {}", targetUrl);

        ResponseEntity<String> response = restTemplate.exchange(
                targetUrl, 
                HttpMethod.GET, 
                entity, 
                String.class
        );        
        
        ObjectMapper objectMapper = new ObjectMapper();
        
        TvShowEpisodeDTO episodeDTO = null;
        try {        
        	objectMapper.readValue(response.getBody(), TvShowEpisodeDTO.class);
        } catch (JsonProcessingException e) {
        	
        }

        return  episodeDTO;
    }


}
