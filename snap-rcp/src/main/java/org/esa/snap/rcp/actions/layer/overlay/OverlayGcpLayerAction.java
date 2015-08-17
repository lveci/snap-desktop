/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.esa.snap.rcp.actions.layer.overlay;

import com.bc.ceres.glayer.Layer;
import com.bc.ceres.glayer.LayerListener;
import com.bc.ceres.glayer.support.AbstractLayerListener;
import org.esa.snap.framework.ui.product.ProductSceneView;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

import javax.swing.Action;
import java.lang.ref.WeakReference;

/**
 * @author Marco Peters
 */
@ActionID(category = "View", id = "OverlayGcpLayerAction")
@ActionRegistration(displayName = "#CTL_OverlayGcpLayerActionName", lazy = false)
@ActionReferences({
        @ActionReference(path = "Menu/Layer", position = 40),
        @ActionReference(path = "Toolbars/Overlay", position = 40)
})
@NbBundle.Messages({
        "CTL_OverlayGcpLayerActionName=GCP Overlay",
        "CTL_OverlayGcpLayerActionToolTip=Show/hide GCP overlay for the selected image"
})
public final class OverlayGcpLayerAction extends AbstractOverlayAction {

    private final LayerListener layerListener;
    private WeakReference<ProductSceneView> lastView;

    public OverlayGcpLayerAction() {
        this(Utilities.actionsGlobalContext());
    }

    public OverlayGcpLayerAction(Lookup lkp) {
        super(lkp);
        layerListener = new GcpLayerListener();
    }

    @Override
    public Action createContextAwareInstance(Lookup lkp) {
        return new OverlayGcpLayerAction(lkp);
    }

    @Override
    protected void initActionProperties() {
        putValue(NAME, Bundle.CTL_OverlayGcpLayerActionName());
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon("org/esa/snap/rcp/icons/GcpOverlay.gif", false));
        putValue(LARGE_ICON_KEY, ImageUtilities.loadImageIcon("org/esa/snap/rcp/icons/GcpOverlay24.gif", false));
        putValue(SHORT_DESCRIPTION, Bundle.CTL_OverlayGcpLayerActionToolTip());
    }

    @Override
    protected boolean getActionSelectionState(ProductSceneView view) {
        return view.isGcpOverlayEnabled();
    }

    @Override
    protected boolean getActionEnableState(ProductSceneView view) {
        return view.getProduct().getGcpGroup().getNodeCount() > 0;
    }

    @Override
    protected void setOverlayEnableState(ProductSceneView view) {
        view.setGcpOverlayEnabled(!getActionSelectionState(view));
    }

    @Override
    protected void selectedProductSceneViewChanged(ProductSceneView newView) {
        ProductSceneView oldView = lastView != null ? lastView.get() : null;
        if (oldView != null) {
            oldView.getRootLayer().removeListener(layerListener);
        }
        if (newView != null) {
            newView.getRootLayer().addListener(layerListener);
        }

        if (newView != null) {
            lastView = new WeakReference<>(newView);
        } else {
            if (lastView != null) {
                lastView.clear();
                lastView = null;
            }
        }
    }
    private class GcpLayerListener extends AbstractLayerListener {
        @Override
        public void handleLayersAdded(Layer parentLayer, Layer[] childLayers) {
            updateActionState();
        }

        @Override
        public void handleLayersRemoved(Layer parentLayer, Layer[] childLayers) {
            updateActionState();
        }
    }

}
