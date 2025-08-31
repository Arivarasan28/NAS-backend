package com.doctor.appointment.controller;

import com.doctor.appointment.model.DTO.ReceptionistCreateDTO;
import com.doctor.appointment.model.DTO.ReceptionistDTO;
import com.doctor.appointment.model.Receptionist;
import com.doctor.appointment.service.ReceptionistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/receptionists")
@RequiredArgsConstructor
@Tag(name = "Receptionist Management", description = "APIs for managing receptionists")
public class ReceptionistController {

    private final ReceptionistService receptionistService;

    @Operation(summary = "Get all receptionists", description = "Returns a list of all receptionists")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved receptionists"),
            @ApiResponse(responseCode = "401", description = "Not authorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping
    public ResponseEntity<List<ReceptionistDTO>> findAll() {
        return ResponseEntity.ok(receptionistService.findAll());
    }

    @Operation(summary = "Get receptionist by ID", description = "Returns a specific receptionist by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved receptionist"),
            @ApiResponse(responseCode = "404", description = "Receptionist not found"),
            @ApiResponse(responseCode = "401", description = "Not authorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ReceptionistDTO> getReceptionist(
            @Parameter(description = "ID of the receptionist to retrieve") 
            @PathVariable int id) {
        return ResponseEntity.ok(receptionistService.findById(id));
    }

    @Operation(summary = "Create a new receptionist", description = "Creates a new receptionist")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Receptionist created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Not authorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping
    public ResponseEntity<ReceptionistDTO> addReceptionist(
            @Parameter(description = "Receptionist details", required = true,
                    content = @Content(schema = @Schema(implementation = ReceptionistCreateDTO.class)))
            @Valid @RequestBody ReceptionistCreateDTO receptionistCreateDTO) {
        // Convert DTO to entity
        Receptionist receptionist = new Receptionist();
        receptionist.setName(receptionistCreateDTO.getName());
        receptionist.setEmail(receptionistCreateDTO.getEmail());
        receptionist.setPhone(receptionistCreateDTO.getPhone());
        receptionist.setDepartment(receptionistCreateDTO.getDepartment());
        
        Receptionist savedReceptionist = receptionistService.save(receptionist);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(receptionistService.findById(savedReceptionist.getId()));
    }

    @Operation(summary = "Update a receptionist", description = "Updates an existing receptionist by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Receptionist updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Receptionist not found"),
            @ApiResponse(responseCode = "401", description = "Not authorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ReceptionistDTO> updateReceptionist(
            @Parameter(description = "ID of the receptionist to update") 
            @PathVariable int id,
            @Parameter(description = "Updated receptionist details", required = true,
                    content = @Content(schema = @Schema(implementation = ReceptionistCreateDTO.class)))
            @Valid @RequestBody ReceptionistCreateDTO receptionistCreateDTO) {
        // Convert DTO to entity
        Receptionist receptionist = new Receptionist();
        receptionist.setName(receptionistCreateDTO.getName());
        receptionist.setEmail(receptionistCreateDTO.getEmail());
        receptionist.setPhone(receptionistCreateDTO.getPhone());
        receptionist.setDepartment(receptionistCreateDTO.getDepartment());
        
        return ResponseEntity.ok(receptionistService.update(id, receptionist));
    }

    @Operation(summary = "Delete a receptionist", description = "Deletes a receptionist by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Receptionist deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Receptionist not found"),
            @ApiResponse(responseCode = "401", description = "Not authorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReceptionist(
            @Parameter(description = "ID of the receptionist to delete") 
            @PathVariable int id) {
        receptionistService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
