package com.pagepal.capstone.controllers;

import com.pagepal.capstone.dtos.event.*;
import com.pagepal.capstone.services.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class EventController {
	private final EventService eventService;

	@QueryMapping("getEventById")
	public EventDto getEventById(@Argument(name = "id") UUID id) {
		return eventService.getEvent(id);
	}

	@QueryMapping("getAllEvents")
	public ListEventDto getAllEvents(
			@Argument(name = "page") Integer page,
			@Argument(name = "pageSize") Integer pageSize,
			@Argument(name = "sort") String sort) {
		return eventService.getAllEvent(page, pageSize, sort);
	}

	@QueryMapping("getAllEventBySeminarId")
	public ListEventDto getAllEventsBySeminarId(
			@Argument(name = "seminarId") UUID seminarId,
			@Argument(name = "page") Integer page,
			@Argument(name = "pageSize") Integer pageSize,
			@Argument(name = "sort") String sort) {
		return eventService.getAllEventBySeminarId(seminarId, page, pageSize, sort);
	}

	@QueryMapping("getAllEventsNotJoinByCustomer")
	public ListEventDto getAllEventsNotJoinByCustomer(
			@Argument(name = "customerId") UUID customerId,
			@Argument(name = "page") Integer page,
			@Argument(name = "pageSize") Integer pageSize,
			@Argument(name = "sort") String sort) {
		return eventService.getAllEventNotJoinByCustomer(customerId, page, pageSize, sort);
	}

	@QueryMapping("getAllEventsByReader")
	public ListEventDto getAllEventsByReader(
			@Argument(name = "readerId") UUID readerId,
			@Argument(name = "page") Integer page,
			@Argument(name = "pageSize") Integer pageSize,
			@Argument(name = "sort") String sort) {
		return eventService.getAllEventByReader(readerId, page, pageSize, sort);
	}

	@QueryMapping("getAllActiveEventsByReader")
	public ListEventDto getAllActiveEventsByReader(
			@Argument(name = "readerId") UUID readerId,
			@Argument(name = "page") Integer page,
			@Argument(name = "pageSize") Integer pageSize,
			@Argument(name = "sort") String sort) {
		return eventService.getAllActiveEventByReader(readerId, page, pageSize, sort);
	}

	@QueryMapping("getAllActiveEvents")
	public ListEventDto getAllActiveEvents(
			@Argument(name = "page") Integer page,
			@Argument(name = "pageSize") Integer pageSize,
			@Argument(name = "sort") String sort) {
		return eventService.getAllActiveEvent(page, pageSize, sort);
	}

	@MutationMapping("createEvent")
	public EventDto createEvent(
			@Argument(name = "input") EventCreateDto eventDto,
			@Argument(name = "readerId") UUID readerId) {
		return eventService.createEvent(eventDto, readerId);
	}

	@MutationMapping("updateEvent")
	public EventDto updateEvent(
			@Argument(name = "id") UUID id,
			@Argument(name = "input") EventUpdateDto eventDto) {
		return eventService.updateEvent(id, eventDto);
	}

	@MutationMapping("deleteEvent")
	public EventDto deleteEvent(@Argument(name = "id") UUID id) {
		return eventService.deleteEvent(id);
	}

	@MutationMapping("bookEvent")
	public EventBookingDto bookEvent(
			@Argument(name = "eventId") UUID eventId,
			@Argument(name = "customerId") UUID customerId) {
		return eventService.bookEvent(eventId, customerId);
	}

	@MutationMapping("advertiseEvent")
	public EventDto advertiseEvent(
			@Argument(name = "eventId") UUID eventId,
			@Argument(name = "readerId") UUID readerId,
			@Argument(name = "advertiseAt") String advertiseAt) {
		return eventService.advertiseEvent(eventId, readerId, advertiseAt);
	}

}
