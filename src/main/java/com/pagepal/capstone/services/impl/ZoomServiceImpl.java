package com.pagepal.capstone.services.impl;

import com.pagepal.capstone.dtos.recording.MeetingRecordings;
import com.pagepal.capstone.dtos.recording.RecordingDto;
import com.pagepal.capstone.dtos.zoom.AuthZoomResponseDto;
import com.pagepal.capstone.dtos.zoom.ZoomPlan;
import com.pagepal.capstone.entities.postgre.Meeting;
import com.pagepal.capstone.entities.postgre.Reader;
import com.pagepal.capstone.enums.MeetingEnum;
import com.pagepal.capstone.repositories.MeetingRepository;
import com.pagepal.capstone.services.ZoomService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Transactional
@Service
public class ZoomServiceImpl implements ZoomService {

    @Value("${zoom.accountId}")
    private String accountId;

    @Value("${zoom.basicAuthToken}")
    private String zoomBasicAuthToken;
    private final MeetingRepository meetingRepository;
    private final WebClient webClientAuth;
    private final WebClient webClientMeeting;
    private final String zoomAuthUrl = "https://zoom.us/oauth/token/";
    private final String baseUrl = "https://api.zoom.us/v2";
//    private final String zoomMeetingUrl = "https://api.zoom.us/v2/users/me/meetings";
//    private final String zoomRecordingUrl = "https://api.zoom.us/v2/meetings/%s/recordings";

    @Autowired
    public ZoomServiceImpl(WebClient.Builder webClientBuilder, MeetingRepository meetingRepository) {
        this.webClientAuth = webClientBuilder
                .baseUrl(zoomAuthUrl)
                .build();
        this.webClientMeeting = webClientBuilder.baseUrl(baseUrl).build();
        this.meetingRepository = meetingRepository;
    }

    public AuthZoomResponseDto getZoomToken() {
        return webClientAuth.post()
                .uri(UriBuilder::build)
                .header(HttpHeaders.AUTHORIZATION, "Basic " + zoomBasicAuthToken)
                .body(BodyInserters.fromFormData("grant_type", "account_credentials")
                        .with("account_id", accountId))
                .retrieve()
                .bodyToMono(AuthZoomResponseDto.class)
                .block();
    }

    public Meeting createMeeting(Reader reader, String agenda, Integer duration, String topic, Date startTime) {
        String accessToken = getZoomToken().getAccess_token();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("agenda", agenda);
        requestBody.put("duration", duration);
        requestBody.put("recurrence", Map.of("type", 1));
        requestBody.put("settings", Map.of("auto_recording", "cloud", "waiting_room", false));
        requestBody.put("type", 2);
        requestBody.put("start_time", startTime);
        requestBody.put("topic", topic);
        requestBody.put("waiting_room", false);
        requestBody.put("join_before_host", true);

        MeetingResponse response = webClientMeeting.post()
                .uri(UriBuilder -> UriBuilder
                        .path("/users/me/meetings")
                        .build()
                )
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(MeetingResponse.class)
                .block();

        if (response == null) {
            throw new RuntimeException("Failed to create meeting");
        }
        Meeting meeting = new Meeting();
        meeting.setCreateAt(new Date());
        meeting.setMeetingCode(String.valueOf(response.id));
        meeting.setPassword(response.password);
        meeting.setStartAt(response.start_time);
        meeting.setState(MeetingEnum.AVAILABLE);
        meeting = meetingRepository.save(meeting);
        return meeting;
    }

    public Meeting createInterviewMeeting(String agenda, Integer duration, String topic, Date startTime) {
        String accessToken = getZoomToken().getAccess_token();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("agenda", agenda);
        requestBody.put("duration", duration);
        requestBody.put("recurrence", Map.of("type", 1));
        requestBody.put("settings", Map.of("auto_recording", "cloud", "waiting_room", false));
        requestBody.put("type", 2);
        requestBody.put("start_time", startTime);
        requestBody.put("topic", topic);
        requestBody.put("waiting_room", false);
        requestBody.put("join_before_host", true);

        MeetingResponse response = webClientMeeting.post()
                .uri(UriBuilder -> UriBuilder
                        .path("/users/me/meetings")
                        .build()
                )
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(MeetingResponse.class)
                .block();

        if (response == null) {
            throw new RuntimeException("Failed to create meeting");
        }
        Meeting meeting = new Meeting();
        meeting.setCreateAt(new Date());
        meeting.setMeetingCode(String.valueOf(response.id));
        meeting.setPassword(response.password);
        meeting.setStartAt(response.start_time);
        meeting.setState(MeetingEnum.AVAILABLE);
        meeting = meetingRepository.save(meeting);
        return meeting;
    }

    public RecordingDto getRecording(String meetingId) {
        String accessToken = getZoomToken().getAccess_token();
        RecordingDto record = null;
        try {
            record = webClientMeeting.get()
                    .uri(UriBuilder -> UriBuilder
                            .path("/meetings/" + meetingId + "/recordings")
                            .queryParam("include_fields", "download_access_token")
                            .queryParam("ttl", 604800)
                            .build())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(RecordingDto.class)
                    .block();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return record;
    }

    public MeetingRecordings getListRecordingByMeetingId(String meetingId) {
        String accessToken = getZoomToken().getAccess_token();

        LocalDate currentDate = LocalDate.now();
        int currentYear = currentDate.getYear();

        LocalDate firstDayOfYear = LocalDate.of(currentYear, 1, 1);
        String formattedFirstDay = firstDayOfYear.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        LocalDate lastDayOfYear = LocalDate.of(currentYear, 12, 31);
        String formattedLastDay = lastDayOfYear.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        MeetingRecordings record = null;
        try {
            record = webClientMeeting.get()
                    .uri(UriBuilder -> UriBuilder
                            .path("/users/me/recordings")
                            .queryParam("meeting_id", meetingId)
                            .queryParam("from", formattedFirstDay)
                            .queryParam("to", formattedLastDay)
                            .build())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(MeetingRecordings.class)
                    .block();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return record;
    }

    @Override
    public ZoomPlan getZoomPlan() {
        String accessToken = getZoomToken().getAccess_token();

        return webClientMeeting.get()
                .uri(UriBuilder -> UriBuilder
                        .path("/accounts/me/plans/usage")
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(ZoomPlan.class)
                .block();

    }

    public static class Recurrence {
        public int type;
    }

    public static class MeetingCreate {
        public String agenda;
        public int duration;
        public Recurrence recurrence;
        public Settings settings;
        public Date start_time;
        public String topic;
        public int type;
    }

    public static class Settings {
        public String auto_recording;
    }

    public static class MeetingResponse {
        public long id;
        public String topic;
        public int type;
        public Date start_time;
        public int duration;
        public String agenda;
        public String password;
    }
}



