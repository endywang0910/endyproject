package com.synchrony.endyproject.repository;

import com.synchrony.endyproject.entity.Image;
import com.synchrony.endyproject.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Long> {
    List<Image> findByUser(User user);

    Image findByImgurIdAndUser(String imgurId, User user);
}
