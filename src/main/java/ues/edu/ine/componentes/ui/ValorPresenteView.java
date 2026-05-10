package ues.edu.ine.componentes.ui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;

import ues.edu.ine.base.ui.MainLayout;

@Route(value = "valor-presente", layout = MainLayout.class)
@PageTitle("Valor Presente | SEAE")
@Menu(order = 1, icon = "vaadin:coins", title = "Valor Presente")
public class ValorPresenteView extends VerticalLayout {

    private AlternativaVP alternativaA;
    private AlternativaVP alternativaB;
    private NumberField tasaDescuento;
    private final Paragraph resultadoA = new Paragraph("VP Alternativa A: --");
    private final Paragraph resultadoB = new Paragraph("VP Alternativa B: --");
    private final Paragraph mejorResultado = new Paragraph("Mejor alternativa: --");
    private ResultadoVP ultimoResultado;

    public ValorPresenteView() {
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

        H1 titulo = new H1("Evaluacion de Alternativas Economicas: Valor Presente");
        titulo.addClassName("seae-view-title");

        Paragraph subtitulo = new Paragraph("Compare dos alternativas mediante el metodo de Valor Presente.");
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
        tasaLayout.addClassName("seae-callout-card");

        tasaDescuento = new NumberField();
        tasaDescuento.setPlaceholder("Ingrese el porcentaje");
        tasaDescuento.setWidth("250px");
        tasaDescuento.setMin(0);
        tasaDescuento.setStep(0.1);

        Paragraph tasaLabel = new Paragraph("Tasa de descuento (%):");
        tasaLabel.getStyle().set("font-weight", "bold");

        tasaLayout.add(tasaLabel, tasaDescuento);

        Button comparar = new Button("Comparar");
        Button exportarBtn = new Button("Exportar PDF");
        estiloBoton(comparar);
        estiloBoton(exportarBtn);
        exportarBtn.setEnabled(false);

        Anchor exportarAnchor = new Anchor();
        exportarAnchor.getElement().setAttribute("download", "reporte_valor_presente.pdf");
        exportarAnchor.add(exportarBtn);

        HorizontalLayout botones = new HorizontalLayout(comparar, exportarAnchor);
        botones.addClassName("seae-actions");

        VerticalLayout indicaciones = new VerticalLayout();
        indicaciones.setWidthFull();
        indicaciones.setAlignItems(Alignment.CENTER);
        indicaciones.addClassName("seae-callout-card");
        Paragraph indicacionTexto = new Paragraph("Complete los datos y presione Comparar para ver el valor presente.");
        indicacionTexto.getStyle().set("margin", "0").set("text-align", "center");
        indicaciones.add(indicacionTexto);

        VerticalLayout panelResultados = new VerticalLayout();
        panelResultados.setWidthFull();
        panelResultados.setMaxWidth("750px");
        panelResultados.setAlignItems(Alignment.CENTER);
        panelResultados.setVisible(false);
        panelResultados.addClassName("seae-result-card");

        H2 tituloResultados = new H2("Resultados");
        tituloResultados.getStyle().set("color", "#23406f");
        resultadoA.addClassName("seae-result-value");
        resultadoB.addClassName("seae-result-value");
        mejorResultado.addClassName("seae-result-value");
        mejorResultado.addClassName("seae-result-highlight");
        panelResultados.add(tituloResultados, resultadoA, resultadoB, mejorResultado);

        comparar.addClickListener(event -> {
            try {
                ResultadoVP resultadoCalculado = calcularResultado();
                if (resultadoCalculado == null) {
                    ultimoResultado = null;
                    exportarBtn.setEnabled(false);
                    exportarAnchor.removeHref();
                    panelResultados.setVisible(false);
                    indicaciones.setVisible(true);
                    Notification.show("Complete todos los campos correctamente");
                    return;
                }

                mostrarResultado(resultadoCalculado);
                panelResultados.setVisible(true);
                indicaciones.setVisible(false);
                exportarBtn.setEnabled(true);
                exportarAnchor.setHref(new StreamResource("reporte_valor_presente.pdf", () -> {
                    try {
                        return new ByteArrayInputStream(generarPdf(resultadoCalculado));
                    } catch (Exception e) {
                        e.printStackTrace();
                        return new ByteArrayInputStream(new byte[0]);
                    }
                }));
                Notification.show("Comparacion realizada", 3500, Position.MIDDLE);
            } catch (IllegalArgumentException ex) {
                ultimoResultado = null;
                exportarBtn.setEnabled(false);
                exportarAnchor.removeHref();
                panelResultados.setVisible(false);
                indicaciones.setVisible(true);
                Notification.show(ex.getMessage(), 4000, Position.MIDDLE);
            }
        });

        mainContainer.add(titulo, subtitulo, alternativas, tasaLayout, botones, indicaciones, panelResultados);
        add(mainContainer);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        Object user = VaadinSession.getCurrent().getAttribute("user");
        if (user == null) {
            attachEvent.getUI().navigate("login");
        }
    }

    private AlternativaVP crearAlternativa(String tituloTexto) {
        VerticalLayout card = new VerticalLayout();
        card.setWidth("100%");
        card.setMaxWidth("420px");
        card.setPadding(true);
        card.setSpacing(true);
        card.addClassName("seae-card");

        H2 titulo = new H2(tituloTexto);
        titulo.addClassName("seae-card-title");

        NumberField inversion = crearCampoNumero("Inversion inicial", "Ingrese la inversion inicial");
        NumberField flujo = crearCampoNumero("Flujo anual", "Ingrese el flujo de efectivo");
        NumberField vida = crearCampoNumero("Vida util (anos)", "Ingrese el numero de anos");
        vida.setStep(1);
        vida.setMin(1);

        card.add(titulo, inversion, flujo, vida);
        return new AlternativaVP(card, inversion, flujo, vida);
    }

    private NumberField crearCampoNumero(String label, String placeholder) {
        NumberField campo = new NumberField(label);
        campo.setPlaceholder(placeholder);
        campo.setWidthFull();
        campo.setMin(0);
        campo.setStep(0.01);
        return campo;
    }

    private ResultadoVP calcularResultado() {
        double tasa = obtenerNumeroObligatorio(tasaDescuento, "Ingrese la tasa de descuento");
        double vpA = calcularVP(alternativaA, tasa);
        double vpB = calcularVP(alternativaB, tasa);

        String mejor = vpA > vpB ? "Alternativa A" : vpB > vpA ? "Alternativa B" : "Ambas son iguales";
        return new ResultadoVP(tasa, vpA, vpB, mejor, alternativaA, alternativaB);
    }

    private void mostrarResultado(ResultadoVP resultadoCalculado) {
        ultimoResultado = resultadoCalculado;
        resultadoA.setText("VP Alternativa A: $" + formatear(resultadoCalculado.vpA()));
        resultadoB.setText("VP Alternativa B: $" + formatear(resultadoCalculado.vpB()));
        mejorResultado.setText("Mejor opcion: " + resultadoCalculado.mejor());
    }

    private double calcularVP(AlternativaVP alternativa, double tasa) {
        double inversionInicial = obtenerNumeroObligatorio(alternativa.inversion(), "Complete todos los campos correctamente");
        double flujoEfectivo = obtenerNumeroObligatorio(alternativa.flujo(), "Complete todos los campos correctamente");
        int anos = obtenerAnosObligatorios(alternativa.vida(), "La vida util debe ser mayor que cero");

        double i = tasa / 100.0;
        double vpFlujos = 0;

        for (int t = 1; t <= anos; t++) {
            vpFlujos += flujoEfectivo / Math.pow(1 + i, t);
        }

        return vpFlujos - inversionInicial;
    }

    private int obtenerAnosObligatorios(NumberField field, String mensajeError) {
        double valor = obtenerNumeroObligatorio(field, mensajeError);
        int anos = (int) Math.round(valor);
        if (anos <= 0 || Math.abs(anos - valor) > 0.00001) {
            throw new IllegalArgumentException(mensajeError);
        }

        return anos;
    }

    private double obtenerNumeroObligatorio(NumberField field, String mensajeError) {
        if (field.isEmpty() || field.getValue() == null || !Double.isFinite(field.getValue())) {
            throw new IllegalArgumentException(mensajeError);
        }

        return field.getValue();
    }

    private void estiloBoton(Button boton) {
        boton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        boton.addClassName("seae-primary-button");
    }

    private byte[] generarPdf(ResultadoVP resultado) throws Exception {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.LETTER);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float pageWidth = page.getMediaBox().getWidth();
                float pageHeight = page.getMediaBox().getHeight();

                dibujarFondo(contentStream, pageWidth, pageHeight);
                dibujarEncabezado(contentStream, pageWidth, pageHeight, resultado);
                dibujarContenido(contentStream, resultado, pageHeight);
                dibujarPie(contentStream, pageWidth);
            }

            document.save(outputStream);
            return outputStream.toByteArray();
        }
    }

    private void dibujarFondo(PDPageContentStream contentStream, float pageWidth, float pageHeight) throws Exception {
        contentStream.setNonStrokingColor(rgb(244, 246, 251));
        contentStream.addRect(0, 0, pageWidth, pageHeight);
        contentStream.fill();

        contentStream.setNonStrokingColor(rgb(223, 230, 247));
        contentStream.addRect(pageWidth - 160, pageHeight - 110, 160, 110);
        contentStream.fill();
    }

    private void dibujarEncabezado(PDPageContentStream contentStream, float pageWidth, float pageHeight, ResultadoVP resultado) throws Exception {
        contentStream.setNonStrokingColor(rgb(36, 64, 111));
        contentStream.addRect(0, pageHeight - 115, pageWidth, 115);
        contentStream.fill();

        contentStream.beginText();
        contentStream.setNonStrokingColor(rgb(255, 255, 255));
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 20);
        contentStream.newLineAtOffset(50, pageHeight - 55);
        contentStream.showText("SEAE - Reporte de Valor Presente");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, 10);
        contentStream.newLineAtOffset(50, pageHeight - 75);
        contentStream.showText("Comparacion de alternativas economicas.");
        contentStream.endText();

        contentStream.setNonStrokingColor(rgb(255, 255, 255));
        contentStream.addRect(pageWidth - 180, pageHeight - 95, 120, 35);
        contentStream.fill();

        contentStream.beginText();
        contentStream.setNonStrokingColor(rgb(36, 64, 111));
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 9);
        contentStream.newLineAtOffset(pageWidth - 160, pageHeight - 73);
        contentStream.showText("Tasa: " + formatear(resultado.tasa()) + "%");
        contentStream.endText();
    }

    private void dibujarContenido(PDPageContentStream contentStream, ResultadoVP resultado, float pageHeight) throws Exception {
        float leftX = 50;
        float cardWidth = 240;
        float cardHeight = 200;
        float gap = 18;

        dibujarTarjetaAlternativa(contentStream, leftX, 650, cardWidth, cardHeight, "Alternativa A", resultado.alternativaA(), resultado.vpA(), new int[] { 54, 93, 173 });
        dibujarTarjetaAlternativa(contentStream, leftX + cardWidth + gap, 650, cardWidth, cardHeight, "Alternativa B", resultado.alternativaB(), resultado.vpB(), new int[] { 91, 123, 216 });

        dibujarBloqueResumen(contentStream, leftX, 395, pageHeight - 100, 130, resultado);
    }

    private void dibujarTarjetaAlternativa(PDPageContentStream contentStream, float x, float y, float width, float height, String titulo, AlternativaVP alternativa, double vp, int[] colorBarra) throws Exception {
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

        dibujarLineaClaveValor(contentStream, textX, textY, "Inversion inicial", "$" + formatear(alternativa.inversion().getValue()));
        dibujarLineaClaveValor(contentStream, textX, textY - lineGap, "Flujo anual", "$" + formatear(alternativa.flujo().getValue()));
        dibujarLineaClaveValor(contentStream, textX, textY - (lineGap * 2), "Vida util", formatear(alternativa.vida().getValue()) + " anos");

        contentStream.setStrokingColor(rgb(230, 235, 244));
        contentStream.moveTo(x + 12, y - 90);
        contentStream.lineTo(x + width - 12, y - 90);
        contentStream.stroke();

        dibujarLineaTexto(contentStream, textX, y - 110, "Valor Presente:", PDType1Font.HELVETICA_BOLD, 10, rgb(71, 85, 105));
        dibujarLineaTexto(contentStream, textX + 118, y - 110, "$" + formatear(vp), PDType1Font.HELVETICA_BOLD, 11, rgb(31, 65, 135));

        contentStream.beginText();
        contentStream.setNonStrokingColor(rgb(109, 117, 130));
        contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 9);
        contentStream.newLineAtOffset(textX, y - 140);
        contentStream.showText("Alternativa registrada para comparacion economica.");
        contentStream.endText();
    }

    private void dibujarLineaClaveValor(PDPageContentStream contentStream, float x, float y, String etiqueta, String valor) throws Exception {
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

    private void dibujarBloqueResumen(PDPageContentStream contentStream, float x, float y, float width, float height, ResultadoVP resumen) throws Exception {
        contentStream.setNonStrokingColor(rgb(31, 65, 135));
        contentStream.addRect(x, y - height, 500, height);
        contentStream.fill();

        contentStream.setNonStrokingColor(rgb(255, 255, 255));
        contentStream.addRect(x + 6, y - height + 6, 500 - 12, height - 12);
        contentStream.fill();

        contentStream.beginText();
        contentStream.setNonStrokingColor(rgb(31, 65, 135));
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 13);
        contentStream.newLineAtOffset(x + 16, y - 28);
        contentStream.showText("Resumen y recomendacion");
        contentStream.endText();

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
        contentStream.showText("VP Alternativa A: $" + formatear(resumen.vpA()) + "   VP Alternativa B: $" + formatear(resumen.vpB()));
        contentStream.endText();

        contentStream.setNonStrokingColor(rgb(236, 241, 252));
        contentStream.addRect(x + 16, y - 104, 500 - 32, 30);
        contentStream.fill();

        contentStream.beginText();
        contentStream.setNonStrokingColor(rgb(31, 65, 135));
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
        contentStream.newLineAtOffset(x + 24, y - 85);
        contentStream.showText("Recomendacion: " + resumen.mejor() + " es la opcion equivalente mas conveniente.");
        contentStream.endText();
    }

    private void dibujarPie(PDPageContentStream contentStream, float pageWidth) throws Exception {
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

    private void dibujarLineaTexto(PDPageContentStream contentStream, float x, float y, String texto, PDType1Font font, int size, PDColor color) throws Exception {
        contentStream.beginText();
        contentStream.setNonStrokingColor(color);
        contentStream.setFont(font, size);
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(texto);
        contentStream.endText();
    }

    private PDColor rgb(int red, int green, int blue) {
        return new PDColor(new float[] { red / 255f, green / 255f, blue / 255f }, PDDeviceRGB.INSTANCE);
    }


    private String formatear(double valor) {
        return String.format("%.2f", valor);
    }

    private record AlternativaVP(VerticalLayout container, NumberField inversion, NumberField flujo, NumberField vida) {
    }

    private record ResultadoVP(double tasa, double vpA, double vpB, String mejor, AlternativaVP alternativaA, AlternativaVP alternativaB) {
    }
}