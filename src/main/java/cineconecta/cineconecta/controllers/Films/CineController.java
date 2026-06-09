package cineconecta.cineconecta.controllers.Films;

import cineconecta.cineconecta.controllers.Films.CineResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cine")
@RequiredArgsConstructor
public class CineController {

    @GetMapping("")
    public ResponseEntity<CineResponse> getAllCategory() {
        return ResponseEntity.ok(CineResponse.builder().message("List of all categories").build());
    }
}
