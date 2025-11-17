package com.example.backend;

import com.example.ejb.BeneficioEjbService;
import com.example.beneficio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/v1/beneficios")
@Tag(name = "Benefícios", description = "API para os Benefícios com integração EJB.")
public class BeneficioController {

    private final BeneficioEjbService beneficioService;

    @Autowired
    public BeneficioController(BeneficioEjbService beneficioService) {
        this.beneficioService = beneficioService;
    }

    // --- Endpoints CRUD ---

    @GetMapping
    @Operation(summary = "Lista dos benefícios")
    public List<Beneficio> findAll() {
        return beneficioService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar benefício pelo ID")
    public ResponseEntity<Beneficio> findById(@PathVariable Long id) {
        Beneficio beneficio = beneficioService.findById(id);
        return beneficio != null ? ResponseEntity.ok(beneficio) : ResponseEntity.notFound().build();
    }

    @PostMapping
    @Operation(summary = "Criar novo benefício")
    public ResponseEntity<Beneficio> create(@RequestBody Beneficio beneficio) {
        Beneficio savedBeneficio = beneficioService.save(beneficio);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedBeneficio);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar benefício")
    public ResponseEntity<Beneficio> update(@PathVariable Long id, @RequestBody Beneficio beneficioDetails) {
        Beneficio existingBeneficio = beneficioService.findById(id);
        if (existingBeneficio == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Atualizar os campos permitidos
        existingBeneficio.setNome(beneficioDetails.getNome());
        existingBeneficio.setValor(beneficioDetails.getValor());

        Beneficio updatedBeneficio = beneficioService.save(existingBeneficio);
        return ResponseEntity.ok(updatedBeneficio);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar benefício pelo ID")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            beneficioService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // --- Endpoint de Transferência (Bug Fix) ---

    @PostMapping("/transfer")
    @Operation(summary = "Transferir valores entre benefícios")
    public ResponseEntity<?> transfer(@RequestBody Map<String, Object> request) {
        try {
            Long fromId = Long.valueOf(request.get("fromId").toString());
            Long toId = Long.valueOf(request.get("toId").toString());
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            
            beneficioService.transfer(fromId, toId, amount);
            
            return ResponseEntity.ok(Map.of("message", "Transferência realizada com sucesso!!"));
        } catch (IllegalArgumentException | IllegalStateException e) {
            // Validações de saldo negativo ou valor <= 0
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (NoSuchElementException e) {
            // Benefício não encontrado
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            // Outros erros de transação/serviço
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Erro na transferência: " + e.getMessage()));
        }
    }
}