package com.velox.starter;

import com.aralis.df.cache.state.DataContextAccessor;
import com.aralis.vm.ClientNotifier;
import com.aralis.vm.ContextClientNotifier;
import com.aralis.vm.SessionState;
import com.caelo.vm.workspace.WorkspaceScreenProvider;
import com.velox.starter.api.Stylist;
import com.velox.starter.api.StylistBlotterScreen;
import com.velox.starter.api.trainingsm.client.TrainingSMClient;

public class StylistBlotterScreenProvider extends WorkspaceScreenProvider<StylistBlotterScreen> {

    private final TrainingSMClient m_smClient;
    public StylistBlotterScreenProvider(TrainingSMClient client, String caption, String group, String icon) {
        super(StylistBlotterScreen.class, caption, group, icon);
        m_smClient = client;
    }

    @Override
    public StylistBlotterScreen createScreen(final SessionState state, final ClientNotifier notifier) {
        DataContextAccessor dc = state.getDataContextAccessor();
        StylistBlotterScreen screen = new StylistBlotterScreen(state, dc.getTable(Stylist.class));
        screen.m_stylists.associateActions(screen.m_addStylist);
        screen.m_addStylist.setListener(action -> {
            StylistEditorScreenProvider.create(m_smClient, state, ContextClientNotifier.modal(notifier, screen));
        });
        return screen;
    }
}
