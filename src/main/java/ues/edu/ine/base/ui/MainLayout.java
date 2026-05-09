package ues.edu.ine.base.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.avatar.AvatarVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.menu.MenuConfiguration;
import com.vaadin.flow.server.menu.MenuEntry;
import com.vaadin.flow.component.button.Button;

//@Layout
public final class MainLayout extends AppLayout {

    MainLayout() {
        setPrimarySection(Section.DRAWER);

        addToNavbar(createApplicationHeader());

        addToDrawer(
                createApplicationDrawer(),
                createApplicationFooter());
    }

    private Component createApplicationHeader() {

        DrawerToggle toggle = new DrawerToggle();

        var appLogo = new Avatar("SEAE");
        appLogo.addThemeVariants(AvatarVariant.AURA_FILLED, AvatarVariant.XSMALL);

        var appName = new Span("SEAE");
        appName.getStyle().set("font-weight", "bold");

        var header = new HorizontalLayout(toggle, appLogo, appName);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setWidthFull();
        header.setPadding(true);

        header.getStyle()
                .set("background-color", "white")
                .set("border-bottom", "1px solid #ddd");

        return header;
    }

    private Component createApplicationDrawer() {
        var scroller = new Scroller(createSideNav());
        scroller.addThemeVariants(ScrollerVariant.OVERFLOW_INDICATORS);
        return scroller;
    }

    private Component createApplicationFooter() {
        var footer = new VerticalLayout(new Span("SEAE"));
        footer.setAlignItems(FlexComponent.Alignment.CENTER);
        footer.addClassName("app-footer");

        // En lugar de hacer UI.getCurrent().navigate("login");
        Button logout = new Button("Cerrar sesión", event -> {
            // 1. Limpiar los atributos de la sesión
            VaadinSession.getCurrent().setAttribute("user", null);

            // 2. Cerrar la sesión de Vaadin por completo
            VaadinSession.getCurrent().close();

            // 3. Forzar una recarga completa del navegador hacia la ruta del login
            UI.getCurrent().getPage().setLocation("/login");
        });

        footer.add(logout);

        return footer;
    }

    private SideNav createSideNav() {
        var nav = new SideNav();
        nav.setMinWidth(200, Unit.PIXELS);
        MenuConfiguration.getMenuEntries().forEach(entry -> nav.addItem(createSideNavItem(entry)));
        return nav;
    }

    private SideNavItem createSideNavItem(MenuEntry menuEntry) {
        if (menuEntry.icon() != null) {
            Component icon = null;
            if (menuEntry.icon().contains(".svg")) {
                icon = new SvgIcon(menuEntry.icon());
            } else {
                icon = new Icon(menuEntry.icon());
            }
            return new SideNavItem(menuEntry.title(), menuEntry.path(), icon);
        } else {
            return new SideNavItem(menuEntry.title(), menuEntry.path());
        }
    }
}
