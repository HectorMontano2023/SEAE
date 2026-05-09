package ues.edu.ine.componentes.ui;

import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

@Route("login")
@PageTitle("Login | SEAE")
public class LoginView extends VerticalLayout {

    public LoginView() {

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        getStyle()
            .set("background", "linear-gradient(135deg, #5d7cf3, #7b4bb7)");

        LoginForm loginForm = new LoginForm();
        loginForm.setForgotPasswordButtonVisible(false);

        loginForm.addLoginListener(event -> {
            // Acepta cualquier correo y contraseña
            VaadinSession.getCurrent().setAttribute("user", event.getUsername());
            getUI().ifPresent(ui -> ui.navigate(""));
        });

        add(loginForm);
    }
}
