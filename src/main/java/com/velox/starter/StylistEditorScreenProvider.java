package com.velox.starter;

import com.aralis.vm.ClientNotifier;
import com.aralis.vm.SessionState;
import com.velox.starter.api.StylistBuilder;
import com.velox.starter.api.StylistEditorScreen;
import com.velox.starter.api.trainingsm.client.TrainingSMClient;

import java.util.List;

public class StylistEditorScreenProvider {
    public static void create(final TrainingSMClient client, final SessionState sessionState, ClientNotifier notifier) {
        StylistEditorScreen screen = new StylistEditorScreen(sessionState);
        screen.m_experienceLevel.setOptions(List.of("Beginner","Medium", "Expert"));
        screen.m_ok.setListener(t-> {
            String firstName = screen.m_firstName.getValue();
            String lastName = screen.m_lastName.getValue();
            client.sendCommand(StylistBuilder.newBuilder()
                            .stylistId(firstName+lastName)
                            .firstName(firstName)
                            .lastName(lastName)
                            .phoneNo(screen.m_phoneNo.getValue())
                            .address(screen.m_address.getValue())
                            .commissionRate(screen.m_commissionRate.getValue())
                    .get());
            notifier.destroyed(screen);
        });
        screen.m_cancel.setListener(t->{
            notifier.destroyed(screen);
        });
        notifier.created(screen);
    }
}
