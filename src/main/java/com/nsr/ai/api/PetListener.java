package com.nsr.ai.api;

/**
 * Listener interface for pet-related events.
 * Part of the experimental V1 system.
 */
public interface PetListener {
    void onPetEvent(PetDataSnapshot petData);
}
