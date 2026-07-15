package it.uniroma3.siw.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import it.uniroma3.siw.dto.CreateBookingRequest;
import it.uniroma3.siw.exception.DuplicateBookingException;
import it.uniroma3.siw.exception.NotEnoughTicketsException;
import it.uniroma3.siw.mapper.BookingMapper;
import it.uniroma3.siw.model.Booking;
import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.model.User;
import it.uniroma3.siw.service.BookingService;
import it.uniroma3.siw.service.CredentialsService;
import it.uniroma3.siw.service.PlayService;
import it.uniroma3.siw.service.UserService;
import jakarta.validation.Valid;

@Controller
public class BookingController {

	@Autowired
	private BookingService bookingService;

	@Autowired
	private PlayService playService;

	@Autowired
	private UserService userService;

	@Autowired
	private CredentialsService credentialsService;

	@Autowired
	private BookingMapper bookingMapper;

	private Credentials getCurrentCredentials() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		return this.credentialsService.getCredentials(userDetails.getUsername());
	}

	@PostMapping("/bookings")
	public String newBooking(@Valid @ModelAttribute("bookingRequest") CreateBookingRequest bookingRequest,
			BindingResult bindingResult, Model model) {

		Credentials credentials = getCurrentCredentials();
		boolean isAdmin = credentials.getRole().equals(Credentials.ADMIN_ROLE);

		/* l'admin prenota per conto dell'utente scelto nel form,
		 * l'utente default sempre e solo per se stesso */
		Long userId = (isAdmin && bookingRequest.getUserId() != null)
				? bookingRequest.getUserId()
				: credentials.getUser().getId();

		if (!bindingResult.hasErrors()) {
			try {
				Booking booking = this.bookingService.createBooking(userId,
						bookingRequest.getPlayId(), bookingRequest.getNumTickets());
				return isAdmin
						? "redirect:/admin/bookings/" + booking.getId()
						: "redirect:/bookings/" + booking.getId();
			} catch (DuplicateBookingException e) {
				bindingResult.reject("duplicate.booking");
			} catch (NotEnoughTicketsException e) {
				bindingResult.rejectValue("numTickets", "error.booking", e.getMessage());
			} catch (ObjectOptimisticLockingFailureException e) {
				bindingResult.reject("error.booking.conflict",
						"Qualcun altro ha appena prenotato gli stessi biglietti: riprova.");
			}
		}

		model.addAttribute("plays", this.playService.findAll());
		if (isAdmin) {
			model.addAttribute("users", this.userService.findAll());
			return "admin/formNewBooking.html";
		}
		return "user/formNewBooking.html";
	}

	/*********************************************************************************************/
	/**************************************** ADMIN ***********************************************/
	/*********************************************************************************************/

	@GetMapping("admin/bookings/{id}")
	public String getBooking(@PathVariable("id") Long id, Model model) {
		model.addAttribute("booking", this.bookingMapper.toDto(this.bookingService.findById(id)));
		return "admin/booking.html";
	}

	@GetMapping("admin/bookings")
	public String showBookings(Model model) {
		model.addAttribute("bookings", this.bookingMapper.toDtoList(this.bookingService.findAll()));
		return "admin/bookings.html";
	}

	@GetMapping("admin/formSearchBooking")
	public String formSearchBookings() {
		return "admin/formSearchBooking.html";
	}

	@PostMapping("admin/searchBooking")
	public String searchBookings(Model model, @RequestParam String username) {
		Optional<Credentials> credentialsOptional = this.credentialsService.findByUsername(username);
		if (credentialsOptional.isEmpty()) {
			model.addAttribute("errorMessage", "L'username inserito non esiste.");
			return "admin/formSearchBooking.html";
		}
		User user = credentialsOptional.get().getUser();
		model.addAttribute("bookings", this.bookingMapper.toDtoList(this.bookingService.findByUser(user)));
		return "admin/bookings.html";
	}

	@GetMapping("/admin/formNewBooking")
	public String formNewBooking(Model model) {
		model.addAttribute("bookingRequest", new CreateBookingRequest());
		model.addAttribute("plays", this.playService.findAll());
		model.addAttribute("users", this.userService.findAll());
		return "admin/formNewBooking.html";
	}

	@GetMapping("/admin/manageBookings")
	public String manageBookings(Model model) {
		model.addAttribute("bookings", this.bookingMapper.toDtoList(this.bookingService.findAll()));
		return "admin/manageBookings.html";
	}

	@GetMapping("/admin/formUpdateBookingTickets/{id}")
	public String formUpdateBooking(@PathVariable("id") Long id, Model model) {
		model.addAttribute("booking", this.bookingMapper.toDto(this.bookingService.findById(id)));
		return "admin/formUpdateBookingTickets.html";
	}

	@PostMapping("/admin/updateBookingTickets/{id}")
	public String updateBookingTickets(@PathVariable("id") Long id,
			@RequestParam("numTickets") int numTickets, Model model) {
		try {
			Booking booking = this.bookingService.updateBookingTickets(id, numTickets);
			return "redirect:/admin/bookings/" + booking.getId();
		} catch (NotEnoughTicketsException | ObjectOptimisticLockingFailureException e) {
			model.addAttribute("errorMessage", errorMessageFor(e));
			model.addAttribute("booking", this.bookingMapper.toDto(this.bookingService.findById(id)));
			return "admin/formUpdateBookingTickets.html";
		}
	}

	@GetMapping("/admin/removeBooking/{id}")
	public String removeBooking(@PathVariable("id") Long id) {
		this.bookingService.cancelBooking(id);
		return "admin/successfulRemoval.html";
	}

	/*********************************************************************************************/
	/**************************************** USER ***********************************************/
	/*********************************************************************************************/

	@GetMapping("bookings")
	public String showBookingsUser(Model model) {
		Credentials credentials = getCurrentCredentials();
		model.addAttribute("bookings",
				this.bookingMapper.toDtoList(this.bookingService.findByUser(credentials.getUser())));
		return "bookings.html";
	}

	@GetMapping("bookings/{id}")
	public String getBookingUser(@PathVariable("id") Long id, Model model) {
		model.addAttribute("booking", this.bookingMapper.toDto(this.bookingService.findById(id)));
		return "user/booking.html";
	}

	@GetMapping("/user/formNewBooking")
	public String formNewBookingUser(Model model) {
		model.addAttribute("bookingRequest", new CreateBookingRequest());
		model.addAttribute("plays", this.playService.findAll());
		return "user/formNewBooking.html";
	}

	@GetMapping("/user/manageBookings")
	public String manageBookingsUser(Model model) {
		Credentials credentials = getCurrentCredentials();
		model.addAttribute("bookings",
				this.bookingMapper.toDtoList(this.bookingService.findByUser(credentials.getUser())));
		return "user/manageBookings.html";
	}

	@GetMapping("/user/updateBookingTickets/{id}")
	public String formUpdateBookingUser(@PathVariable("id") Long id, Model model) {
		model.addAttribute("booking", this.bookingMapper.toDto(this.bookingService.findById(id)));
		return "user/formUpdateBookingTickets.html";
	}

	@PostMapping("/user/updateBookingTickets/{id}")
	public String updateBookingTicketsUser(@PathVariable("id") Long id,
			@RequestParam("numTickets") int numTickets, Model model) {
		try {
			Booking booking = this.bookingService.updateBookingTickets(id, numTickets);
			return "redirect:/bookings/" + booking.getId();
		} catch (NotEnoughTicketsException | ObjectOptimisticLockingFailureException e) {
			model.addAttribute("errorMessage", errorMessageFor(e));
			model.addAttribute("booking", this.bookingMapper.toDto(this.bookingService.findById(id)));
			return "user/formUpdateBookingTickets.html";
		}
	}

	@GetMapping("/user/removeBooking/{id}")
	public String removeBookingUser(@PathVariable("id") Long id) {
		this.bookingService.cancelBooking(id);
		return "user/successfulRemoval.html";
	}

	private String errorMessageFor(RuntimeException e) {
		if (e instanceof ObjectOptimisticLockingFailureException)
			return "Qualcun altro ha appena prenotato gli stessi biglietti: riprova.";
		return e.getMessage();
	}
}
