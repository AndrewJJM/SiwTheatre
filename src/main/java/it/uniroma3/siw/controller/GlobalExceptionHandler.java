package it.uniroma3.siw.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import it.uniroma3.siw.exception.NotEnoughTicketsException;
import it.uniroma3.siw.exception.ResourceNotFoundException;

/**
 * Gestione centralizzata delle eccezioni: ogni handler restituisce una pagina
 * di errore dedicata invece della whitelabel page o di uno stack trace.
 * Le pagine sotto templates/error/ sono standalone (niente navbar: i
 * ModelAttribute del GlobalController non sono popolati in queste view).
 */
@ControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(ResourceNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String handleNotFound(ResourceNotFoundException e, Model model) {
		model.addAttribute("errorMessage", e.getMessage());
		return "error/404";
	}

	@ExceptionHandler(ObjectOptimisticLockingFailureException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	public String handleConflict(Model model) {
		model.addAttribute("errorMessage",
				"Qualcun altro ha modificato gli stessi dati nello stesso momento: riprova.");
		return "error/conflict";
	}

	/* Fallback: il form di prenotazione la gestisce localmente come errore
	 * di campo, qui arriva solo se lanciata fuori da quel flusso. */
	@ExceptionHandler(NotEnoughTicketsException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	public String handleNotEnoughTickets(NotEnoughTicketsException e, Model model) {
		model.addAttribute("errorMessage", e.getMessage());
		return "error/conflict";
	}

	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public String handleGeneric(Exception e, Model model) {
		log.error("Unhandled exception", e);
		model.addAttribute("errorMessage", "Si è verificato un errore inatteso.");
		return "error/500";
	}
}
