package com.tonkar.volleyballreferee.controller;

import com.itextpdf.text.DocumentException;
import com.tonkar.volleyballreferee.pdf.PdfGame;
import com.tonkar.volleyballreferee.pdf.PdfGameWriter;
import com.tonkar.volleyballreferee.service.GameService;
import com.tonkar.volleyballreferee.model.Game;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/view/game")
@CrossOrigin("*")
public class ViewGameController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ViewGameController.class);

    @Autowired
    private GameService gameService;

    @GetMapping("/{id}")
    public ResponseEntity<Game> getGame(@PathVariable("id") long id) {
        LOGGER.debug(String.format("Request get game with date %d", id));

        Game game = gameService.getGame(id);

        if (game == null) {
            LOGGER.error(String.format("No game with date %d for viewing", id));
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(game, HttpStatus.OK);
        }
    }

    @GetMapping("/pdf/{id}")
    public ResponseEntity<?> getGameAsPdf(@PathVariable("id") long id) {
        LOGGER.debug(String.format("Request download pdf with date %d", id));

        Game game = gameService.getGame(id);

        if (game == null) {
            LOGGER.error(String.format("No game with date %d for downloading pdf", id));
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            try {
                PdfGame pdfGame = PdfGameWriter.writeGame(game);
                ByteArrayResource resource = new ByteArrayResource(pdfGame.getData());
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + pdfGame.getFilename())
                        .contentType(MediaType.APPLICATION_PDF)
                        .contentLength(pdfGame.getData().length)
                        .body(resource);
            } catch (IOException | DocumentException e) {
                LOGGER.error(e.getMessage(), e);
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }

}
