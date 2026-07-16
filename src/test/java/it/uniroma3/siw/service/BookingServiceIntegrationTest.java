package it.uniroma3.siw.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;

import it.uniroma3.siw.AbstractIntegrationTest;
import it.uniroma3.siw.exception.DuplicateBookingException;
import it.uniroma3.siw.exception.NotEnoughTicketsException;
import it.uniroma3.siw.exception.ResourceNotFoundException;
import it.uniroma3.siw.model.Booking;
import it.uniroma3.siw.model.Play;
import it.uniroma3.siw.model.User;
import it.uniroma3.siw.repository.BookingRepository;
import it.uniroma3.siw.repository.PlayRepository;
import it.uniroma3.siw.repository.UserRepository;

/**
 * Test di integrazione del flusso di prenotazione su PostgreSQL reale
 * (Testcontainers). La classe NON e' @Transactional: il test di concorrenza
 * ha bisogno che ogni thread apra la propria transazione, come in produzione.
 */
class BookingServiceIntegrationTest extends AbstractIntegrationTest {

	@Autowired
	private BookingService bookingService;

	@Autowired
	private BookingRepository bookingRepository;

	@Autowired
	private PlayRepository playRepository;

	@Autowired
	private UserRepository userRepository;

	private Play play;
	private User firstUser;
	private User secondUser;

	@BeforeEach
	void setUp() {
		this.bookingRepository.deleteAll();
		this.playRepository.deleteAll();
		this.userRepository.deleteAll();

		this.play = newPlay(10);
		this.firstUser = newUser("Mario", "Rossi", "mario.rossi@example.com");
		this.secondUser = newUser("Luigi", "Verdi", "luigi.verdi@example.com");
	}

	@Test
	void createBookingDecrementsAvailableTickets() {
		Booking booking = this.bookingService.createBooking(firstUser.getId(), play.getId(), 3);

		assertThat(booking.getId()).isNotNull();
		assertThat(booking.getNumTickets()).isEqualTo(3);
		assertThat(reloadPlay().getAvailableTickets()).isEqualTo(7);
	}

	@Test
	void createBookingFailsAndRollsBackWhenNotEnoughTickets() {
		assertThatThrownBy(() -> this.bookingService.createBooking(firstUser.getId(), play.getId(), 11))
				.isInstanceOf(NotEnoughTicketsException.class);

		assertThat(reloadPlay().getAvailableTickets()).isEqualTo(10);
		assertThat(this.bookingRepository.count()).isZero();
	}

	@Test
	void createBookingRejectsDuplicateForSameUserAndPlay() {
		this.bookingService.createBooking(firstUser.getId(), play.getId(), 2);

		assertThatThrownBy(() -> this.bookingService.createBooking(firstUser.getId(), play.getId(), 1))
				.isInstanceOf(DuplicateBookingException.class);

		assertThat(this.bookingRepository.count()).isEqualTo(1);
		assertThat(reloadPlay().getAvailableTickets()).isEqualTo(8);
	}

	/**
	 * Due utenti provano a prenotare contemporaneamente TUTTI i biglietti
	 * rimasti: senza optimistic locking entrambe le transazioni andrebbero a
	 * buon fine vendendo 20 biglietti su 10. Con @Version su Play esattamente
	 * una deve riuscire.
	 */
	@Test
	void concurrentBookingsOnLastTicketsDoNotOversell() throws Exception {
		CyclicBarrier barrier = new CyclicBarrier(2);
		ExecutorService executor = Executors.newFixedThreadPool(2);
		try {
			List<Callable<Object>> tasks = List.of(
					bookingTask(barrier, firstUser, 10),
					bookingTask(barrier, secondUser, 10));

			List<Throwable> failures = new ArrayList<>();
			int successes = 0;
			for (Future<Object> future : executor.invokeAll(tasks)) {
				try {
					future.get();
					successes++;
				} catch (java.util.concurrent.ExecutionException e) {
					failures.add(e.getCause());
				}
			}

			assertThat(successes).isEqualTo(1);
			assertThat(failures).hasSize(1);
			assertThat(failures.get(0)).isInstanceOfAny(
					OptimisticLockingFailureException.class, NotEnoughTicketsException.class);

			assertThat(reloadPlay().getAvailableTickets()).isZero();
			assertThat(this.bookingRepository.count()).isEqualTo(1);
		} finally {
			executor.shutdownNow();
		}
	}

	@Test
	void updateBookingTicketsAdjustsAvailability() {
		Booking booking = this.bookingService.createBooking(firstUser.getId(), play.getId(), 3);

		this.bookingService.updateBookingTickets(booking.getId(), 5);

		assertThat(reloadPlay().getAvailableTickets()).isEqualTo(5);
		assertThat(this.bookingService.findById(booking.getId()).getNumTickets()).isEqualTo(5);
	}

	@Test
	void cancelBookingRestoresTickets() {
		Booking booking = this.bookingService.createBooking(firstUser.getId(), play.getId(), 4);

		this.bookingService.cancelBooking(booking.getId());

		assertThat(reloadPlay().getAvailableTickets()).isEqualTo(10);
		assertThat(this.bookingRepository.count()).isZero();
	}

	@Test
	void findByIdThrowsWhenBookingDoesNotExist() {
		assertThatThrownBy(() -> this.bookingService.findById(99999L))
				.isInstanceOf(ResourceNotFoundException.class);
	}

	private Callable<Object> bookingTask(CyclicBarrier barrier, User user, int numTickets) {
		return () -> {
			barrier.await();
			return this.bookingService.createBooking(user.getId(), play.getId(), numTickets);
		};
	}

	private Play reloadPlay() {
		return this.playRepository.findById(this.play.getId()).orElseThrow();
	}

	private Play newPlay(int availableTickets) {
		Play p = new Play();
		p.setName("Macbeth");
		p.setDescription("Spettacolo di prova");
		p.setDate(LocalDate.now().plusMonths(1));
		p.setTime(LocalTime.of(21, 0));
		p.setCity("Roma");
		p.setLocation("Teatro di prova");
		p.setAvailableTickets(availableTickets);
		p.setPrice(20f);
		return this.playRepository.save(p);
	}

	private User newUser(String name, String surname, String email) {
		User u = new User();
		u.setName(name);
		u.setSurname(surname);
		u.setEmail(email);
		u.setDateOfBirth(LocalDate.of(1995, 1, 1));
		return this.userRepository.save(u);
	}
}
