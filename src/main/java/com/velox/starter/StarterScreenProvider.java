package com.velox.starter;

import com.aralis.df.cache.CachePublisher;
import com.aralis.vm.ClientNotifier;
import com.aralis.vm.SessionState;
import com.caelo.vm.workspace.WorkspaceScreenProvider;
import com.velox.starter.api.StarterScreen;
import com.velox.starter.api.User;
import com.velox.starter.api.UserBuilder;
import java.util.Arrays;

public class StarterScreenProvider extends WorkspaceScreenProvider<StarterScreen> {
    private final CachePublisher<User, Object> m_userPublisher;

    public StarterScreenProvider(
      final String caption, final String group, final String icon, CachePublisher<User, Object> userPublisher) {
        super(StarterScreen.class, caption, group, icon);
        m_userPublisher = userPublisher;
    }

    @Override
    public StarterScreen createScreen(SessionState state, ClientNotifier notifier) {
        StarterScreen screen = new StarterScreen(state, state.getDataContextAccessor().getTable(User.class));
        screen.title("Starter");

        screen.m_region.setOptions(Arrays.asList("AMRS", "APAC", "EMEA"));

        screen.m_addUser.setListener(action -> {
            String firstName = screen.m_firstName.getValue();
            String lastName = screen.m_lastName.getValue();
            int age = screen.m_age.getValue() == null ? 0 : screen.m_age.getValue();
            String region = screen.m_region.getValue();
            String email = screen.m_email.getValue();
            User user = UserBuilder.newBuilder()
              .firstName(firstName)
              .lastName(lastName)
              .age(age)
              .region(region)
              .email(email)
              .get();
            m_userPublisher.publish(user);
        });

        return screen;
    }
}
