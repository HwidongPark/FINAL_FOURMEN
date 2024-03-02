package com.itwill.teamfourmen.web;

import com.itwill.teamfourmen.dto.person.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.itwill.teamfourmen.service.PersonService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/person")
public class PersonController {

	private final PersonService personService;

	@GetMapping("/list")
	public String list(
			@RequestParam(name = "page", required = false, defaultValue = "1") Integer page,
			Model model) {

		// 서비스 메서드 호출
		// 영어 데이터가 필요한 경우(인물 이름은 영어로만 받아옴):
		PageAndListDto pageAndListDtoEnUS = personService.getPersonListEnUS(page);
		// 한글 데이터가 필요한 경우(인물 이름은 영어로만 받아옴):
		PageAndListDto pageAndListDtoKoKR = personService.getPersonListKoKR(page);

		// 페이징 처리 관련 서비스 메서드 호출
		PersonPagingDto pagingDto = personService.paging(page);

		model.addAttribute("pageInfoEnUS", pageAndListDtoEnUS.getPage());
		model.addAttribute("pageInfoKoKR", pageAndListDtoKoKR.getPage());
		pageAndListDtoKoKR.getPage();
		model.addAttribute("personListEnUS", pageAndListDtoEnUS.getResults());
		model.addAttribute("personListKoKR", pageAndListDtoKoKR.getResults());
		model.addAttribute("paging", pagingDto);

		return "person/person-lists";

	} // end list

	@GetMapping("/details/{id}")
	public String details(
			@PathVariable("id") int id,
			Model model
	) {

//		log.info("details(id={})", id);

		// 서비스 메서드 호출 (인물의 id 값을 파라미터로 전달)
		DetailsPersonDto detailsPersonDtoEnUS = personService.getPersonDetailsEnUS(id);
		DetailsPersonDto detailsPersonDtoKoKR = personService.getPersonDetailsKoKR(id);

		// 인물의 생년월일을 처리하고 전달하는 코드.
		if (detailsPersonDtoKoKR.getBirthday() != null) {
			// 인물의 생년월일을 LocalDate로 파싱
			LocalDate birthday = LocalDate.parse(detailsPersonDtoKoKR.getBirthday(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			// 나이 계산
			int age = personService.calculateAge(birthday);
			// 모델 객체에 인물의 나이 추가
			model.addAttribute("age", age);
		} else {
			// 생년월일 정보가 null인 경우, 나이 대신 "-" 표시
			model.addAttribute("age", "-");
		}
//		// 인물의 생년월일을 LocalDate 로 파싱.
//		LocalDate birthday = LocalDate.parse(detailsPersonDtoKoKR.getBirthday(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
//		// 나이 계산
//		int age = personService.calculateAge(birthday);
//		// 모델 객체에 인물의 나이 추가
//		model.addAttribute("age", age);

		ExternalIDsDto externalIDsDto = personService.getExternalIDs(id);

		CombinedCreditsDto combinedCreditsDtoEnUS = personService.getCombinedCreditsEnUS(id);
		CombinedCreditsDto combinedCreditsDtoKoKR = personService.getCombinedCreditsKoKR(id);

		List<CombinedCreditsCastDto> combinedCreditsCastListEnUS = personService.getCombinedCreditsCastEnUS(id);
		List<CombinedCreditsCastDto> combinedCreditsCastListKoKR = personService.getCombinedCreditsCastKoKR(id);

		List<CombinedCreditsCrewDto> combinedCreditsCrewListEnUS = personService.getCombinedCreditsCrewEnUS(id);
		List<CombinedCreditsCrewDto> combinedCreditsCrewListKoKR = personService.getCombinedCreditsCrewKoKR(id);

		/*
		  1. combinedCreditsDtoEnUS(KoKR)에서 cast 만 가져와서 리스트 생성.
		  2. castListEnUs(KoKR)를 정렬하기 위한 새로운 리스트 sortedCastListEnUS(KoKR) 생성.
		  3. sortedCastListEnUS(KoKR)를 투표 수를 기준으로 내림차순 정렬.
		 */
		List<CombinedCreditsCastDto> castListEnUS = combinedCreditsDtoEnUS.getCast();
		List<CombinedCreditsCastDto> sortedCastListEnUS = new ArrayList<>(castListEnUS);
		sortedCastListEnUS.sort(Comparator.comparingDouble(CombinedCreditsCastDto::getVoteCount).reversed());

		List<CombinedCreditsCastDto> castListKoKR = combinedCreditsDtoKoKR.getCast();
		List<CombinedCreditsCastDto> sortedCastListKoKR = new ArrayList<>(castListKoKR);
		sortedCastListKoKR.sort(Comparator.comparingDouble(CombinedCreditsCastDto::getVoteCount).reversed());

		/*
		  1. combinedCreditsDtoEnUS(KoKR)에서 crew 만 가져와서 리스트 생성.
		  2. crewListEnUs(KoKR)를 정렬하기 위한 새로운 리스트 sortedCrewListEnUS(KoKR) 생성.
		  3. sortedCrewListEnUS(KoKR)를 투표 수를 기준으로 내림차순 정렬.
		 */
		List<CombinedCreditsCrewDto> crewListEnUS = combinedCreditsDtoEnUS.getCrew();
		List<CombinedCreditsCrewDto> sortedCrewListEnUS = new ArrayList<>(crewListEnUS);
		sortedCrewListEnUS.sort(Comparator.comparingDouble(CombinedCreditsCrewDto::getVoteCount).reversed());

		List<CombinedCreditsCrewDto> crewListKoKR = combinedCreditsDtoKoKR.getCrew();
		List<CombinedCreditsCrewDto> sortedCrewListKoKR = new ArrayList<>(crewListEnUS);
		sortedCrewListKoKR.sort(Comparator.comparingDouble(CombinedCreditsCrewDto::getVoteCount).reversed());

		/*
		 * 중복 요소를 허용하지 않는 컬렉션(HashSet)을 사용하여, 중복을 제거한 Cast 리스트 생성.
		 * Cast 포스터 경로를 전달하기 위함. (다른 요소들도 가지고 있음)
		 */
		// 중복 요소를 허용하지 않는 컬렉션(HashSet)을 사용하여, 포스터 경로(path)가 고유한지 확인.
		Set<String> uniqueCastPosterPath = new HashSet<>();
		// Cast 필터링. (중복X, 고유한 값을 가지도록 필터링함)
		List<CombinedCreditsCastDto> uniqueCastListPosterEnUS = castListEnUS .stream()
				.filter(cast -> uniqueCastPosterPath.add(cast.getPosterPath()))
				.limit(10) // 10개만 가져오도록 함.
				.collect(Collectors.toList());
		List<CombinedCreditsCastDto> uniqueCastListPosterKoKR = castListKoKR .stream()
				.filter(cast -> uniqueCastPosterPath.add(cast.getPosterPath()))
				.limit(10) // 10개만 가져오도록 함.
				.collect(Collectors.toList());
		// 필터링한 Cast 를 voteCount 기준 내림차순 정렬.
		uniqueCastListPosterEnUS.sort(Comparator.comparingDouble(CombinedCreditsCastDto::getVoteCount).reversed());
		uniqueCastListPosterKoKR.sort(Comparator.comparingDouble(CombinedCreditsCastDto::getVoteCount).reversed());

		/*
		 * 중복 요소를 허용하지 않는 컬렉션(HashSet)을 사용하여, 중복을 제거한 Crew 리스트 생성.
		 * Crew 포스터 경로를 전달하기 위함. (다른 요소들도 가지고 있음)
		 */
		// 중복 요소를 허용하지 않는 컬렉션(HashSet)을 사용하여, 포스터 경로(path)가 고유한지 확인.
		Set<String> uniqueCrewPosterPath = new HashSet<>();
		// Crew 필터링. (중복X, 고유한 값을 가지도록 필터링함)
		List<CombinedCreditsCrewDto> uniqueCrewListPosterEnUS = crewListEnUS.stream()
				.filter(cast -> uniqueCrewPosterPath.add(cast.getPosterPath()))
				.limit(10) // 10개만 가져오도록 함.
				.collect(Collectors.toList());
		List<CombinedCreditsCrewDto> uniqueCrewListPosterKoKR = crewListKoKR.stream()
				.filter(cast -> uniqueCrewPosterPath.add(cast.getPosterPath()))
				.limit(10) // 10개만 가져오도록 함.
				.collect(Collectors.toList());
		// 필터링한 Crew 를 voteCount 기준 내림차순 정렬.
		uniqueCrewListPosterEnUS.sort(Comparator.comparingDouble(CombinedCreditsCrewDto::getVoteCount).reversed());
		uniqueCrewListPosterKoKR.sort(Comparator.comparingDouble(CombinedCreditsCrewDto::getVoteCount).reversed());

		/*
		 * 인물의 약력 부분에서 참여 작품을 작품 출시 날짜의 내림차순으로 정렬하여 보여주기 위한 코드.
		 * Cast 만 해당함.
		 */
		// 방법 1. combinedCreditsCastList 를 내림차순으로 정렬:
		// Comparator.nullFirst()를 사용하여 null 값이 있는 요소를 정렬된 목록의 가장 앞에 배치.
		// Comparator.nullLast() -> null 값이 있는 요소를 정렬된 목록의 가장 뒤에 배치.
		combinedCreditsCastListEnUS.sort(Comparator.comparing(CombinedCreditsCastDto::getYear, Comparator.nullsFirst(Comparator.reverseOrder())));
		combinedCreditsCastListKoKR.sort(Comparator.comparing(CombinedCreditsCastDto::getYear, Comparator.nullsFirst(Comparator.reverseOrder())));
		// 방법 2. Map 사용:
		// 연도별로 그룹화하고, 각 그룹을 내림차순으로 정렬
		Map<Year, List<CombinedCreditsCastDto>> groupedByYearCastEnUS = combinedCreditsCastListEnUS.stream()
				.filter(cast -> cast.getYear() != null) // 연도 정보가 있는 항목만 처리
				.collect(Collectors.groupingBy(CombinedCreditsCastDto::getYear, // 연도별로 그룹화
						Collectors.toList()));
		// 그룹화된 맵을 연도 내림차순으로 정렬
		Map<Year, List<CombinedCreditsCastDto>> sortedByYearDescCastEnUS = new TreeMap<>(Comparator.reverseOrder());
		sortedByYearDescCastEnUS.putAll(groupedByYearCastEnUS);

		/*
		 * 인물의 약력 부분에서 참여 작품을 작품 출시 날짜의 내림차순으로 정렬하여 보여주기 위한 코드 작성.
		 * Crew 만 해당함.
		 */
		// 방법 1. combinedCreditsCrewList 를 내림차순으로 정렬:
		// Comparator.nullFirst()를 사용하여 null 값이 있는 요소를 정렬된 목록의 가장 앞에 배치.
		// Comparator.nullLast() -> null 값이 있는 요소를 정렬된 목록의 가장 뒤에 배치.
		combinedCreditsCrewListEnUS.sort(Comparator.comparing(CombinedCreditsCrewDto::getYear, Comparator.nullsFirst(Comparator.reverseOrder())));
		combinedCreditsCrewListKoKR.sort(Comparator.comparing(CombinedCreditsCrewDto::getYear, Comparator.nullsFirst(Comparator.reverseOrder())));
		// 방법 2. Map 사용:
		// 연도별로 그룹화하고, 각 그룹을 내림차순으로 정렬
		Map<Year, List<CombinedCreditsCrewDto>> groupedByYearCrewEnUS = combinedCreditsCrewListEnUS.stream()
				.filter(crew -> crew.getYear() != null) // 연도 정보가 있는 항목만 처리
				.collect(Collectors.groupingBy(CombinedCreditsCrewDto::getYear, // 연도별로 그룹화
						Collectors.toList()));
		// 그룹화된 맵을 연도 내림차순으로 정렬
		Map<Year, List<CombinedCreditsCrewDto>> sortedByYearDescCrewEnUS = new TreeMap<>(Comparator.reverseOrder());
		sortedByYearDescCrewEnUS.putAll(groupedByYearCrewEnUS);

		/*
		 * KnownCreditsNameOrTitle(참여 작품 수)를 가지는 리스트.
		 * tv 면 name, movie 면 title 을 가짐.
		 * 다른 요소는 갖지 않음.
		 * HashSet을 사용하여 중복을 제거하여 사용해야 함.
		 * 중복을 제거한 참여 작품 수를 모델에 전달하기 위함.
		 */
		List<String> knownCreditsNameOrTitle = new ArrayList<>();
		for (int i=0; i<combinedCreditsCastListEnUS.size(); i++) {
			if (combinedCreditsCastListEnUS.get(i).getMediaType() != null && combinedCreditsCastListEnUS.get(i).getMediaType().equals("tv")) {
				knownCreditsNameOrTitle.add(combinedCreditsCastListEnUS.get(i).getName());
			} else if (combinedCreditsCastListEnUS.get(i).getMediaType() != null && combinedCreditsCastListEnUS.get(i).getMediaType().equals("movie")) {
				knownCreditsNameOrTitle.add(combinedCreditsCastListEnUS.get(i).getTitle());
			} else if (combinedCreditsCastListEnUS.get(i) == null) {
				log.info("{}번째 미디어 타입이 null입니다...", i);
			}
		}
		for (int i=0; i<combinedCreditsCrewListEnUS.size(); i++) {
			if (combinedCreditsCrewListEnUS.get(i).getMediaType() != null && combinedCreditsCrewListEnUS.get(i).getMediaType().equals("tv")) {
				knownCreditsNameOrTitle.add(combinedCreditsCrewListEnUS.get(i).getName());
			} else if (combinedCreditsCrewListEnUS.get(i).getMediaType() != null && combinedCreditsCrewListEnUS.get(i).getMediaType().equals("movie")) {
				knownCreditsNameOrTitle.add(combinedCreditsCrewListEnUS.get(i).getTitle());
			} else if (combinedCreditsCrewListEnUS.get(i) == null) {
				log.info("{}번째 미디어 타입이 null입니다...", i);
			}
		}
		// name, title 만을 가지는 것이 아닌 모든 요소를 가지는 knownCreditsAllCast 리스트 생성.
		List<CombinedCreditsCastDto> knownCreditsAllCast = new ArrayList<>(combinedCreditsCastListEnUS);
		// name, title 만을 가지는 것이 아닌 모든 요소를 가지는 knownCreditsAllCrew 리스트 생성.
		List<CombinedCreditsCrewDto> knownCreditsAllCrew = new ArrayList<>(combinedCreditsCrewListEnUS);
		// *** 이 아래에서 중복을 제거 ***
		// knownCredits 값이 어떻게 저장되어 있는지 확인
		log.info("===============================================");
//		log.info("** 중복 제거 전 ** knownCreditsNameOrTitleSize(중복 제거 전 참여 작품 수)={}", knownCreditsNameOrTitle.size());
		// HastSet을 사용하여 중복을 제거. (name 과 title 만을 가지는 knownCreditsNameOrTitle 리스트를 중복 제거 처리)
		Set<String> uniqueKnownCreditsNameOrTitle = new HashSet<>(knownCreditsNameOrTitle);
		log.info("===============================================");
//		log.info("** 중복 제거 후 ** uniqueKnownCreditsNameOrTitleSize(중복 제거 후 참여 작품 수(set))={}", uniqueKnownCreditsNameOrTitle.size());
		// 증복을 제거한 uniqueKnownCreditsNameOrTitle 을 다시 리스트로 변환.
		List<String> uniqueKnownCreditsNameOrTitleList = new ArrayList<>(uniqueKnownCreditsNameOrTitle);
		log.info("===============================================");
//		log.info("** 중복 제거 후 ** uniqueKnownCreditsNameOrTitleListSize(중복 제거 후 참여 작품 수(리스트))={}", uniqueKnownCreditsNameOrTitleList.size());
		// ===============================  구분선  =============================== //
		// HashSet을 사용하여 중복을 제거. (모든 요소를 가지는 리스트를 중복 제거 처리)
		Set<CombinedCreditsCastDto> uniqueKnownCreditsAllCast = new HashSet<>(knownCreditsAllCast);
		Set<CombinedCreditsCrewDto> uniqueKnownCreditsAllCrew = new HashSet<>(knownCreditsAllCrew);
		// 중복을 제거한 모든 요소를 가지는 set 을 다시 리스트로 변환.
		List<CombinedCreditsCastDto> uniqueKnownCreditsAllCastList = new ArrayList<>(uniqueKnownCreditsAllCast);
		log.info("===============================================");
//		log.info("** 모든 요소를 가지는 Cast 리스트 중복 제거 후 리스트의 크기 ** uniqueKnownCreditsAllCastListSize(중복이 제거된 모든 Cast 요소를 가지는 리스트의 크기)={}", uniqueKnownCreditsAllCastList.size());
		List<CombinedCreditsCrewDto> uniqueKnownCreditsAllCrewList = new ArrayList<>(uniqueKnownCreditsAllCrew);
		log.info("===============================================");
//		log.info("** 모든 요소를 가지는 Crew 리스트 중복 제거 후 리스트의 크기 ** uniqueKnownCreditsAllCrewListSize(중복이 제거된 모든 Crew 요소를 가지는 리스트의 크기)={}", uniqueKnownCreditsAllCrewList.size());

		// 필터링한 CastList 를 모델에 추가. (중복 제거 O)
		model.addAttribute("uniqueCastListPosterEnUS", uniqueCastListPosterEnUS);
		model.addAttribute("uniqueCastListPosterKoKR", uniqueCastListPosterKoKR);
		// 필터링한 CrewList 를 모델에 추가. (중복 제거 O)
		model.addAttribute("uniqueCrewListPosterEnUS", uniqueCrewListPosterEnUS);
		model.addAttribute("uniqueCrewListPosterKoKR", uniqueCrewListPosterKoKR);
		// uniqueKnownCreditsList(중복이 제거된 참여 작품 이름 또는 제목만을 담은 리스트)를 모델에 추가. (중복 제거 O)
		model.addAttribute("uniqueKnownCreditsNameORTitleList", uniqueKnownCreditsNameOrTitleList);
		// 중복을 제거한 모든 Cast 요소를 가진 리스트를 모델에 추가. (중복 제거 O)
		model.addAttribute("uniqueAllCastList", uniqueKnownCreditsAllCastList);
		// 중복을 제거한 모든 Crew 요소를 가진 리스트를 모델에 추가. (중복 제거 O)
		model.addAttribute("uniqueAllCrewList", uniqueKnownCreditsAllCrewList);
		// 인물의 상세 정보를 가진 객체를 모델에 추가.
		model.addAttribute("detailsPersonEnUS", detailsPersonDtoEnUS);
		model.addAttribute("detailsPersonKoKR", detailsPersonDtoKoKR);
		// 인물의 외부 링크 정보(sns, 홈페이지 등)를 가진 객체를 모델에 추가.
		model.addAttribute("externalIDs", externalIDsDto);
		// 해당 인물이 cast(연기) 또는 crew(제작 등)로 참여한 모든 출연작(영화, TV 프로그램)의 정보를 가진 객체를 모델에 추가.
		model.addAttribute("combinedCreditsEnUS", combinedCreditsDtoEnUS);
		model.addAttribute("combinedCreditsKoKR", combinedCreditsDtoKoKR);
		// 해당 인물이 cast(연기)로 참여한 모든 출연작(영화, TV 프로그램)의 정보를 가진 리스트를 모델에 추가.
		model.addAttribute("combinedCreditsCastListEnUS", combinedCreditsCastListEnUS);
		model.addAttribute("combinedCreditsCastListKoKR", combinedCreditsCastListKoKR);
		// 해당 인물이 crew(제작 등)로 참여한 모든 출연작(영화, TV 프로그램)의 정보를 가진 리스트를 모델에 추가.
		model.addAttribute("combinedCreditsCrewListEnUS", combinedCreditsCrewListEnUS);
		model.addAttribute("combinedCreditsCrewListKoKR", combinedCreditsCrewListKoKR);

		return "person/person-details";
	} // end details



}
