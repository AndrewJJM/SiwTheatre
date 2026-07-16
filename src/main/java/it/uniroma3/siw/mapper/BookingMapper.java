package it.uniroma3.siw.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import it.uniroma3.siw.dto.BookingDTO;
import it.uniroma3.siw.model.Booking;

@Mapper(componentModel = "spring")
public interface BookingMapper {

	@Mapping(source = "play.id", target = "playId")
	@Mapping(source = "play.name", target = "playName")
	@Mapping(source = "play.date", target = "playDate")
	@Mapping(source = "play.time", target = "playTime")
	@Mapping(source = "play.city", target = "playCity")
	@Mapping(source = "play.location", target = "playLocation")
	@Mapping(source = "play.image.id", target = "playImageId")
	@Mapping(source = "user.username", target = "username")
	@Mapping(target = "userFullName", expression = "java(fullName(booking))")
	@Mapping(target = "totalPrice", expression = "java(totalPrice(booking))")
	BookingDTO toDto(Booking booking);

	List<BookingDTO> toDtoList(Iterable<Booking> bookings);

	default String fullName(Booking booking) {
		if (booking.getUser() == null)
			return "";
		return booking.getUser().getName() + " " + booking.getUser().getSurname();
	}

	default float totalPrice(Booking booking) {
		if (booking.getPlay() == null || booking.getPlay().getPrice() == null)
			return 0f;
		return booking.getNumTickets() * booking.getPlay().getPrice();
	}
}
