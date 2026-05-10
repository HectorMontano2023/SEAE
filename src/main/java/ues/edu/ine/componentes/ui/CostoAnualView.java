package ues.edu.ine.componentes.ui;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import ues.edu.ine.base.ui.MainLayout;

@Route(value = "costo-anual", layout = MainLayout.class)
@PageTitle("Costo Anual | SEAE")
@Menu(order = 2, icon = "vaadin:coins", title = "Costo Anual")
public class CostoAnualView extends VerticalLayout {

    private final Paragraph resultado = new Paragraph("Complete los datos y presione Calcular para ver el costo anual equivalente.");

    public CostoAnualView() {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        getStyle()
            .set("background", "linear-gradient(135deg, #23406f, #5a7bd8)")
            .set("padding", "20px");

        VerticalLayout mainContainer = new VerticalLayout();
        mainContainer.setMaxWidth("1100px");
        mainContainer.setWidthFull();
        mainContainer.setPadding(true);
        mainContainer.setSpacing(true);
        mainContainer.setAlignItems(Alignment.CENTER);

        mainContainer.getStyle()
            .set("background-color", "#f4f6fb")
            .set("border-radius", "25px")
            .set("box-shadow", "0 8px 25px rgba(0, 0, 0, 0.15)")
            .set("padding", "35px");

        H1 titulo = new H1("Evaluación de Alternativas Económicas: Costo Anual");
        titulo.getStyle()
            .set("margin-bottom", "0")
            .set("font-size", "clamp(28px, 4vw, 48px)")
            .set("color", "#1f2937");

        Paragraph subtitulo = new Paragraph("Compare dos alternativas con base en su costo anual equivalente. La opción recomendada es la de menor costo anual.");
        subtitulo.getStyle()
            .set("color", "#4b5563")
            .set("max-width", "900px")
            .set("text-align", "center");

        AlternativaCosto alternativaA = crearAlternativa("Alternativa A");
        AlternativaCosto alternativaB = crearAlternativa("Alternativa B");

        HorizontalLayout alternativas = new HorizontalLayout(alternativaA.container(), alternativaB.container());
        alternativas.setSpacing(true);
        alternativas.setWrap(true);
        alternativas.setWidthFull();
        alternativas.setJustifyContentMode(JustifyContentMode.CENTER);

        HorizontalLayout tasaLayout = new HorizontalLayout();
        tasaLayout.setWidth("100%");
        tasaLayout.setAlignItems(Alignment.CENTER);
        tasaLayout.setJustifyContentMode(JustifyContentMode.CENTER);

        tasaLayout.getStyle()
            .set("background-color", "#e9eefb")
            .set("padding", "20px")
            .set("border-radius", "15px");

        NumberField tasaDescuento = new NumberField();
        tasaDescuento.setPlaceholder("Ingrese el porcentaje");
        tasaDescuento.setMin(0);
        tasaDescuento.setStep(0.1);
        tasaDescuento.setWidth("250px");

        Paragraph tasaLabel = new Paragraph("Tasa de descuento (%):");
        tasaLabel.getStyle().set("font-weight", "bold");

        tasaLayout.add(tasaLabel, tasaDescuento);

        Button calcular = new Button("Calcular");
        estiloBoton(calcular);

        calcular.addClickListener(event -> {
            try {
                if (tasaDescuento.isEmpty()) {
                    resultado.setText("Ingrese la tasa de descuento para calcular el costo anual equivalente.");
                    return;
                }

                double tasa = tasaDescuento.getValue();
                double costoA = calcularCostoAnual(alternativaA, tasa);
                double costoB = calcularCostoAnual(alternativaB, tasa);

                String mejor = costoA < costoB ? "Alternativa A" : "Alternativa B";
                resultado.setText(
                    "Costo anual A: $" + formatear(costoA) +
                    " | Costo anual B: $" + formatear(costoB) +
                    " | Mejor opción: " + mejor
                );
            } catch (Exception ex) {
                resultado.setText("Complete todos los campos correctamente para calcular.");
            }
        });

        VerticalLayout resultadoCard = new VerticalLayout();
        resultadoCard.getStyle()
            .set("background-color", "#eef2ff")
            .set("border-radius", "18px")
            .set("padding", "18px")
            .set("width", "100%")
            .set("max-width", "900px");
        resultadoCard.add(resultado);

        mainContainer.add(
            titulo,
            subtitulo,
            alternativas,
            tasaLayout,
            calcular,
            resultadoCard
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

    private AlternativaCosto crearAlternativa(String tituloTexto) {
        VerticalLayout card = new VerticalLayout();
        card.setWidth("100%");
        card.setMaxWidth("420px");
        card.setPadding(true);
        card.setSpacing(true);

        card.getStyle()
            .set("background-color", "#ffffff")
            .set("border-radius", "20px")
            .set("box-shadow", "0 4px 12px rgba(0,0,0,0.1)")
            .set("padding", "25px");

        H2 titulo = new H2(tituloTexto);
        titulo.getStyle()
            .set("color", "#1f4b99")
            .set("border-left", "4px solid #5a7bd8")
            .set("padding-left", "12px");

        NumberField inversion = crearCampoNumero("Inversión inicial", "Ingrese la inversión inicial");
        NumberField costoOperativo = crearCampoNumero("Costo operativo anual", "Ingrese el costo anual");
        NumberField valorRescate = crearCampoNumero("Valor de rescate", "Ingrese el valor de rescate");
        NumberField vidaUtil = crearCampoNumero("Vida útil (años)", "Ingrese los años de vida útil");

        card.add(titulo, inversion, costoOperativo, valorRescate, vidaUtil);

        return new AlternativaCosto(card, inversion, costoOperativo, valorRescate, vidaUtil);
    }

    private NumberField crearCampoNumero(String label, String placeholder) {
        NumberField campo = new NumberField(label);
        campo.setPlaceholder(placeholder);
        campo.setMin(0);
        campo.setStep(0.01);
        campo.setWidthFull();
        return campo;
    }

    private void estiloBoton(Button boton) {
        boton.getStyle()
            .set("background", "linear-gradient(90deg, #23406f, #5a7bd8)")
            .set("color", "white")
            .set("border-radius", "25px")
            .set("padding", "12px 28px")
            .set("font-weight", "bold")
            .set("border", "none");
    }

    private double calcularCostoAnual(AlternativaCosto alternativa, double tasa) {
        double inversionInicial = valorOmitidoCero(alternativa.inversion());
        double costoOperativo = valorOmitidoCero(alternativa.costoOperativo());
        double valorRescate = valorOmitidoCero(alternativa.valorRescate());
        double vidaUtil = valorOmitidoCero(alternativa.vidaUtil());

        if (vidaUtil <= 0) {
            throw new IllegalArgumentException("La vida útil debe ser mayor que cero");
        }

        double i = tasa / 100.0;
        double factorRecuperacionCapital = (i == 0)
            ? 1 / vidaUtil
            : (i * Math.pow(1 + i, vidaUtil)) / (Math.pow(1 + i, vidaUtil) - 1);
        double factorFondoRecuperacion = (i == 0)
            ? 1 / vidaUtil
            : i / (Math.pow(1 + i, vidaUtil) - 1);

        return (inversionInicial * factorRecuperacionCapital) + costoOperativo - (valorRescate * factorFondoRecuperacion);
    }

    private double valorOmitidoCero(NumberField field) {
        return field.isEmpty() ? 0.0 : field.getValue();
    }

    private String formatear(double valor) {
        return String.format("%.2f", valor);
    }

    private record AlternativaCosto(
        VerticalLayout container,
        NumberField inversion,
        NumberField costoOperativo,
        NumberField valorRescate,
        NumberField vidaUtil
    ) {
    }
}