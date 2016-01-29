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

import org.imdea.rubis.benchmark.entity.CommentEntity;
import org.imdea.rubis.benchmark.entity.IndexEntity;
import org.imdea.rubis.benchmark.entity.UserEntity;

public class ViewUserInfoTransaction extends AbsRUBiSTransaction {
    private long mUserId;

    public ViewUserInfoTransaction(Jessy jessy, long userId) throws Exception {
        super(jessy);
        mUserId = userId;
    }

    @Override
    public ExecutionHistory execute() {
        try {
            UserEntity user = readEntityFrom(users).withKey(mUserId);

            if (user != null) {
                IndexEntity commentsIndex = readIndex(comments.to_user_id).find(mUserId);

                for (long key : commentsIndex.getPointers()) {
                    CommentEntity comment = readEntityFrom(comments).withKey(key);
                    UserEntity author = readEntityFrom(users).withKey(comment.getFromUserId());
                }
            }

            return commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
