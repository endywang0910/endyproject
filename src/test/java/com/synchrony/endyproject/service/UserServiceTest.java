package com.synchrony.endyproject.service;

import com.synchrony.endyproject.entity.Image;
import com.synchrony.endyproject.entity.User;
import com.synchrony.endyproject.repository.UserRepository;
import com.synchrony.endyproject.service.ImageService;
import com.synchrony.endyproject.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@SpringBootTest
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ImageService imageService;

    @InjectMocks
    private UserService userService;

    public UserServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void findByUsernameTest() {
        User user = new User();
        user.setUsername("testUser");
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));

        User foundUser = userService.findByUsername("testUser");

        assertEquals(user, foundUser);
    }

    @Test
    public void findByUsernameUserNotFoundTest() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> userService.findByUsername("testUser"));
    }

    @Test
    public void createUserTest() {
        User user = new User();
        user.setUsername("testUser");
        when(userRepository.save(any(User.class))).thenReturn(user);

        User savedUser = userService.createUser(user);

        assertEquals(user, savedUser);
        ArgumentCaptor<User> argumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(argumentCaptor.capture());
        assertEquals(user.getUsername(), argumentCaptor.getValue().getUsername());
    }

    @Test
    public void getUserDetailsTest() {
        User user = new User();
        user.setId(1L);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        List<Image> images = new ArrayList<>();
        when(imageService.getUserImages(any(User.class))).thenReturn(images);

        User userDetails = userService.getUserDetails(1L);

        assertEquals(user, userDetails);
        assertEquals(images, userDetails.getImages());
    }

    @Test
    public void getUserDetailsNotFoundTest() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> userService.getUserDetails(1L));
    }
}

