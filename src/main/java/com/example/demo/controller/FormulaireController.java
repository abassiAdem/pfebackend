package com.example.demo.controller;

import java.util.Collections;
import java.util.List;
import java.util.Optional;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.ChampRequest;
import com.example.demo.dto.FormulaireDTO;
import com.example.demo.dto.TypeDTO;
import com.example.demo.dto.TypeRequest;
import com.example.demo.entities.Champ;
import com.example.demo.entities.Type;
import com.example.demo.service.FormulaireService;

@RestController
@RequestMapping(path="/api/type")

public class FormulaireController {
	@Autowired
    private final FormulaireService formulaireService;
	public FormulaireController(FormulaireService formulaireServices) {
		this.formulaireService=formulaireServices;
	}


    
	@GetMapping("/all")
	public ResponseEntity<List<TypeDTO>> getAllTypesWithFormulairesAndChamps() {
	    List<TypeDTO> typeDTOs = formulaireService.findAllTypesWithFormulairesAndChamps();  
	    return ResponseEntity.ok(typeDTOs);
	}
	@GetMapping("/allNotNormal")
	public ResponseEntity<List<TypeDTO>> getAllTypesWithFormulairesAndChampsNotNormal() {
	    List<TypeDTO> typeDTOs = formulaireService.findAllTypesWithFormulairesAndChampsNotNormal();  
	    return ResponseEntity.ok(typeDTOs);
	}
    
    @PostMapping("/create")
    public ResponseEntity<?> createTypeWithForm(@RequestBody TypeRequest request) {
        try {

            Type savedType = formulaireService.createTypeWithForm(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedType);
        } catch (Exception e) {
            e.printStackTrace(); 
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error processing request: " + e.getMessage());
        }
    }
    
 
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteType(@PathVariable Long id) {
        boolean deleted = formulaireService.deleteType(id);
        return deleted 
            ? ResponseEntity.ok().body(Collections.singletonMap("status", "success"))
            : ResponseEntity.notFound().build();
    }


    @GetMapping("/name")
    public ResponseEntity<List<FormulaireDTO>> getFormulairesByTypeName(@RequestParam  String type) {

    	
        List<FormulaireDTO> formulaireDTOs = formulaireService.getFormulaireByTypeName(type);

        return ResponseEntity.ok(formulaireDTOs);
    }
    @GetMapping("/{typeId}")
    public ResponseEntity<List<FormulaireDTO>> getFormulairesByTypeId(@PathVariable Long typeId) {
        List<FormulaireDTO> formulaireDTOs = formulaireService.getFormulaireByTypeId(typeId);
        return ResponseEntity.ok(formulaireDTOs);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Type> updateType(
        @PathVariable Long id,
        @RequestBody TypeRequest typeRequest) {

        Optional<Type> updatedType = formulaireService.updateType(id, typeRequest);

        return updatedType.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    
}