package ues.edu.ine.componentes.ui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.StringJoiner;

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
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;

import ues.edu.ine.base.ui.MainLayout;

@Route(value = "tasa-rendimiento", layout = MainLayout.class)
@PageTitle("Tasa de Rendimiento | SEAE")
@Menu(order = 2, icon = "vaadin:chart", title = "Tasa de Rendimiento")
public class TasaRendimientoView extends VerticalLayout {

    private AlternativaTir alternativaA;
    private AlternativaTir alternativaB;
    private final Paragraph resultadoA = new Paragraph("TIR Alternativa A: --");
    private final Paragraph resultadoB = new Paragraph("TIR Alternativa B: --");
    private final Paragraph mejorResultado = new Paragraph("Mejor alternativa: --");
    private ResultadoTir ultimoResultado;

    public TasaRendimientoView() {
        setWidthFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.START);
        setPadding(true);
        setSpacing(true);
        addClassName("seae-page");

        VerticalLayout mainContainer = new VerticalLayout();
        mainContainer.setWidthFull();
        mainContainer.setMaxWidth("1150px");
        mainContainer.setPadding(true);
        mainContainer.setSpacing(true);
        mainContainer.setAlignItems(Alignment.CENTER);
        mainContainer.addClassName("seae-surface");

        H1 titulo = new H1("Evaluacion de Alternativas Economicas: Tasa de Rendimiento");
        titulo.addClassName("seae-view-title");

        Paragraph subtitulo = new Paragraph("Compare dos alternativas usando la TIR.");
        subtitulo.addClassName("seae-view-subtitle");

        alternativaA = crearAlternativa("Alternativa A");
        alternativaB = crearAlternativa("Alternativa B");

        HorizontalLayout alternativas = new HorizontalLayout(alternativaA.container(), alternativaB.container());
        alternativas.setWidthFull();
        alternativas.setSpacing(true);
        alternativas.setWrap(true);
        alternativas.setJustifyContentMode(JustifyContentMode.CENTER);

        Button calcular = new Button("Comparar");
        Button exportarBtn = new Button("Exportar PDF");
        estiloBoton(calcular);
        estiloBoton(exportarBtn);
        exportarBtn.setEnabled(false);

        Anchor exportarAnchor = new Anchor();
        exportarAnchor.getElement().setAttribute("download", "reporte_tir.pdf");
        exportarAnchor.add(exportarBtn);

        HorizontalLayout botones = new HorizontalLayout(calcular, exportarAnchor);
        botones.addClassName("seae-actions");

        VerticalLayout panelResultados = new VerticalLayout();
        panelResultados.setWidthFull();
        panelResultados.setMaxWidth("750px");
        panelResultados.setAlignItems(Alignment.CENTER);
        panelResultados.setVisible(false);
        panelResultados.addClassName("seae-result-card");

        VerticalLayout indicaciones = new VerticalLayout();
        indicaciones.setWidthFull();
        indicaciones.setAlignItems(Alignment.CENTER);
        indicaciones.addClassName("seae-callout-card");

        Paragraph indicacionTexto = new Paragraph("Complete los datos y presione Comparar para ver la tasa de rendimiento.");
        indicacionTexto.getStyle().set("margin", "0").set("text-align", "center");
        indicaciones.add(indicacionTexto);

        H2 tituloResultados = new H2("Resultados");
        tituloResultados.getStyle().set("color", "#23406f");

        resultadoA.addClassName("seae-result-value");
        resultadoB.addClassName("seae-result-value");
        mejorResultado.addClassName("seae-result-value");
        mejorResultado.addClassName("seae-result-highlight");

        panelResultados.add(tituloResultados, resultadoA, resultadoB, mejorResultado);

        calcular.addClickListener(event -> {
            try {
                ResultadoTir resultado = calcularResultado();
                if (resultado == null) {
                    ultimoResultado = null;
                    panelResultados.setVisible(false);
                    indicaciones.setVisible(true);
                    Notification.show("Complete correctamente los datos de ambas alternativas");
                    return;
                }

                mostrarResultado(resultado);
                panelResultados.setVisible(true);
                indicaciones.setVisible(false);
                exportarBtn.setEnabled(true);
                exportarAnchor.setHref(new StreamResource("reporte_tir.pdf", () -> {
                    try {
                        return new ByteArrayInputStream(generarPDF(resultado));
                    } catch (Exception e) {
                        e.printStackTrace();
                        return new ByteArrayInputStream(new byte[0]);
                    }
                }));
                Notification.show("Comparacion realizada", 3000, Position.MIDDLE);
            } catch (IllegalArgumentException ex) {
                ultimoResultado = null;
                panelResultados.setVisible(false);
                indicaciones.setVisible(true);
                exportarBtn.setEnabled(false);
                exportarAnchor.removeHref();
                Notification.show(ex.getMessage(), 4500, Position.MIDDLE);
            }
        });

        mainContainer.add(titulo, subtitulo, alternativas, botones, indicaciones, panelResultados);
        add(mainContainer);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        Object user = VaadinSession.getCurrent().getAttribute("user");
        if (user == null) {
            attachEvent.getUI().navigate("login");
        }
    }

    private AlternativaTir crearAlternativa(String tituloTexto) {
        VerticalLayout card = crearCardBase(tituloTexto);

        NumberField inversion = new NumberField("Inversion inicial");
        inversion.setPlaceholder("Ingrese la inversion inicial");
        inversion.setWidthFull();

        NumberField periodos = new NumberField("Numero de periodos (n)");
        periodos.setPlaceholder("Ingrese el numero de periodos");
        periodos.setWidthFull();
        periodos.setMin(1);
        periodos.setStep(1);

        VerticalLayout flujosContainer = new VerticalLayout();
        flujosContainer.setWidthFull();
        flujosContainer.setSpacing(true);
        flujosContainer.setPadding(false);

        AlternativaTir alternativa = new AlternativaTir(card, inversion, periodos, flujosContainer, new ArrayList<>());
        periodos.addValueChangeListener(event -> actualizarCamposFlujo(alternativa, event.getValue()));
        actualizarCamposFlujo(alternativa, null);

        card.add(inversion, periodos, flujosContainer);
        return alternativa;
    }

    private VerticalLayout crearCardBase(String tituloTexto) {
        VerticalLayout card = new VerticalLayout();
        card.setWidth("100%");
        card.setMaxWidth("450px");
        card.setPadding(true);
        card.setSpacing(true);
        card.addClassName("seae-card");

        H2 titulo = new H2(tituloTexto);
        titulo.addClassName("seae-card-title");

        card.add(titulo);
        return card;
    }

    private void estiloBoton(Button boton) {
        boton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        boton.addClassName("seae-primary-button");
    }

    private ResultadoTir calcularResultado() {
        double inversionAValor = obtenerNumeroObligatorio(alternativaA.inversion(), "Alternativa A: la inversion inicial es obligatoria");
        double inversionBValor = obtenerNumeroObligatorio(alternativaB.inversion(), "Alternativa B: la inversion inicial es obligatoria");

        int vidaAValor = obtenerPeriodoObligatorio(alternativaA.periodos(), "Alternativa A: la vida util debe ser mayor que cero");
        int vidaBValor = obtenerPeriodoObligatorio(alternativaB.periodos(), "Alternativa B: la vida util debe ser mayor que cero");

        if (alternativaA.flujos().size() != vidaAValor) {
            throw new IllegalArgumentException("Alternativa A: complete un flujo por cada periodo generado");
        }

        if (alternativaB.flujos().size() != vidaBValor) {
            throw new IllegalArgumentException("Alternativa B: complete un flujo por cada periodo generado");
        }

        double[] flujosA = parseFlujos(alternativaA);
        double[] flujosB = parseFlujos(alternativaB);

        double tirAValor = calcularTIR(inversionAValor, flujosA);
        double tirBValor = calcularTIR(inversionBValor, flujosB);

        String mejor;
        if (tirAValor > tirBValor) {
            mejor = "Alternativa A";
        } else if (tirBValor > tirAValor) {
            mejor = "Alternativa B";
        } else {
            mejor = "Ambas son iguales";
        }

        return new ResultadoTir(tirAValor, tirBValor, mejor, alternativaA, alternativaB);
    }

    private void mostrarResultado(ResultadoTir resultado) {
        ultimoResultado = resultado;
        resultadoA.setText("TIR Alternativa A: " + formatear(resultado.tirA()) + "%");
        resultadoB.setText("TIR Alternativa B: " + formatear(resultado.tirB()) + "%");
        mejorResultado.setText("Mejor alternativa: " + resultado.mejorAlternativa());
    }

    private double[] parseFlujos(AlternativaTir alternativa) {
        List<NumberField> campos = alternativa.flujos();
        if (campos.isEmpty()) {
            throw new IllegalArgumentException("Indique el numero de periodos antes de calcular");
        }

        double[] flujos = new double[campos.size()];
        for (int i = 0; i < campos.size(); i++) {
            NumberField campo = campos.get(i);
            if (campo.isEmpty() || campo.getValue() == null || !Double.isFinite(campo.getValue())) {
                throw new IllegalArgumentException("Complete los flujos de efectivo de todos los periodos");
            }

            flujos[i] = campo.getValue();
        }

        return flujos;
    }

    private void actualizarCamposFlujo(AlternativaTir alternativa, Double valorPeriodos) {
        alternativa.flujos().clear();
        alternativa.flujosContainer().removeAll();

        if (valorPeriodos == null || !Double.isFinite(valorPeriodos) || valorPeriodos < 1) {
            Paragraph indicacion = new Paragraph("Indique el numero de periodos para generar los flujos de efectivo.");
            indicacion.getStyle().set("margin", "0").set("color", "#4b5563");
            alternativa.flujosContainer().add(indicacion);
            return;
        }

        int periodos = (int) Math.round(valorPeriodos);
        if (Math.abs(periodos - valorPeriodos) > 0.00001) {
            Paragraph indicacion = new Paragraph("El numero de periodos debe ser entero.");
            indicacion.getStyle().set("margin", "0").set("color", "#4b5563");
            alternativa.flujosContainer().add(indicacion);
            return;
        }

        for (int periodo = 1; periodo <= periodos; periodo++) {
            NumberField flujo = new NumberField("Flujo F" + periodo);
            flujo.setPlaceholder("Ingrese F" + periodo);
            flujo.setWidthFull();
            flujo.setStep(0.01);
            flujo.setMin(-999999999);
            alternativa.flujos().add(flujo);
            alternativa.flujosContainer().add(flujo);
        }
    }

    private int obtenerPeriodoObligatorio(NumberField field, String mensajeError) {
        double valor = obtenerNumeroObligatorio(field, mensajeError);
        int periodo = (int) Math.round(valor);
        if (periodo <= 0 || Math.abs(periodo - valor) > 0.00001) {
            throw new IllegalArgumentException(mensajeError);
        }
        return periodo;
    }

    private double obtenerNumeroObligatorio(NumberField field, String mensajeError) {
        if (field.isEmpty() || field.getValue() == null || !Double.isFinite(field.getValue())) {
            throw new IllegalArgumentException(mensajeError);
        }

        return field.getValue();
    }

    private double calcularTIR(double inversionInicial, double[] flujos) {
        double low = -0.9999;
        double high = 1000.0;
        double tir = 0;

        for (int iteracion = 0; iteracion < 1000; iteracion++) {
            tir = (low + high) / 2.0;
            double vpn = -inversionInicial;

            for (int t = 0; t < flujos.length; t++) {
                vpn += flujos[t] / Math.pow(1 + tir, t + 1);
            }

            if (Math.abs(vpn) < 1e-7) {
                return tir * 100;
            }

            if (vpn > 0) {
                low = tir;
            } else {
                high = tir;
            }
        }

        return tir * 100;
    }

    private byte[] generarPDF(ResultadoTir resultado) throws Exception {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.LETTER);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float pageWidth = page.getMediaBox().getWidth();
                float pageHeight = page.getMediaBox().getHeight();

                dibujarFondo(contentStream, pageWidth, pageHeight);
                dibujarEncabezado(contentStream, pageWidth, pageHeight);

                float leftX = 50;
                float cardWidth = 240;
                float cardHeight = 200;
                float gap = 18;

                dibujarAlternativa(contentStream, leftX, 650, "Alternativa A", resultado.alternativaA(), resultado.tirA(), new int[] { 54, 93, 173 });
                dibujarAlternativa(contentStream, leftX + cardWidth + gap, 650, "Alternativa B", resultado.alternativaB(), resultado.tirB(), new int[] { 91, 123, 216 });
                dibujarResumen(contentStream, leftX, 395, 500, 110, resultado);
                dibujarPie(contentStream, pageWidth);
            }

            document.save(baos);
            return baos.toByteArray();
        }
    }

    private void dibujarFondo(PDPageContentStream contentStream, float pageWidth, float pageHeight) throws Exception {
        contentStream.setNonStrokingColor(rgb(245, 247, 252));
        contentStream.addRect(0, 0, pageWidth, pageHeight);
        contentStream.fill();
    }

    private void dibujarEncabezado(PDPageContentStream contentStream, float pageWidth, float pageHeight) throws Exception {
        contentStream.setNonStrokingColor(rgb(123, 75, 183));
        contentStream.addRect(0, pageHeight - 115, pageWidth, 115);
        contentStream.fill();

        contentStream.beginText();
        contentStream.setNonStrokingColor(rgb(255, 255, 255));
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 20);
        contentStream.newLineAtOffset(50, pageHeight - 55);
        contentStream.showText("SEAE - Reporte de Tasa de Rendimiento");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, 10);
        contentStream.newLineAtOffset(50, pageHeight - 75);
        contentStream.showText("Comparacion de alternativas con TIR.");
        contentStream.endText();
    }

    private void dibujarAlternativa(PDPageContentStream contentStream, float x, float y, String titulo, AlternativaTir alternativa, double tir, int[] colorBarra) throws Exception {
        float width = 240;
        float height = 200;

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
        dibujarLineaClaveValor(contentStream, textX, textY - lineGap, "Periodos", formatear(alternativa.periodos().getValue()) + " anos");

        String flujosStr = formatearFlujos(alternativa.flujos());
        if (flujosStr.length() > 20) {
            flujosStr = flujosStr.substring(0, 17) + "...";
        }
        dibujarLineaClaveValor(contentStream, textX, textY - (lineGap * 2), "Flujos", flujosStr);

        contentStream.setStrokingColor(rgb(230, 235, 244));
        contentStream.moveTo(x + 12, y - 90);
        contentStream.lineTo(x + width - 12, y - 90);
        contentStream.stroke();

        dibujarLineaTexto(contentStream, textX, y - 110, "TIR:", PDType1Font.HELVETICA_BOLD, 10, rgb(71, 85, 105));
        dibujarLineaTexto(contentStream, textX + 118, y - 110, formatear(tir) + "%", PDType1Font.HELVETICA_BOLD, 11, rgb(46, 125, 50));

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

    private void dibujarResumen(PDPageContentStream contentStream, float x, float y, float width, float height, ResultadoTir resultado) throws Exception {
        contentStream.setNonStrokingColor(rgb(35, 64, 111));
        contentStream.addRect(x, y - height, width, height);
        contentStream.fill();

        contentStream.setNonStrokingColor(rgb(255, 255, 255));
        contentStream.addRect(x + 6, y - height + 6, width - 12, height - 12);
        contentStream.fill();

        contentStream.beginText();
        contentStream.setNonStrokingColor(rgb(35, 64, 111));
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 13);
        contentStream.newLineAtOffset(x + 16, y - 28);
        contentStream.showText("Resumen y recomendacion");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setNonStrokingColor(rgb(15, 23, 42));
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 11);
        contentStream.newLineAtOffset(x + 16, y - 52);
        contentStream.showText("TIR Alternativa A: " + formatear(resultado.tirA()) + "%   TIR Alternativa B: " + formatear(resultado.tirB()) + "%");
        contentStream.endText();

        contentStream.setNonStrokingColor(rgb(236, 241, 252));
        contentStream.addRect(x + 16, y - 84, width - 32, 30);
        contentStream.fill();

        contentStream.beginText();
        contentStream.setNonStrokingColor(rgb(35, 64, 111));
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
        contentStream.newLineAtOffset(x + 24, y - 65);
        contentStream.showText("Recomendacion: " + resultado.mejorAlternativa() + " es la opcion equivalente mas conveniente.");
        contentStream.endText();
    }

    private void dibujarPie(PDPageContentStream contentStream, float pageWidth) throws Exception {
        contentStream.setStrokingColor(rgb(214, 222, 235));
        contentStream.moveTo(50, 70);
        contentStream.lineTo(pageWidth - 50, 70);
        contentStream.stroke();

        dibujarTexto(contentStream, 50, 52, "Generado por SEAE - Sistema de Evaluacion de Alternativas Economicas", PDType1Font.HELVETICA, 9, rgb(100, 116, 139));
    }

    private void dibujarTexto(PDPageContentStream contentStream, float x, float y, String texto, PDType1Font font, int size, PDColor color) throws Exception {
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

    private String formatearFlujos(List<NumberField> flujos) {
        StringJoiner joiner = new StringJoiner(", ");
        for (NumberField flujo : flujos) {
            joiner.add(flujo.isEmpty() ? "" : formatear(flujo.getValue()));
        }

        return joiner.toString();
    }

    private record AlternativaTir(VerticalLayout container, NumberField inversion, NumberField periodos, VerticalLayout flujosContainer, List<NumberField> flujos) {
    }

    private record ResultadoTir(double tirA, double tirB, String mejorAlternativa, AlternativaTir alternativaA, AlternativaTir alternativaB) {
    }
}