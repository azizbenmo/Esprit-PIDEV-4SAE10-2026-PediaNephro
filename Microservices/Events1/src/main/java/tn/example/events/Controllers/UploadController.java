package tn.example.events.Controllers;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Map;

@RestController
@RequestMapping("/uploads")
/*@CrossOrigin(origins = "http://localhost:4200")*/
public class UploadController {

    @PostMapping("/event-image")
    public ResponseEntity<Map<String, String>> uploadEventImage(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) throws IOException {
        return upload(file, "uploads/events/", "events", request);
    }

    @PostMapping("/event-video")
    public ResponseEntity<Map<String, String>> uploadEventVideo(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) throws IOException {
        return upload(file, "uploads/videos/", "videos", request);
    }

    @PostMapping("/logo")
    public ResponseEntity<Map<String, String>> uploadLogo(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) throws IOException {
        return upload(file, "uploads/logos/", "logos", request);
    }

    private ResponseEntity<Map<String, String>> upload(
            MultipartFile file, String dir, String type, HttpServletRequest request) throws IOException {

        File folder = new File(dir);
        if (!folder.exists()) folder.mkdirs();

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(dir + fileName);
        Files.write(filePath, file.getBytes());

        String base = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        String url = base + "/uploads/" + type + "/" + fileName;
        return ResponseEntity.ok(Map.of("url", url));
    }
}
