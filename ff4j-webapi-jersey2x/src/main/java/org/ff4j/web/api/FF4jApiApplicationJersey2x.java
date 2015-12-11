package org.ff4j.web.api;

import org.ff4j.FF4j;
import org.ff4j.web.ApiConfig;
import org.ff4j.web.api.filter.JerseyApplicationEventListener;
import org.ff4j.web.api.filter.JerseyRequestEventListener;
import org.ff4j.web.api.resources.FF4jResource;
import org.ff4j.web.api.security.FF4jAuthenticationFilter;
import org.ff4j.web.api.security.FF4jAuthorizationFilter;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.config.ScannerFactory;
import io.swagger.jaxrs.config.BeanConfig;

/**
 * Parent class to provide FF4J REST API using Jersey2x.
 *
 * @author Cedrick Lunven (@clunven)</a>
 */
public abstract class FF4jApiApplicationJersey2x extends ResourceConfig {

    /** logger for this class. */
    protected final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Configuration for this.
     */
    private ApiConfig apiConfig;
    
    protected abstract ApiConfig getWebApiConfiguration();
   
    /**
     * Injection of FF4J.
     *
     * @author Cedrick Lunven (@clunven)</a>
     */
    public class FF4jBinder extends AbstractBinder {
        @Override
        protected void configure() {
            // singleton instance binding
            bind(getWebApiConfiguration().getFF4j()).to(FF4j.class);
            log.info("FF4J is now bound to Jersey Context.");
        }
    }

    /**
     * Initialisation of Jersey2 application.
     *
     * @param serviceLocator
     */
    public FF4jApiApplicationJersey2x() {
        log.info("  __  __ _  _   _ ");
        log.info(" / _|/ _| || | (_)");
        log.info("| |_| |_| || |_| |");
        log.info("|  _|  _|__   _| |");
        log.info("|_| |_|    |_|_/ |");
        log.info("             |__/   WEB API Initialization...");
        log.info(" ");

        apiConfig = getWebApiConfiguration();
        packages(FF4jResource.class.getPackage().getName());

        register(new FF4jBinder());
        register(JerseyApplicationEventListener.class);
        register(JerseyRequestEventListener.class);
        
        if (apiConfig.isAutorize()) {
            enableAuthenticationFilter();
            enableAuthorizationFilter();
            
        } else if (apiConfig.isAuthenticate()) {
            enableAuthenticationFilter();
        }
    
        // Swagger configuration
        if (apiConfig.isDocumentation()) {
            BeanConfig beanConfig = new BeanConfig();
            beanConfig.setTitle("FF4J (ff4j.org) WebAPI");
            beanConfig.setDescription("Administrate and operate all tasks on your features through this api");
            beanConfig.setResourcePackage("org.ff4j.web.api.resources");
            beanConfig.setContact("@clunven");
            beanConfig.setLicense("Apache 2.0");
            beanConfig.setLicenseUrl("http://www.apache.org/licenses/LICENSE-2.0.html");
            beanConfig.setVersion(apiConfig.getVersion());
            beanConfig.setSchemes(new String[] {"http"});
            beanConfig.setHost(apiConfig.getHost() + ":" + apiConfig.getPort());
            beanConfig.setBasePath("/" + apiConfig.getWebContext() + "/api");
            beanConfig.setScan(true);
            
            ScannerFactory.setScanner(beanConfig);
            register(io.swagger.jaxrs.listing.ApiListingResource.class);
            register(io.swagger.jaxrs.listing.SwaggerSerializers.class);
            log.info("Initialisation Swagger [OK]");
        }
        log.info("Initialisation WebAPI [OK]");
    }
    
    private void enableAuthenticationFilter() {
        FF4jAuthenticationFilter.apiConfig = apiConfig;
        register(FF4jAuthenticationFilter.class);
        log.info("WebService Authentication is now enabled");
    }
    
    private void enableAuthorizationFilter() {
        FF4jAuthorizationFilter.apiConfig = apiConfig;
        register(FF4jAuthorizationFilter.class);
        log.info("WebService Authorization is now enabled");
    }
    
}
