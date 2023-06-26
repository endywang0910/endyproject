package com.synchrony.endyproject.controller;

import com.synchrony.endyproject.entity.Image;
import com.synchrony.endyproject.entity.User;
import com.synchrony.endyproject.service.ImageService;
import com.synchrony.endyproject.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/images")
public class ImageController {

    private UserService userService;
    private ImageService imageService;

    @Autowired
    public ImageController(UserService userService, ImageService imageService) {
        this.userService = userService;
        this.imageService = imageService;
    }

    @PostMapping
    @PreAuthorize("#userId == authentication.principal.id")
    public ResponseEntity<Image> uploadImage(@PathVariable Long userId, @RequestParam MultipartFile file) throws Exception {
        User user = userService.getUserDetails(userId);
        return new ResponseEntity<>(imageService.uploadImage(user, file), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("#userId == authentication.principal.id")
    public ResponseEntity<List<Image>> getUserImages(@PathVariable Long userId) {
        User user = userService.getUserDetails(userId);
        return new ResponseEntity<>(imageService.getUserImages(user), HttpStatus.OK);
    }

    @GetMapping("/{imgurId}")
    @PreAuthorize("#userId == authentication.principal.id")
    public ResponseEntity<String> viewImage(@PathVariable Long userId, @PathVariable String imgurId) {
        User user = userService.getUserDetails(userId);
        return new ResponseEntity<>(imageService.viewImage(user, imgurId), HttpStatus.OK);
    }

    @DeleteMapping("/{imgurId}")
    @PreAuthorize("#userId == authentication.principal.id")
    public ResponseEntity<String> deleteImage(@PathVariable Long userId, @PathVariable String imgurId) {
        User user = userService.getUserDetails(userId);
        return new ResponseEntity<>(imageService.deleteImage(user, imgurId), HttpStatus.OK);
    }
}

