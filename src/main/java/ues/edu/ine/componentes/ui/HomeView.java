package ues.edu.ine.componentes.ui;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import ues.edu.ine.base.ui.MainLayout;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;


@Route(value = "", layout = MainLayout.class)
@PageTitle("Home | SEAE")
@Menu(order = 0, icon = "vaadin:home-o", title = "Home")
public class HomeView extends VerticalLayout {

    public HomeView() {
        setSpacing(true);
        setPadding(true);

        H1 title = new H1("Bienvenido al Sistema de Evaluación de Alternativas Económicas - SEAE");
        title.getStyle().set("alignSelf", "center");
        Paragraph mensaje = new Paragraph("Seleccione una opción del menú para comenzar a evaluar alternativas económicas.");
        add(title, mensaje);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        Object user = VaadinSession.getCurrent().getAttribute("user");

        if (user == null) {
            attachEvent.getUI().navigate("login");
        }
    }

}
