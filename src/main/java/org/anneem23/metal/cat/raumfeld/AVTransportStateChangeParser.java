package org.anneem23.metal.cat.raumfeld;


import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable.CurrentTransportActions;
import org.fourthline.cling.support.lastchange.EventedValue;
import org.fourthline.cling.support.lastchange.LastChangeParser;

import javax.xml.transform.Source;
import java.util.Set;

public class AVTransportStateChangeParser extends LastChangeParser {
    public static final String AVT_NS = "urn:schemas-upnp-org:metadata-1-0/AVT/";

    private final Set<Class<? extends EventedValue>> _variables;
    private final String _namespace = AVT_NS;

    public AVTransportStateChangeParser() {
        _variables = AVTransportVariable.ALL;
        _variables.add(CurrentTransportActions.class);
    }

    @Override
    protected String getNamespace() {
        return _namespace;
    }

    @Override
    protected Source[] getSchemaSources() {
        return null;
    }

    @Override
    protected Set<Class<? extends EventedValue>> getEventedVariables() {
        return _variables;
    }
}

