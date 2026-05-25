package com.paymentmatchingtool.controller;

import com.paymentmatchingtool.dto.MatchResultDto;
import com.paymentmatchingtool.dto.MatchRunResponse;
import com.paymentmatchingtool.dto.ResolveRequest;
import com.paymentmatchingtool.enums.ResultFilter;
import com.paymentmatchingtool.service.MatchService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/matches")
@CrossOrigin(origins = {"http://localhost:4200"})
public class MatchController {

    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    @PostMapping(value = "/run", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public MatchRunResponse runMatch(
            @RequestParam("systemFile") MultipartFile systemFile,
            @RequestParam("providerFile") MultipartFile providerFile
    ) {
        return matchService.runMatch(systemFile, providerFile);
    }

    @GetMapping
    public List<MatchResultDto> getResults(@RequestParam(defaultValue = "UNRESOLVED") ResultFilter filter) {
        return matchService.getResults(filter);
    }

    @PatchMapping("/{id}/resolve")
    public MatchResultDto resolve(
            @PathVariable Long id,
            @Valid @RequestBody ResolveRequest request
    ) {
        return matchService.resolve(id, request.resolutionSide());
    }
}
