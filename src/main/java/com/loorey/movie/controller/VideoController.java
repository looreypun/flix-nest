package com.loorey.movie.controller;

import com.loorey.movie.entity.Video;
import com.loorey.movie.repository.VideoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class VideoController {
    private final VideoRepository videoRepository;

    @Autowired
    public VideoController(VideoRepository videoRepository) {
        this.videoRepository = videoRepository;
    }

    @GetMapping("/video")
    public List<Video> index(@RequestParam(required = false) String title) {
        List<Video> videos;

        if (StringUtils.hasText(title)) {
            videos = videoRepository.findByTitleContaining(title);
        } else {
            videos = new ArrayList<>(videoRepository.findAll());
        }
        return videos;
    }
}
