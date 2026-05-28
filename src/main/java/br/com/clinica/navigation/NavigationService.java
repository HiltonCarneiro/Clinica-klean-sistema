package br.com.clinica.navigation;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.Deque;

public class NavigationService {

    public static final String HOME = "__HOME__";

    private final Class<?> resourceBaseClass;
    private final Deque<String> backStack = new ArrayDeque<>();
    private final Deque<String> forwardStack = new ArrayDeque<>();

    private String currentView;

    public NavigationService(Class<?> resourceBaseClass) {
        this.resourceBaseClass = resourceBaseClass;
    }

    public Parent load(String fxmlPath) throws IOException {
        URL fxmlUrl = resourceBaseClass.getResource(fxmlPath);

        if (fxmlUrl == null) {
            throw new IOException("FXML não encontrado: " + fxmlPath);
        }

        return new FXMLLoader(fxmlUrl).load();
    }

    public void navigateTo(String destination) {
        if (currentView != null) {
            backStack.push(currentView);
        }

        forwardStack.clear();
        currentView = destination;
    }

    public void replaceCurrent(String destination) {
        currentView = destination;
    }

    public String back() {
        if (!canGoBack()) {
            return null;
        }

        String previous = backStack.pop();

        if (currentView != null) {
            forwardStack.push(currentView);
        }

        currentView = previous;
        return previous;
    }

    public String forward() {
        if (!canGoForward()) {
            return null;
        }

        String next = forwardStack.pop();

        if (currentView != null) {
            backStack.push(currentView);
        }

        currentView = next;
        return next;
    }

    public void clearHistory() {
        backStack.clear();
        forwardStack.clear();
    }

    public boolean canGoBack() {
        return !backStack.isEmpty();
    }

    public boolean canGoForward() {
        return !forwardStack.isEmpty();
    }

    public String getCurrentView() {
        return currentView;
    }

    public boolean isHome(String destination) {
        return HOME.equals(destination);
    }
}