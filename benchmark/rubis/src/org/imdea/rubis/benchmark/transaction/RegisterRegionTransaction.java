/*
 * RUBiS Benchmark
 * Copyright (C) 2016 IMDEA Software Institute
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or any later
 * version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */

package org.imdea.rubis.benchmark.transaction;

import static org.imdea.rubis.benchmark.table.Tables.*;

import fr.inria.jessy.Jessy;
import fr.inria.jessy.transaction.ExecutionHistory;

import org.imdea.rubis.benchmark.entity.RegionEntity;

public class RegisterRegionTransaction extends AbsRUBiSTransaction {
    private RegionEntity mRegion;

    public RegisterRegionTransaction(Jessy jessy, long id, String name) throws Exception {
        super(jessy);
        mRegion = new RegionEntity(id, name);
    }

    private void createNeededIndexEntities() {
        createIndex(users.region).justEmpty().forKey(mRegion.getId());
    }

    @Override
    public ExecutionHistory execute() {
        try {
            create(mRegion);
            createNeededIndexEntities();
            updateScanner();
            return commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private void updateScanner() {
        readScannerOf(regions).edit().addPointer(mRegion.getId()).write(this);
    }
}