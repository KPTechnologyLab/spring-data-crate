package org.springframework.data.crate.core.convert;

import org.springframework.data.convert.EntityWriter;
import org.springframework.data.crate.core.mapping.CrateDocument;

/**
 * CrateWriter marker interface for converting an object of type T to crate document representation {@link CrateDocument}.
 * 
 * @author Hasnain Javed
 * @since 1.0.0
 * @param <T> the type of the object to be converted to a CrateDocument
 */
public interface CrateWriter<T> extends EntityWriter<T, CrateDocument> {
}