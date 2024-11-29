package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.EventService;
import com.matmuh.matmuhsite.business.constants.EventMessages;
import com.matmuh.matmuhsite.business.constants.ImageMessages;
import com.matmuh.matmuhsite.core.utilities.results.*;
import com.matmuh.matmuhsite.dataAccess.abstracts.EventDao;
import com.matmuh.matmuhsite.entities.Event;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class EventManager implements EventService {

    private final EventDao eventDao;


    public EventManager(EventDao eventDao) {
        this.eventDao = eventDao;
    }

    @Override
    public Result addEvent(Event event) {
        if (event.getName().isEmpty()){
            return new ErrorResult(EventMessages.nameCanotBeNull, HttpStatus.BAD_REQUEST);
        }

        if(event.getContext().isEmpty()){
            return new ErrorResult(EventMessages.contentCanotBeNull, HttpStatus.BAD_REQUEST);
        }

        if(event.getDate() == null){
            return new ErrorResult(ImageMessages.photoCanotBeNull, HttpStatus.BAD_REQUEST);
        }

        eventDao.save(event);
        return new SuccessResult(EventMessages.eventAddSuccess, HttpStatus.CREATED);
    }

    @Override
    public Result updateEvent(Event event) {

        var eventResult = eventDao.findById(event.getId());
        if (eventResult.isEmpty()){
            return new ErrorResult(EventMessages.eventNotFound, HttpStatus.NOT_FOUND);
        }

        eventDao.save(event);
        return new SuccessResult(EventMessages.eventUpdateSuccess, HttpStatus.OK);
    }

        @Override
    public DataResult<List<Event>> getEvents() {
        var result = eventDao.findAll();

        if (result.isEmpty())
            return new ErrorDataResult<>(EventMessages.eventNotFound, HttpStatus.NOT_FOUND);

        return new SuccessDataResult<List<Event>>(result, EventMessages.getEventsSuccess, HttpStatus.OK);
    }

    @Override
    public DataResult<Event> getEventById(UUID id){
        var result = eventDao.findById(id);
        if(result.isEmpty()){
            return new ErrorDataResult<>(EventMessages.eventNotFound, HttpStatus.NOT_FOUND);
        }

        return new SuccessDataResult<Event>(result.get(), EventMessages.getEventSuccess, HttpStatus.OK);
    }

    @Override
    public Result deleteEvent(UUID id) {
        var result = eventDao.findById(id);

        if (result.isEmpty()){
            return new ErrorResult(EventMessages.eventNotFound, HttpStatus.NOT_FOUND);
        }

        this.eventDao.delete(result.get());
        return new SuccessResult(EventMessages.eventDeleteSuccess, HttpStatus.OK);

    }
}
