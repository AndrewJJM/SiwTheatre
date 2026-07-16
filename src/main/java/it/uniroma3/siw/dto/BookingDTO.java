package it.uniroma3.siw.dto;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Vista di sola lettura di una prenotazione per i template Thymeleaf:
 * espone solo i campi mostrati, isolando le entity JPA dal frontend.
 */
public class BookingDTO {

	private Long id;
	private Long playId;
	private String playName;
	private LocalDate playDate;
	private LocalTime playTime;
	private String playCity;
	private String playLocation;
	private Long playImageId;
	private int numTickets;
	private float totalPrice;
	private String userFullName;
	private String username;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getPlayId() {
		return playId;
	}

	public void setPlayId(Long playId) {
		this.playId = playId;
	}

	public String getPlayName() {
		return playName;
	}

	public void setPlayName(String playName) {
		this.playName = playName;
	}

	public LocalDate getPlayDate() {
		return playDate;
	}

	public void setPlayDate(LocalDate playDate) {
		this.playDate = playDate;
	}

	public LocalTime getPlayTime() {
		return playTime;
	}

	public void setPlayTime(LocalTime playTime) {
		this.playTime = playTime;
	}

	public String getPlayCity() {
		return playCity;
	}

	public void setPlayCity(String playCity) {
		this.playCity = playCity;
	}

	public String getPlayLocation() {
		return playLocation;
	}

	public void setPlayLocation(String playLocation) {
		this.playLocation = playLocation;
	}

	public Long getPlayImageId() {
		return playImageId;
	}

	public void setPlayImageId(Long playImageId) {
		this.playImageId = playImageId;
	}

	public int getNumTickets() {
		return numTickets;
	}

	public void setNumTickets(int numTickets) {
		this.numTickets = numTickets;
	}

	public float getTotalPrice() {
		return totalPrice;
	}

	public void setTotalPrice(float totalPrice) {
		this.totalPrice = totalPrice;
	}

	public String getUserFullName() {
		return userFullName;
	}

	public void setUserFullName(String userFullName) {
		this.userFullName = userFullName;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}
