package eu.rmaftei.knote.controller;

import eu.rmaftei.knote.infrastructure.KnoteProperties;
import eu.rmaftei.knote.model.Note;
import eu.rmaftei.knote.repository.NotesRepository;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.Renderer;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Controller
public class NoteController {

    @Autowired
    private NotesRepository notesRepository;

    @Autowired
    private KnoteProperties knoteProperties;

    private final Parser PARSER = Parser.builder().build();
    private final Renderer RENDER = new HtmlRenderer.Builder().build();

    @GetMapping("/")
    public String index(Model model) {
        getAllNotes(model);

        return "index";
    }

    @PostMapping("/note")
    public String saveNotes(@RequestParam("image") MultipartFile file,
                            @RequestParam String description,
                            @RequestParam(required = false) String publish,
                            @RequestParam(required = false) String upload,
                            Model model) throws IOException {

        if (publish != null && publish.equals("Publish")) {
            saveNote(description, model);
            getAllNotes(model);
            return "redirect:/";
        }

        if (upload != null && upload.equals("Upload")) {
            if (file != null && file.getOriginalFilename() != null
                    && !file.getOriginalFilename().isEmpty()) {
                uploadImage(file, description, model);
            }
            getAllNotes(model);
            return "index";
        }

        // After save fetch all notes again
        return "index";
    }

    private void getAllNotes(Model model) {
        final List<Note> all = notesRepository.findAll();

        Collections.reverse(all);

        model.addAttribute("notes", all);
    }

    private void saveNote(String description, Model model) {
        if (description != null && !description.trim().isEmpty()) {
            final Node node = PARSER.parse(description.trim());
            final String html = RENDER.render(node);
            notesRepository.save(new Note(null, html));
            //After publish you need to clean up the textarea
            model.addAttribute("description", "");
        }
    }

    private void uploadImage(MultipartFile file, String description, Model model) {
        try {
            File uploadsDir = new File(knoteProperties.getUploadDir());

            if (!uploadsDir.exists()) {
                uploadsDir.mkdir();
            }
            String fileId = UUID.randomUUID() + "."
                    + Objects.requireNonNull(file.getOriginalFilename()).split("\\.")[1];

            file.transferTo(new File(knoteProperties.getUploadDir() + "/" + fileId));
            model.addAttribute("description", description + " ![](/uploads/" + fileId + ")");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
