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

import fr.inria.jessy.Jessy;
import fr.inria.jessy.transaction.ExecutionHistory;

import java.util.Date;

import org.imdea.rubis.benchmark.entity.ItemEntity;

public class RegisterItemTransaction extends AbsRUBiSTransaction {
    private ItemEntity mItem;

    public RegisterItemTransaction(Jessy jessy, long id, String name, String description, float initialPrice, int
                quantity, float reservePrice, float buyNow, int nbOfBids, float maxBid, Date startDate, Date endDate,
                long seller, long categoryId) throws Exception {
        super(jessy);
        mItem = new ItemEntity(id, name, description, initialPrice, quantity, reservePrice, buyNow, nbOfBids, maxBid,
                startDate, endDate, seller, categoryId);
    }

    @Override
    public ExecutionHistory execute() {
        try {
            create(mItem);
            updateIndexes();
            return commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private void updateIndexes() {
        create(new ItemEntity.CategoryIdIndex(mItem.getCategoryId(), mItem.getId()));
        create(new ItemEntity.SellerIndex(mItem.getSeller(), mItem.getId()));
    }
}
