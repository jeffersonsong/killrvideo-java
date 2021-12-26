package com.killrvideo.dse.dto;

import java.io.IOException;
import java.io.Serializable;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.killrvideo.model.CommonConstants;

/**
 * Entities to be used in the application.
 *
 * @author DataStax Developer Advocates team.
 */
public abstract class AbstractEntity implements Serializable, CommonConstants  {
    private static final long serialVersionUID = 7239223683486549695L;
    
    /** Helping Loging. */
    private static final ObjectMapper om = new ObjectMapper();

    /** {@inheritDoc} */
    @Override
    public String toString() {
        try {
            return getClass().getSimpleName() + " : " + om.writeValueAsString(this);
        } catch (IOException e) {
            return super.toString();
        }
    }
}
