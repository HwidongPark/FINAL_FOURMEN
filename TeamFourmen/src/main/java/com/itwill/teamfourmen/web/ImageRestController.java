package com.itwill.teamfourmen.web;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.itwill.teamfourmen.service.ImageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
public class ImageRestController {
	
	private final ImageService imageService;

	
	@GetMapping("/image/proxy")
	public ResponseEntity<ByteArrayResource> renderFromUrlImage(@RequestParam(name = "url") String url) throws IOException {
		
		log.info("renderFromUrlImage(url={})", url);
		String decodedUrl = URLDecoder.decode(url, "UTF-8");
		log.info("decoded url={}", decodedUrl);
		ByteArrayResource imageResource = imageService.renderFromUrlImage(decodedUrl);
		
		HttpHeaders header = new HttpHeaders();
		header.setContentType(MediaType.IMAGE_JPEG);
		
		return ResponseEntity.ok().headers(header).body(imageResource);
	}
	
	@GetMapping(value = "/uploaded/file/{sFileName}", produces = {MediaType.IMAGE_PNG_VALUE, MediaType.IMAGE_JPEG_VALUE})
	public ResponseEntity<Resource> readFileBinary(@PathVariable(name = "sFileName") String sFileName) throws IOException {
		log.info("readFilePath(sFileName={})", sFileName);
		
		String filePath = "C:\\images\\ckeditor" + File.separator + sFileName;
		Path path = Paths.get(filePath);
		
		Resource resource = new FileSystemResource(filePath);
		
		
		
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", Files.probeContentType(path));
		
		return ResponseEntity.ok().headers(headers).body(resource);
	}
	
	
}
