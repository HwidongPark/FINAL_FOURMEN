package com.itwill.teamfourmen.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itwill.teamfourmen.domain.ImdbRatings;
import com.itwill.teamfourmen.domain.ImdbRatingsRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImdbRatingUtil {

    @Value("${tmdb.hd.token}")
    private String TMDB_TOKEN;

    @Value("${tmdb.api-key}")
    private String API_KEY;

    private final ImdbRatingsRepository imdbRatingsDao;

    private final String BASE_URL = "https://api.themoviedb.org/3";

    /**
     * IMDB ID를 이용해서 IMDB의 평점과 투표 수를 가진 객체를 가져온다.
     * @param imdb_id String imdb_id
     * @return ImdbRatings 객체를 가져옴.
     */
    public ImdbRatings getImdbRating(String imdb_id) {
        log.info("GET IMDB_RATING - IMDB_ID = {}", imdb_id);

        if (imdb_id != null) {
            try {
                ImdbRatings imdbRatings = imdbRatingsDao.findById(imdb_id)
                        .orElseThrow(() -> new EntityNotFoundException());
                log.info("if ratings ? = {}", imdbRatings);
                return imdbRatings;
            } catch (EntityNotFoundException e) {
                log.warn(e.getMessage());
                // 기본값으로 설정된 객체를 반환
                return ImdbRatings.builder().imdbId(imdb_id).averagerating(0.0).numvotes(0).build();
            }
        }
        else {
            log.info("IMDB ID IS NULL");
            return null;
        }
    }

    /**
     * tmdb의 아이디를 이용해서 imdb의 아이디를 받아온다
     *
     * @param tmdb_id tmdb의 아이디는 int
     * @param category category = {"movie" , "tv"} 중 하나...!
     * @return String ImdbId
     */
    public String getImdbId (int tmdb_id, String category) {
        log.info("GET IMDB ID - TMDB ID = {}, CATEGORY = {}", tmdb_id, category);

        RestTemplate restTemplate = new RestTemplate();

        String targetUrl = "";

        switch (category) {
            case "movie":
               targetUrl = UriComponentsBuilder.fromUriString(BASE_URL)
                       .path("/movie/{movie_id}/external_ids")
                       .queryParam("api_key", API_KEY)
                       .buildAndExpand(String.valueOf(tmdb_id))
                       .toUriString();
               break;
            case "tv":
                targetUrl = UriComponentsBuilder.fromUriString(BASE_URL)
                        .path("/tv/{series_id}/external_ids")
                        .queryParam("api_key", API_KEY)
                        .buildAndExpand(String.valueOf(tmdb_id))
                        .toUriString();
                break;
            default:
                throw  new IllegalArgumentException("잘못된 카테고리 : " + category);
        }

        // String imdb_id 의 값만 뽑아서 리턴해주고 싶음
        String jsonResult = restTemplate.getForObject(targetUrl, String.class);

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> resultMap = null;

        try{
            resultMap = objectMapper.readValue(jsonResult, Map.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String imdbId = "";

        if(resultMap != null && resultMap.containsKey("imdb_id")) {
            imdbId = (String) resultMap.get("imdb_id");
        } else {
            imdbId = null;
        }

        return imdbId;
    }

}

