package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.EventService;
import com.matmuh.matmuhsite.business.constants.EventMessages;
import com.matmuh.matmuhsite.business.constants.PhotoMessages;
import com.matmuh.matmuhsite.core.utilities.results.*;
import com.matmuh.matmuhsite.dataAccess.abstracts.EventDao;
import com.matmuh.matmuhsite.entities.Event;
import com.matmuh.matmuhsite.entities.Photo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventManager implements EventService {

    private final EventDao eventDao;

    @Autowired
    public EventManager(EventDao eventDao) {
        this.eventDao = eventDao;
    }

    @Override
    public Result addEvent(Event event) {
        eventDao.save(event);
        return new SuccessResult(EventMessages.eventAddSuccess);
    }

    @Override
    public DataResult<List<Event>> getEvents() {
        var result = eventDao.findAll();
        return new SuccessDataResult<List<Event>>(result, EventMessages.getEventsSuccess);
    }

    @Override
    public DataResult<Event> getEventById(int id) {
        var result = eventDao.findById(id);
        return new SuccessDataResult<Event>(result, EventMessages.getEventSuccess);
    }
}
