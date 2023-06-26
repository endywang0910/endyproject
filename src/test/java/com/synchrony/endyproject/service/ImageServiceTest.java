package com.synchrony.endyproject.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.synchrony.endyproject.dto.ImgurResponse;
import com.synchrony.endyproject.entity.Image;
import com.synchrony.endyproject.entity.User;
import com.synchrony.endyproject.repository.ImageRepository;
import com.synchrony.endyproject.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@SpringBootTest
public class ImageServiceTest {

    @MockBean
    private ImageRepository imageRepository;

    @MockBean
    private UserRepository userRepository;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private ImageService imageService;

    private UserService userService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        imageService = new ImageService(imageRepository, userRepository, userService, kafkaTemplate);
    }

    @Test
    public void getUserImagesTest() {
        // Arrange
        User user = new User(); // Initialize user object as per your requirement

        Image image1 = new Image();
        image1.setUser(user);
        Image image2 = new Image();
        image2.setUser(user);
        List<Image> expectedImages = Arrays.asList(image1, image2);

        when(imageRepository.findByUser(user)).thenReturn(expectedImages);

        // Act
        List<Image> resultImages = imageService.getUserImages(user);

        // Assert
        assertEquals(expectedImages.size(), resultImages.size());
    }

    @Test
    public void viewImageTest() {
        // Arrange
        User user = new User(); // Initialize user object as per your requirement
        String imgurId = "123456";

        Image expectedImage = new Image();
        expectedImage.setImgurId(imgurId);
        expectedImage.setUser(user);

        when(imageRepository.findByImgurIdAndUser(imgurId, user)).thenReturn(expectedImage);

        // Act
        String resultUrl = imageService.viewImage(user, imgurId);

        // Assert
        assertEquals(expectedImage.getUrl(), resultUrl);
    }

    @Test
    public void deleteImageTest() {
        // Arrange
        User user = new User(); // Initialize user object as per your requirement
        String imgurId = "123456";

        Image image = new Image();
        image.setImgurId(imgurId);
        image.setUser(user);

        ResponseEntity<String> mockResponseEntity = mock(ResponseEntity.class);
        when(mockResponseEntity.getBody()).thenReturn("Deleted");

        when(imageRepository.findByImgurIdAndUser(imgurId, user)).thenReturn(image);
        when(restTemplate.exchange(anyString(), any(), any(), eq(String.class))).thenReturn(mockResponseEntity);

        // Act
        String response = imageService.deleteImage(user, imgurId);

        // Assert
        assertEquals("Deleted", response);
        verify(imageRepository, times(1)).delete(image);
    }
}
