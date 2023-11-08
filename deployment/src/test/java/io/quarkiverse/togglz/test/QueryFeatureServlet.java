package io.quarkiverse.togglz.test;

import java.io.IOException;
import java.util.Objects;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.togglz.core.manager.FeatureManager;

@WebServlet(urlPatterns = "/query-basic-feature")
public class QueryFeatureServlet extends HttpServlet {

    private final FeatureManager featureManager;

    public QueryFeatureServlet(final FeatureManager featureManager) {
        this.featureManager = Objects.requireNonNull(featureManager);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final boolean isActive = featureManager.isActive(BasicFeatures.FEATURE1);
        resp.getWriter().write(Boolean.toString(isActive));
    }

}
