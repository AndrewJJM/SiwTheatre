package it.uniroma3.siw.exception;

/**
 * Lanciata quando un utente ha gia' una prenotazione per lo stesso spettacolo.
 */
public class DuplicateBookingException extends RuntimeException {

	public DuplicateBookingException() {
		super("Questa prenotazione esiste gia'");
	}
}
