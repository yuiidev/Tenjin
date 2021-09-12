package net.demozo.tenjin.exceptions;

import net.demozo.tenjin.RelationshipType;

public class InvalidRelationshipTypeException extends RuntimeException {
    public InvalidRelationshipTypeException(String message) {
        super(message);
    }
}
