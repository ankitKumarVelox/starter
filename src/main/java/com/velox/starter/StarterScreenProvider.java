package com.velox.starter;

import com.aralis.vm.BaseScreenProvider;
import com.aralis.vm.ClientNotifier;
import com.aralis.vm.SessionState;
import com.caelo.vm.workspace.WorkspaceScreenProvider;
import com.velox.starter.api.StarterScreen;
import com.velox.starter.api.User;
import com.velox.starter.api.UserBuilder;
import java.util.Arrays;

public class StarterScreenProvider extends WorkspaceScreenProvider<StarterScreen> {
    public StarterScreenProvider(final String caption, final String group, final String icon) {
        super(StarterScreen.class, caption, group, icon);
    }

    @Override
    public StarterScreen createScreen(final SessionState state, final ClientNotifier notifier) {
        final StarterScreen screen = new StarterScreen(state, state.getDataContextAccessor().getTable(User.class));
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
            state.getDataContextAccessor().getPublisher(User.class).publish(user);
        });

        return screen;
    }
}
