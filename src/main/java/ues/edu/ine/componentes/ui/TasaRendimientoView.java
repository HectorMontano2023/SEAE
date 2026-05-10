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

        double tir = 0.1;

        for (
            int iteracion = 0;
            iteracion < 1000;
            iteracion++
        ) {

            double vpn =
                -inversionInicial;

            double derivada = 0;

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

                derivada -=
                    (t + 1)
                    * flujos[t] /
                    Math.pow(
                        1 + tir,
                        t + 2
                    );
            }

            double nuevaTir =
                tir -
                (vpn / derivada);

            if (
                Math.abs(
                    nuevaTir - tir
                ) < 0.00001
            ) {

                tir = nuevaTir;

                break;
            }

            tir = nuevaTir;
        }

        return tir * 100;
    }

   // GENERAR PDF
private StreamResource generarPDF()
    throws Exception {

    ByteArrayOutputStream baos =
        new ByteArrayOutputStream();

    Document documento =
        new Document();

    PdfWriter.getInstance(
        documento,
        baos
    );

    documento.open();

    // TITULO
    com.lowagie.text.Font tituloFont =
        new com.lowagie.text.Font(
            com.lowagie.text.Font.HELVETICA,
            20,
            com.lowagie.text.Font.BOLD
        );

    com.lowagie.text.Font subtituloFont =
        new com.lowagie.text.Font(
            com.lowagie.text.Font.HELVETICA,
            15,
            com.lowagie.text.Font.BOLD
        );

    com.lowagie.text.Font textoFont =
        new com.lowagie.text.Font(
            com.lowagie.text.Font.HELVETICA,
            12,
            com.lowagie.text.Font.NORMAL
        );

    com.lowagie.text.Font resultadoFont =
        new com.lowagie.text.Font(
            com.lowagie.text.Font.HELVETICA,
            13,
            com.lowagie.text.Font.BOLD
        );

    com.lowagie.text.Paragraph titulo =
        new com.lowagie.text.Paragraph(
            "REPORTE DE TASA DE RENDIMIENTO",
            tituloFont
        );

    titulo.setAlignment(
        com.lowagie.text.Element.ALIGN_CENTER
    );

    documento.add(titulo);

    documento.add(
        new com.lowagie.text.Paragraph(
            " "
        )
    );

    // ALTERNATIVA A
    documento.add(
        new com.lowagie.text.Paragraph(
            "ALTERNATIVA A",
            subtituloFont
        )
    );

    documento.add(
        new com.lowagie.text.Paragraph(
            "Inversión Inicial: $"
            + inversionA.getValue(),
            textoFont
        )
    );

    documento.add(
        new com.lowagie.text.Paragraph(
            "Flujos de efectivo: "
            + flujoA.getValue(),
            textoFont
        )
    );

    documento.add(
        new com.lowagie.text.Paragraph(
            "Vida útil: "
            + vidaA.getValue()
            + " años",
            textoFont
        )
    );

    documento.add(
        new com.lowagie.text.Paragraph(
            "TIR Alternativa A: "
            + String.format(
                "%.2f",
                tirA
            )
            + "%",
            resultadoFont
        )
    );

    documento.add(
        new com.lowagie.text.Paragraph(
            " "
        )
    );

    // ALTERNATIVA B
    documento.add(
        new com.lowagie.text.Paragraph(
            "ALTERNATIVA B",
            subtituloFont
        )
    );

    documento.add(
        new com.lowagie.text.Paragraph(
            "Inversión Inicial: $"
            + inversionB.getValue(),
            textoFont
        )
    );

    documento.add(
        new com.lowagie.text.Paragraph(
            "Flujos de efectivo: "
            + flujoB.getValue(),
            textoFont
        )
    );

    documento.add(
        new com.lowagie.text.Paragraph(
            "Vida útil: "
            + vidaB.getValue()
            + " años",
            textoFont
        )
    );

    documento.add(
        new com.lowagie.text.Paragraph(
            "TIR Alternativa B: "
            + String.format(
                "%.2f",
                tirB
            )
            + "%",
            resultadoFont
        )
    );

    documento.add(
        new com.lowagie.text.Paragraph(
            " "
        )
    );

    // MEJOR OPCION
    com.lowagie.text.Paragraph mejor =
        new com.lowagie.text.Paragraph(
            "MEJOR OPCIÓN: "
            + mejorAlternativa,
            resultadoFont
        );

    mejor.setAlignment(
        com.lowagie.text.Element.ALIGN_CENTER
    );

    documento.add(mejor);

    documento.close();

    return new StreamResource(
        "reporte_tir.pdf",
        () ->
            new ByteArrayInputStream(
                baos.toByteArray()
            )
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