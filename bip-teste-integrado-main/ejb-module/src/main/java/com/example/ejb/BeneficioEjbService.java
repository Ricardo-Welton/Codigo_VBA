package com.example.ejb;

import com.example.backend.Beneficio;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service; 
import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

@Stateless
@Service 
public class BeneficioEjbService {

    @PersistenceContext 
    private EntityManager em;

    /**
     * @param fromId ID do Benefício de origem.
     * @param toId ID do Benefício de destino.
     * @param amount Valor a ser transferido.
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRED) // valida a transação
    public void transfer(Long fromId, Long toId, BigDecimal amount) {
        // Valida o valor
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor da transferência deve ser positivo.");
        }

        // Entidades com bloqueio pessimista
        Beneficio from = em.find(Beneficio.class, fromId, LockModeType.PESSIMISTIC_WRITE);
        Beneficio to   = em.find(Beneficio.class, toId, LockModeType.PESSIMISTIC_WRITE);

        if (from == null || to == null) {
            throw new NoSuchElementException("Benefício de origem ou destino não encontrado.");
        }

        // Valida o saldo
        if (from.getValor().compareTo(amount) < 0) {
            throw new IllegalStateException("Saldo insuficiente para realizar a transferência.");
        }

        // Atualizar os saldos
        from.setValor(from.getValor().subtract(amount));
        to.setValor(to.getValor().add(amount));
        em.merge(from); 
        em.merge(to);
    }

    // --- CRUD para integração EJB ---

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public Beneficio save(Beneficio beneficio) {
        if (beneficio.getId() == null) {
            em.persist(beneficio);
            return beneficio;
        } else {
            return em.merge(beneficio);
        }
    }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<Beneficio> findAll() {
        return em.createQuery("SELECT b FROM Beneficio b", Beneficio.class).getResultList();
    }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public Beneficio findById(Long id) {
        return em.find(Beneficio.class, id);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void delete(Long id) {
        Beneficio beneficio = em.find(Beneficio.class, id);
        if (beneficio != null) {
            em.remove(beneficio);
        }
    }
}