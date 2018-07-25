/*
 * Copyright (C) 2014 by Array Systems Computing Inc. http://www.array.ca
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */
package org.esa.snap.graphbuilder.rcp.wizards;

import org.esa.snap.core.datamodel.GeoPos;
import org.esa.snap.productlibrary.db.ProductEntry;
import org.esa.snap.graphbuilder.gpf.ui.worldmap.WorldMapUI;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * Map Panel
 */
public abstract class AbstractMapPanel extends WizardPanel {

    protected final File[] productFileList;
    protected final File targetFolder;

    public AbstractMapPanel(final File[] productFileList, final File targetFolder) {
        super("Viewing the footprint");
        this.productFileList = productFileList;
        this.targetFolder = targetFolder;

        createPanel(productFileList);
    }

    public void returnFromLaterStep() {
    }

    public boolean canRedisplayNextPanel() {
        return false;
    }

    public boolean hasNextPanel() {
        return true;
    }

    public boolean canFinish() {
        return false;
    }

    public abstract WizardPanel getNextPanel();

    public boolean validateInput() {
        return true;
    }

    protected String getInstructions() {
        return "View the footprint of the input products on the world map\n" +
                "Use the mouse wheel to zoom in and out. Hold and drag the right mouse button to pan\n";
    }

    private void createPanel(final File[] productFileList) {

        final JPanel textPanel = createTextPanel("Instructions", getInstructions());
        this.add(textPanel, BorderLayout.NORTH);

        final WorldMapUI worldMapUI = new WorldMapUI();
        this.add(worldMapUI.getWorlMapPane(), BorderLayout.CENTER);

        final ProductEntry[] productEntryList =ProductEntry.createProductEntryList(productFileList);
        final GeoPos[][] geoBoundaries = new GeoPos[productEntryList.length][4];
        int i = 0;
        for (ProductEntry entry : productEntryList) {
            geoBoundaries[i++] = entry.getGeoBoundary();
        }

        worldMapUI.setAdditionalGeoBoundaries(geoBoundaries);
    }
}
