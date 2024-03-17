package com.itwill.teamfourmen.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itwill.teamfourmen.dto.person.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.Period;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PersonService {

	@Value("${tmdb.api-key}")
	private String apiKey;
	private static final String POPULAR = "popular";
    private static final String apiUrl = "https://api.themoviedb.org/3";
	private final WebClient webClient;

	// 인물 리스트 페이징 처리를 위한 변수 선언.
	int pagesShowInBar = 10; // 페이징 바에 얼마큼씩 보여줄 건지 설정. (10개씩 보여줄 것임)

	@Autowired
	public PersonService(WebClient webClient) {
		this.webClient = webClient;
	}

	/**
	 * 페이징 처리를 위한 코드.
	 */
	public PersonPagingDto paging(int page) {

		int startPage = (int) Math.ceil( ((double) page / pagesShowInBar) - 1 ) * pagesShowInBar + 1;
		int totalPage = 500;
		int endPage = 0;
		if ((startPage + pagesShowInBar - 1) >= totalPage) {
			endPage = totalPage;
		} else {
			endPage = startPage + pagesShowInBar - 1;
		}

        return PersonPagingDto.builder()
				.startPage(startPage).endPage(endPage)
				.totalPage(totalPage).pagesShowInBar(pagesShowInBar)
				.build();

	}

    /**
     * JSON 데이터를 PageAndListDto 객체로 변환.
	 * 인물의 리스트를 받아오기 위한 메서드.
	 * @param page
	 * 파라미터는 page로 api 요청 주소를 생성하는 데 사용됨.
	 * @return API 요청으로 받아온 JSON 데이터를 매핑한 pageAndListDtoEnUS(영어) pageAndListDtoKoKR(한국어) 객체.
     */
    public PageAndListDto getPersonListEnUS(int page) {

    	// API 요청 주소 생성. (페이지에 해당하는 인물의 리스트를 받아옴)
    	String uri = String.format(apiUrl + "/person/" + POPULAR + "?api_key=%s&language=en-US&page=%d", apiKey, page);

    	PageAndListDto pageAndListDtoEnUS;
        pageAndListDtoEnUS = webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(PageAndListDto.class)
                .block();

		return pageAndListDtoEnUS;

	}
	public PageAndListDto getPersonListKoKR(int page) {

		// API 요청 주소 생성. (페이지에 해당하는 인물의 리스트를 받아옴)
		String uri = String.format(apiUrl + "/person/" + POPULAR + "?api_key=%s&language=ko-KR&page=%d", apiKey, page);

		PageAndListDto pageAndListDtoKoKR;
		pageAndListDtoKoKR = webClient.get()
				.uri(uri)
				.retrieve()
				.bodyToMono(PageAndListDto.class)
				.block();

		return pageAndListDtoKoKR;

	}

	// ************************************ 리스트 끝 ************************************ //

	/**
	 * 인물의 생년월일로 인물의 나이를 구하는 메서드.
	 * @param birthday
	 * 파라미터는 인물의 생년월일.
	 * @return 인물의 생년월일을 LocalDate 타입으로 변환하여 인물의 나이를 구하고 그 값을 리턴.
	 */
	public int calculateAge(LocalDate birthday) {
		LocalDate currentDate = LocalDate.now();
		return Period.between(birthday, currentDate).getYears();
	}

	/**
	 * JSON 데이터를 DetailsPersonDto 객체로 변환.
	 * 인물의 상세 정보를 받아오기 위한 메서드.
	 * @param id
	 * 파라미터는 인물의 id 값.
	 * @return API 요청으로 받아온 JSON 데이터를 매핑한 detailsPersonDtoEnUS(영어) detailsPersonDtoKoKR(한국어) 객체.
	 */
	public DetailsPersonDto getPersonDetailsEnUS(int id) {

		// API 요청 주소 생성. (각 인물의 상세 정보를 받아옴)
		String uri = String.format(apiUrl + "/person/" + id + "?api_key=%s&language=en-US", apiKey);

		DetailsPersonDto detailsPersonDtoEnUS;
		detailsPersonDtoEnUS = webClient.get()
				.uri(uri)
				.retrieve()
				.bodyToMono(DetailsPersonDto.class)
				.block();

		return detailsPersonDtoEnUS;

	}
	public DetailsPersonDto getPersonDetailsKoKR(int id) {

		// API 요청 주소 생성. (각 인물의 상세 정보를 받아옴)
		String uri = String.format(apiUrl + "/person/" + id + "?api_key=%s&language=ko-KR", apiKey);

		DetailsPersonDto detailsPersonDtoKoKR;
		detailsPersonDtoKoKR = webClient.get()
				.uri(uri)
				.retrieve()
				.bodyToMono(DetailsPersonDto.class)
				.block();

		return detailsPersonDtoKoKR;

	}

	/**
	 * JSON 데이터를 받아 ExternalIDsDto 객체로 변환.
	 * 인물의 외부 링크 정보(sns, 홈페이지 등)를 받아오기 위한 메서드.
	 * @param id
	 * 파라미터는 인물의 id 값.
	 * @return API 요청으로 받아온 JSON 데이터를 매핑한 externalIDsDto 객체.
	 */
	public ExternalIDsDto getExternalIDs(int id) {

		// API 요청 주소 생성. (각 인물의 SNS, 유튜브, 홈페이지 등의 외부 아이디 정보를 받아옴)
		String uri = String.format(apiUrl + "/person/" + id + "/external_ids" + "?api_key=%s", apiKey);

		ExternalIDsDto externalIDsDto;
		externalIDsDto = webClient.get()
				.uri(uri)
				.retrieve()
				.bodyToMono(ExternalIDsDto.class)
				.block();

		return externalIDsDto;

	}

	/**
	 * JSON 데이터를 받아 MovieCreditsDto 객체로 변환.
	 * 해당 인물이 cast(연기) 또는 crew(제작 등)로 참여한 영화의 정보를 받아오기 위한 메서드.
	 * @param id
	 * 파라미터는 인물의 id 값.
	 * @return API 요청으로 받아온 JSON 데이터를 매핑한 movieCreditsDtoEnUS(영어), movieCreditsDtoKoKR(한국어) 객체.
	 */
	public MovieCreditsDto getMovieCreditsEnUS(int id) {

		// API 요청 주소 생성.
		String uri = String.format(apiUrl + "/person/" + id + "/movie_credits" + "?api_key=%s&language=en-US", apiKey);

		MovieCreditsDto movieCreditsDtoEnUS;
		movieCreditsDtoEnUS = webClient.get()
				.uri(uri)
				.retrieve()
				.bodyToMono(MovieCreditsDto.class)
				.block();

		return movieCreditsDtoEnUS;

	}
	public MovieCreditsDto getMovieCreditsKoKR(int id) {

		// API 요청 주소 생성.
		String uri = String.format(apiUrl + "/person/" + id + "/movie_credits" + "?api_key=%s&language=ko-KR", apiKey);

		MovieCreditsDto movieCreditsDtoKoKR;
		movieCreditsDtoKoKR = webClient.get()
				.uri(uri)
				.retrieve()
				.bodyToMono(MovieCreditsDto.class)
				.block();

		return movieCreditsDtoKoKR;

	}

	/**
	 * JSON 데이터를 받아 tvCreditsDtoEnUS(영어), tvCreditsDtoKoKR(한국어) 객체로 변환.
	 * 해당 인물이 cast(연기) 또는 crew(제작 등)로 참여한 TV 프로그램의 정보를 받아오기 위한 메서드.
	 * @param id
	 * 파라미터는 인물의 id 값.
	 * @return API 요청으로 받아온 JSON 데이터를 매핑한 tvCreditsDtoEnUS(영어), tvCreditsDtoKoKR(한국어) 객체.
	 */
	public TvCreditsDto getTvCreditsEnUS(int id) {

		// API 요청 주소 생성.
		String uri = String.format(apiUrl + "/person/" + id + "/tv_credits" + "?api_key=%s&language=en-US", apiKey);

		TvCreditsDto tvCreditsDtoEnUS;
		tvCreditsDtoEnUS = webClient.get()
				.uri(uri)
				.retrieve()
				.bodyToMono(TvCreditsDto.class)
				.block();

		return tvCreditsDtoEnUS;

	}
	public TvCreditsDto getTvCreditsKoKR(int id) {

		// API 요청 주소 생성.
		String uri = String.format(apiUrl + "/person/" + id + "/tv_credits" + "?api_key=%s&language=ko-KR", apiKey);

		TvCreditsDto tvCreditsDtoKoKR;
		tvCreditsDtoKoKR = webClient.get()
				.uri(uri)
				.retrieve()
				.bodyToMono(TvCreditsDto.class)
				.block();

		return tvCreditsDtoKoKR;

	}

	/**
	 * JSON 데이터를 받아 CombinedCreditsDto 객체로 변환.
	 * 해당 인물이 cast(연기) 또는 crew(제작 등)로 참여한 모든 출연작(영화, TV 프로그램)의 정보를 받아오기 위한 메서드.
	 * @param id
	 * 파라미터는 인물의 id 값.
	 * @return API 요청으로 받아온 JSON 데이터를 매핑한 combinedCreditsDtoEnUS(영어), combinedCreditsDtoKoKR(한국어) 객체.
	 */
	public CombinedCreditsDto getCombinedCreditsEnUS(int id) {

		// API 요청 주소 생성.
		String uri = String.format(apiUrl + "/person/" + id + "/combined_credits" + "?api_key=%s&language=en-US", apiKey);

		CombinedCreditsDto combinedCreditsDtoEnUS;
		combinedCreditsDtoEnUS = webClient.get()
				.uri(uri)
				.retrieve()
				.bodyToMono(CombinedCreditsDto.class)
				.block();

		return combinedCreditsDtoEnUS;

	}
	public CombinedCreditsDto getCombinedCreditsKoKR(int id) {

		// API 요청 주소 생성.
		String uri = String.format(apiUrl + "/person/" + id + "/combined_credits" + "?api_key=%s&language=ko-KR", apiKey);

		CombinedCreditsDto combinedCreditsDtoKoKR;
		combinedCreditsDtoKoKR = webClient.get()
				.uri(uri)
				.retrieve()
				.bodyToMono(CombinedCreditsDto.class)
				.block();

		return combinedCreditsDtoKoKR;

	}

	/**
	 * JSON 데이터를 받아 List<CombinedCreditsCastDto> 객체로 변환.
	 * 해당 인물이 cast(연기)로 참여한 모든 출연작(영화, TV 프로그램)의 정보를 리스트 형태로 받아오기 위한 메서드.
	 * @param id
	 * 파라미터는 인물의 id 값.
	 * @return API 요청으로 받아온 JSON 데이터를 매핑한 castListEnUS(영어), castListKoKR(한국어) 객체.
	 */
	public List<CombinedCreditsCastDto> getCombinedCreditsCastEnUS(int id) {

		// API 요청 주소 생성.
		String uri = String.format(apiUrl + "/person/" + id + "/combined_credits" + "?api_key=%s&language=en-US", apiKey);

		CombinedCreditsCastDto combinedCreditsCastDtoEnUS;
		JsonNode node = webClient.get()
				.uri(uri)
				.retrieve()
				.bodyToMono(JsonNode.class)
				.block();

		JsonNode castNode = node.get("cast");

		ObjectMapper mapper = new ObjectMapper();

        try {
            CombinedCreditsCastDto[] castArray = mapper.treeToValue(castNode, CombinedCreditsCastDto[].class);
			List<CombinedCreditsCastDto> castListEnUS = Arrays.asList(castArray);
			return castListEnUS;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
			return null;
        }

	}
	public List<CombinedCreditsCastDto> getCombinedCreditsCastKoKR(int id) {

		// API 요청 주소 생성.
		String uri = String.format(apiUrl + "/person/" + id + "/combined_credits" + "?api_key=%s&language=ko-KR", apiKey);

		CombinedCreditsCastDto combinedCreditsCastDtoKoKR;
		JsonNode node = webClient.get()
				.uri(uri)
				.retrieve()
				.bodyToMono(JsonNode.class)
				.block();

		JsonNode castNode = node.get("cast");

		ObjectMapper mapper = new ObjectMapper();

		try {
			CombinedCreditsCastDto[] castArray = mapper.treeToValue(castNode, CombinedCreditsCastDto[].class);
			List<CombinedCreditsCastDto> castListKoKR = Arrays.asList(castArray);
			return castListKoKR;
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return null;
		}

	}

	/**
	 * JSON 데이터를 받아 List<CombinedCreditsCastDto> 객체로 변환.
	 * 해당 인물이 crew(제작 등)로 참여한 모든 출연작(영화, TV 프로그램)의 정보를 리스트 형태로 받아오기 위한 메서드.
	 * @param id
	 * 파라미터는 인물의 id 값.
	 * @return API 요청으로 받아온 JSON 데이터를 매핑한 crewListEnUS(영어), crewListKoKR(한국어) 객체.
	 */
	public List<CombinedCreditsCrewDto> getCombinedCreditsCrewEnUS(int id) {

		// API 요청 주소 생성.
		String uri = String.format(apiUrl + "/person/" + id + "/combined_credits" + "?api_key=%s&language=en-US", apiKey);

		CombinedCreditsCrewDto combinedCreditsCrewDtoEnUS;
		JsonNode node = webClient.get()
				.uri(uri)
				.retrieve()
				.bodyToMono(JsonNode.class)
				.block();

		JsonNode crewNode = node.get("crew");

		ObjectMapper mapper = new ObjectMapper();

		try {
			CombinedCreditsCrewDto[] crewArray = mapper.treeToValue(crewNode, CombinedCreditsCrewDto[].class);
			List<CombinedCreditsCrewDto> crewListEnUS = Arrays.asList(crewArray);
			return crewListEnUS;
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return null;
		}

	}
	public List<CombinedCreditsCrewDto> getCombinedCreditsCrewKoKR(int id) {

		// API 요청 주소 생성.
		String uri = String.format(apiUrl + "/person/" + id + "/combined_credits" + "?api_key=%s&language=ko-KR", apiKey);

		CombinedCreditsCrewDto combinedCreditsCrewDtoKoKR;
		JsonNode node = webClient.get()
				.uri(uri)
				.retrieve()
				.bodyToMono(JsonNode.class)
				.block();

		JsonNode crewNode = node.get("crew");

		ObjectMapper mapper = new ObjectMapper();

		try {
			CombinedCreditsCrewDto[] crewArray = mapper.treeToValue(crewNode, CombinedCreditsCrewDto[].class);
			List<CombinedCreditsCrewDto> crewListKoKR = Arrays.asList(crewArray);
			return crewListKoKR;
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return null;
		}

	}

}