package com.velox.starter;

import com.aralis.df.cache.CachePublisherFactory;
import com.aralis.df.cache.filter.FilterExpression;
import com.aralis.df.cache.helpers.MutableFilterListener;
import com.aralis.vm.ClientNotifier;
import com.aralis.vm.SessionState;
import com.caelo.vm.workspace.WorkspaceScreenProvider;
import com.velox.starter.api.StarterScreen;
import com.velox.starter.api.User;
import com.velox.starter.api.UserBuilder;
import org.apache.commons.lang3.StringUtils;
import java.util.Arrays;

public class StarterScreenProvider extends WorkspaceScreenProvider<StarterScreen> {
    public StarterScreenProvider(final String caption, final String group, final String icon) {
        super(StarterScreen.class, caption, group, icon);
    }

    @Override
    public StarterScreen createScreen(final SessionState state, final ClientNotifier notifier) {
        final var users = state.getDataContextAccessor().getTable(User.class);
        final var filtered = CachePublisherFactory.createBaseOn(users);
        final var filterer = MutableFilterListener.createForPublisher(FilterExpression.alwaysTrue(), filtered);
        final StarterScreen screen = new StarterScreen(state, filtered.getTable());
        screen.title("Starter");
        users.subscribe(filterer, screen.cancellationManager());

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

        screen.m_search.setListener(() -> {
            String search = screen.m_search.getValue();
            if (StringUtils.isBlank(search)) {
                filterer.changeFilter(FilterExpression.alwaysTrue());
            } else {
                filterer.changeFilter(t -> {
                    var fields = new String[] {t.firstName(), t.lastName(), t.email()};
                    return Arrays.stream(fields).anyMatch(it -> it != null && it.contains(search));
                });
            }
        });

        return screen;
    }
}
