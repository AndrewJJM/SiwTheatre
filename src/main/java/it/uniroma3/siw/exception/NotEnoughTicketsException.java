package it.uniroma3.siw.exception;

/**
 * Lanciata quando si tenta di prenotare piu' biglietti di quelli disponibili.
 */
public class NotEnoughTicketsException extends RuntimeException {

	public NotEnoughTicketsException(int requested, int available) {
		super("Non ci sono abbastanza biglietti disponibili: richiesti " + requested
				+ ", disponibili " + available);
	}
}
