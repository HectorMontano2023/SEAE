package ues.edu.ine.componentes.ui;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.theme.lumo.LumoUtility;

@Route("login")
@PageTitle("Login | SEAE")
public class LoginView extends VerticalLayout {

    public LoginView() {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        
        getStyle().set("background", "linear-gradient(135deg, #5d7cf3, #7b4bb7)");
        setPadding(false);
        setMargin(false);

        // --- Configuración de la Tarjeta ---
        VerticalLayout card = new VerticalLayout();
        card.setWidth("auto");
        card.setAlignItems(Alignment.CENTER);
        
        // 1. Cambio de color: Efecto "Cristal" (Blanco semitransparente con desenfoque)
        card.getStyle().set("background", "rgba(255, 255, 255, 0.85)"); 
        card.getStyle().set("backdrop-filter", "blur(12px)"); // Crea el efecto de cristal esmerilado
        card.getStyle().set("border-radius", "var(--lumo-border-radius-l)");
        card.getStyle().set("box-shadow", "0 25px 50px rgba(0, 0, 0, 0.5)"); 
        
        // Borde superior con un nuevo color vibrante para contrastar
        card.getStyle().set("border-top", "6px solid #091413"); 
        
        card.setPadding(true);
        card.getStyle().set("padding", "var(--lumo-space-xl)"); 

        // --- Elementos de Cabecera ---
        // 2. Ícono circular de usuario MUCHO más grande
        Icon userIcon = VaadinIcon.USER.create();
        userIcon.setSize("40px"); // Tamaño aumentado significativamente
        userIcon.getStyle()
            .set("background", "rgba(255, 64, 129, 0.1)") // Fondo rosa muy sutil
            .set("padding", "20px") // Más espacio interno para que el círculo se vea proporcionado
            .set("border-radius", "50%")
            .set("color", "#ff4081"); // Color del ícono a juego con el borde superior

        H1 title = new H1("SEAE");
        title.addClassNames(LumoUtility.Margin.Top.MEDIUM, LumoUtility.Margin.Bottom.NONE);
        
        Paragraph subtitle = new Paragraph("Acceso al sistema");
        subtitle.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.Margin.Top.XSMALL);

        // --- Formulario ---
        LoginForm loginForm = new LoginForm();
        loginForm.setForgotPasswordButtonVisible(false);
        loginForm.setI18n(crearTraduccionEspanol());

        loginForm.addLoginListener(event -> {
            VaadinSession.getCurrent().setAttribute("user", event.getUsername());
            getUI().ifPresent(ui -> ui.navigate(""));
        });

        // Ensamblaje
        card.add(userIcon, title, subtitle, loginForm);
        add(card);
    }

    private LoginI18n crearTraduccionEspanol() {
        LoginI18n i18n = LoginI18n.createDefault();

        LoginI18n.Form i18nForm = i18n.getForm();
        i18nForm.setTitle("Iniciar Sesión");
        i18nForm.setUsername("Usuario");
        i18nForm.setPassword("Contraseña");
        i18nForm.setSubmit("Ingresar");
        i18n.setForm(i18nForm);

        LoginI18n.ErrorMessage i18nErrorMessage = i18n.getErrorMessage();
        i18nErrorMessage.setTitle("Credenciales incorrectas");
        i18nErrorMessage.setMessage("Comprueba que tu usuario y contraseña sean correctos e inténtalo de nuevo.");
        i18n.setErrorMessage(i18nErrorMessage);

        return i18n;
    }
}