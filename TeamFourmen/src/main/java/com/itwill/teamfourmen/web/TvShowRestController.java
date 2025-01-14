package com.itwill.teamfourmen.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.itwill.teamfourmen.dto.tvshow.TvShowListDTO;
import com.itwill.teamfourmen.dto.tvshow.TvShowQueryParamDTO;
import com.itwill.teamfourmen.service.TvShowApiUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/tv")
public class TvShowRestController {

    private final TvShowApiUtil util;


    @GetMapping("/trend-list")
    public ResponseEntity<TvShowListDTO> getAdditonalTrendList (@RequestParam(name = "timeWindow") String timeWindow, @RequestParam(name = "page") int page) throws JsonMappingException, JsonProcessingException{
        log.info("Get AdditionalTrendList - timeWindow = {}, Page = {}", timeWindow, page);

        TvShowListDTO trendListDto = util.getTrendTvShowList(timeWindow,page);

        return ResponseEntity.ok(trendListDto);
    }

    @GetMapping("/ott-list")
    public ResponseEntity<TvShowListDTO> getAddtionalOttTvShowList (@RequestParam(name = "platform") String platform, @RequestParam(name = "page") int page) throws JsonMappingException, JsonProcessingException{
        log.info("Get AdditionalOttTvShowList - platform = {}, page = {}", platform, page);

        TvShowListDTO ottTvShowList = util.getOttTvShowList(platform, page);

        return ResponseEntity.ok(ottTvShowList);
    }


    @GetMapping("/list")
    public ResponseEntity<TvShowListDTO> getAdditionalList (@ModelAttribute TvShowQueryParamDTO paramDTO){
        log.info("Get AdditionalList paramDTO = {}" ,paramDTO);

        TvShowListDTO listDTO = util.getTvShowList(paramDTO);
        //log.info("listDTO = {}", listDTO);

        return ResponseEntity.ok(listDTO);
    }


}
