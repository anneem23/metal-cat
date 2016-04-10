package org.anneem23.metal.cat.raumfeld;

import org.fourthline.cling.support.contentdirectory.DIDLParser;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.ProtocolInfo;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.Item;

// TODO use MetaDataParser to send the serialized objects instead of xml strings
public class MetaDataParser {



    private static final DIDLParser DIDL_PARSER = new DIDLParser();

    private MetaDataParser() {}

    public static DIDLContent parse(String didlXml) {
        DIDLContent result = null;
        try {
            result = DIDL_PARSER.parse(didlXml);
        } catch (final Exception e) {
            System.err.println("failed to parse didl xml " + didlXml + ". Exception is: " + e.toString());
        }
        return result;
    }

    private static String generate(DIDLContent didlContent) {
        String xml = null;
        try {
            xml = DIDL_PARSER.generate(didlContent);
        } catch (final Exception e) {
            System.err.println("failed to generate xml for didlobject " + didlContent + ". Exception is: " + e.toString());
        }
        return xml;
    }

    public static String generate(DIDLObject object) {
        final DIDLContent content = new DIDLContent();

        if (object.getFirstResource() != null) {
            final ProtocolInfo protocolInfo = object.getFirstResource().getProtocolInfo();
            final ProtocolInfo fixedProtocolInfo = new ProtocolInfo(protocolInfo.getContentFormatMimeType());
            object.getFirstResource().setProtocolInfo(fixedProtocolInfo);
        }


        if (object instanceof Item) {
            content.addItem((Item) object);
        } else if (object instanceof Container) {
            content.addContainer((Container) object);
        }
        return generate(content);
    }

}
