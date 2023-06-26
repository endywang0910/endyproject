package com.synchrony.endyproject.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synchrony.endyproject.dto.ImgurResponse;
import com.synchrony.endyproject.entity.Image;
import com.synchrony.endyproject.entity.User;
import com.synchrony.endyproject.exception.ImageNotFoundException;
import com.synchrony.endyproject.repository.ImageRepository;
import com.synchrony.endyproject.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

@Service
public class ImageService {

    @Value("${imgur.client.id}")
    private String clientId;

    @Value("${kafka.topic.name}")
    private String kafkaTopic;

    private final ImageRepository imageRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public ImageService(ImageRepository imageRepository, UserRepository userRepository, UserService userService, KafkaTemplate<String, String> kafkaTemplate) {
        this.imageRepository = imageRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.kafkaTemplate = kafkaTemplate;
    }

    public Image uploadImage(User user, MultipartFile file) throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Client-ID " + clientId);

        String body = null;
        try {
            body = Base64.getEncoder().encodeToString(file.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

        HttpEntity<String> request = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity("https://api.imgur.com/3/image", request, String.class);

        // Now parse the response and extract the necessary fields
        ObjectMapper objectMapper = new ObjectMapper();
        String responseBody = response.getBody();
        ImgurResponse imgurResponse = objectMapper.readValue(responseBody, ImgurResponse.class);

        // Create new image and save in repository
        Image image = new Image();
        image.setUrl(imgurResponse.data.link);
        image.setImgurId(imgurResponse.data.id);
        image.setUser(user);
        Image savedImage = imageRepository.save(image);

        // Send message to Kafka
        kafkaTemplate.send(kafkaTopic, "User: " + user.getUsername() + ", Imgur Id: " + savedImage.getImgurId());

        return savedImage;
    }

    public List<Image> getUserImages(User user) {
        // Fetch all images associated with the given user
        return imageRepository.findByUser(user);
    }

    public String viewImage(User user, String imgurId) {
        // Find the image in your database
        Image image = imageRepository.findByImgurIdAndUser(imgurId, user);

        if (image == null) {
            // Image not found, return an appropriate message or throw an exception
            throw new IllegalArgumentException("Image not found for the user");
        }

        // The image URL is already in your database, so you can just return it
        return image.getUrl();
    }

    public String deleteImage(User user, String imgurId) {
        Image image = imageRepository.findByImgurIdAndUser(imgurId, user);

        if (image == null) {
            throw new ImageNotFoundException("Image not found for the user");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Client-ID " + clientId);

        HttpEntity<String> request = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange("https://api.imgur.com/3/image/" + imgurId, HttpMethod.DELETE, request, String.class);

        // Delete the image from your database
        imageRepository.delete(image);

        return response.getBody();
    }

}
