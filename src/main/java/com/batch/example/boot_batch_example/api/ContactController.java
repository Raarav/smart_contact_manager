package com.batch.example.boot_batch_example.api;

import java.net.URI;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/contacts")
public class ContactController {
    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @PostMapping
    public ResponseEntity<ContactResponse> create(@Valid @RequestBody ContactRequest request) {
        ContactResponse contact = contactService.create(request);
        return ResponseEntity.created(URI.create("/api/contacts/" + contact.id())).body(contact);
    }

    @GetMapping
    public ContactPage list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean favorite,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "firstName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        return contactService.list(search, favorite, page, size, sortBy, sortDirection);
    }

    @GetMapping("/favorites")
    public ContactPage favorites(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return contactService.list(null, true, page, size, "firstName", "asc");
    }

    @GetMapping("/{id}")
    public ContactResponse get(@PathVariable String id) {
        return contactService.get(id);
    }

    @PutMapping("/{id}")
    public ContactResponse update(@PathVariable String id, @Valid @RequestBody ContactRequest request) {
        return contactService.update(id, request);
    }

    @PatchMapping("/{id}/favorite")
    public ContactResponse setFavorite(
            @PathVariable String id,
            @Valid @RequestBody FavoriteRequest request) {
        return contactService.setFavorite(id, request.favorite());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        contactService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
