package com.b2g.orderservice.model;

public enum SagaStatus {
    /**
     * La SAGA è stata avviata. L'ordine è stato salvato e si attende di iniziare
     * la comunicazione con gli altri microservizi.
     */
    STARTED,

    /**
     * Il primo passo è stato avviato: è stato inviato il comando/richiesta al
     * PaymentService. L'orchestratore è in attesa di un evento di conferma
     * (successo o fallimento) dal PaymentService.
     */
    AWAITING_PAYMENT_CONFIRMATION,

    /**
     * Il pagamento ha avuto successo. È stato inviato il comando al LibraryService
     * per garantire l'accesso ai contenuti digitali. L'orchestratore è in attesa
     * di una risposta dal LibraryService.
     */
    AWAITING_LIBRARY_UPDATE,

    /**
     * Tutti i passaggi della SAGA sono stati completati con successo.
     * Lo stato finale e desiderato. L'OrderStatus può essere impostato a COMPLETED.
     */
    COMPLETED,

    /**
     * La SAGA è fallita durante il passo del pagamento. Il processo si interrompe qui.
     * L'OrderStatus può essere impostato a FAILED.
     */
    FAILED_PAYMENT,

    /**
     * La SAGA è fallita in un passo successivo al pagamento (es. LibraryService non risponde).
     * È stata avviata la logica di compensazione (es. rimborso).
     * Lo stato attuale è quello di "tornare indietro".
     */
    ROLLING_BACK,

    /**
     * La transazione di compensazione (es. rimborso) è stata completata con successo.
     * La SAGA è terminata in uno stato consistente ma non riuscito.
     * L'OrderStatus può essere impostato a FAILED.
     */
    ROLLED_BACK
}

