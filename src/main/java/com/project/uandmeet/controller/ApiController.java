package com.project.uandmeet.controller;

import com.project.uandmeet.api.OpenApiManager;
import com.project.uandmeet.api.OpenApiResponseParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.parser.ParseException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.List;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ApiController {
    private final OpenApiManager openApiManager;
    private final Path imageDirectory = Paths.get("src\\main\\resources\\static\\images");

    // @PostMapping("/allow_info/basic")
    // public List<OpenApiResponseParams> fetch() throws UnsupportedEncodingException, ParseException {
    //     return openApiManager.fetch();
    // }

    // @GetMapping("/allow_info/dataRequest")
    // public List<OpenApiResponseParams> dataRequest() {
    //     return openApiManager.dataRequest();
    // }

    // image 조회
    @GetMapping("/images/{filename}")
    public ResponseEntity<Resource> getImage(@PathVariable("filename") String filename) {
        try {
            Path imagePath = imageDirectory.resolve(filename);
            Resource resource = new UrlResource(imagePath.toUri());

            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch(MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }

}
