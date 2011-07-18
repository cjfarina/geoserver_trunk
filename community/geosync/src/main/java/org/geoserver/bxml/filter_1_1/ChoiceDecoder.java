package org.geoserver.bxml.filter_1_1;

import javax.xml.namespace.QName;

import org.geoserver.bxml.Decoder;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.gvsig.bxml.stream.EventType;


public class ChoiceDecoder<T> implements Decoder<T> {

    private final Decoder<T>[] options;

    public ChoiceDecoder(Decoder<T>... options){
        this.options = options;
    }
    
    @Override
    public T decode(final BxmlStreamReader r) throws Exception {
        r.require(EventType.START_ELEMENT, null, null);
        final QName name = r.getElementName();
        for(Decoder<T> decoder : options){
            if(decoder.canHandle(name)){
                return decoder.decode(r);
            }
        }
        throw new IllegalArgumentException("No decoder found for " + name);
    }

    @Override
    public Boolean canHandle(QName name) {
        for(Decoder<T> decoder : options){
            if(decoder.canHandle(name)){
                return true;
            }
        }
        return false;
    }
}
