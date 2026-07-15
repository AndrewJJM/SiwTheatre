package it.uniroma3.siw.exception;

/**
 * Lanciata quando una risorsa richiesta (Play, Booking, User, ...) non esiste.
 * Gestita centralmente da GlobalExceptionHandler con una pagina 404.
 */
public class ResourceNotFoundException extends RuntimeException {

	public ResourceNotFoundException(String resource, Long id) {
		super(resource + " con id " + id + " non trovato");
	}
}
