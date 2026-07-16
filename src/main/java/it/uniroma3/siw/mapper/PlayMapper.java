package it.uniroma3.siw.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import it.uniroma3.siw.dto.PlayDTO;
import it.uniroma3.siw.model.Artist;
import it.uniroma3.siw.model.Play;

@Mapper(componentModel = "spring")
public interface PlayMapper {

	@Mapping(source = "image.id", target = "imageId")
	@Mapping(source = "artists", target = "artistNames")
	PlayDTO toDto(Play play);

	List<PlayDTO> toDtoList(Iterable<Play> plays);

	default String artistName(Artist artist) {
		return artist.toString();
	}
}
