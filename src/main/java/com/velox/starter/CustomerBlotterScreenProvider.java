package com.velox.starter;

import com.aralis.df.cache.CachePublisher;
import com.aralis.df.cache.CachePublisherTrackingFactory;
import com.aralis.df.cache.CacheTable;
import com.aralis.df.cache.filter.FilterExpression;
import com.aralis.df.cache.helpers.MutableFilterListener;
import com.aralis.df.cache.state.DataContextAccessor;
import com.aralis.vm.ClientNotifier;
import com.aralis.vm.ContextClientNotifier;
import com.aralis.vm.SessionState;
import com.caelo.vm.workspace.WorkspaceScreenProvider;
import com.velox.starter.api.*;
import com.velox.starter.api.trainingsm.client.TrainingSMClient;

public class CustomerBlotterScreenProvider extends WorkspaceScreenProvider<CustomerBlotterScreen> {
    private final TrainingSMClient m_smClient;
    public CustomerBlotterScreenProvider(TrainingSMClient client, String caption, String group, String icon) {
        super(CustomerBlotterScreen.class, caption, group, icon);
        this.m_smClient = client;
    }

    @Override
    public CustomerBlotterScreen createScreen(final SessionState state, final ClientNotifier notifier) {
        DataContextAccessor dc = state.getDataContextAccessor();
        CachePublisherTrackingFactory factory = state.getCachePublisherFactory();
        CachePublisher<CustomerJoin, String> customerPub =
                factory.create(CustomerJoinTableDescriptor.Descriptor, "Test");


        MutableFilterListener<CustomerJoin, String> filterListener =
                MutableFilterListener.createForPublisher(FilterExpression.alwaysTrue(), customerPub);

        CustomerBlotterScreen screen = new CustomerBlotterScreen(state, dc.getTable(CustomerJoin.class));

        dc.getTable(CustomerJoin.class).subscribe(filterListener, screen.cancellationManager());

        screen.m_delete.setListener(action -> {

        });
        screen.m_customerJoin.associateActions(screen.m_addCustomer);
        screen.m_search.setListener((field, oldValue) -> {
            String name = field.getValue();

            System.out.println(name);
            filterListener.changeFilter(customer -> {
                String lastName = customer.customer().lastName();
                if(lastName!=null && lastName.contains(name)) return true;
                return false;
            });
        });
        screen.m_addCustomer.setListener(action -> {
            CustomerEditorScreenProvider.create(m_smClient, state, ContextClientNotifier.modal(notifier, screen));
        });
        return screen;
    }
}
