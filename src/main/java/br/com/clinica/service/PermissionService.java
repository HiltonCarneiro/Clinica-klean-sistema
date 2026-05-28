package br.com.clinica.service;

import br.com.clinica.auth.AuthGuard;
import br.com.clinica.auth.Permissao;
import javafx.scene.Node;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;

public class PermissionService {

    public void exigir(Permissao permissao) {
        AuthGuard.exigirPermissao(permissao);
    }

    public boolean temPermissao(Permissao permissao) {
        try {
            AuthGuard.exigirPermissao(permissao);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean temAlgumaPermissao(Permissao... permissoes) {
        if (permissoes == null) return false;

        for (Permissao permissao : permissoes) {
            if (temPermissao(permissao)) {
                return true;
            }
        }

        return false;
    }

    public void aplicarPermissao(Node node, Permissao permissao) {
        aplicarVisibilidade(node, temPermissao(permissao));
    }

    public void aplicarVisibilidade(Node node, boolean visivel) {
        if (node == null) return;

        node.setVisible(visivel);
        node.setManaged(visivel);
    }

    public void removerItemSeSemPermissao(Menu menu, MenuItem item, Permissao permissao) {
        if (!temPermissao(permissao)) {
            removerItem(menu, item);
        }
    }

    public void removerMenuSeVazio(MenuBar menuBar, Menu menu) {
        if (menuBar != null && menu != null && menu.getItems().isEmpty()) {
            menuBar.getMenus().remove(menu);
        }
    }

    private void removerItem(Menu menu, MenuItem item) {
        if (menu != null && item != null) {
            menu.getItems().remove(item);
        }
    }
}