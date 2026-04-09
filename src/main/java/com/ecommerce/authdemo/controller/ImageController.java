package com.ecommerce.authdemo.controller;

import com.ecommerce.authdemo.service.ImageUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

    @RestController
    @RequestMapping("/image")
    public class ImageController {

        @Autowired
        private ImageUploadService imageUploadService;

        @PostMapping("/upload")
        public String upload(@RequestParam("file") MultipartFile file) {

            return imageUploadService.uploadImage(file);

        }
    }

