package ues.edu.ine.componentes.ui;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.theme.lumo.LumoUtility;

import ues.edu.ine.base.ui.MainLayout;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Home | SEAE")
@Menu(order = 0, icon = "vaadin:home-o", title = "Home")
public class HomeView extends VerticalLayout {

    public HomeView() {
        // 1. Configurar el contenedor principal
        setSizeFull(); // Ocupar todo el alto disponible
        setJustifyContentMode(JustifyContentMode.CENTER); // Centrar verticalmente
        setDefaultHorizontalComponentAlignment(Alignment.CENTER); // Centrar horizontalmente
        getStyle().set("text-align", "center"); // Centrar textos largos

        // 2. Agregar un ícono decorativo (Ej: Gráfico de líneas para economía)
        Icon icon = VaadinIcon.CHART_LINE.create();
        icon.setSize("80px");
        icon.addClassNames(
            LumoUtility.TextColor.PRIMARY, 
            LumoUtility.Margin.Bottom.MEDIUM
        );

        // 3. Estilizar el título principal
        H1 acronym = new H1("SEAE");
        acronym.addClassNames(
            LumoUtility.FontSize.XXXLARGE,
            LumoUtility.Margin.NONE
        );

        H2 subtitle = new H2("Sistema de Evaluación de Alternativas Económicas");
        subtitle.addClassNames(
            LumoUtility.FontSize.XLARGE,
            LumoUtility.TextColor.HEADER,
            LumoUtility.Margin.Top.XSMALL,
            LumoUtility.Margin.Bottom.MEDIUM
        );

        // 4. Estilizar el mensaje de bienvenida
        Paragraph mensaje = new Paragraph("Seleccione una opción del menú lateral para comenzar a evaluar alternativas económicas y tomar decisiones informadas.");
        mensaje.addClassNames(
            LumoUtility.TextColor.SECONDARY, 
            LumoUtility.FontSize.LARGE
        );
        // Limitar el ancho máximo hace que el texto se vea como un bloque centrado elegante
        mensaje.setMaxWidth("600px"); 

        // 5. Añadir los componentes al layout
        add(icon, acronym, subtitle, mensaje);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        Object user = VaadinSession.getCurrent().getAttribute("user");

        if (user == null) {
            attachEvent.getUI().navigate("login");
        }
    }
}
