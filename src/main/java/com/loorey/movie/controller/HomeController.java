package com.loorey.movie.controller;

import com.loorey.movie.entity.User;
import com.loorey.movie.entity.Video;
import com.loorey.movie.repository.UserRepository;
import com.loorey.movie.repository.VideoRepository;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Controller
public class HomeController {
    private final UserRepository userRepository;
    private final VideoRepository videoRepository;

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    public HomeController(
        UserRepository userRepository,
        VideoRepository videoRepository
    )
    {
        this.userRepository = userRepository;
        this.videoRepository = videoRepository;
    }

    @GetMapping("/")
    public String index(Model model) {
        List<Video> videos = new ArrayList<Video>(videoRepository.findAll());
        model.addAttribute("videos", videos);
        return "home";
    }

    @GetMapping("/video/{id}")
    public String show(Model model, @PathVariable("id") Long id) {
        Video video = videoRepository.findById(id).orElse(null);
        model.addAttribute("video", video);
        return "video/index";
    }

    @GetMapping("/upload")
    public String create(Model model) {
        model.addAttribute("video", new Video());
        return "upload";
    }

    @PostMapping("/upload")
    public String create(@Valid Video video, BindingResult result, @RequestParam("video") MultipartFile file ) {
        // Validate the video object
        if (result.hasErrors()) {
            return "upload";
        }

        if (!video.getUrl().isEmpty()) {
            User user = userRepository.findById(1L).orElse(null);
            video.setUser(user);
            videoRepository.save(video);
            return "redirect:/";
        }

        System.out.println(video);

        // Check if file is empty
        if (file.isEmpty()) {
            result.rejectValue("url", "url.empty", "File Not Selected");
            return "upload";
        }

        try {
            String originalFileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
            String fileExtension = com.google.common.io.Files.getFileExtension(originalFileName);
            String fileName = generateHash(originalFileName) + "." + fileExtension; // New hashed filename

            User user = userRepository.findById(1L).orElse(null);

            video.setUrl(fileName);
            video.setUser(user);
            videoRepository.save(video);

            String uploadDir = "src/main/resources/static/videos";
            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            try (InputStream inputStream = file.getInputStream()) {
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                logger.error("Error occurred while saving the file", e);
                result.rejectValue("url", "url.error", "Video Upload Failed");
                return "upload";
            }
        } catch (Exception e) {
            // Handle other exceptions
            result.rejectValue("url", "url.error", "Video Upload Failed");
            logger.error("Error occurred while uploading the file", e);
            return "upload";
        }

        return "redirect:/";
    }

    private String generateHash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(input.getBytes());

            // Convert the byte array to a hexadecimal string
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            logger.error("Error occurred while hashing video filename", e);
            return null;
        }
    }
}
