package ues.edu.ine.componentes.ui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;

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
    private final Paragraph resultado = new Paragraph("Complete los datos y presione Comparar para ver el valor presente.");
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
        Button exportar = new Button("Exportar PDF");
        estiloBoton(comparar);
        estiloBoton(exportar);
        exportar.setEnabled(false);

        Anchor descargaPdf = new Anchor();
        descargaPdf.getElement().setAttribute("download", "reporte_valor_presente.pdf");
        descargaPdf.getStyle().set("display", "none");

        comparar.addClickListener(event -> {
            try {
                ResultadoVP resultadoCalculado = calcularResultado();
                if (resultadoCalculado == null) {
                    ultimoResultado = null;
                    exportar.setEnabled(false);
                    resultado.setText("Complete todos los campos correctamente para comparar.");
                    Notification.show("Complete todos los campos correctamente");
                    return;
                }

                mostrarResultado(resultadoCalculado);
                exportar.setEnabled(true);
                Notification.show("Comparacion realizada", 3500, Position.MIDDLE);
            } catch (IllegalArgumentException ex) {
                ultimoResultado = null;
                exportar.setEnabled(false);
                resultado.setText(ex.getMessage());
                Notification.show(ex.getMessage(), 4000, Position.MIDDLE);
            }
        });

        exportar.addClickListener(event -> {
            if (ultimoResultado == null) {
                Notification.show("Primero realice una comparacion valida antes de exportar.");
                return;
            }

            try {
                StreamResource resource = generarPdf(ultimoResultado);
                descargaPdf.setHref(resource);
                descargaPdf.getElement().executeJs("this.click()");
            } catch (Exception ex) {
                Notification.show("Error al generar PDF");
            }
        });

        HorizontalLayout botones = new HorizontalLayout(comparar, exportar);
        botones.addClassName("seae-actions");

        VerticalLayout resultadoCard = new VerticalLayout();
        resultadoCard.addClassName("seae-result-card");
        resultadoCard.add(resultado);

        mainContainer.add(titulo, subtitulo, alternativas, tasaLayout, botones, descargaPdf, resultadoCard);
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
        resultado.setText(
            "VP A: $" + formatear(resultadoCalculado.vpA()) +
            " | VP B: $" + formatear(resultadoCalculado.vpB()) +
            " | Mejor opcion: " + resultadoCalculado.mejor()
        );
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

    private StreamResource generarPdf(ResultadoVP resultado) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try (PDDocument document = new PDDocument()) {
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
        }

        return new StreamResource("reporte_valor_presente.pdf", () -> new ByteArrayInputStream(outputStream.toByteArray()));
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
        float startY = pageHeight - 150;
        float lineGap = 18;

        dibujarLineaTexto(contentStream, 50, startY, "Alternativa A", PDType1Font.HELVETICA_BOLD, 12, rgb(36, 64, 111));
        dibujarLineaTexto(contentStream, 50, startY - lineGap, "Inversion inicial: $" + formatear(resultado.alternativaA().inversion().getValue()), PDType1Font.HELVETICA, 11, rgb(15, 23, 42));
        dibujarLineaTexto(contentStream, 50, startY - (lineGap * 2), "Flujo anual: $" + formatear(resultado.alternativaA().flujo().getValue()), PDType1Font.HELVETICA, 11, rgb(15, 23, 42));
        dibujarLineaTexto(contentStream, 50, startY - (lineGap * 3), "Vida util: " + formatear(resultado.alternativaA().vida().getValue()) + " anos", PDType1Font.HELVETICA, 11, rgb(15, 23, 42));
        dibujarLineaTexto(contentStream, 50, startY - (lineGap * 4), "VP: $" + formatear(resultado.vpA()), PDType1Font.HELVETICA_BOLD, 11, rgb(31, 65, 135));

        dibujarLineaTexto(contentStream, 300, startY, "Alternativa B", PDType1Font.HELVETICA_BOLD, 12, rgb(36, 64, 111));
        dibujarLineaTexto(contentStream, 300, startY - lineGap, "Inversion inicial: $" + formatear(resultado.alternativaB().inversion().getValue()), PDType1Font.HELVETICA, 11, rgb(15, 23, 42));
        dibujarLineaTexto(contentStream, 300, startY - (lineGap * 2), "Flujo anual: $" + formatear(resultado.alternativaB().flujo().getValue()), PDType1Font.HELVETICA, 11, rgb(15, 23, 42));
        dibujarLineaTexto(contentStream, 300, startY - (lineGap * 3), "Vida util: " + formatear(resultado.alternativaB().vida().getValue()) + " anos", PDType1Font.HELVETICA, 11, rgb(15, 23, 42));
        dibujarLineaTexto(contentStream, 300, startY - (lineGap * 4), "VP: $" + formatear(resultado.vpB()), PDType1Font.HELVETICA_BOLD, 11, rgb(31, 65, 135));

        contentStream.setNonStrokingColor(rgb(31, 65, 135));
        contentStream.addRect(50, pageHeight - 330, 500, 70);
        contentStream.fill();

        contentStream.setNonStrokingColor(rgb(255, 255, 255));
        contentStream.addRect(56, pageHeight - 324, 488, 58);
        contentStream.fill();

        dibujarLineaTexto(contentStream, 70, pageHeight - 295, "Mejor opcion: " + resultado.mejor(), PDType1Font.HELVETICA_BOLD, 12, rgb(31, 65, 135));
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

    private String resourceRegistry(StreamResource resource) {
        return getUI().get().getSession().getResourceRegistry().registerResource(resource).getResourceUri().toString();
    }

    private String formatear(double valor) {
        return String.format("%.2f", valor);
    }

    private record AlternativaVP(VerticalLayout container, NumberField inversion, NumberField flujo, NumberField vida) {
    }

    private record ResultadoVP(double tasa, double vpA, double vpB, String mejor, AlternativaVP alternativaA, AlternativaVP alternativaB) {
    }
}