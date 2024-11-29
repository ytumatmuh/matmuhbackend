package com.matmuh.matmuhsite.webAPI.controllers;

import com.matmuh.matmuhsite.business.abstracts.FileService;
import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.entities.File;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("api/files")
public class FileController {

    private final FileService fileService;

    @Value("${api.url}")
    private String API_URL;

    @Value("${file.get.url}")
    private String FILE_GET_URL;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }


    @PostMapping("/addFile")
    public ResponseEntity<DataResult<File>> addFile(@RequestParam("file") MultipartFile file){
        var result = fileService.addFile(file);


        if (!result.isSuccess()){
            return ResponseEntity.badRequest().body(result);
        }

        result.getData().setData(null);
        result.getData().setUrl(API_URL+FILE_GET_URL+result.getData().getUrl());

        return ResponseEntity.ok().body(result);

    }

    @GetMapping("/getFileDetailsByUrl/{url}")
    public ResponseEntity<DataResult<File>> getFileDetailsByUrl(@PathVariable String url){
        var result = fileService.getFileByUrl(url);

        if (!result.isSuccess()){
            return ResponseEntity.badRequest().body(result);
        }

        result.getData().setData(null);
        result.getData().setUrl(API_URL+FILE_GET_URL+result.getData().getUrl());

        return ResponseEntity.ok().body(result);
    }
    @GetMapping("/getFileByUrl/{url}")
    public ResponseEntity<byte[]> getFileByUrl(@PathVariable String url) {
        var result = fileService.getFileByUrl(url);

        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(null);
        }

        result.getData().setUrl(API_URL + FILE_GET_URL + result.getData().getUrl());

        String filename = result.getData().getName();

        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");

        String dispositionType = "attachment";
        String contentType = result.getData().getType();

        if (contentType.equalsIgnoreCase("application/pdf")) {
            dispositionType = "inline";
        }

        String contentDisposition = dispositionType + "; filename*=UTF-8''" + encodedFilename;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf(contentType));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, contentDisposition);

        return new ResponseEntity<>(result.getData().getData(), headers, HttpStatus.OK);
    }
}
