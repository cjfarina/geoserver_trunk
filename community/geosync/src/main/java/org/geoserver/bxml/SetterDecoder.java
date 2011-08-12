package org.geoserver.bxml;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.commons.beanutils.BeanUtils;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.springframework.util.Assert;

public class SetterDecoder<T> implements Decoder<T> {

    private final Decoder<? extends T> propertyDecoder;

    private final Object target;

    private final String propertyName;

    private final Method setter;

    private boolean isCollection;

    public SetterDecoder(final Decoder<? extends T> propertyDecoder, final Object target,
            final String propertyName) {
        Assert.notNull(propertyDecoder);
        Assert.notNull(target);
        Assert.notNull(propertyName);

        this.propertyDecoder = propertyDecoder;
        this.target = target;
        this.propertyName = propertyName;
        this.setter = findSetter();
    }

    private Method findSetter() {
        for (Method m : target.getClass().getMethods()) {
            if (!Modifier.isPublic(m.getModifiers())) {
                continue;
            }
            String name = m.getName().toLowerCase();
            if (name.equals("set" + propertyName.toLowerCase())) {
                this.isCollection = false;
                return m;
            }

            if (name.equals("get" + propertyName.toLowerCase())) {
                Class<?> returnType = m.getReturnType();
                if (returnType != null && Collection.class.isAssignableFrom(returnType)) {
                    this.isCollection = true;
                    return m;
                }
            }
        }
        throw new IllegalArgumentException("No setter for property " + propertyName
                + " found in class " + target.getClass().getName());
    }

    @Override
    public T decode(BxmlStreamReader r) throws Exception {
        T propertyValue = propertyDecoder.decode(r);

        if (isCollection) {
            @SuppressWarnings("unchecked")
            Collection<T> c = (Collection<T>) this.setter.invoke(target, null);
            if(propertyValue != null){
                c.add(propertyValue);
            }
        } else {
            BeanUtils.setProperty(target, propertyName, propertyValue);
        }
        return propertyValue;
    }

    @Override
    public boolean canHandle(QName name) {
        return propertyDecoder.canHandle(name);
    }

    @Override
    public Set<QName> getTargets() {
        return propertyDecoder.getTargets();
    }

}
