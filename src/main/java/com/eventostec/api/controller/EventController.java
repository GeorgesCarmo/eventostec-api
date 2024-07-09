package com.eventostec.api.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.eventostec.api.domain.event.Event;
import com.eventostec.api.domain.event.EventRequestDTO;
import com.eventostec.api.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping(path = "/api/event")
public class EventController {

    @Autowired
    private EventService eventService;

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<Event> create(@RequestParam ("title") String title,
                                        @RequestParam (value = "description", required = false) String description,
                                        @RequestParam ("date") Long date,
                                        @RequestParam ("city") String city,
                                        @RequestParam ("state") String state,
                                        @RequestParam ("remote") Boolean remote,
                                        @RequestParam ("eventUrl") String eventUrl,
                                        @RequestParam (value = "image", required = false)MultipartFile image) throws IOException {
        EventRequestDTO eventRequestDTO = new EventRequestDTO(title, description, date, city, state, remote, eventUrl, image);
        Event newEvent = this.eventService.createEvent(eventRequestDTO);
        return ResponseEntity.ok(newEvent);
    }

    @PostMapping("/fileUpload")
    public ResponseEntity<Event> saveFile(@RequestParam ("title") String title,
                           @RequestParam (value = "description", required = false) String description,
                           @RequestParam ("date") Long date,
                           @RequestParam ("city") String city,
                           @RequestParam ("state") String state,
                           @RequestParam ("remote") Boolean remote,
                           @RequestParam ("eventUrl") String eventUrl,
                           @RequestParam (value = "image", required = false)MultipartFile image) throws IOException {
        EventRequestDTO eventRequestDTO = new EventRequestDTO(title, description, date, city, state, remote, eventUrl, image);
        Event newEvent = this.eventService.createEvent(eventRequestDTO);
        String fileName = image.getOriginalFilename();
        try {
            eventService.uploadToS3(image.getInputStream(), fileName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok(newEvent);
    }
}
