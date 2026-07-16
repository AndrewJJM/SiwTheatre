package it.uniroma3.siw.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Form-backing object per la creazione di una prenotazione: il controller
 * riceve solo gli id, mai entity JPA bindate direttamente dal form.
 */
public class CreateBookingRequest {

	@NotNull(message = "Selezionare uno spettacolo")
	private Long playId;

	/* Usato solo dal form admin, che prenota per conto di un utente. */
	private Long userId;

	@Min(value = 1, message = "Inserire almeno 1 biglietto")
	private int numTickets;

	public Long getPlayId() {
		return playId;
	}

	public void setPlayId(Long playId) {
		this.playId = playId;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public int getNumTickets() {
		return numTickets;
	}

	public void setNumTickets(int numTickets) {
		this.numTickets = numTickets;
	}
}
