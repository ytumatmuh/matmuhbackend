package com.matmuh.matmuhsite.business.abstracts;

import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.Result;
import com.matmuh.matmuhsite.entities.Event;

import java.util.List;
import java.util.UUID;

public interface EventService {

    Result addEvent(Event event);

    Result updateEvent(Event event);

    DataResult<List<Event>> getEvents();

    DataResult<Event> getEventById(UUID id);

    Result deleteEvent(UUID id);



}
