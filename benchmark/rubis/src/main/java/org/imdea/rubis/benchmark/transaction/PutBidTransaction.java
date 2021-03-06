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

import java.util.Collection;
import org.imdea.rubis.benchmark.entity.BidEntity;
import org.imdea.rubis.benchmark.entity.ItemEntity;
import org.imdea.rubis.benchmark.entity.UserEntity;

public class PutBidTransaction extends AbsRUBiSTransaction {
    private long mItemId;
    private String mNickname;
    private String mPassword;

    public PutBidTransaction(Jessy jessy, long itemId, String nickname, String password) throws Exception {
        super(jessy);
        mItemId = itemId;
        mNickname = nickname;
        mPassword = password;
    }

    @Override
    public ExecutionHistory execute() {
        try {
            long userId = authenticate(mNickname, mPassword);

            if (userId != -1) {
                ItemEntity item = read(ItemEntity.class, ItemEntity.getKeyFromId(mItemId));
                UserEntity seller = read(UserEntity.class, UserEntity.getKeyFromId(item.getSeller()));
                Collection<BidEntity.ItemIdIndex> pointers = readIndex(BidEntity.ItemIdIndex.class, "mItemId", item
                        .getId());
                int count = 0;
                float max = 0.0f;

                for (BidEntity.ItemIdIndex pointer : pointers) {
                    BidEntity bid = read(BidEntity.class, pointer.getBidKey());
                    max = Math.max(max, bid.getBid());
                    count++;
                }
            }

            return commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
