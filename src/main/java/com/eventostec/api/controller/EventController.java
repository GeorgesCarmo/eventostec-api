package com.eventostec.api.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.eventostec.api.domain.event.Event;
import com.eventostec.api.domain.event.EventDetailsDTO;
import com.eventostec.api.domain.event.EventRequestDTO;
import com.eventostec.api.domain.event.EventResponseDTO;
import com.eventostec.api.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

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

    @GetMapping
    public ResponseEntity<List<EventResponseDTO>> getEvents(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size){
        List<EventResponseDTO> allEvents = this.eventService.getUpcomingEvents(page, size);
        return ResponseEntity.ok(allEvents);
    }

    @GetMapping("/filter")
    public ResponseEntity<List<EventResponseDTO>> getFilteredEvents(@RequestParam(defaultValue = "0") int page,
                                                                    @RequestParam(defaultValue = "10") int size,
                                                                    @RequestParam String city,
                                                                    @RequestParam String uf,
                                                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
                                                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate) {
        List<EventResponseDTO> events = eventService.getFilteredEvents(page, size, city, uf, startDate, endDate);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventDetailsDTO> getEventDetails(@PathVariable UUID eventId) {
        EventDetailsDTO eventDetails = eventService.getEventDetails(eventId);
        return ResponseEntity.ok(eventDetails);
    }
}
