package com.matmuh.matmuhsite.business.abstracts;

import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.Result;
import com.matmuh.matmuhsite.entities.Event;
import com.matmuh.matmuhsite.entities.dtos.RequestEventDto;

import java.util.List;

public interface EventService {

    Result addEvent(RequestEventDto requestEventDto);

    Result updateEvent(RequestEventDto requestEventDto);

    DataResult<List<Event>> getEvents();

    DataResult<Event> getEventById(int id);

}
