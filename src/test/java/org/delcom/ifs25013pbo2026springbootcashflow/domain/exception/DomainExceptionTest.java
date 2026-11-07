package org.delcom.ifs25013pbo2026springbootcashflow.domain.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

class DomainExceptionTest {

    @Test
    void domainExceptionKeepsMessageAndCause() {
        Throwable cause = new IllegalStateException("akar");

        DomainException withMessage = new DomainException("pesan");
        DomainException withCause = new DomainException("pesan", cause);

        assertEquals("pesan", withMessage.getMessage());
        assertEquals("pesan", withCause.getMessage());
        assertSame(cause, withCause.getCause());
    }

    @Test
    void entityNotFoundExceptionCarriesId() {
        EntityNotFoundException exception = new EntityNotFoundException(7);

        assertEquals(7, exception.getId());
        assertEquals("Transaksi dengan ID 7 tidak ditemukan.", exception.getMessage());
        assertInstanceOf(DomainException.class, exception);
    }

    @Test
    void validationExceptionKeepsMessage() {
        ValidationException exception = new ValidationException("tidak valid");

        assertEquals("tidak valid", exception.getMessage());
        assertInstanceOf(DomainException.class, exception);
    }
}
