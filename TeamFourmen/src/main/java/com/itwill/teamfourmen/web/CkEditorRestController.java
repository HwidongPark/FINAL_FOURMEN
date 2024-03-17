package com.itwill.teamfourmen.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartRequest;

import com.itwill.teamfourmen.service.CkEditorService;
import com.itwill.teamfourmen.service.MovieApiUtil;

import jakarta.servlet.ServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping("/ckeditor")
public class CkEditorRestController {

	private final CkEditorService ckEditorService;

	@Value("${app.context-root}")
	private String contextRoot;

	@PostMapping("/image/upload")
	public Map<String, Object> uploadImage(MultipartRequest request, ServletRequest servletRequest) throws IllegalStateException, IOException {
		log.info("uploadImage(imgFile={})", request);
		Map<String, Object> responseData = new HashMap<>();

		// 서버의 폴더위치
		String sDirectory = servletRequest.getServletContext().getRealPath("");
		log.info("sDirectory={}", sDirectory);

		String s3Url = ckEditorService.imageUpload(request, sDirectory);

		responseData.put("uploaded", true);
		responseData.put("url", s3Url);
		log.info("이미지 주소={}", s3Url);
		

		return responseData;
	}

}
