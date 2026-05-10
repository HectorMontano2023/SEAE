package ues.edu.ine.componentes.ui;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.data.value.ValueChangeMode;
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
    private ResultadoComparacion ultimoResultado;

    public CostoAnualView() {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        addClassName("seae-page");

        VerticalLayout mainContainer = new VerticalLayout();
        mainContainer.setMaxWidth("1100px");
        mainContainer.setWidthFull();
        mainContainer.setPadding(true);
        mainContainer.setSpacing(true);
        mainContainer.setAlignItems(Alignment.CENTER);
        mainContainer.addClassName("seae-surface");

        H1 titulo = new H1("Evaluación de Alternativas Económicas: Costo Anual");
        titulo.addClassName("seae-view-title");

        Paragraph subtitulo = new Paragraph("Compare dos alternativas con base en su costo anual equivalente. La opción recomendada es la de menor costo anual.");
        subtitulo.addClassName("seae-view-subtitle");

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
        tasaLayout.addClassName("seae-result-card");

        tasaDescuentoField.setPlaceholder("Ingrese el porcentaje");
        tasaDescuentoField.setMin(0);
        tasaDescuentoField.setStep(0.1);
        tasaDescuentoField.setValueChangeMode(ValueChangeMode.EAGER);
        tasaDescuentoField.setWidth("250px");

        Paragraph tasaLabel = new Paragraph("Tasa de descuento (%):");
        tasaLabel.getStyle().set("font-weight", "bold");

        tasaLayout.add(tasaLabel, tasaDescuentoField);

        Button calcular = new Button("Calcular");
        estiloBoton(calcular);

        calcular.addClickListener(event -> {
            mostrarResultado(calcularResultados());
        });

        Anchor descargaPdf = new Anchor();
        descargaPdf.getElement().setAttribute("download", true);
        descargaPdf.getStyle().set("display", "none");

        Button exportar = new Button("Exportar PDF", event -> {
            if (!mostrarResultado(calcularResultados())) {
                return;
            }

            descargaPdf.setHref(crearUrlPdf());
            descargaPdf.getElement().executeJs("this.click()");
        });
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
            descargaPdf,
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
        card.addClassName("seae-card");

        H2 titulo = new H2(tituloTexto);
        titulo.addClassName("seae-card-title");

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
        campo.setValueChangeMode(ValueChangeMode.EAGER);
        campo.setWidthFull();
        return campo;
    }

    private boolean mostrarResultado(ResultadoComparacion resultadoCalculado) {
        if (resultadoCalculado == null) {
            resultado.setText("Complete todos los campos correctamente para calcular.");
            ultimoResultado = null;
            return false;
        }

        ultimoResultado = resultadoCalculado;

        resultado.setText(
            "Costo anual A: $" + formatear(resultadoCalculado.costoA()) +
            " | Costo anual B: $" + formatear(resultadoCalculado.costoB()) +
            " | Mejor opción: " + resultadoCalculado.mejor()
        );
        return true;
    }

    private ResultadoComparacion calcularResultados() {
        try {
            if (tasaDescuentoField.isEmpty()) {
                ultimoResultado = null;
                return null;
            }

            double tasa = tasaDescuentoField.getValue();
            double costoA = calcularCostoAnual(alternativaA, tasa);
            double costoB = calcularCostoAnual(alternativaB, tasa);

            String mejor = costoA < costoB ? "Alternativa A" : "Alternativa B";
            return new ResultadoComparacion(tasa, costoA, costoB, mejor);
        } catch (Exception ex) {
            ultimoResultado = null;
            return null;
        }
    }

    private void estiloBoton(HasStyle boton) {
        if (boton instanceof Button button) {
            button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            button.addClassName("seae-primary-button");
        } else {
            boton.addClassName("seae-primary-button");
        }
    }

    private String crearUrlPdf() {
        return "data:application/pdf;base64," + Base64.getEncoder().encodeToString(generarPdf());
    }

    private byte[] generarPdf() {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.LETTER);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float pageWidth = page.getMediaBox().getWidth();
                float pageHeight = page.getMediaBox().getHeight();

                dibujarFondo(document, contentStream, pageWidth, pageHeight);
                dibujarEncabezado(contentStream, pageWidth, pageHeight);

                float leftX = 50;
                float topY = 650;
                float cardWidth = 240;
                float cardHeight = 235;
                float gap = 18;

                topY = dibujarTarjetaAlternativa(contentStream, leftX, topY, cardWidth, cardHeight, "Alternativa A", alternativaA, new int[] { 54, 93, 173 });
                dibujarTarjetaAlternativa(contentStream, leftX + cardWidth + gap, 650, cardWidth, cardHeight, "Alternativa B", alternativaB, new int[] { 91, 123, 216 });

                dibujarBloqueResumen(contentStream, leftX, 360, pageWidth - 100, 130);

                dibujarPie(contentStream, pageWidth);
            }

            document.save(outputStream);
            return outputStream.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo generar el PDF", ex);
        }
    }

    private void dibujarFondo(PDDocument document, PDPageContentStream contentStream, float pageWidth, float pageHeight) throws IOException {
        contentStream.setNonStrokingColor(rgb(245, 247, 252));
        contentStream.addRect(0, 0, pageWidth, pageHeight);
        contentStream.fill();

        contentStream.setNonStrokingColor(rgb(221, 228, 245));
        contentStream.addRect(pageWidth - 160, pageHeight - 110, 160, 110);
        contentStream.fill();

        contentStream.setNonStrokingColor(rgb(236, 241, 252));
        contentStream.addRect(0, pageHeight - 80, 220, 80);
        contentStream.fill();
    }

    private void dibujarEncabezado(PDPageContentStream contentStream, float pageWidth, float pageHeight) throws IOException {
        contentStream.setNonStrokingColor(rgb(31, 65, 135));
        contentStream.addRect(0, pageHeight - 115, pageWidth, 115);
        contentStream.fill();

        contentStream.beginText();
        contentStream.setNonStrokingColor(rgb(255, 255, 255));
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 20);
        contentStream.newLineAtOffset(50, pageHeight - 55);
        contentStream.showText("SEAE - Reporte de Costo Anual");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, 10);
        contentStream.newLineAtOffset(50, pageHeight - 75);
        contentStream.showText("Comparacion de alternativas.");
        contentStream.endText();

        contentStream.setNonStrokingColor(rgb(255, 255, 255));
        contentStream.addRect(pageWidth - 180, pageHeight - 95, 120, 35);
        contentStream.fill();

        contentStream.beginText();
        contentStream.setNonStrokingColor(rgb(31, 65, 135));
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 9);
        contentStream.newLineAtOffset(pageWidth - 160, pageHeight - 73);
        contentStream.showText("Tasa: " + valorTexto(tasaDescuentoField) + "%");
        contentStream.endText();
    }

    private float dibujarTarjetaAlternativa(PDPageContentStream contentStream, float x, float y, float width, float height, String titulo, AlternativaCosto alternativa, int[] colorBarra) throws IOException {
        contentStream.setNonStrokingColor(rgb(255, 255, 255));
        contentStream.addRect(x, y - height, width, height);
        contentStream.fill();

        contentStream.setNonStrokingColor(rgb(220, 226, 237));
        contentStream.addRect(x, y - 22, width, 22);
        contentStream.fill();

        contentStream.setNonStrokingColor(rgb(colorBarra[0], colorBarra[1], colorBarra[2]));
        contentStream.addRect(x, y - 22, width, 22);
        contentStream.fill();

        contentStream.beginText();
        contentStream.setNonStrokingColor(rgb(255, 255, 255));
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
        contentStream.newLineAtOffset(x + 12, y - 16);
        contentStream.showText(titulo);
        contentStream.endText();

        float textX = x + 14;
        float textY = y - 48;
        float lineGap = 17;

        dibujarLineaClaveValor(contentStream, textX, textY, "Inversión inicial", valorMoneda(alternativa.inversion()));
        dibujarLineaClaveValor(contentStream, textX, textY - lineGap, "Costo operativo anual", valorMoneda(alternativa.costoOperativo()));
        dibujarLineaClaveValor(contentStream, textX, textY - (lineGap * 2), "Valor de rescate", valorMoneda(alternativa.valorRescate()));
        dibujarLineaClaveValor(contentStream, textX, textY - (lineGap * 3), "Vida útil", valorTexto(alternativa.vidaUtil()) + " años");

        contentStream.setStrokingColor(rgb(230, 235, 244));
        contentStream.moveTo(x + 12, y - 108);
        contentStream.lineTo(x + width - 12, y - 108);
        contentStream.stroke();

        contentStream.beginText();
        contentStream.setNonStrokingColor(rgb(109, 117, 130));
        contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 9);
        contentStream.newLineAtOffset(textX, y - 128);
        contentStream.showText("Alternativa registrada para comparacion economica.");
        contentStream.endText();

        return y - height - 20;
    }

    private void dibujarLineaClaveValor(PDPageContentStream contentStream, float x, float y, String etiqueta, String valor) throws IOException {
        contentStream.beginText();
        contentStream.setNonStrokingColor(rgb(71, 85, 105));
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(etiqueta + ":");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setNonStrokingColor(rgb(15, 23, 42));
        contentStream.setFont(PDType1Font.HELVETICA, 10);
        contentStream.newLineAtOffset(x + 118, y);
        contentStream.showText(valor);
        contentStream.endText();
    }

    private void dibujarBloqueResumen(PDPageContentStream contentStream, float x, float y, float width, float height) throws IOException {
        contentStream.setNonStrokingColor(rgb(31, 65, 135));
        contentStream.addRect(x, y - height, width, height);
        contentStream.fill();

        contentStream.setNonStrokingColor(rgb(255, 255, 255));
        contentStream.addRect(x + 6, y - height + 6, width - 12, height - 12);
        contentStream.fill();

        contentStream.beginText();
        contentStream.setNonStrokingColor(rgb(31, 65, 135));
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 13);
        contentStream.newLineAtOffset(x + 16, y - 28);
        contentStream.showText("Resumen y recomendacion");
        contentStream.endText();

        ResultadoComparacion resumen = ultimoResultado;
        if (resumen == null) {
            contentStream.beginText();
            contentStream.setNonStrokingColor(rgb(71, 85, 105));
            contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 10);
            contentStream.newLineAtOffset(x + 16, y - 52);
            contentStream.showText("Sin calculo previo. Complete los datos y presione Calcular antes de exportar.");
            contentStream.endText();

            return;
        }

        contentStream.beginText();
        contentStream.setNonStrokingColor(rgb(15, 23, 42));
        contentStream.setFont(PDType1Font.HELVETICA, 11);
        contentStream.newLineAtOffset(x + 16, y - 52);
        contentStream.showText("Tasa de descuento aplicada: " + formatear(resumen.tasa()) + "%");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setNonStrokingColor(rgb(15, 23, 42));
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 11);
        contentStream.newLineAtOffset(x + 16, y - 72);
        contentStream.showText("Costo anual A: $" + formatear(resumen.costoA()) + "   Costo anual B: $" + formatear(resumen.costoB()));
        contentStream.endText();

        contentStream.setNonStrokingColor(rgb(236, 241, 252));
        contentStream.addRect(x + 16, y - 104, width - 32, 30);
        contentStream.fill();

        contentStream.beginText();
        contentStream.setNonStrokingColor(rgb(31, 65, 135));
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
        contentStream.newLineAtOffset(x + 24, y - 85);
        contentStream.showText("Recomendacion: " + resumen.mejor() + " es la opcion equivalente mas conveniente.");
        contentStream.endText();
    }

    private void dibujarPie(PDPageContentStream contentStream, float pageWidth) throws IOException {
        contentStream.setStrokingColor(rgb(214, 222, 235));
        contentStream.moveTo(50, 70);
        contentStream.lineTo(pageWidth - 50, 70);
        contentStream.stroke();

        contentStream.beginText();
        contentStream.setNonStrokingColor(rgb(100, 116, 139));
        contentStream.setFont(PDType1Font.HELVETICA, 9);
        contentStream.newLineAtOffset(50, 52);
        contentStream.showText("Generado por SEAE - Sistema de Evaluacion de Alternativas Economicas");
        contentStream.endText();
    }

    private String valorTexto(NumberField field) {
        return field.isEmpty() ? "" : formatear(field.getValue());
    }

    private String valorMoneda(NumberField field) {
        if (field.isEmpty()) {
            return "";
        }

        return "$" + formatear(field.getValue());
    }

    private record ResultadoComparacion(double tasa, double costoA, double costoB, String mejor) {
    }

    private PDColor rgb(int red, int green, int blue) {
        return new PDColor(new float[] { red / 255f, green / 255f, blue / 255f }, PDDeviceRGB.INSTANCE);
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