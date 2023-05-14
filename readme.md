# Sound Graffiti

### Basic presentation

**Sound Graffiti** is a full stack app that runs natively on android. Any user can record a sound at any given location and the sound will save "at that location". When browsing the map, you can check for sound graffitis around you in a broader area and play any that are in your near proximity. 
If there are more markers in a smaller area, we cluster them to display only one marker, where upon pressing the marker the user gets a list of the Sound Graffiti around the marker. These Graffiti are sorted by the number of likes users input in order to have "peer filtering", as any other more complex solution of filtering sound designs happens to be too difficult in 24 hours. 

### Backend

Our backend is completely **dockerized**, enabling simple deployment and easy scalability. The architecture can then be implemented as microservices. Another big security benefit can be providing software-defined networking, which we very simply use in our hack.

## Use cases

- Tourism: Can make partnerships with tourist guides to record audio graffiti
- Local Tourism: Locals can record voice recordings of less known to tourists spots, such as cool pubs
- Fun facts, fun content, jokes etc.
