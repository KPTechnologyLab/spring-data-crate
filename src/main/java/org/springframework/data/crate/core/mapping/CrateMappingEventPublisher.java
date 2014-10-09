package org.springframework.data.crate.core.mapping;

import static org.springframework.util.Assert.notNull;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.crate.core.CrateTemplate;
import org.springframework.data.mapping.context.MappingContextEvent;

/**
 * An implementation of ApplicationEventPublisher that will only fire {@link CrateContextEvent}s for use by the table
 * creator when CrateTemplate is used 'stand-alone', that is not declared inside a Spring {@link ApplicationContext}.
 * Declare {@link CrateTemplate} inside an {@link ApplicationContext} to enable the publishing of all persistence events
 * such as {@link AfterLoadEvent}, {@link AfterSaveEvent}, etc.
 * 
 * @author Hasnain Javed
 * 
 * @since 1.0.0
 */
public class CrateMappingEventPublisher implements ApplicationEventPublisher {

	private final CratePersistentEntityTableCreator tableCreator;

	/**
	 * Creates a new {@link CrateMappingEventPublisher} for the given {@link CratePersistentEntityTableCreator}.
	 * 
	 * @param tableCreator must not be {@literal null}.
	 */
	public CrateMappingEventPublisher(CratePersistentEntityTableCreator tableCreator) {
		notNull(tableCreator);
		this.tableCreator = tableCreator;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.springframework.context.ApplicationEventPublisher#publishEvent(org.springframework.context.ApplicationEvent)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void publishEvent(ApplicationEvent event) {
		if (event instanceof MappingContextEvent) {
			tableCreator.onApplicationEvent((MappingContextEvent<CratePersistentEntity<?>, CratePersistentProperty>) event);
		}
	}
}