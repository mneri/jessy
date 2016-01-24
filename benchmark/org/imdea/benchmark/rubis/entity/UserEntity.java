package org.imdea.benchmark.rubis.entity;

import static fr.inria.jessy.ConstantPool.JESSY_MID;

import static org.imdea.benchmark.rubis.table.Tables.*;

import fr.inria.jessy.transaction.Transaction;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Date;

public class UserEntity extends AbsTableEntity {
    private static final long serialVersionUID = JESSY_MID;

    public static class Editor implements AbsRUBiSEntity.Editor {
        private float mBalance;
        private Date mCreationDate;
        private String mEmail;
        private String mFirstname;
        private long mId;
        private String mLastname;
        private String mNickname;
        private String mPassword;
        private int mRating;
        private long mRegion;

        Editor(UserEntity source) {
            mBalance = source.getBalance();
            mCreationDate = source.getCreationDate();
            mEmail = source.getEmail();
            mFirstname = source.getFirstname();
            mId = source.getId();
            mLastname = source.getLastname();
            mNickname = source.getNickname();
            mPassword = source.getPassword();
            mRating = source.getRating();
            mRegion = source.getRegion();
        }

        private UserEntity done() {
            return new UserEntity(mId, mFirstname, mLastname, mNickname, mPassword, mEmail, mRating, mBalance,
                    mCreationDate, mRegion);
        }

        public Editor setBalance(float balance) {
            mBalance = balance;
            return this;
        }

        public Editor setCreationDate(Date creationDate) {
            mCreationDate = creationDate;
            return this;
        }

        public Editor setEmail(String email) {
            mEmail = email;
            return this;
        }

        public Editor setFirstname(String firstname) {
            mFirstname = firstname;
            return this;
        }

        public Editor setId(long id) {
            mId = id;
            return this;
        }

        public Editor setLastname(String lastname) {
            mLastname = lastname;
            return this;
        }

        public Editor setNickname(String nickname) {
            mNickname = nickname;
            return this;
        }

        public Editor setPassword(String password) {
            mPassword = password;
            return this;
        }

        public Editor setRating(int rating) {
            mRating = rating;
            return this;
        }

        public Editor setRegion(long region) {
            mRegion = region;
            return this;
        }

        public void write(Transaction trans) {
            trans.write(done());
        }
    }

    private float mBalance;
    private Date mCreationDate;
    private String mEmail;
    private String mFirstname;
    private long mId;
    private String mLastname;
    private String mNickname;
    private String mPassword;
    private int mRating;
    private long mRegion;
    
    public UserEntity() {
    }

    public UserEntity(long id, String firstname, String lastname, String nickname, String password, String email, int
            rating, float balance, Date creationDate, long region) {
        super(users, id);

        mId = id;
        mFirstname = firstname;
        mLastname = lastname;
        mNickname = nickname;
        mPassword = password;
        mEmail = email;
        mRating = rating;
        mBalance = balance;
        mCreationDate = creationDate;
        mRegion = region;
    }
    
    @Override
    public Object clone() {
    	UserEntity entity = (UserEntity) super.clone();
    	entity.mId = mId;
    	entity.mFirstname = mFirstname;
    	entity.mLastname = mLastname;
    	entity.mNickname = mNickname;
    	entity.mPassword = mPassword;
    	entity.mEmail = mEmail;
    	entity.mRating = mRating;
    	entity.mBalance = mBalance;
    	entity.mCreationDate = (Date) mCreationDate.clone();
    	entity.mRegion = mRegion;
    	return entity;
    }

    public Editor edit() {
        return new Editor(this);
    }

    public Date getCreationDate() {
        return mCreationDate;
    }

    public String getEmail() {
        return mEmail;
    }

    public String getFirstname() {
        return mFirstname;
    }

    public long getId() {
        return mId;
    }

    public String getLastname() {
        return mLastname;
    }

    public String getNickname() {
        return mNickname;
    }

    public String getPassword() {
        return mPassword;
    }

    public int getRating() {
        return mRating;
    }

    public long getRegion() {
        return mRegion;
    }

    public float getBalance() {
        return mBalance;
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);

        mId = in.readLong();
        mFirstname = (String) in.readObject();
        mLastname = (String) in.readObject();
        mNickname = (String) in.readObject();
        mPassword = (String) in.readObject();
        mEmail = (String) in.readObject();
        mRating = in.readInt();
        mBalance = in.readFloat();
        mCreationDate = (Date) in.readObject();
        mRegion = in.readLong();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);

        out.writeLong(mId);
        out.writeObject(mFirstname);
        out.writeObject(mLastname);
        out.writeObject(mNickname);
        out.writeObject(mPassword);
        out.writeObject(mEmail);
        out.writeInt(mRating);
        out.writeFloat(mBalance);
        out.writeObject(mCreationDate);
        out.writeLong(mRegion);
    }
}