package com.devsuperior.dsmovie.dto;

import java.io.Serializable;

public class FieldMessageDTO implements Serializable {
	
	private String fieldName;
    private String message;

    public FieldMessageDTO(String fieldName, String message) {
        this.fieldName = fieldName;
        this.message = message;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getMessage() {
        return message;
    }

}
