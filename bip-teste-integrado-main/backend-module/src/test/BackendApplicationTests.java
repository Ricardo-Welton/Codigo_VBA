package com.example.backend;

import com.example.ejb.BeneficioEjbService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BackendApplicationTests {

    @Autowired
    private BeneficioEjbService beneficioEjbService;
    
    @Autowired
    private BeneficioRepository beneficioRepository;

    @Test
    void contextLoads() {
        assertNotNull(beneficioEjbService);
    }

    @Test
    @Transactional
    void testTransferBugFixWithPessimisticLocking() throws InterruptedException {
        
        Beneficio conta1 = beneficioRepository.save(new Beneficio("Conta 1", new BigDecimal("1000.00")));
        Beneficio conta2 = beneficioRepository.save(new Beneficio("Conta 2", new BigDecimal("0.00")));

        final Long id1 = conta1.getId();
        final Long id2 = conta2.getId();
        final BigDecimal amount = new BigDecimal("1.00");
        final int iterations = 100; 

        // Executar transferências
        var executor = Executors.newFixedThreadPool(10); 

        IntStream.range(0, iterations).forEach(i -> 
            executor.submit(() -> {
                try {
                    beneficioEjbService.transfer(id1, id2, amount);
                } catch (Exception e) {
                    System.err.println("Thread error (expected only after balance depletion, if any): " + e.getMessage());
                }
            })
        );

        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS); 

        // Verificação
        Beneficio finalConta1 = beneficioRepository.findById(id1).orElseThrow();
        Beneficio finalConta2 = beneficioRepository.findById(id2).orElseThrow();

        // O valores usados para testes
        BigDecimal expectedFinalBalance1 = new BigDecimal("900.00");
        BigDecimal expectedFinalBalance2 = new BigDecimal("100.00");
        
        assertEquals(0, finalConta1.getValor().compareTo(expectedFinalBalance1), "Saldo final da Conta 1 deve ser 900.00 (1000 - 100)");
        assertEquals(0, finalConta2.getValor().compareTo(expectedFinalBalance2), "Saldo final da Conta 2 deve ser 100.00 (0 + 100)");

        System.out.println("Teste de Concorrência (EJB Bug Fix) Concluído com Sucesso.");
    }

}