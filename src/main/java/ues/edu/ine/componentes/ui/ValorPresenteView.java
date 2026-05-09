package ues.edu.ine.componentes.ui;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import ues.edu.ine.base.ui.MainLayout;

@Route(value = "valor-presente", layout = MainLayout.class)
@PageTitle("Valor Presente | SEAE")
@Menu(order = 1, icon = "vaadin:coins", title = "Valor Presente")

public class ValorPresenteView extends VerticalLayout {

    public ValorPresenteView() {

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        getStyle()
            .set("background", "linear-gradient(135deg, #5d7cf3, #7b4bb7)")
            .set("padding", "20px");

        // Contenedor principal
        VerticalLayout mainContainer = new VerticalLayout();
        mainContainer.setMaxWidth("1100px");
        mainContainer.setWidthFull();
        mainContainer.setPadding(true);
        mainContainer.setSpacing(true);
        mainContainer.setAlignItems(Alignment.CENTER);

        mainContainer.getStyle()
            .set("background-color", "#f3f3f5")
            .set("border-radius", "25px")
            .set("box-shadow", "0 8px 25px rgba(0, 0, 0, 0.15)")
            .set("padding", "35px");

        H1 titulo = new H1("Evaluación de Alternativas Económicas: Valor Presente");
        titulo.getStyle()
            .set("margin-bottom", "0")
            .set("font-size", "clamp(28px, 4vw, 48px)")
            .set("color", "#2c2c2c");

        Paragraph subtitulo = new Paragraph("Compare dos alternativas mediante el método de Valor Presente.");
        subtitulo.getStyle()
            .set("color", "#666");

        // Alternativas
        VerticalLayout alternativaA = crearAlternativa("Alternativa A");
        VerticalLayout alternativaB = crearAlternativa("Alternativa B");

        HorizontalLayout alternativas = new HorizontalLayout(alternativaA, alternativaB);
        alternativas.setSpacing(true);
        alternativas.setWrap(true);
        alternativas.setWidthFull();
        alternativas.setJustifyContentMode(JustifyContentMode.CENTER);

        // Tasa de descuento
        HorizontalLayout tasaLayout = new HorizontalLayout();
        tasaLayout.setWidth("100%");
        tasaLayout.setAlignItems(Alignment.CENTER);
        tasaLayout.setJustifyContentMode(JustifyContentMode.CENTER);

        tasaLayout.getStyle()
            .set("background-color", "#ececec")
            .set("padding", "20px")
            .set("border-radius", "15px");

        NumberField tasaDescuento = new NumberField();
        tasaDescuento.setPlaceholder("Ingrese el porcentaje");
        tasaDescuento.setSuffixComponent(new Div());
        tasaDescuento.setWidth("250px");

        Paragraph tasaLabel = new Paragraph("Tasa de descuento (%):");
        tasaLabel.getStyle().set("font-weight", "bold");

        tasaLayout.add(tasaLabel, tasaDescuento);

        // Botones
        Button comparar = new Button("Comparar");
        Button exportar = new Button("Exportar Reporte (PDF)");

        estiloBoton(comparar);
        estiloBoton(exportar);

        comparar.addClickListener(event -> {
            try {
                if (tasaDescuento.isEmpty()) {
                    Notification.show("Ingrese la tasa de descuento");
                    return;
                }

                double vpA = calcularVP(alternativaA, tasaDescuento.getValue());
                double vpB = calcularVP(alternativaB, tasaDescuento.getValue());

                String mejor = vpA > vpB ? "Alternativa A" : "Alternativa B";

                Notification.show(
                    "VP A: $" + String.format("%.2f", vpA) +
                    " | VP B: $" + String.format("%.2f", vpB) +
                    " | Mejor opción: " + mejor,
                    8000,
                    Notification.Position.MIDDLE
                );

            } catch (Exception ex) {
                Notification.show("Complete todos los campos correctamente");
            }
        });

        HorizontalLayout botones = new HorizontalLayout(comparar, exportar);
        botones.setSpacing(true);

        mainContainer.add(
            titulo,
            subtitulo,
            alternativas,
            tasaLayout,
            botones
        );

        add(mainContainer);
    }

@Override
protected void onAttach(AttachEvent attachEvent) {
    Object user = VaadinSession.getCurrent().getAttribute("user");

    if (user == null) {
        attachEvent.getUI().navigate("login");
    }
}

    private VerticalLayout crearAlternativa(String tituloTexto) {

        VerticalLayout card = new VerticalLayout();
        card.setWidth("100%");
        card.setMaxWidth("420px");
        card.setPadding(true);
        card.setSpacing(true);

        card.getStyle()
            .set("background-color", "#e9e9eb")
            .set("border-radius", "20px")
            .set("box-shadow", "0 4px 12px rgba(0,0,0,0.1)")
            .set("padding", "25px");

        H2 titulo = new H2(tituloTexto);
        titulo.getStyle()
            .set("color", "#6a4ca3")
            .set("border-left", "4px solid #7b4bb7")
            .set("padding-left", "12px");

        NumberField inversion = new NumberField("Inversión Inicial:");
        inversion.setPlaceholder("Ingrese la inversión inicial");

        NumberField flujo = new NumberField("Flujos de efectivo anuales:");
        flujo.setPlaceholder("Ingrese el flujo de efectivo");

        NumberField vida = new NumberField("Vida útil (años):");
        vida.setPlaceholder("Ingrese el número de años");

        card.add(titulo, inversion, flujo, vida);

        return card;
    }

    private void estiloBoton(Button boton) {
        boton.getStyle()
            .set("background", "linear-gradient(90deg, #5d7cf3, #7b4bb7)")
            .set("color", "white")
            .set("border-radius", "25px")
            .set("padding", "12px 28px")
            .set("font-weight", "bold")
            .set("border", "none");
    }

    private double calcularVP(VerticalLayout layout, Double tasa) {

        NumberField inversion = (NumberField) layout.getChildren().skip(1).findFirst().get();
        NumberField flujo = (NumberField) layout.getChildren().skip(2).findFirst().get();
        NumberField vida = (NumberField) layout.getChildren().skip(3).findFirst().get();

        double inversionInicial = inversion.getValue();
        double flujoEfectivo = flujo.getValue();
        double años = vida.getValue();
        double i = tasa / 100;

        double vpFlujos = 0;

        for (int t = 1; t <= años; t++) {
            vpFlujos += flujoEfectivo / Math.pow((1 + i), t);
        }

        return vpFlujos - inversionInicial;
    }
}