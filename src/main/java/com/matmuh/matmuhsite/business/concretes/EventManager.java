package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.EventService;
import com.matmuh.matmuhsite.business.constants.EventMessages;
import com.matmuh.matmuhsite.business.constants.ImageMessages;
import com.matmuh.matmuhsite.core.utilities.results.*;
import com.matmuh.matmuhsite.dataAccess.abstracts.EventDao;
import com.matmuh.matmuhsite.entities.Event;
import com.matmuh.matmuhsite.entities.dtos.RequestEventDto;
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
    public Result addEvent(RequestEventDto requestEventDto) {
        if (requestEventDto.getName() == null){
            return new ErrorResult(EventMessages.nameCanotBeNull);
        }

        if(requestEventDto.getContext() == null){
            return new ErrorResult(EventMessages.contentCanotBeNull);
        }

        if(requestEventDto.getDate() == null){
            return new ErrorResult(ImageMessages.photoCanotBeNull);
        }

        var event = Event.builder()
                .name(requestEventDto.getName())
                .context(requestEventDto.getContext())
                .date(requestEventDto.getDate())
                .build();

        eventDao.save(event);
        return new SuccessResult(EventMessages.eventAddSuccess);
    }

    @Override
    public Result updateEvent(RequestEventDto requestEventDto) {

        var event = getEventById(requestEventDto.getId()).getData();

        if (event == null) {
            return new ErrorResult(EventMessages.eventNotFound);
        }

        if (requestEventDto.getName() == null) {
            return new ErrorResult(EventMessages.nameCanotBeNull);
        }

        if (requestEventDto.getContext() == null) {
            return new ErrorResult(EventMessages.contentCanotBeNull);
        }

        if (requestEventDto.getDate() == null) {
            return new ErrorResult(ImageMessages.photoCanotBeNull);
        }

        event.setContext(requestEventDto.getContext().isEmpty() ? event.getContext() : requestEventDto.getContext());
        event.setName(requestEventDto.getName().isEmpty() ? event.getName() : requestEventDto.getName());
        event.setDate(requestEventDto.getDate() == null ? event.getDate() : requestEventDto.getDate());


        eventDao.save(event);
        return new SuccessResult(EventMessages.eventAddSuccess);
    }

        @Override
    public DataResult<List<Event>> getEvents() {
        var result = eventDao.findAll();

        if (result.isEmpty())
            return new ErrorDataResult<>(EventMessages.eventNotFound);

        return new SuccessDataResult<List<Event>>(result, EventMessages.getEventsSuccess);
    }

    @Override
    public DataResult<Event> getEventById(int id){
        var result = eventDao.findById(id);
        if(result == null){
            return new ErrorDataResult<>(EventMessages.eventNotFound);
        }

        return new SuccessDataResult<Event>(result, EventMessages.getEventSuccess);
    }

    @Override
    public Result deleteEvent(int id) {
        return null;
    }


}
