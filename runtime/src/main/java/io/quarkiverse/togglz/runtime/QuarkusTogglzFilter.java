package io.quarkiverse.togglz.runtime;

import java.io.IOException;

import jakarta.enterprise.inject.Any;
import jakarta.servlet.*;

import org.togglz.core.manager.FeatureManager;
import org.togglz.servlet.TogglzFilter;

import io.quarkus.arc.Arc;

public class QuarkusTogglzFilter implements Filter {

    private final TogglzFilter togglzFilter;

    public QuarkusTogglzFilter(TogglzFilter togglzFilter) {
        this.togglzFilter = togglzFilter;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Force init
        Arc.container().instance(FeatureManager.class, Any.Literal.INSTANCE);
        this.togglzFilter.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        this.togglzFilter.doFilter(request, response, chain);
    }

    @Override
    public void destroy() {
        this.togglzFilter.destroy();
    }
}
