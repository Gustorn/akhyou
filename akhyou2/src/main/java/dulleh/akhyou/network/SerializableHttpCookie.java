package dulleh.akhyou.network;

/*
 * Copyright (c) 2011 James Smith <james@loopj.com>
 * Copyright (c) 2015 Fran Montiel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Based on the code from this StackOverflow answer http://stackoverflow.com/a/25462286/980387 by janoliver
 * Modifications in the structure of the class and addition of serialization of httpOnly attribute
 */

import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.net.HttpCookie;

import lombok.Getter;

public class SerializableHttpCookie implements Serializable {
    private static final String TAG = SerializableHttpCookie.class.getSimpleName();

    private static final long serialVersionUID = 6374381323722046732L;

    @Getter
    private transient HttpCookie cookie;

    // Workaround httpOnly: The httpOnly attribute is not accessible so when we
    // serialize and deserialize the cookie it not preserve the same value. We
    // need to access it using reflection
    private Field fieldHttpOnly;

    public SerializableHttpCookie() {
    }

    public SerializableHttpCookie(HttpCookie cookie) {
        this.cookie = cookie;
    }

    // Workaround httpOnly (getter)
    private boolean getHttpOnly() {
        try {
            initFieldHttpOnly();
            return (boolean) fieldHttpOnly.get(cookie);
        } catch (Exception e) {
            Log.w(TAG, e);
        }
        return false;
    }

    // Workaround httpOnly (setter)
    private void setHttpOnly(boolean httpOnly) {
        try {
            initFieldHttpOnly();
            fieldHttpOnly.set(cookie, httpOnly);
        } catch (Exception e) {
            Log.w(TAG, e);
        }
    }

    private void initFieldHttpOnly() throws NoSuchFieldException {
        fieldHttpOnly = cookie.getClass().getDeclaredField("httpOnly");
        fieldHttpOnly.setAccessible(true);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(cookie.getName());
        out.writeObject(cookie.getValue());
        out.writeObject(cookie.getComment());
        out.writeObject(cookie.getCommentURL());
        out.writeObject(cookie.getDomain());
        out.writeLong(cookie.getMaxAge());
        out.writeObject(cookie.getPath());
        out.writeObject(cookie.getPortlist());
        out.writeInt(cookie.getVersion());
        out.writeBoolean(cookie.getSecure());
        out.writeBoolean(cookie.getDiscard());
        out.writeBoolean(getHttpOnly());
    }

    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        String name = (String) in.readObject();
        String value = (String) in.readObject();
        cookie = new HttpCookie(name, value);
        cookie.setComment((String) in.readObject());
        cookie.setCommentURL((String) in.readObject());
        cookie.setDomain((String) in.readObject());
        cookie.setMaxAge(in.readLong());
        cookie.setPath((String) in.readObject());
        cookie.setPortlist((String) in.readObject());
        cookie.setVersion(in.readInt());
        cookie.setSecure(in.readBoolean());
        cookie.setDiscard(in.readBoolean());
        setHttpOnly(in.readBoolean());
    }


}
