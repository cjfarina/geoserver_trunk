package org.geoserver.filter.pojo;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathContextFactory;
import org.geotools.factory.Hints;
import org.geotools.filter.expression.PropertyAccessor;
import org.geotools.filter.expression.PropertyAccessorFactory;
import org.opengis.feature.Property;
import org.opengis.feature.type.PropertyType;

/**
 * {@link PropertyAccessorFactory} to access properties out of regular java beans.
 * <p>
 * Implementation details: the {@link PropertyAccessor} created by this factory uses Apache <a
 * href="http://commons.apache.org/jxpath/">Commons JXPath</a> to evaluate the xpath expressions
 * against the provided Java Bean.
 * </p>
 * Also, this factory explicitly avoids returning a {@code PropertyAccessor} if the object to
 * evaluate is derived from {@link PropertyType} or {@link Property}.
 * 
 * @author groldan
 * 
 */
public class PojoPropertyAccessorFactory implements PropertyAccessorFactory {

    @Override
    public PropertyAccessor createPropertyAccessor(final Class<?> type, final String xpath,
            final Class<?> target, final Hints hints) {

        if (Property.class.isAssignableFrom(type) || PropertyType.class.isAssignableFrom(type)) {
            return null;
        }
        if ("".equals(xpath)) {
            return null;
        }

        return new PojoPropertyAccessor();
    }

    /**
     * We strip off namespace prefix, we need new feature model to do this property
     * <ul>
     * <li>BEFORE: foo:bar
     * <li>AFTER: bar
     * </ul>
     * 
     * @param xpath
     * @return xpath with any XML prefixes removed
     */
    static String stripPrefix(String xpath) {
        int split = xpath.indexOf(":");
        if (split != -1) {
            return xpath.substring(split + 1);
        }
        return xpath;
    }

    private static class PojoPropertyAccessor implements PropertyAccessor {

        private static final JXPathContextFactory CONTEXT_FACTORY = JXPathContextFactory
                .newInstance();

        @Override
        public boolean canHandle(final Object object, final String xpath, final Class<?> target) {
            return (xpath != null) && !"".equals(xpath.trim());
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public Object get(final Object object, final String xpath, final Class target)
                throws IllegalArgumentException {
            return context(object).getValue(xpath);
        }

        @Override
        public <T> void set(final Object object, final String xpath, final T value,
                final Class<T> target) throws IllegalArgumentException {
            context(object).setValue(xpath, value);
        }

        private JXPathContext context(Object object) {
            JXPathContext context = CONTEXT_FACTORY.newContext(null, object);
            context.setLenient(true);

            return context;
        }
    }
}
