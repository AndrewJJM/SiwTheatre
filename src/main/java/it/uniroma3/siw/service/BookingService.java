package it.uniroma3.siw.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.siw.exception.DuplicateBookingException;
import it.uniroma3.siw.exception.NotEnoughTicketsException;
import it.uniroma3.siw.exception.ResourceNotFoundException;
import it.uniroma3.siw.model.Booking;
import it.uniroma3.siw.model.Play;
import it.uniroma3.siw.model.User;
import it.uniroma3.siw.repository.BookingRepository;
import it.uniroma3.siw.repository.PlayRepository;
import it.uniroma3.siw.repository.UserRepository;

@Service
public class BookingService {

	@Autowired
	private BookingRepository bookingRepository;

	@Autowired
	private PlayRepository playRepository;

	@Autowired
	private UserRepository userRepository;

	/**
	 * Crea una prenotazione e scala i biglietti disponibili in un'unica
	 * transazione. Play viene riletto dal database dentro la transazione, cosi'
	 * il controllo di disponibilita' e il decremento avvengono sulla stessa
	 * versione dell'entita': se un'altra transazione la modifica nel frattempo,
	 * il commit fallisce con ObjectOptimisticLockingFailureException (grazie al
	 * campo {@code @Version} su Play) e nessun biglietto viene venduto due volte.
	 */
	@Transactional
	public Booking createBooking(Long userId, Long playId, int numTickets) {
		Play play = this.playRepository.findById(playId)
				.orElseThrow(() -> new ResourceNotFoundException("Spettacolo", playId));
		User user = this.userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("Utente", userId));

		if (this.bookingRepository.existsByUserAndPlay(user, play))
			throw new DuplicateBookingException();

		if (play.getAvailableTickets() < numTickets)
			throw new NotEnoughTicketsException(numTickets, play.getAvailableTickets());

		play.setAvailableTickets(play.getAvailableTickets() - numTickets);

		Booking booking = new Booking();
		booking.setUser(user);
		booking.setPlay(play);
		booking.setNumTickets(numTickets);
		return this.bookingRepository.save(booking);
	}

	/**
	 * Aggiorna il numero di biglietti di una prenotazione, riallineando i
	 * biglietti disponibili dello spettacolo nella stessa transazione.
	 */
	@Transactional
	public Booking updateBookingTickets(Long bookingId, int newNumTickets) {
		Booking booking = this.bookingRepository.findById(bookingId)
				.orElseThrow(() -> new ResourceNotFoundException("Prenotazione", bookingId));
		Play play = booking.getPlay();

		int difference = newNumTickets - booking.getNumTickets();
		if (play.getAvailableTickets() < difference)
			throw new NotEnoughTicketsException(difference, play.getAvailableTickets());

		play.setAvailableTickets(play.getAvailableTickets() - difference);
		booking.setNumTickets(newNumTickets);
		return booking;
	}

	/**
	 * Cancella una prenotazione riaccreditando i biglietti allo spettacolo.
	 */
	@Transactional
	public void cancelBooking(Long bookingId) {
		Booking booking = this.bookingRepository.findById(bookingId)
				.orElseThrow(() -> new ResourceNotFoundException("Prenotazione", bookingId));
		Play play = booking.getPlay();
		play.setAvailableTickets(play.getAvailableTickets() + booking.getNumTickets());
		this.bookingRepository.delete(booking);
	}

	public Booking findById(Long id) {
		return this.bookingRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Prenotazione", id));
	}

	public Iterable<Booking> findAll() {
		return this.bookingRepository.findAll();
	}

	public Iterable<Booking> findByUser(User user) {
		return this.bookingRepository.findByUser(user);
	}
}
