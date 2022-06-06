package com.velox.starter;

import com.aralis.gooey.api.GridHeaderBuilder;
import com.aralis.gooey.api.GridSummaryBuilder;
import com.aralis.gooey.api.ViewConfigBuilder;
import com.aralis.vm.*;
import com.velox.starter.api.QuickGridSettingsScreen;

public class QuickGridSettingsScreenProvider extends BaseScreenProvider<QuickGridSettingsScreen> {
    private final DataGrid<?, ?> m_grid;
    private final DomainControlActionHandlerRegistry m_registry;

    public QuickGridSettingsScreenProvider(final DataGrid<?, ?> grid, DomainControlActionHandlerRegistry registry) {
        super(QuickGridSettingsScreen.class, "", "", "");
        m_grid = grid;
        m_registry = registry;
    }

    @Override
    public void create(final SessionState state, final ClientNotifier notifier) {
        var screen = new QuickGridSettingsScreen(state);
        var realNotifier = ContextClientNotifier.contained(notifier, m_grid.parent());

        screen.m_gridId.setValue(m_grid.controlId().screenId() + "_" + m_grid.controlId().controlName());

        screen.m_showFilter.setListener(() -> {
            var show = screen.m_showFilter.getValue();
            toggleFilter(show);
        });

        screen.m_showTotalSummary.setListener(() -> {
            var show = screen.m_showTotalSummary.getValue();
            toggleTotalSummary(show);
        });

        screen.m_showSelectionSummary.setListener(() -> {
            var show = screen.m_showSelectionSummary.getValue();
            toggleSelectionSummary(show);
        });

        screen.m_configureGrid.setListener(() -> {
            notifier.destroyed(screen);
            m_registry.handler(m_grid, "ConfigureGrid").handle(notifier, state, m_grid, "", () -> {});
        });

        screen.m_exportGrid.setListener(() -> {
            notifier.destroyed(screen);
            m_registry.handler(m_grid, "ExportGrid").handle(notifier, state, m_grid, "", () -> {});
        });

        screen.m_saveGrid.setListener(() -> {
            notifier.destroyed(screen);
            m_registry.handler(m_grid, "SaveGridConfiguration").handle(notifier, state, m_grid, "", () -> {});

        });

        screen.m_close.setListener(() -> realNotifier.destroyed(screen));
        realNotifier.created(screen);
    }

    private void toggleFilter(boolean show) {
        var vc = m_grid.viewConfig();
        m_grid.viewConfigDelta(ViewConfigBuilder.newBuilder()
          .headerSettings(GridHeaderBuilder.newBuilder(vc.headerSettings()).showFilter(show).get())
          .get());
    }

    private void toggleTotalSummary(boolean show) {
        var vc = m_grid.viewConfig();
        m_grid.viewConfigDelta(ViewConfigBuilder.newBuilder()
          .summarySettings(GridSummaryBuilder.newBuilder(vc.summarySettings()).summary(show).get())
          .get());
    }

    private void toggleSelectionSummary(boolean show) {
        var vc = m_grid.viewConfig();
        m_grid.viewConfigDelta(ViewConfigBuilder.newBuilder()
          .summarySettings(GridSummaryBuilder.newBuilder(vc.summarySettings()).selectionSummary(show).get())
          .get());
    }
}
