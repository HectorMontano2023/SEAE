package ues.edu.ine.componentes.ui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

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
import com.vaadin.flow.component.textfield.TextField;
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

        H1 titulo = new H1("Comparacion de Tasa de Rendimiento");
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
        Button exportar = new Button("Exportar PDF");
        estiloBoton(calcular);
        estiloBoton(exportar);
        exportar.setEnabled(false);

        Anchor descargaPdf = new Anchor();
        descargaPdf.getElement().setAttribute("download", true);
        descargaPdf.getStyle().set("display", "none");

        HorizontalLayout botones = new HorizontalLayout(calcular, exportar);
        botones.addClassName("seae-actions");

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

        calcular.addClickListener(event -> {
            try {
                ResultadoTir resultado = calcularResultado();
                if (resultado == null) {
                    ultimoResultado = null;
                    panelResultados.setVisible(false);
                    exportar.setEnabled(false);
                    Notification.show("Complete correctamente los datos de ambas alternativas");
                    return;
                }

                mostrarResultado(resultado);
                panelResultados.setVisible(true);
                exportar.setEnabled(true);
                Notification.show("Comparacion realizada", 3000, Position.MIDDLE);
            } catch (IllegalArgumentException ex) {
                ultimoResultado = null;
                panelResultados.setVisible(false);
                exportar.setEnabled(false);
                Notification.show(ex.getMessage(), 4500, Position.MIDDLE);
            }
        });

        exportar.addClickListener(event -> {
            if (ultimoResultado == null) {
                Notification.show("Primero realice una comparacion valida antes de exportar.");
                return;
            }

            try {
                StreamResource resource = generarPDF(ultimoResultado);
                descargaPdf.setHref(resource);
                descargaPdf.getElement().executeJs("this.click()");
            } catch (Exception ex) {
                Notification.show("Error al generar PDF");
            }
        });

        mainContainer.add(titulo, subtitulo, alternativas, botones, panelResultados);
        mainContainer.add(descargaPdf);
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

        TextField flujo = new TextField("Flujos de efectivo");
        flujo.setPlaceholder("Separe los flujos por comas");
        flujo.setWidthFull();

        NumberField vida = new NumberField("Vida util (anos)");
        vida.setPlaceholder("Ingrese la vida util");
        vida.setWidthFull();
        vida.setMin(1);
        vida.setStep(1);

        card.add(inversion, flujo, vida);
        return new AlternativaTir(card, inversion, flujo, vida);
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

        double[] flujosA = parseFlujos(alternativaA);
        double[] flujosB = parseFlujos(alternativaB);

        int vidaAValor = obtenerPeriodoObligatorio(alternativaA.vida(), "Alternativa A: la vida util debe ser mayor que cero");
        int vidaBValor = obtenerPeriodoObligatorio(alternativaB.vida(), "Alternativa B: la vida util debe ser mayor que cero");

        if (flujosA.length != vidaAValor) {
            throw new IllegalArgumentException("Alternativa A: la cantidad de flujos debe coincidir con la vida util");
        }

        if (flujosB.length != vidaBValor) {
            throw new IllegalArgumentException("Alternativa B: la cantidad de flujos debe coincidir con la vida util");
        }

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
        String texto = alternativa.flujos().getValue();
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("Complete los flujos de efectivo de ambas alternativas");
        }

        String[] valores = texto.split(",");
        List<Double> flujos = new ArrayList<>(valores.length);

        for (String valor : valores) {
            String limpio = valor.trim();
            if (limpio.isEmpty()) {
                throw new IllegalArgumentException("No deje flujos vacios en la lista de valores");
            }

            flujos.add(Double.parseDouble(limpio));
        }

        double[] arreglo = new double[flujos.size()];
        for (int i = 0; i < flujos.size(); i++) {
            arreglo[i] = flujos.get(i);
        }

        return arreglo;
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
        double tir = 0.1;

        for (int iteracion = 0; iteracion < 100; iteracion++) {
            double vpn = -inversionInicial;
            double derivada = 0;

            for (int t = 0; t < flujos.length; t++) {
                double factor = Math.pow(1 + tir, t + 1);
                if (!Double.isFinite(factor) || factor == 0.0) {
                    throw new IllegalArgumentException("La TIR no converge con los flujos ingresados");
                }

                vpn += flujos[t] / factor;
                derivada -= (t + 1) * flujos[t] / (factor * (1 + tir));
            }

            if (Math.abs(derivada) < 1e-12) {
                throw new IllegalArgumentException("La TIR no converge con los flujos ingresados");
            }

            double nuevaTir = tir - (vpn / derivada);
            if (!Double.isFinite(nuevaTir) || nuevaTir <= -0.999999) {
                throw new IllegalArgumentException("La TIR no converge con los flujos ingresados");
            }

            if (Math.abs(nuevaTir - tir) < 0.00001) {
                return nuevaTir * 100;
            }

            tir = nuevaTir;
        }

        throw new IllegalArgumentException("La TIR no converge con los flujos ingresados");
    }

    private StreamResource generarPDF(ResultadoTir resultado) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.LETTER);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float pageWidth = page.getMediaBox().getWidth();
                float pageHeight = page.getMediaBox().getHeight();

                dibujarFondo(contentStream, pageWidth, pageHeight);
                dibujarEncabezado(contentStream, pageWidth, pageHeight);
                dibujarAlternativa(contentStream, 50, pageHeight - 150, "ALTERNATIVA A", resultado.alternativaA(), resultado.tirA());
                dibujarAlternativa(contentStream, 300, pageHeight - 150, "ALTERNATIVA B", resultado.alternativaB(), resultado.tirB());
                dibujarResumen(contentStream, 50, pageHeight - 330, 500, 80, resultado.mejorAlternativa());
                dibujarPie(contentStream, pageWidth);
            }

            document.save(baos);
        }

        return new StreamResource("reporte_tir.pdf", () -> new ByteArrayInputStream(baos.toByteArray()));
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

    private void dibujarAlternativa(PDPageContentStream contentStream, float x, float y, String titulo, AlternativaTir alternativa, double tir) throws Exception {
        contentStream.setNonStrokingColor(rgb(255, 255, 255));
        contentStream.addRect(x, y - 180, 220, 180);
        contentStream.fill();

        contentStream.setNonStrokingColor(rgb(123, 75, 183));
        contentStream.addRect(x, y - 22, 220, 22);
        contentStream.fill();

        dibujarTexto(contentStream, x + 12, y - 16, titulo, PDType1Font.HELVETICA_BOLD, 12, rgb(255, 255, 255));
        dibujarTexto(contentStream, x + 12, y - 50, "Inversion inicial: $" + formatear(alternativa.inversion().getValue()), PDType1Font.HELVETICA, 10, rgb(15, 23, 42));
        dibujarTexto(contentStream, x + 12, y - 70, "Flujos: " + alternativa.flujos().getValue(), PDType1Font.HELVETICA, 10, rgb(15, 23, 42));
        dibujarTexto(contentStream, x + 12, y - 90, "Vida util: " + formatear(alternativa.vida().getValue()) + " anos", PDType1Font.HELVETICA, 10, rgb(15, 23, 42));
        dibujarTexto(contentStream, x + 12, y - 120, "TIR: " + formatear(tir) + "%", PDType1Font.HELVETICA_BOLD, 11, rgb(46, 125, 50));
    }

    private void dibujarResumen(PDPageContentStream contentStream, float x, float y, float width, float height, String mejorAlternativa) throws Exception {
        contentStream.setNonStrokingColor(rgb(35, 64, 111));
        contentStream.addRect(x, y - height, width, height);
        contentStream.fill();

        contentStream.setNonStrokingColor(rgb(255, 255, 255));
        contentStream.addRect(x + 6, y - height + 6, width - 12, height - 12);
        contentStream.fill();

        dibujarTexto(contentStream, x + 16, y - 26, "Resumen", PDType1Font.HELVETICA_BOLD, 13, rgb(35, 64, 111));
        dibujarTexto(contentStream, x + 16, y - 50, "Mejor opcion: " + mejorAlternativa, PDType1Font.HELVETICA_BOLD, 11, rgb(35, 64, 111));
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

    private String resourceRegistry(StreamResource resource) {
        return getUI().get().getSession().getResourceRegistry().registerResource(resource).getResourceUri().toString();
    }

    private String formatear(double valor) {
        return String.format("%.2f", valor);
    }

    private record AlternativaTir(VerticalLayout container, NumberField inversion, TextField flujos, NumberField vida) {
    }

    private record ResultadoTir(double tirA, double tirB, String mejorAlternativa, AlternativaTir alternativaA, AlternativaTir alternativaB) {
    }
}