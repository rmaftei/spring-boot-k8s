package eu.rmaftei.knote.repository;

import eu.rmaftei.knote.model.Note;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

public interface NotesRepository extends MongoRepository<Note, String> {
}
