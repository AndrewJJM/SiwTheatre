package it.uniroma3.siw.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.siw.exception.ResourceNotFoundException;
import it.uniroma3.siw.model.Play;
import it.uniroma3.siw.repository.PlayRepository;

@Service
public class PlayService {

	@Autowired
	private PlayRepository playRepository;
	
	@Transactional
	public void save(Play play) {
		this.playRepository.save(play);
	}
	
	@Transactional
	public void delete(Play play) {
		this.playRepository.delete(play);
	}
	

	public Play findById(Long id) {
		return this.playRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Spettacolo", id));
	}
	
	
	public List<Play> findAll() {
		return (List<Play>) this.playRepository.findAll();
	}
	
	
	public Play findByName(String name) {
		return this.playRepository.findByName(name);
	}

	public void remove(Play play) {
		this.playRepository.delete(play);
	}

	public boolean alreadyExists(Play target) {
		return this.playRepository.existsByNameAndDateAndCity(target.getName(), target.getDate(), target.getCity());
	}

}
