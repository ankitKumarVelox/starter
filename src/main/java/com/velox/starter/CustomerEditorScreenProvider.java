package com.velox.starter;

import com.aralis.df.cache.state.DataContextAccessor;
import com.aralis.vm.ClientNotifier;
import com.aralis.vm.SessionState;
import com.velox.boule.S;
import com.velox.starter.api.*;
import com.velox.starter.api.trainingsm.client.TrainingSMClient;

import java.util.List;
import java.util.stream.Collectors;

public class CustomerEditorScreenProvider {
    public static void create(final TrainingSMClient client, final SessionState sessionState, ClientNotifier notifier) {
        DataContextAccessor dc = sessionState.getDataContextAccessor();
        CustomerEditorScreen screen = new CustomerEditorScreen(sessionState);

        List<String> stylistList = dc.getTable(Stylist.class).values().stream()
                .map(s -> s.stylistId()).sorted().collect(Collectors.toList());
        screen.m_stylistId.setOptions(stylistList);
        screen.m_ok.setListener(action -> {
            String firstName = screen.m_firstName.getValue();
            String lastName = screen.m_lastName.getValue();
            client.sendCommand(CustomerBuilder.newBuilder()
                            .customerId(firstName+lastName)
                            .firstName(firstName)
                            .lastName(lastName)
                            .address(screen.m_address.getValue())
                            .phoneNo(screen.m_phoneNo.getValue())
                            .stylistId(screen.m_stylistId.getValue())
                    .get());
            notifier.destroyed(screen);
        });
        screen.m_cancel.setListener(t->{
            notifier.destroyed(screen);
        });
        notifier.created(screen);
    }
}
