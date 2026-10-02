package itec.emsa.emsa.config;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Expone el ApplicationContext de Spring como singleton estático.
 * Útil en callbacks JavaFX donde la inyección normal no está disponible.
 */
@Component
public class AppContextProvider {

    private static ApplicationContext context;

    public AppContextProvider(ApplicationContext context) {
        AppContextProvider.context = context;
    }

    public static ApplicationContext getContext() {
        return context;
    }

    public static <T> T getBean(Class<T> beanClass) {
        return context.getBean(beanClass);
    }
}
