package ues.edu.ine.componentes.ui;

import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfWriter;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;

import ues.edu.ine.base.ui.MainLayout;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

@Route(value = "tasa-rendimiento", layout = MainLayout.class)
@PageTitle("Tasa de Rendimiento | SEAE")
@Menu(order = 2, icon = "vaadin:chart", title = "Tasa de Rendimiento")

public class TasaRendimientoView extends VerticalLayout {

    // Alternativa A
    private NumberField inversionA;
    private TextField flujoA;
    private NumberField vidaA;

    // Alternativa B
    private NumberField inversionB;
    private TextField flujoB;
    private NumberField vidaB;

    // Resultados
    private Paragraph resultadoA;
    private Paragraph resultadoB;
    private Paragraph mejorResultado;

    private double tirA = 0;
    private double tirB = 0;

    private String mejorAlternativa = "";

    public TasaRendimientoView() {

        setWidthFull();

        setAlignItems(Alignment.CENTER);

        setJustifyContentMode(
            JustifyContentMode.START
        );

        setPadding(true);

        setSpacing(true);

        getStyle()
            .set("padding", "30px");

        // CONTENEDOR PRINCIPAL
        VerticalLayout mainContainer =
            new VerticalLayout();

        mainContainer.setWidthFull();

        mainContainer.setMaxWidth("1150px");

        mainContainer.setPadding(true);

        mainContainer.setSpacing(true);

        mainContainer.setAlignItems(
            Alignment.CENTER
        );

        // TITULO
        H1 titulo = new H1(
            "Comparación de Tasa de Rendimiento"
        );

        titulo.getStyle()
            .set("color", "#2c2c2c")
            .set("margin-bottom", "0");

        Paragraph subtitulo =
            new Paragraph(
                "Compare dos alternativas usando la TIR."
            );

        subtitulo.getStyle()
            .set("color", "#666")
            .set("margin-top", "0");

        // CARDS
        VerticalLayout cardA =
            crearAlternativaA();

        VerticalLayout cardB =
            crearAlternativaB();

        HorizontalLayout alternativas =
            new HorizontalLayout(
                cardA,
                cardB
            );

        alternativas.setWidthFull();

        alternativas.setSpacing(true);

        alternativas.setWrap(true);

        alternativas.setJustifyContentMode(
            JustifyContentMode.CENTER
        );

        // BOTONES
        Button calcular =
            new Button("Comparar");

        Button exportar =
            new Button("Exportar PDF");

        estiloBoton(calcular);

        estiloBoton(exportar);

        HorizontalLayout botones =
            new HorizontalLayout(
                calcular,
                exportar
            );

        botones.setSpacing(true);

        // PANEL RESULTADOS
        VerticalLayout panelResultados =
            new VerticalLayout();

        panelResultados.setWidthFull();

        panelResultados.setMaxWidth("750px");

        panelResultados.setAlignItems(
            Alignment.CENTER
        );

        panelResultados.setVisible(false);

        panelResultados.getStyle()
            .set("background-color", "#ffffff")
            .set("border-radius", "20px")
            .set("padding", "30px")
            .set("margin-top", "25px")
            .set(
                "box-shadow",
                "0 4px 12px rgba(0,0,0,0.1)"
            );

        H2 tituloResultados =
            new H2("Resultados");

        tituloResultados.getStyle()
            .set("color", "#7b4bb7");

        resultadoA =
            new Paragraph(
                "TIR Alternativa A:"
            );

        resultadoB =
            new Paragraph(
                "TIR Alternativa B:"
            );

        mejorResultado =
            new Paragraph(
                "Mejor alternativa:"
            );

        resultadoA.getStyle()
            .set("font-size", "18px")
            .set("font-weight", "bold");

        resultadoB.getStyle()
            .set("font-size", "18px")
            .set("font-weight", "bold");

        mejorResultado.getStyle()
            .set("font-size", "20px")
            .set("font-weight", "bold")
            .set("color", "#2e7d32");

        panelResultados.add(
            tituloResultados,
            resultadoA,
            resultadoB,
            mejorResultado
        );

        // EVENTO CALCULAR
        calcular.addClickListener(event -> {

            try {

                tirA = calcularTIR(
                    inversionA.getValue(),
                    flujoA.getValue()
                );

                tirB = calcularTIR(
                    inversionB.getValue(),
                    flujoB.getValue()
                );

                if (tirA > tirB) {

                    mejorAlternativa =
                        "Alternativa A";

                } else if (tirB > tirA) {

                    mejorAlternativa =
                        "Alternativa B";

                } else {

                    mejorAlternativa =
                        "Ambas son iguales";
                }

                resultadoA.setText(
                    "TIR Alternativa A: "
                    + String.format(
                        "%.2f",
                        tirA
                    )
                    + "%"
                );

                resultadoB.setText(
                    "TIR Alternativa B: "
                    + String.format(
                        "%.2f",
                        tirB
                    )
                    + "%"
                );

                mejorResultado.setText(
                    "Mejor alternativa: "
                    + mejorAlternativa
                );

                panelResultados.setVisible(
                    true
                );

                Notification.show(
                    "Comparación realizada",
                    4000,
                    Position.MIDDLE
                );

            } catch (Exception ex) {

                Notification.show(
                    "Ingrese correctamente los datos"
                );
            }
        });

        // EXPORTAR PDF
        exportar.addClickListener(event -> {

            try {

                StreamResource resource =
                    generarPDF();

                getUI().ifPresent(ui -> {

                    String url =
                        resourceRegistry(
                            resource
                        );

                    ui.getPage().open(
                        url,
                        "_blank"
                    );
                });

            } catch (Exception ex) {

                Notification.show(
                    "Error al generar PDF"
                );
            }
        });

        mainContainer.add(
            titulo,
            subtitulo,
            alternativas,
            botones,
            panelResultados
        );

        add(mainContainer);
    }

    @Override
    protected void onAttach(
        AttachEvent attachEvent
    ) {

        Object user =
            VaadinSession.getCurrent()
                .getAttribute("user");

        if (user == null) {

            attachEvent.getUI()
                .navigate("login");
        }
    }

   // ALTERNATIVA A
private VerticalLayout crearAlternativaA() {

    VerticalLayout card =
        crearCardBase(
            "Alternativa A"
        );

    inversionA =
        new NumberField(
            "Inversión Inicial"
        );

    inversionA.setPlaceholder(
        "Ingrese la inversión inicial"
    );

    flujoA =
        new TextField(
            "Flujos de efectivo"
        );

    flujoA.setPlaceholder(
        "Separe los flujos por comas"
    );

    vidaA =
        new NumberField(
            "Vida útil (años)"
        );

    vidaA.setPlaceholder(
        "Ingrese la vida útil"
    );

    card.add(
        inversionA,
        flujoA,
        vidaA
    );

    return card;
}

// ALTERNATIVA B
private VerticalLayout crearAlternativaB() {

    VerticalLayout card =
        crearCardBase(
            "Alternativa B"
        );

    inversionB =
        new NumberField(
            "Inversión Inicial"
        );

    inversionB.setPlaceholder(
        "Ingrese la inversión inicial"
    );

    flujoB =
        new TextField(
            "Flujos de efectivo"
        );

    flujoB.setPlaceholder(
        "Separe los flujos por comas"
    );

    vidaB =
        new NumberField(
            "Vida útil (años)"
        );

    vidaB.setPlaceholder(
        "Ingrese la vida útil"
    );

    card.add(
        inversionB,
        flujoB,
        vidaB
    );

    return card;
}

    // CARD BASE
    private VerticalLayout crearCardBase(
        String tituloTexto
    ) {

        VerticalLayout card =
            new VerticalLayout();

        card.setWidth("100%");

        card.setMaxWidth("450px");

        card.setPadding(true);

        card.setSpacing(true);

        card.getStyle()
            .set(
                "background-color",
                "#f5f5f5"
            )
            .set(
                "border-radius",
                "18px"
            )
            .set(
                "padding",
                "25px"
            )
            .set(
                "box-shadow",
                "0 3px 10px rgba(0,0,0,0.08)"
            );

        H2 titulo =
            new H2(tituloTexto);

        titulo.getStyle()
            .set(
                "color",
                "#7b4bb7"
            )
            .set(
                "font-weight",
                "bold"
            )
            .set(
                "border-left",
                "5px solid #7b4bb7"
            )
            .set(
                "padding-left",
                "12px"
            )
            .set(
                "margin-bottom",
                "10px"
            );

        card.add(titulo);

        return card;
    }

    // ESTILO BOTON
    private void estiloBoton(
        Button boton
    ) {

        boton.getStyle()
            .set(
                "background",
                "#7b4bb7"
            )
            .set("color", "white")
            .set(
                "border-radius",
                "25px"
            )
            .set(
                "padding",
                "12px 28px"
            )
            .set(
                "font-weight",
                "bold"
            )
            .set("border", "none");
    }

    // CALCULAR TIR
private double calcularTIR(
    double inversionInicial,
    String flujosTexto
) {

    String[] valores =
        flujosTexto.split(",");

    double[] flujos =
        new double[valores.length];

    for (
        int i = 0;
        i < valores.length;
        i++
    ) {

        flujos[i] =
            Double.parseDouble(
                valores[i].trim()
            );
    }

    double low = -0.9999;

    double high = 1000.0;

    double tir = 0;

    for (
        int iteracion = 0;
        iteracion < 1000;
        iteracion++
    ) {

        tir =
            (low + high) / 2.0;

        double vpn =
            -inversionInicial;

        for (
            int t = 0;
            t < flujos.length;
            t++
        ) {

            vpn +=
                flujos[t] /
                Math.pow(
                    1 + tir,
                    t + 1
                );
        }

        if (
            Math.abs(vpn)
            < 0.0000001
        ) {

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
  // GENERAR PDF
private StreamResource generarPDF()
    throws Exception {

    ByteArrayOutputStream baos =
        new ByteArrayOutputStream();

    org.apache.pdfbox.pdmodel.PDDocument document =
        new org.apache.pdfbox.pdmodel.PDDocument();

    org.apache.pdfbox.pdmodel.PDPage page =
        new org.apache.pdfbox.pdmodel.PDPage(
            org.apache.pdfbox.pdmodel.common.PDRectangle.LETTER
        );

    document.addPage(page);

    org.apache.pdfbox.pdmodel.PDPageContentStream contentStream =
        new org.apache.pdfbox.pdmodel.PDPageContentStream(
            document,
            page
        );

    float pageWidth =
        page.getMediaBox().getWidth();

    float pageHeight =
        page.getMediaBox().getHeight();

    // FONDO
    contentStream.setNonStrokingColor(
        rgb(245,247,252)
    );

    contentStream.addRect(
        0,
        0,
        pageWidth,
        pageHeight
    );

    contentStream.fill();

    // ENCABEZADO
    contentStream.setNonStrokingColor(
        rgb(36,64,111)
    );

    contentStream.addRect(
        0,
        pageHeight - 115,
        pageWidth,
        115
    );

    contentStream.fill();

    escribirTexto(
        contentStream,
        50,
        pageHeight - 55,
        "SEAE - Reporte de Tasa de Rendimiento",
        org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA_BOLD,
        20,
        rgb(255,255,255)
    );

    escribirTexto(
        contentStream,
        50,
        pageHeight - 75,
        "Comparacion de alternativas con TIR.",
        org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA,
        10,
        rgb(255,255,255)
    );

    // TARJETAS
    dibujarTarjeta(
        contentStream,
        50,
        650,
        "Alternativa A",
        inversionA.getValue(),
        flujoA.getValue(),
        vidaA.getValue(),
        tirA,
        new int[]{54,93,173}
    );

    dibujarTarjeta(
        contentStream,
        308,
        650,
        "Alternativa B",
        inversionB.getValue(),
        flujoB.getValue(),
        vidaB.getValue(),
        tirB,
        new int[]{91,123,216}
    );

    // RESUMEN
    contentStream.setNonStrokingColor(
        rgb(35,64,111)
    );

    contentStream.addRect(
        50,
        285,
        500,
        110
    );

    contentStream.fill();

    contentStream.setNonStrokingColor(
        rgb(255,255,255)
    );

    contentStream.addRect(
        56,
        291,
        488,
        98
    );

    contentStream.fill();

    escribirTexto(
        contentStream,
        66,
        365,
        "Resumen y recomendacion",
        org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA_BOLD,
        13,
        rgb(35,64,111)
    );

    escribirTexto(
        contentStream,
        66,
        340,
        "TIR Alternativa A: "
        + String.format("%.2f", tirA)
        + "%   TIR Alternativa B: "
        + String.format("%.2f", tirB)
        + "%",
        org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA_BOLD,
        11,
        rgb(15,23,42)
    );

    // CAJA RECOMENDACION
    contentStream.setNonStrokingColor(
        rgb(236,241,252)
    );

    contentStream.addRect(
        66,
        305,
        460,
        30
    );

    contentStream.fill();

    escribirTexto(
        contentStream,
        74,
        315,
        "Recomendacion: "
        + mejorAlternativa
        + " es la opcion mas conveniente.",
        org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA_BOLD,
        10,
        rgb(35,64,111)
    );

    // PIE
    contentStream.setStrokingColor(
        rgb(214,222,235)
    );

    contentStream.moveTo(
        50,
        70
    );

    contentStream.lineTo(
        pageWidth - 50,
        70
    );

    contentStream.stroke();

    escribirTexto(
        contentStream,
        50,
        52,
        "Generado por SEAE - Sistema de Evaluacion de Alternativas Economicas",
        org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA,
        9,
        rgb(100,116,139)
    );

    contentStream.close();

    document.save(baos);

    document.close();

    return new StreamResource(
        "reporte_tir.pdf",
        () ->
            new ByteArrayInputStream(
                baos.toByteArray()
            )
    );
}

// DIBUJAR TARJETA
private void dibujarTarjeta(
    org.apache.pdfbox.pdmodel.PDPageContentStream contentStream,
    float x,
    float y,
    String titulo,
    Double inversion,
    String flujos,
    Double vida,
    double tir,
    int[] color
) throws Exception {

    float width = 240;
    float height = 200;

    // TARJETA
    contentStream.setNonStrokingColor(
        rgb(255,255,255)
    );

    contentStream.addRect(
        x,
        y - height,
        width,
        height
    );

    contentStream.fill();

    // BARRA SUPERIOR
    contentStream.setNonStrokingColor(
        rgb(color[0],color[1],color[2])
    );

    contentStream.addRect(
        x,
        y - 22,
        width,
        22
    );

    contentStream.fill();

    escribirTexto(
        contentStream,
        x + 12,
        y - 16,
        titulo,
        org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA_BOLD,
        12,
        rgb(255,255,255)
    );

    // TEXOS
    escribirTexto(
        contentStream,
        x + 14,
        y - 48,
        "Inversion inicial: $" + inversion,
        org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA_BOLD,
        10,
        rgb(71,85,105)
    );

    escribirTexto(
        contentStream,
        x + 14,
        y - 68,
        "Flujos: " + flujos,
        org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA,
        10,
        rgb(15,23,42)
    );

    escribirTexto(
        contentStream,
        x + 14,
        y - 88,
        "Vida util: " + vida + " años",
        org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA,
        10,
        rgb(15,23,42)
    );

    // LINEA
    contentStream.setStrokingColor(
        rgb(230,235,244)
    );

    contentStream.moveTo(
        x + 12,
        y - 110
    );

    contentStream.lineTo(
        x + width - 12,
        y - 110
    );

    contentStream.stroke();

    escribirTexto(
        contentStream,
        x + 14,
        y - 135,
        "TIR:",
        org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA_BOLD,
        11,
        rgb(71,85,105)
    );

    escribirTexto(
        contentStream,
        x + 45,
        y - 135,
        String.format("%.2f", tir) + "%",
        org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA_BOLD,
        11,
        rgb(color[0],color[1],color[2])
    );

    escribirTexto(
        contentStream,
        x + 14,
        y - 165,
        "Alternativa registrada para comparacion economica.",
        org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA_OBLIQUE,
        9,
        rgb(109,117,130)
    );
}

// ESCRIBIR TEXTO
private void escribirTexto(
    org.apache.pdfbox.pdmodel.PDPageContentStream contentStream,
    float x,
    float y,
    String texto,
    org.apache.pdfbox.pdmodel.font.PDType1Font font,
    int size,
    org.apache.pdfbox.pdmodel.graphics.color.PDColor color
) throws Exception {

    contentStream.beginText();

    contentStream.setNonStrokingColor(
        color
    );

    contentStream.setFont(
        font,
        size
    );

    contentStream.newLineAtOffset(
        x,
        y
    );

    contentStream.showText(texto);

    contentStream.endText();
}

// RGB
private org.apache.pdfbox.pdmodel.graphics.color.PDColor rgb(
    int r,
    int g,
    int b
) {

    return new org.apache.pdfbox.pdmodel.graphics.color.PDColor(
        new float[]{
            r / 255f,
            g / 255f,
            b / 255f
        },
        org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB.INSTANCE
    );
}

    // REGISTRO PDF
    private String resourceRegistry(
        StreamResource resource
    ) {

        return getUI().get()
            .getSession()
            .getResourceRegistry()
            .registerResource(resource)
            .getResourceUri()
            .toString();
    }
}