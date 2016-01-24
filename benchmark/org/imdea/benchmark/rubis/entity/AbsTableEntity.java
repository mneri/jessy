package org.imdea.benchmark.rubis.entity;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import org.imdea.benchmark.rubis.table.Entities;
import org.imdea.benchmark.rubis.table.AbsTable;

public abstract class AbsTableEntity extends AbsRUBiSEntity {
    public AbsTableEntity() {
        super("");
    }

    public AbsTableEntity(AbsTable<? extends AbsRUBiSEntity> table, long id) {
        super(Entities.of(table).withKey(id).getDatastoreUniqueIdentifier());
    }

    @Override
    public Object clone() {
        return super.clone();
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
    }
}
