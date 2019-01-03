package org.kzilla.srmkzilla.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import org.kzilla.srmkzilla.db.CarouselRepository
import org.kzilla.srmkzilla.db.RegisteredEventsRepository
import org.kzilla.srmkzilla.db.UpcomingEventsRepository

class EventClass(application: Application):AndroidViewModel(application) {

    lateinit var _upcomingEvents:LiveData<UpcomingEventsRepository.UpcomingEventsData>
    lateinit var _registeredEvents:LiveData<RegisteredEventsRepository.RegisteredEventsData>
    lateinit var _carousel:LiveData<CarouselRepository.Carousel>

    private var upcomingEventsRepo:UpcomingEventsRepository = UpcomingEventsRepository.init(application)
    private var registeredEventsRepo:RegisteredEventsRepository = RegisteredEventsRepository.init(application)
    private var carouselRepo:CarouselRepository = CarouselRepository.init(application)



    fun getUpcomingEvents(fromNetwork:Boolean?):LiveData<UpcomingEventsRepository.UpcomingEventsData> {
        _upcomingEvents = upcomingEventsRepo.getEvents(fromNetwork)
        return _upcomingEvents
    }

    fun getRegisteredEvents(fromNetwork:Boolean?):LiveData<RegisteredEventsRepository.RegisteredEventsData> {
        _registeredEvents = registeredEventsRepo.getRegistrations(fromNetwork)
        return _registeredEvents
    }

    fun getCarousel(fromNetwork: Boolean?):LiveData<CarouselRepository.Carousel> {
        _carousel = carouselRepo.getCarousels(fromNetwork)
        return _carousel
    }

    fun getEventById(eventId:String, fromNetwork: Boolean?):LiveData<UpcomingEventsRepository.UpcomingEventsData>{
        return upcomingEventsRepo.getEventById(eventId,fromNetwork)
    }

    fun getRegistrationById(registrationId:String, fromNetwork: Boolean?):LiveData<RegisteredEventsRepository.RegisteredEventsData>{
        return registeredEventsRepo.getRegistrationById(registrationId,fromNetwork)
    }


}

