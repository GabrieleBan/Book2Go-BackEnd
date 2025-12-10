package com.b2g.orderservice.model;

public enum OrderStatus {
    /**
     * L'ordine è stato creato ma il pagamento non è ancora stato confermato.
     * Questo è lo stato iniziale visibile all'utente mentre completa il pagamento.
     */
    PENDING,

    /**
     * Il pagamento è stato confermato e tutti i passaggi per garantire l'accesso
     * ai beni (digitali e/o fisici) sono stati completati con successo.
     * L'ordine è concluso dal punto di vista logico.
     */
    COMPLETED,

    /**
     * Applicabile solo a ordini contenenti articoli fisici.
     * L'ordine è stato completato e il pacco è stato affidato al corriere.
     */
    SHIPPED,

    /**
     * La transazione è fallita in uno dei suoi passaggi (es. pagamento rifiutato,
     * sistema non disponibile) e non è andata a buon fine.
     * L'utente deve riprovare.
     */
    FAILED,

    /**
     * L'ordine è stato annullato, o dall'utente (se permesso) o da un amministratore.
     * Potrebbe richiedere un rimborso se era già stato pagato.
     */
    CANCELLED
}
