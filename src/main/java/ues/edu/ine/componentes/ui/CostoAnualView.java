package ues.edu.ine.componentes.ui;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
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
    private final NumberField tasaDescuentoField = new NumberField();
    private AlternativaCosto alternativaA;
    private AlternativaCosto alternativaB;

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

        alternativaA = crearAlternativa("Alternativa A");
        alternativaB = crearAlternativa("Alternativa B");

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

        tasaDescuentoField.setPlaceholder("Ingrese el porcentaje");
        tasaDescuentoField.setMin(0);
        tasaDescuentoField.setStep(0.1);
        tasaDescuentoField.setWidth("250px");

        Paragraph tasaLabel = new Paragraph("Tasa de descuento (%):");
        tasaLabel.getStyle().set("font-weight", "bold");

        tasaLayout.add(tasaLabel, tasaDescuentoField);

        Button calcular = new Button("Calcular");
        estiloBoton(calcular);

        calcular.addClickListener(event -> {
            try {
                if (tasaDescuentoField.isEmpty()) {
                    resultado.setText("Ingrese la tasa de descuento para calcular el costo anual equivalente.");
                    return;
                }

                double tasa = tasaDescuentoField.getValue();
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

        Anchor exportar = new Anchor();
        exportar.setHref(crearUrlPdf());
        exportar.getElement().setAttribute("download", true);
        exportar.setText("Exportar PDF");
        estiloBoton(exportar);

        HorizontalLayout acciones = new HorizontalLayout(calcular, exportar);
        acciones.setSpacing(true);

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
            acciones,
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

    private void estiloBoton(HasStyle boton) {
        boton.getStyle()
            .set("background", "linear-gradient(90deg, #23406f, #5a7bd8)")
            .set("color", "white")
            .set("border-radius", "25px")
            .set("padding", "12px 28px")
            .set("font-weight", "bold")
            .set("border", "none");
    }

    private String crearUrlPdf() {
        return "data:application/pdf;base64," + Base64.getEncoder().encodeToString(generarPdf());
    }

    private byte[] generarPdf() {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.LETTER);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
                contentStream.newLineAtOffset(50, 730);
                contentStream.showText("SEAE - Reporte de Costo Anual");

                contentStream.setFont(PDType1Font.HELVETICA, 11);
                contentStream.newLineAtOffset(0, -30);
                escribirLinea(contentStream, "Tasa de descuento: " + valorTexto(tasaDescuentoField) + " %");
                escribirLinea(contentStream, "Alternativa A - Inversion inicial: $" + valorTexto(alternativaA.inversion()));
                escribirLinea(contentStream, "Alternativa A - Costo operativo anual: $" + valorTexto(alternativaA.costoOperativo()));
                escribirLinea(contentStream, "Alternativa A - Valor de rescate: $" + valorTexto(alternativaA.valorRescate()));
                escribirLinea(contentStream, "Alternativa A - Vida util: " + valorTexto(alternativaA.vidaUtil()) + " anos");
                escribirLinea(contentStream, "Alternativa B - Inversion inicial: $" + valorTexto(alternativaB.inversion()));
                escribirLinea(contentStream, "Alternativa B - Costo operativo anual: $" + valorTexto(alternativaB.costoOperativo()));
                escribirLinea(contentStream, "Alternativa B - Valor de rescate: $" + valorTexto(alternativaB.valorRescate()));
                escribirLinea(contentStream, "Alternativa B - Vida util: " + valorTexto(alternativaB.vidaUtil()) + " anos");
                escribirLinea(contentStream, "");
                escribirLinea(contentStream, "Resultado: " + resultado.getText());
                contentStream.endText();
            }

            document.save(outputStream);
            return outputStream.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo generar el PDF", ex);
        }
    }

    private void escribirLinea(PDPageContentStream contentStream, String texto) throws IOException {
        contentStream.showText(texto);
        contentStream.newLineAtOffset(0, -16);
    }

    private String valorTexto(NumberField field) {
        return field.isEmpty() ? "" : formatear(field.getValue());
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